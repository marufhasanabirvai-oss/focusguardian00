package com.focusguardian.app.domain.matching

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.focusguardian.app.ui.PenaltyLockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PenaltyManager {

    /**
     * Executes the configured self-control penalty:
     * 1. Sends the user immediately to the Android Home screen (ACTION_MAIN, CATEGORY_HOME)
     * 2. Displays brief toast alert
     * 3. Launches fullscreen PenaltyLockActivity with countdown timer
     */
    fun triggerPenalty(
        context: Context,
        reason: String,
        durationSeconds: Int = 30
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            // Show transparent toast warning
            Toast.makeText(
                context,
                "Focus Guardian: Restricted content detected! ($reason)",
                Toast.LENGTH_LONG
            ).show()

            // 1. Immediately go to Android Home Screen
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)

            // 2. Open PenaltyLockActivity in full screen
            val penaltyIntent = Intent(context, PenaltyLockActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_REASON", reason)
                putExtra("EXTRA_DURATION_SECONDS", durationSeconds)
            }
            context.startActivity(penaltyIntent)
        }
    }
}
