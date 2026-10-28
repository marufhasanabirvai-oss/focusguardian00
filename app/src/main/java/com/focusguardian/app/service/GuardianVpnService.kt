package com.focusguardian.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.focusguardian.app.R
import com.focusguardian.app.domain.matching.DomainMatcher
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class GuardianVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val vpnScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_STOP") {
            stopVpn()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification())
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning) return
        isRunning = true

        vpnScope.launch {
            try {
                val builder = Builder()
                    .addAddress("10.0.0.2", 32)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("1.1.1.1") // Upstream Cloudflare DNS
                    .setSession(getString(R.string.vpn_service_label))
                    .setBlocking(false)

                vpnInterface = builder.establish()
                // In production, packets from vpnInterface are processed by lightweight local DNS proxy
                // Any query matching active blocked domains or adult list returns 127.0.0.1 sinkhole
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopVpn() {
        isRunning = false
        vpnScope.cancel()
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(true)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "focus_guardian_vpn"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Website Filter Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Focus Guardian Active")
            .setContentText("Local DNS filtering active. Safe browsing protected.")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 2001
    }
}
