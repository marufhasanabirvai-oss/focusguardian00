package com.focusguardian.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.focusguardian.app.data.local.AppDatabase
import com.focusguardian.app.domain.matching.ImageHasher
import com.focusguardian.app.domain.matching.PenaltyManager
import kotlinx.coroutines.*

class ScreenCaptureService : Service() {

    private val captureScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(3001, createNotification())
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "screen_capture_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Visual Pattern Protection",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Screen Pattern Monitor Active")
            .setContentText("Comparing screen patterns locally. No data is saved or uploaded.")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        captureScope.cancel()
        super.onDestroy()
    }
}
