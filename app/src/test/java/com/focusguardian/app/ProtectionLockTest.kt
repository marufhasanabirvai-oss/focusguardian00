package com.focusguardian.app

import com.focusguardian.app.domain.matching.ProtectionLockManager
import org.junit.Assert.*
import org.junit.Test

class ProtectionLockTest {

    @Test
    fun testMandatoryLockPeriodCalculations() {
        val now = 1_000_000L
        val millisInDay = 86_400_000L

        // Test all required options: 1, 3, 7, 14, 21, and 30 days
        assertEquals(now + (1 * millisInDay), ProtectionLockManager.calculateLockUntilTimestamp(now, 1))
        assertEquals(now + (3 * millisInDay), ProtectionLockManager.calculateLockUntilTimestamp(now, 3))
        assertEquals(now + (7 * millisInDay), ProtectionLockManager.calculateLockUntilTimestamp(now, 7))
        assertEquals(now + (14 * millisInDay), ProtectionLockManager.calculateLockUntilTimestamp(now, 14))
        assertEquals(now + (21 * millisInDay), ProtectionLockManager.calculateLockUntilTimestamp(now, 21))
        assertEquals(now + (30 * millisInDay), ProtectionLockManager.calculateLockUntilTimestamp(now, 30))
    }

    @Test
    fun testCurrentTimeLessThanActivationPlusLockPreventsToggleOff() {
        val activationTime = 10_000_000L
        val lockDays = 7
        val lockDurationMs = 7 * 86_400_000L

        // Immediately after activation -> MUST be locked
        assertTrue(ProtectionLockManager.isLockActive(activationTime, lockDays, currentEpochMs = activationTime + 100))
        assertFalse(ProtectionLockManager.canToggleOff(activationTime, lockDays, currentEpochMs = activationTime + 100))

        // Midway (day 3) -> MUST be locked
        val midway = activationTime + (3 * 86_400_000L)
        assertTrue(ProtectionLockManager.isLockActive(activationTime, lockDays, currentEpochMs = midway))
        assertFalse(ProtectionLockManager.canToggleOff(activationTime, lockDays, currentEpochMs = midway))

        // 1 millisecond before expiration -> MUST STILL be locked
        val oneMsBefore = activationTime + lockDurationMs - 1L
        assertTrue(ProtectionLockManager.isLockActive(activationTime, lockDays, currentEpochMs = oneMsBefore))
        assertFalse(ProtectionLockManager.canToggleOff(activationTime, lockDays, currentEpochMs = oneMsBefore))
    }

    @Test
    fun testExpiredLockAllowsToggleOffSuccessfully() {
        val activationTime = 10_000_000L
        val lockDays = 7
        val lockDurationMs = 7 * 86_400_000L

        // At exact expiration -> Expired, toggle allowed
        val atExpiration = activationTime + lockDurationMs
        assertFalse(ProtectionLockManager.isLockActive(activationTime, lockDays, currentEpochMs = atExpiration))
        assertTrue(ProtectionLockManager.canToggleOff(activationTime, lockDays, currentEpochMs = atExpiration))

        // After expiration -> Toggle allowed
        val afterExpiration = activationTime + lockDurationMs + 5_000L
        assertFalse(ProtectionLockManager.isLockActive(activationTime, lockDays, currentEpochMs = afterExpiration))
        assertTrue(ProtectionLockManager.canToggleOff(activationTime, lockDays, currentEpochMs = afterExpiration))
    }

    @Test
    fun testRemainingDaysCalculation() {
        val now = 1_000_000L
        val millisInDay = 86_400_000L

        // At day 0 of a 14-day lock -> 14 days remaining
        assertEquals(14, ProtectionLockManager.getRemainingDays(now, 14, currentEpochMs = now))

        // At day 6 of a 14-day lock -> 8 days remaining
        val day6 = now + (6 * millisInDay)
        assertEquals(8, ProtectionLockManager.getRemainingDays(now, 14, currentEpochMs = day6))

        // Expired -> 0 days remaining
        val expired = now + (15 * millisInDay)
        assertEquals(0, ProtectionLockManager.getRemainingDays(now, 14, currentEpochMs = expired))
    }

    @Test
    fun testLockErrorMessageFormatting() {
        val errorEn = ProtectionLockManager.getLockErrorMessage(5, isBengali = false)
        assertEquals("This block is locked for another 5 days and cannot be disabled.", errorEn)

        val errorBn = ProtectionLockManager.getLockErrorMessage(5, isBengali = true)
        assertTrue(errorBn.contains("৫"))
        assertTrue(errorBn.contains("লক করা আছে"))
        assertTrue(errorBn.contains("বন্ধ করা যাবে না"))
    }

    @Test
    fun testDurationUnalterableDuringActiveLock() {
        val now = 1_000_000L
        val lockDays = 20
        val expiry = ProtectionLockManager.calculateLockUntilTimestamp(now, lockDays)

        // During active lock period: Duration and switch MUST be unalterable
        assertTrue(ProtectionLockManager.isDurationUnalterable(expiry, currentEpochMs = now + 500_000L))
        assertTrue(ProtectionLockManager.isLockActive(expiry, currentEpochMs = now + 500_000L))

        // Once expired: Can be modified or toggled
        assertFalse(ProtectionLockManager.isDurationUnalterable(expiry, currentEpochMs = expiry + 1000L))
        assertFalse(ProtectionLockManager.isLockActive(expiry, currentEpochMs = expiry + 1000L))
    }

