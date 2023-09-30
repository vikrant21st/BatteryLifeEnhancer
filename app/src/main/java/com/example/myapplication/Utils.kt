package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myapplication.navigation.ChargingAlarmService
import com.example.myapplication.work.ChargingStatus
import kotlinx.coroutines.runBlocking

inline fun runCatchingAndLogIfError(
    tag: String,
    message: String,
    block: () -> Unit,
) =
    runCatching(block).onFailure { Log.e(tag, message, it) }

typealias TransformFunction<T> = suspend (T) -> T

fun ChargingStatus.isOvercharging(appSettings: AppSettings) =
    isCharging && levelPercentage >= appSettings.overchargingLimit

class ChargingNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return
        runBlocking {
            context.appSettingsDataStore.updateData { it.copy(snooze = true) }
            ChargingAlarmService.getInstance(context).snooze()
        }
    }
}
