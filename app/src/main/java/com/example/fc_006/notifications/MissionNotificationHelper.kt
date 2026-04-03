package com.example.fc_006.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.fc_006.MainActivity
import com.example.fc_006.R

class MissionNotificationHelper(context: Context) {

    private val appContext = context.applicationContext

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val updatesChannel = NotificationChannel(
            UPDATES_CHANNEL_ID,
            appContext.getString(R.string.notification_channel_updates_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = appContext.getString(R.string.notification_channel_updates_description)
        }

        val alertsChannel = NotificationChannel(
            ALERTS_CHANNEL_ID,
            appContext.getString(R.string.notification_channel_alerts_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = appContext.getString(R.string.notification_channel_alerts_description)
            enableVibration(true)
        }

        val manager = appContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(updatesChannel)
        manager.createNotificationChannel(alertsChannel)
    }

    fun hasRuntimePermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun canSendNotifications(): Boolean {
        return hasRuntimePermission() && NotificationManagerCompat.from(appContext).areNotificationsEnabled()
    }

    fun showIncomingSignal() {
        postNotification(
            channelId = UPDATES_CHANNEL_ID,
            notificationId = SIGNAL_NOTIFICATION_ID,
            title = appContext.getString(R.string.notification_signal_title),
            message = appContext.getString(R.string.notification_signal_message),
            priority = NotificationCompat.PRIORITY_DEFAULT,
            iconRes = android.R.drawable.ic_dialog_info
        )
    }

    fun showHazardDetected(asteroidName: String, distanceKm: String) {
        postNotification(
            channelId = ALERTS_CHANNEL_ID,
            notificationId = HAZARD_NOTIFICATION_ID,
            title = appContext.getString(R.string.notification_hazard_title),
            message = appContext.getString(
                R.string.notification_hazard_message,
                asteroidName,
                distanceKm
            ),
            priority = NotificationCompat.PRIORITY_HIGH,
            iconRes = android.R.drawable.ic_dialog_alert
        )
    }

    fun showThreatEvaded(asteroidName: String) {
        postNotification(
            channelId = UPDATES_CHANNEL_ID,
            notificationId = SAFE_NOTIFICATION_ID,
            title = appContext.getString(R.string.notification_safe_title),
            message = appContext.getString(R.string.notification_safe_message, asteroidName),
            priority = NotificationCompat.PRIORITY_DEFAULT,
            iconRes = android.R.drawable.ic_dialog_info
        )
    }

    private fun postNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        priority: Int,
        iconRes: Int
    ) {
        if (!canSendNotifications()) {
            return
        }

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(createContentIntent(message))
            .build()

        NotificationManagerCompat.from(appContext).notify(notificationId, notification)
    }

    private fun createContentIntent(message: String): PendingIntent {
        val launchIntent = Intent(appContext, MainActivity::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_MESSAGE, message)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        return PendingIntent.getActivity(
            appContext,
            message.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_NOTIFICATION_MESSAGE = "notification_message"

        private const val UPDATES_CHANNEL_ID = "mission_updates"
        private const val ALERTS_CHANNEL_ID = "mission_alerts"

        private const val SIGNAL_NOTIFICATION_ID = 2001
        private const val HAZARD_NOTIFICATION_ID = 2002
        private const val SAFE_NOTIFICATION_ID = 2003
    }
}