    @Test
    fun testStrictWarningMessageFormatting() {
        val warningEn = ProtectionLockManager.getStrictLockWarningMessage(10, isBengali = false)
        assertTrue(warningEn.contains("10 days"))
        assertTrue(warningEn.contains("cannot be modified or disabled"))

        val warningBn = ProtectionLockManager.getStrictLockWarningMessage(10, isBengali = true)
        assertTrue(warningBn.contains("১০"))
        assertTrue(warningBn.contains("অপরিবর্তনীয়ভাবে লক করা আছে"))
    }

    @Test
    fun testFormatRemainingEnglishAndBengali() {
        val now = 1_000_000L
        // 5 days and 14 hours = (5 * 24 + 14) * 3600 * 1000 = 134 * 3,600,000 = 482,400,000 ms
        val lockUntil = now + (134 * 3_600_000L)

        val remainingEn = ProtectionLockManager.formatRemaining(lockUntil, isBengali = false, currentEpochMs = now)
        assertEquals("5 days 14 hours left", remainingEn)

        val remainingBn = ProtectionLockManager.formatRemaining(lockUntil, isBengali = true, currentEpochMs = now)
        assertEquals("৫ দিন ১৪ ঘণ্টা বাকি", remainingBn)
    }

    @Test
    fun testAccessibilityLockErrorMessage() {
        val now = 1_000_000L
        val lockUntil = now + (72 * 3_600_000L) // 3 days 0 hours

        val msgEn = ProtectionLockManager.getAccessibilityLockErrorMessage(lockUntil, isBengali = false, currentEpochMs = now)
        assertTrue(msgEn.contains("Accessibility Service is commitment-locked (3 days 0 hours left)"))
        assertTrue(msgEn.contains("cannot be revoked"))

        val msgBn = ProtectionLockManager.getAccessibilityLockErrorMessage(lockUntil, isBengali = true, currentEpochMs = now)
        assertTrue(msgBn.contains("৩ দিন ০ ঘণ্টা বাকি"))
        assertTrue(msgBn.contains("প্রতিশ্রুতি-লক করা আছে"))
    }

    @Test
    fun testBasicMinuteModeCalculations() {
        val now = 10_000_000L
        val millisInMinute = 60_000L

        // Test Basic mode calculations (1 to 100 minutes)
        assertEquals(now + (1 * millisInMinute), ProtectionLockManager.calculateLockUntilTimestamp(now, 1, isMinuteBased = true))
        assertEquals(now + (25 * millisInMinute), ProtectionLockManager.calculateLockUntilTimestamp(now, 25, isMinuteBased = true))
        assertEquals(now + (100 * millisInMinute), ProtectionLockManager.calculateLockUntilTimestamp(now, 100, isMinuteBased = true))

        // Active state check during 25 minutes lock
        val lockUntil = now + (25 * millisInMinute)
        assertTrue(ProtectionLockManager.isLockActive(lockUntil, currentEpochMs = now + (10 * millisInMinute)))
        assertFalse(ProtectionLockManager.canToggleOff(now, 25, isMinuteBased = true, currentEpochMs = now + (10 * millisInMinute)))

        // Expired after 25 minutes
        assertFalse(ProtectionLockManager.isLockActive(lockUntil, currentEpochMs = lockUntil + 1000L))
        assertTrue(ProtectionLockManager.canToggleOff(now, 25, isMinuteBased = true, currentEpochMs = lockUntil + 1000L))
    }

    @Test
    fun testBasicMinuteFormatRemaining() {
        val now = 1_000_000L
        // 25 mins and 30 seconds
        val lockUntil = now + (25 * 60_000L) + 30_000L

        val remainingEn = ProtectionLockManager.formatRemaining(lockUntil, isBengali = false, currentEpochMs = now)
        assertTrue(remainingEn.contains("25 mins 30 s left"))

        val remainingBn = ProtectionLockManager.formatRemaining(lockUntil, isBengali = true, currentEpochMs = now)
        assertTrue(remainingBn.contains("২৫ মিনিট ৩০ সেকেন্ড বাকি"))
    }

    @Test
    fun testItemLevelLockPrevention() {
        val now = 1_000_000L
        val lockUntil = now + (14 * ProtectionLockManager.MILLIS_PER_DAY)

        // During active lock: items CANNOT be modified/deleted/added
        assertFalse(ProtectionLockManager.canModifyItems(lockUntil, currentEpochMs = now + 500_000L))

        // After lock expires: items CAN be modified/deleted/added
        assertTrue(ProtectionLockManager.canModifyItems(lockUntil, currentEpochMs = lockUntil + 1000L))
    }

    @Test
    fun testItemLockWarningMessages() {
        val now = 1_000_000L
        val lockUntil = now + (7 * ProtectionLockManager.MILLIS_PER_DAY)

        val warnEn = ProtectionLockManager.getItemLockWarningMessage(
            "Keyword Blocker",
            lockUntil,
            action = "delete",
            isBengali = false,
            currentEpochMs = now
        )
        assertTrue(warnEn.contains("Keyword Blocker is locked"))
        assertTrue(warnEn.contains("Items cannot be deleted until the lock period expires"))

        val warnBn = ProtectionLockManager.getItemLockWarningMessage(
            "কিওয়ার্ড ব্লকিং",
            lockUntil,
            action = "delete",
            isBengali = true,
            currentEpochMs = now
        )
        assertTrue(warnBn.contains("কিওয়ার্ড ব্লকিং"))
        assertTrue(warnBn.contains("সুরক্ষা লক সক্রিয়"))
        assertTrue(warnBn.contains("মোছা সম্পূর্ণ নিষিদ্ধ"))
    }
}
