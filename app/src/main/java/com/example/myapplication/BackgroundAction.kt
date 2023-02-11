package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

enum class NotificationStage {
    NORMAL,
    UNDERCHARGED_NOT_CHARGING,
    OVERCHARGED_NOT_CHARGING,
    UNDERCHARGED_CHARGING,
    OVERCHARGED_CHARGING;

    companion object {
        operator fun invoke(isCharging: Boolean, levelPercentage: Int, appSettings: AppSettings) =
            if (isCharging) {
                if (appSettings.overchargingLimit < levelPercentage)
                    OVERCHARGED_CHARGING
                else if (appSettings.underchargingLimit > levelPercentage)
                    UNDERCHARGED_CHARGING
                else
                    NORMAL
            } else {
                if (appSettings.overchargingLimit < levelPercentage)
                    OVERCHARGED_NOT_CHARGING
                else if (appSettings.underchargingLimit > levelPercentage)
                    UNDERCHARGED_NOT_CHARGING
                else NORMAL
            }
    }
}

class BackgroundAction(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    private var isCharging = false
    private var levelPercentage: Int = 0

    private fun getCurrentStage(appSettings: AppSettings) =
        NotificationStage(isCharging, levelPercentage, appSettings)

    override suspend fun doWork(): Result = coroutineScope {
        updateChargingStatus()

        val appSettings = applicationContext.dataStore.data.firstOrNull()
        val notificationStage = appSettings?.let(::getCurrentStage) ?: NotificationStage.NORMAL

        launch {
            if (appSettings != null)
                runCatching {
                    applicationContext.cancelNotification()
                    notifyIfNeeded(appSettings, notificationStage)
                }.onFailure {
                    Log.e(logTag, it.stackTraceToString())
                }
        }

        launch {
            applicationContext.scheduleNextWork(notificationStage)
        }

        joinAll()
        Result.success()
    }

    private suspend fun updateChargingStatus() = coroutineScope {
        var workDone = false
        val connectionReceiver = PowerConnectionReceiver { isChargingVal, levelPercentageVal ->
            isCharging = isChargingVal
            levelPercentage = levelPercentageVal
            workDone = true
        }

        applicationContext.registerReceiver(
            connectionReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        withTimeoutOrNull(1.seconds) {
            while (!workDone)
                delay(100)
        }
        runCatching { applicationContext.unregisterReceiver(connectionReceiver) }
    }

    private fun isNotificationRequired(notificationStage: NotificationStage) =
        when (notificationStage) {
            NotificationStage.NORMAL,
            NotificationStage.OVERCHARGED_NOT_CHARGING,
            NotificationStage.UNDERCHARGED_CHARGING -> false
            else -> true
        }

    @SuppressLint("MissingPermission")
    private fun notifyIfNeeded(appSettings: AppSettings, notificationStage: NotificationStage) {
        with(NotificationManagerCompat.from(applicationContext)) {
            if (isNotificationRequired(notificationStage) &&
                applicationContext.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
            ) {
                notify(TAG_BATTERY_STATUS_CHECKER, NOTIFICATION_ID, createNotification(appSettings))
            }
        }
    }

    private fun createNotification(appSettings: AppSettings): Notification {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationTitle =
            if (isCharging) "Unplug charger"
            else "Plug-in the charger"

        val notificationText =
            if (isCharging)
                "Charging level reached/crossed ${appSettings.overchargingLimit}%"
            else
                "Charging level dropped below ${appSettings.underchargingLimit}%"



        return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(Color.RED)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    companion object {
        private const val logTag = "BackgroundAction"
        private const val TAG_BATTERY_STATUS_CHECKER = "BatteryLifeEnhancerCheckerWork"
        private const val NOTIFICATION_CHANNEL = "BatteryLifeEnhancerChannel"
        private const val NOTIFICATION_ID = 1

        fun ComponentActivity.createNotificationChannel() {
            val name = "Warning"
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Overcharging notifications"
                }
            // Register the channel with the system
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        fun Context.scheduleNextWork(notificationStage: NotificationStage) {
            val time: Long = when (notificationStage) {
                NotificationStage.OVERCHARGED_CHARGING,
                NotificationStage.UNDERCHARGED_NOT_CHARGING -> 5

                NotificationStage.OVERCHARGED_NOT_CHARGING,
                NotificationStage.UNDERCHARGED_CHARGING,
                NotificationStage.NORMAL -> 120
            }

            workManager
                .enqueue(
                    OneTimeWorkRequestBuilder<BackgroundAction>()
                        .setInitialDelay(time, TimeUnit.SECONDS)
                        .addTag(TAG_BATTERY_STATUS_CHECKER)
                        .build()
                )
        }

        fun Context.cancelPrevious() {
            runCatching {
                workManager.cancelAllWorkByTag(TAG_BATTERY_STATUS_CHECKER)
                cancelNotification()
            }
        }

        fun Context.cancelNotification() =
            with(NotificationManagerCompat.from(this)) {
                runCatching { this.cancel(TAG_BATTERY_STATUS_CHECKER, NOTIFICATION_ID) }
            }
    }
}
