package com.pluto.adb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BackgroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "foreNotification")
            .setContentTitle("BK ADB is running in the background.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app's icon
            .build()

        startForeground(1, notification)

        // Your background tasks go here.
        // For example, you could start your ADB connection and UI Automator tests.

        return START_STICKY // This will restart the service if it's killed by the system.
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "foreNotification",
                "Background Service Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}