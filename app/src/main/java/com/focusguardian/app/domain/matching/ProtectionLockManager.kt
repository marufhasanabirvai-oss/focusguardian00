package com.focusguardian.app.domain.matching

import kotlin.math.ceil

object ProtectionLockManager {

    const val MILLIS_PER_MINUTE = 60_000L
    const val MILLIS_PER_DAY = 86_400_000L // 24 * 60 * 60 * 1000

    /**
     * Dual Time Selection System:
     * - Basic Mode: duration in minutes (1 to 100 minutes)
     * - Advanced Mode: duration in days (1 to 30 days)
     */
    fun calculateLockUntilTimestamp(
        activationTimestamp: Long,
        duration: Int,
        isMinuteBased: Boolean = false
    ): Long {
        return if (isMinuteBased) {
            require(duration in 1..100) { "Basic mode duration must be between 1 and 100 minutes" }
            activationTimestamp + (duration * MILLIS_PER_MINUTE)
        } else {
            require(duration in 1..30) { "Advanced mode duration must be between 1 and 30 days" }
            activationTimestamp + (duration * MILLIS_PER_DAY)
        }
    }

    /**
     * Checks if current time is less than activation_timestamp + duration.
     * Returns true if lock is currently active and switch CANNOT be turned OFF.
     */
    fun isLockActive(
        activationTimestamp: Long,
        duration: Int,
        isMinuteBased: Boolean = false,
        currentEpochMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (activationTimestamp <= 0L || duration <= 0) return false
        val lockUntil = calculateLockUntilTimestamp(activationTimestamp, duration, isMinuteBased)
        return currentEpochMs < lockUntil
    }

    /**
     * Overload checking against calculated lockUntil timestamp directly
     */
    fun isLockActive(lockUntilTimestamp: Long, currentEpochMs: Long = System.currentTimeMillis()): Boolean {
        return lockUntilTimestamp > currentEpochMs
    }

    /**
     * Returns true if lock period has expired and switch CAN be turned OFF successfully.
     */
    fun canToggleOff(
        activationTimestamp: Long,
        duration: Int,
        isMinuteBased: Boolean = false,
        currentEpochMs: Long = System.currentTimeMillis()
    ): Boolean {
        return !isLockActive(activationTimestamp, duration, isMinuteBased, currentEpochMs)
    }

    /**
     * Returns remaining whole days left in the lock period.
     */
    fun getRemainingDays(
        activationTimestamp: Long,
        lockDays: Int,
        currentEpochMs: Long = System.currentTimeMillis()
    ): Int {
        if (!isLockActive(activationTimestamp, lockDays, false, currentEpochMs)) return 0
        val lockUntil = calculateLockUntilTimestamp(activationTimestamp, lockDays, false)
        val diffMs = lockUntil - currentEpochMs
        return maxOf(1, ceil(diffMs.toDouble() / MILLIS_PER_DAY.toDouble()).toInt())
    }

    /**
     * Overload calculating remaining days directly from lockUntilTimestamp
     */
    fun getRemainingDays(lockUntilTimestamp: Long, currentEpochMs: Long = System.currentTimeMillis()): Int {
        if (lockUntilTimestamp <= currentEpochMs) return 0
        val diffMs = lockUntilTimestamp - currentEpochMs
        return maxOf(1, ceil(diffMs.toDouble() / MILLIS_PER_DAY.toDouble()).toInt())
    }

    /**
     * Returns remaining minutes left in short-term lock period.
     */
    fun getRemainingMinutes(lockUntilTimestamp: Long, currentEpochMs: Long = System.currentTimeMillis()): Int {
        if (lockUntilTimestamp <= currentEpochMs) return 0
        val diffMs = lockUntilTimestamp - currentEpochMs
        return maxOf(1, ceil(diffMs.toDouble() / MILLIS_PER_MINUTE.toDouble()).toInt())
    }

    /**
     * Formats remaining time in standard "X days Y hours left" (or "X mins Y secs left" for Basic Mode)
     */
    fun formatRemaining(
        lockUntilTimestamp: Long,
        isBengali: Boolean = false,
        currentEpochMs: Long = System.currentTimeMillis()
    ): String {
        val diffMs = lockUntilTimestamp - currentEpochMs
        if (diffMs <= 0) return if (isBengali) "মেয়াদ শেষ" else "Expired"

        val totalSeconds = diffMs / 1000
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val bengaliDigits = arrayOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
        fun toBn(n: Long): String = n.toString().map { c -> if (c.isDigit()) bengaliDigits[c.digitToInt()] else c.toString() }.joinToString("")

        return if (isBengali) {
            when {
                days > 0 -> "${toBn(days)} দিন ${toBn(hours)} ঘণ্টা বাকি"
                hours > 0 -> "${toBn(hours)} ঘণ্টা ${toBn(minutes)} মিনিট বাকি"
                minutes > 0 -> "${toBn(minutes)} মিনিট ${toBn(seconds)} সেকেন্ড বাকি"
                else -> "${toBn(seconds)} সেকেন্ড বাকি"
            }
        } else {
            when {
                days > 0 -> "$days days $hours hours left"
                hours > 0 -> "$hours hours $minutes mins left"
                minutes > 0 -> "$minutes mins $seconds s left"
                else -> "$seconds s left"
            }
        }
    }

    /**
     * Formats mandatory Feature 8 error message:
     * "This block is locked for another X days and cannot be disabled."
     */
    fun getLockErrorMessage(remainingDays: Int, isBengali: Boolean = false): String {
        return if (isBengali) {
            val bengaliDigits = arrayOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
            val bnNum = remainingDays.toString().map { c ->
                if (c.isDigit()) bengaliDigits[c.digitToInt()] else c.toString()
            }.joinToString("")
            "এই ব্লকিং ব্যবস্থাটি আরও $bnNum দিনের জন্য লক করা আছে এবং এখন বন্ধ করা যাবে না।"
        } else {
            "This block is locked for another $remainingDays days and cannot be disabled."
        }
    }

