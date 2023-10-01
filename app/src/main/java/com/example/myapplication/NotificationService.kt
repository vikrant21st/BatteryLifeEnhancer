package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.time.Duration.Companion.seconds

interface NotificationService {
    fun createNotificationChannel(): Result<Any>
    fun cancelAllNotifications(): Result<Any>
    suspend fun sendNotificationForOvercharging(): Result<Any>
    suspend fun sendNotificationForChargingStarted(): Result<Any>
    suspend fun sendNotificationForAppRevival(): Result<Any>
    suspend fun cancelNotificationForOvercharging(): Result<Any>
    suspend fun cancelNotificationForChargingStarted(): Result<Any>
    suspend fun cancelNotificationForAppRevival(): Result<Any>

    companion object {
        fun getInstance(context: Context): NotificationService =
            NotificationServiceImpl(context)
    }
}

val Context.notificationManager: NotificationManager
    get() =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

private class NotificationServiceImpl(
    private val applicationContext: Context,
) : NotificationService {
    override fun createNotificationChannel() =
        runCatchingAndLogIfError(TAG, "createNotificationChannel: Error") {
            // Register the channel with the system
            applicationContext.notificationManager.let { manager ->
                manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
                    ?.let {
                        Log.d(
                            TAG,
                            "createNotificationChannel: channel id: " +
                                    "$NOTIFICATION_CHANNEL_ID exists",
                        )
                    }
                    ?: manager.createNotificationChannel(getNotificationChannel())
            }
        }

    private fun getNotificationChannel() =
        NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Warning",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Overcharging notifications"
        }

    override fun cancelAllNotifications() =
        runCatchingAndLogIfError(TAG, "cancelAllNotifications: Error") {
            Log.d(TAG, "cancelAllNotifications")
            applicationContext.notificationManager.let { manager ->
                manager.cancel(
                    TAG_BATTERY_STATUS_CHECKER,
                    OVERCHARGING_ALARM_NOTIFICATION_ID
                )
                manager.cancel(
                    TAG_BATTERY_STATUS_CHECKER,
                    CHARGING_STARTED_NOTIFICATION_ID
                )
                manager.cancel(
                    TAG_BATTERY_STATUS_CHECKER,
                    APP_REVIVER_NOTIFICATION_ID
                )
            }
        }

    @SuppressLint("MissingPermission")
    override suspend fun sendNotificationForOvercharging() =
        runCatchingAndLogIfError(
            TAG,
            "sendNotificationForOvercharging: Error"
        ) {
            cancelNotificationForChargingStarted()
            if (hasPermission()) {
                Log.d(TAG, "sendNotificationForOvercharging")
                val appSettings = applicationContext.savedAppSettings()
                    ?: error("AppSettings not found")
                val notification = getOverchargingNotification(appSettings)
                applicationContext.notificationManager.let { manager ->
                    manager.notify(
                        TAG_BATTERY_STATUS_CHECKER,
                        OVERCHARGING_ALARM_NOTIFICATION_ID,
                        notification,
                    )

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            manager.cancel(
                                TAG_BATTERY_STATUS_CHECKER,
                                OVERCHARGING_ALARM_NOTIFICATION_ID,
                            )
                        },
                        4.seconds.inWholeMilliseconds,
                    )
                }
            }
        }

    @SuppressLint("MissingPermission")
    override suspend fun sendNotificationForChargingStarted() =
        runCatchingAndLogIfError(
            TAG,
            "sendNotificationForChargingStarted: Error"
        ) {
            if (hasPermission()) {
                Log.d(TAG, "sendNotificationForChargingStarted")
                val manager = applicationContext.notificationManager

                val notification = manager.activeNotifications.find {
                    it.tag == TAG_BATTERY_STATUS_CHECKER &&
                            it.id == CHARGING_STARTED_NOTIFICATION_ID
                }
                if (notification == null) {
                    manager.notify(
                        TAG_BATTERY_STATUS_CHECKER,
                        CHARGING_STARTED_NOTIFICATION_ID,
                        getChargingStartedNotification(),
                    )
                }
            }
        }

    override suspend fun cancelNotificationForChargingStarted() =
        runCatchingAndLogIfError(
            TAG,
            "cancelNotificationForChargingStarted: Error"
        ) {
            Log.d(TAG, "cancelNotificationForChargingStarted")
            applicationContext.notificationManager
                .cancel(
                    TAG_BATTERY_STATUS_CHECKER,
                    CHARGING_STARTED_NOTIFICATION_ID
                )
        }

    override suspend fun cancelNotificationForOvercharging() =
        runCatchingAndLogIfError(
            TAG,
            "cancelNotificationForOvercharging: Error"
        ) {
            Log.d(TAG, "cancelNotificationForOvercharging")
            applicationContext.notificationManager
                .cancel(
                    TAG_BATTERY_STATUS_CHECKER,
                    OVERCHARGING_ALARM_NOTIFICATION_ID
                )
        }

    override suspend fun sendNotificationForAppRevival(): Result<Any> =
        runCatchingAndLogIfError(
            TAG,
            "sendNotificationForAppRevival: Error"
        ) {
            if (hasPermission()) {
                Log.d(TAG, "sendNotificationForAppRevival")
                val manager = applicationContext.notificationManager

                val notification = manager.activeNotifications.find {
                    it.tag == TAG_BATTERY_STATUS_CHECKER &&
                            it.id == APP_REVIVER_NOTIFICATION_ID
                }
                if (notification == null) {
                    manager.notify(
                        TAG_BATTERY_STATUS_CHECKER,
                        APP_REVIVER_NOTIFICATION_ID,
                        getAppRevivalNotification(),
                    )
                }
            }
        }

    override suspend fun cancelNotificationForAppRevival(): Result<Any> =
        runCatchingAndLogIfError(
            TAG,
            "cancelNotificationForOvercharging: Error"
        ) {
            Log.d(TAG, "cancelNotificationForOvercharging")
            applicationContext.notificationManager
                .cancel(
                    TAG_BATTERY_STATUS_CHECKER,
                    APP_REVIVER_NOTIFICATION_ID,
                )
        }

    private fun hasPermission() =
        !isMinApiVersion(Build.VERSION_CODES.TIRAMISU) ||
                applicationContext.hasPermission(Manifest.permission.POST_NOTIFICATIONS)

    private fun getOverchargingNotification(appSettings: AppSettings) =
        NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(Color.RED)
            .setContentTitle("Unplug charger")
            .setContentText(
                "Charging level exceeding ${appSettings.overchargingLimit}%"
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(mainActivityLaunchIntent())
            .setAutoCancel(true)
            .build()

    private fun mainActivityLaunchIntent(): PendingIntent? {
        val intent =
            Intent(applicationContext, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private suspend fun getAppRevivalNotification(): Notification =
        NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(Color.RED)
            .setContentTitle("Action needed")
            .setContentText("App needs to be opened every " +
                    "${applicationContext.savedAppSettings()?.reviveAppInDays} days")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(mainActivityLaunchIntent())
            .setAutoCancel(false)
            .build()

    private fun getChargingStartedNotification(): Notification {
        val intent =
            Intent(applicationContext, ChargingNotificationReceiver::class.java)
                .apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

        return NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(Color.RED)
            .setContentTitle("Charging started")
            .setContentText("Waiting until phone charges adequately")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(mainActivityLaunchIntent())
            .setAutoCancel(false)
            .addAction(NotificationCompat.Action(null, "Snooze", pendingIntent))
            .build()
    }

    companion object {
        private val TAG = this::class.java.enclosingClass.simpleName.take(23)
        private const val OVERCHARGING_ALARM_NOTIFICATION_ID = 1
        private const val CHARGING_STARTED_NOTIFICATION_ID = 2
        private const val APP_REVIVER_NOTIFICATION_ID = 3

        private const val TAG_BATTERY_STATUS_CHECKER =
            "BatteryLifeEnhancerCheckerWork"
        private const val NOTIFICATION_CHANNEL_ID = "BatteryLifeEnhancerChannel"
    }
}
