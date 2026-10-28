package com.focusguardian.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class FocusGuardianApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val penaltyChannel = NotificationChannel(
                CHANNEL_PENALTY,
                "Penalty & Lockout Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when self-imposed blocking rules trigger penalty"
            }

            val vpnChannel = NotificationChannel(
                CHANNEL_VPN,
                "Protection Status",
                NotificationManager.IMPORTANCE_LOW
            )

            manager?.createNotificationChannel(penaltyChannel)
            manager?.createNotificationChannel(vpnChannel)
        }
    }

    companion object {
        const val CHANNEL_PENALTY = "channel_penalty"
        const val CHANNEL_VPN = "channel_vpn"
    }
}
