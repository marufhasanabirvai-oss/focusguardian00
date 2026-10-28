package com.focusguardian.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.focusguardian.app.data.local.AppDatabase
import com.focusguardian.app.data.local.entity.BlockLogEntity
import com.focusguardian.app.data.preferences.GuardianPreferences
import com.focusguardian.app.domain.matching.PenaltyManager
import com.focusguardian.app.domain.matching.WordMatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class GuardianAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    private var isScanDebounced = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Throttle rapid scroll events to protect battery
        if (isScanDebounced) return
        isScanDebounced = true
        handler.postDelayed({ isScanDebounced = false }, 350)

        val rootNode = rootInActiveWindow ?: return

        serviceScope.launch {
            val prefs = GuardianPreferences(applicationContext)
            val wordBlockingOn = prefs.wordBlockingEnabled.first()
            if (!wordBlockingOn) {
                rootNode.recycle()
                return@launch
            }

            val db = AppDatabase.getInstance(applicationContext)
            val activeWords = db.wordDao().getActiveWords()
            if (activeWords.isEmpty()) {
                rootNode.recycle()
                return@launch
            }

            val wordPairs = activeWords.map { Pair(it.word, it.isExactMatch) }

            // Recursively inspect visible text without saving or transmitting
            val matchedWord = scanNodeForBlockedWord(rootNode, wordPairs)
            rootNode.recycle()

            if (matchedWord != null) {
                // Log attempt
                db.logDao().insertLog(
                    BlockLogEntity(
                        triggerType = "word",
                        details = "Blocked word detected: '$matchedWord'",
                        penaltyDurationSeconds = 30
                    )
                )

                // Send to Home screen + Penalty lockout
                performGlobalAction(GLOBAL_ACTION_HOME)
                PenaltyManager.triggerPenalty(
                    applicationContext,
                    reason = "Forbidden word detected: $matchedWord",
                    durationSeconds = 30
                )
            }
        }
    }

    private fun scanNodeForBlockedWord(
        node: AccessibilityNodeInfo,
        words: List<Pair<String, Boolean>>
    ): String? {
        val text = node.text
        if (!text.isNullOrEmpty()) {
            val matched = WordMatcher.findFirstMatch(text, words)
            if (matched != null) return matched
        }

        val desc = node.contentDescription
        if (!desc.isNullOrEmpty()) {
            val matched = WordMatcher.findFirstMatch(desc, words)
            if (matched != null) return matched
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val matched = scanNodeForBlockedWord(child, words)
            child.recycle()
            if (matched != null) return matched
        }

        return null
    }

    override fun onInterrupt() {
        // Enforce commitment lock: if accessibility service is interrupted/revoked during active lock,
        // immediately send to Home screen and trigger penalty cooldown to prevent bypass.
        serviceScope.launch {
            val prefs = GuardianPreferences(applicationContext)
            val lockUntil = prefs.accessibilityLockUntil.first()
            if (com.focusguardian.app.domain.matching.ProtectionLockManager.isLockActive(lockUntil)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                PenaltyManager.triggerPenalty(
                    applicationContext,
                    reason = "Accessibility Guardian Service is commitment-locked (${com.focusguardian.app.domain.matching.ProtectionLockManager.formatRemaining(lockUntil)}) and cannot be interrupted!",
                    durationSeconds = 60
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
