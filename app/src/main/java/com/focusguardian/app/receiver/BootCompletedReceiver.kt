package com.focusguardian.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusguardian.app.data.preferences.GuardianPreferences
import com.focusguardian.app.service.GuardianVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == "android.intent.action.QUICKBOOT_POWERON") {
            CoroutineScope(Dispatchers.IO).launch {
                val prefs = GuardianPreferences(context)
                val websiteOn = prefs.websiteBlockingEnabled.first()
                val adultOn = prefs.adultBlockingEnabled.first()

                if (websiteOn || adultOn) {
                    val vpnIntent = Intent(context, GuardianVpnService::class.java)
                    context.startForegroundService(vpnIntent)
                }
            }
        }
    }
}
