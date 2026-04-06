package com.example.fc_006.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
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
            iconRes = R.drawable.ic_notification_signal,
            colorRes = R.color.console_cyan,
            subText = appContext.getString(R.string.notification_signal_subtext),
            category = NotificationCompat.CATEGORY_STATUS
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
            iconRes = R.drawable.ic_notification_hazard,
            colorRes = R.color.console_red,
            subText = appContext.getString(R.string.notification_hazard_subtext),
            category = NotificationCompat.CATEGORY_ALARM
        )
    }

    fun showThreatEvaded(asteroidName: String) {
        postNotification(
            channelId = UPDATES_CHANNEL_ID,
            notificationId = SAFE_NOTIFICATION_ID,
            title = appContext.getString(R.string.notification_safe_title),
            message = appContext.getString(R.string.notification_safe_message, asteroidName),
            priority = NotificationCompat.PRIORITY_DEFAULT,
            iconRes = R.drawable.ic_notification_evaded,
            colorRes = R.color.console_green,
            subText = appContext.getString(R.string.notification_safe_subtext),
            category = NotificationCompat.CATEGORY_STATUS
        )
    }

    private fun postNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        priority: Int,
        @DrawableRes iconRes: Int,
        @ColorRes colorRes: Int,
        subText: String,
        category: String
    ) {
        if (!canSendNotifications()) {
            return
        }

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setCategory(category)
            .setColor(ContextCompat.getColor(appContext, colorRes))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(createContentIntent(title, message))
            .build()

        NotificationManagerCompat.from(appContext).notify(notificationId, notification)
    }

    private fun createContentIntent(title: String, message: String): PendingIntent {
        val launchIntent = Intent(appContext, MainActivity::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TITLE, title)
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
        const val EXTRA_NOTIFICATION_TITLE = "notification_title"
        const val EXTRA_NOTIFICATION_MESSAGE = "notification_message"

        private const val UPDATES_CHANNEL_ID = "mission_updates"
        private const val ALERTS_CHANNEL_ID = "mission_alerts"

        private const val SIGNAL_NOTIFICATION_ID = 2001
        private const val HAZARD_NOTIFICATION_ID = 2002
        private const val SAFE_NOTIFICATION_ID = 2003
    }
}
