package com.example.myapplication.work

import android.content.Context
import com.example.myapplication.NotificationService
import com.example.myapplication.isOvercharging
import com.example.myapplication.savedAppSettings
import com.example.myapplication.workManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

interface ActionOnCheckChargingStatus {
    suspend fun onOvercharging(): Result<Any>
    suspend fun onCharging(): Result<Any>
    suspend fun onNotCharging(): Result<Any>
}

class ActionOnCheckHotChargingStatus(
    private val applicationContext: Context,
) : ActionOnCheckChargingStatus {
    private suspend fun isOvercharging(): Boolean =
        runCatching {
            val appSettings = applicationContext.savedAppSettings() ?: return false
            if (appSettings.snooze)
                return false
            ChargingStatus(applicationContext).isOvercharging(appSettings)
        }.getOrNull() ?: false

    override suspend fun onOvercharging(): Result<Any> = coroutineScope {
        runCatching {
            val appSettings = applicationContext.savedAppSettings() ?: return@runCatching
            if (!appSettings.snooze) {
                withTimeoutOrNull(30.seconds) {
                    do {
                        NotificationService.getInstance(applicationContext).let {
                            it.cancelNotificationForOvercharging()
                            it.sendNotificationForOvercharging()
                        }

                        delay(5.seconds)
                    } while (isOvercharging())
                }

                applicationContext.workManager.enqueueHotWorkRequest()
            }
        }
    }

    override suspend fun onCharging(): Result<Any> {
        NotificationService.getInstance(applicationContext)
            .cancelNotificationForOvercharging()
        return applicationContext.workManager.enqueueColdWorkRequest()
    }

    override suspend fun onNotCharging(): Result<Any> {
        NotificationService.getInstance(applicationContext).let {
            it.cancelNotificationForOvercharging()
            it.cancelNotificationForChargingStarted()
        }
        return applicationContext.workManager.enqueueColdWorkRequest()
    }
}

class ActionOnCheckColdChargingStatus(
    private val applicationContext: Context,
) : ActionOnCheckChargingStatus {
    // Switch to hot mode meaning frequently check and notify the user
    override suspend fun onOvercharging(): Result<Unit> {
        NotificationService.getInstance(applicationContext)
            .sendNotificationForChargingStarted()
        return applicationContext.workManager.enqueueHotWorkRequest()
    }

    override suspend fun onCharging(): Result<Unit> {
        NotificationService.getInstance(applicationContext)
            .sendNotificationForChargingStarted()
        return applicationContext.workManager.enqueueColdWorkRequest()
    }

    // Even if WorkRequest is enqueued now, it won't start until
    // the charging constraint is satisfied
    override suspend fun onNotCharging(): Result<Unit> {
        NotificationService.getInstance(applicationContext).let {
            it.cancelNotificationForOvercharging()
            it.cancelNotificationForChargingStarted()
        }
        return applicationContext.workManager.enqueueColdWorkRequest()
    }
}
