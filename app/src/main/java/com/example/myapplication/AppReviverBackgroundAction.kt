package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class AppReviverBackgroundAction(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    private var isCharging = false
    private var levelPercentage: Int = 0

    private fun getCurrentStage(appSettings: AppSettings) =
        NotificationStage(isCharging, levelPercentage, appSettings)

    override suspend fun doWork(): Result {
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

        val appSettings = applicationContext.dataStore.data.firstOrNull()
        val notificationStage = appSettings?.let(::getCurrentStage) ?: NotificationStage.NORMAL

        with(BackgroundAction) {
            applicationContext.cancelPrevious()
            applicationContext.scheduleNextWork(notificationStage)
        }
        return Result.success()
    }

    companion object {
        fun scheduleWork(context: Context) {
            context.workManager
                .enqueue(
                    PeriodicWorkRequestBuilder<AppReviverBackgroundAction>(1.hours.toJavaDuration())
                        .addTag(BackgroundAction.TAG_APP_REVIEWER)
                        .build()
                )
        }

        fun cancelPrevious(context: Context) {
            runCatching {
                context.workManager.cancelAllWorkByTag(BackgroundAction.TAG_APP_REVIEWER)
            }
        }
    }
}