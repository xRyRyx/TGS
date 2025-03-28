package com.tgs.app.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tgs.app.R

object NotificationHelper {
    private const val CHANNEL_ID = "HAZARD_CHANNEL"

    fun sendHazardNotification(context: Context, temp: Float, gas: Int, flame: Int) {
        val hazardMessages = mutableListOf<String>()

        if (temp >= 50) {
            hazardMessages.add("\uD83C\uDF21\uFE0F Temperature Alert! Unsafe heat levels.")
        }
        if (gas >= 600) {
            hazardMessages.add("⛽ Gas/Smoke Alert! High levels detected.")
        }
        if (flame < 60) {
            hazardMessages.add("\uD83D\uDD25 Flame Detected! Check the area.")
        }

        if (hazardMessages.isNotEmpty()) {
            val message = hazardMessages.joinToString("\n")

            val intent = Intent(context, QuestionsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Hazard Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.danger_icon)
                .setContentTitle("\uD83D\uDEA8 TGS ALERT❗")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }
}
