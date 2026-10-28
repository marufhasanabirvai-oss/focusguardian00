package com.focusguardian.app.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.focusguardian.app.domain.matching.PenaltyManager

class GuardianDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Focus Guardian Uninstall Protection Enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        // Warn the user honestly before deactivation
        PenaltyManager.triggerPenalty(
            context,
            reason = "Attempted to deactivate Uninstall Protection during active focus!",
            durationSeconds = 60
        )
        return "WARNING: Focus Guardian has an active protection lock. Deactivating will trigger your self-control penalty cooldown!"
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Uninstall Protection Deactivated", Toast.LENGTH_SHORT).show()
    }
}