    fun getLockErrorMessageWithRemaining(
        lockUntilTimestamp: Long,
        isBengali: Boolean = false,
        currentEpochMs: Long = System.currentTimeMillis()
    ): String {
        val remaining = formatRemaining(lockUntilTimestamp, isBengali, currentEpochMs)
        return if (isBengali) {
            "এই ব্লকিং ব্যবস্থাটি প্রতিশ্রুতি-লক করা আছে ($remaining) এবং এখন বন্ধ করা যাবে না।"
        } else {
            "This block is commitment-locked ($remaining) and cannot be disabled."
        }
    }

    /**
     * Accessibility Service Commitment Lock Error Message
     */
    fun getAccessibilityLockErrorMessage(
        lockUntilTimestamp: Long,
        isBengali: Boolean = false,
        currentEpochMs: Long = System.currentTimeMillis()
    ): String {
        val remaining = formatRemaining(lockUntilTimestamp, isBengali, currentEpochMs)
        return if (isBengali) {
            "অ্যাক্সেসিবিলিটি সার্ভিসটি প্রতিশ্রুতি-লক করা আছে ($remaining) এবং বাইপাস প্রতিরোধের জন্য মেয়াদ শেষ না হওয়া পর্যন্ত বন্ধ বা প্রত্যাহার করা যাবে না।"
        } else {
            "Accessibility Service is commitment-locked ($remaining) and cannot be revoked to prevent bypass."
        }
    }

    /**
     * Human readable remaining lock time
     */
    fun formatRemainingLockTime(lockUntilTimestamp: Long, currentEpochMs: Long = System.currentTimeMillis()): String {
        return formatRemaining(lockUntilTimestamp, isBengali = false, currentEpochMs = currentEpochMs)
    }

    /**
     * Feature 8 Strict Requirement:
     * Returns true if lock duration is active, meaning both the toggle switch
     * and duration selection UI (preset buttons / slider) must be completely disabled & unalterable.
     */
    fun isDurationUnalterable(activationTimestamp: Long, lockDays: Int, currentEpochMs: Long = System.currentTimeMillis()): Boolean {
        return isLockActive(activationTimestamp, lockDays, currentEpochMs)
    }

    fun isDurationUnalterable(lockUntilTimestamp: Long, currentEpochMs: Long = System.currentTimeMillis()): Boolean {
        return isLockActive(lockUntilTimestamp, currentEpochMs)
    }

    /**
     * Warning message when user clicks on locked settings or disabled duration controls:
     */
    fun getStrictLockWarningMessage(remainingDays: Int, isBengali: Boolean = false): String {
        return if (isBengali) {
            val bengaliDigits = arrayOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
            val bnNum = remainingDays.toString().map { c ->
                if (c.isDigit()) bengaliDigits[c.digitToInt()] else c.toString()
            }.joinToString("")
            "সতর্কতা: এই ব্লকটি আরও $bnNum দিনের জন্য অপরিবর্তনীয়ভাবে লক করা আছে। মেয়াদ শেষ না হওয়া পর্যন্ত এটি বন্ধ করা বা এর দিন পরিবর্তন করা যাবে না।"
        } else {
            "Warning: This block is locked for another $remainingDays days and cannot be modified or disabled until expiration."
        }
    }

    fun getStrictLockWarningMessageWithRemaining(
        lockUntilTimestamp: Long,
        isBengali: Boolean = false,
        currentEpochMs: Long = System.currentTimeMillis()
    ): String {
        val remaining = formatRemaining(lockUntilTimestamp, isBengali, currentEpochMs)
        return if (isBengali) {
            "সতর্কতা: এই ফিচারটি অপরিবর্তনীয়ভাবে লক করা আছে ($remaining)। মেয়াদ শেষ না হওয়া পর্যন্ত এটি বন্ধ বা পরিবর্তন করা যাবে না।"
        } else {
            "Warning: This feature is strictly locked ($remaining) and cannot be modified or disabled until countdown finishes."
        }
    }

    /**
     * Strict Item-Level Deletion & Modification Lock:
     * Returns true if user is allowed to add, edit, or delete items.
     * Returns false during active lock period (switch/duration locked).
     */
    fun canModifyItems(lockUntilTimestamp: Long, currentEpochMs: Long = System.currentTimeMillis()): Boolean {
        return !isLockActive(lockUntilTimestamp, currentEpochMs)
    }

    /**
     * Polite warning toast/alert when user tries to delete/edit/add items during active lock.
     */
    fun getItemLockWarningMessage(
        moduleName: String,
        lockUntilTimestamp: Long,
        action: String = "delete",
        isBengali: Boolean = false,
        currentEpochMs: Long = System.currentTimeMillis()
    ): String {
        val remaining = formatRemaining(lockUntilTimestamp, isBengali, currentEpochMs)
        return if (isBengali) {
            val act = when (action) {
                "delete" -> "মোছা"
                "edit" -> "সম্পাদনা করা"
                else -> "নতুন আইটেম যোগ করা"
            }
            "সুরক্ষা লক সক্রিয় ($remaining): $moduleName থেকে কোনো আইটেম $act সম্পূর্ণ নিষিদ্ধ।"
        } else {
            val act = when (action) {
                "delete" -> "deleted"
                "edit" -> "edited"
                else -> "added"
            }
            "$moduleName is locked ($remaining): Items cannot be $act until the lock period expires."
        }
    }
}
