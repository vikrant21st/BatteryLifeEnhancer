package com.example.myapplication.navigation

import android.content.Context
import android.util.Log
import com.example.myapplication.NotificationService
import com.example.myapplication.runCatchingAndLogIfError
import com.example.myapplication.work.cancelColdWorkRequest
import com.example.myapplication.work.cancelHotWorkRequest
import com.example.myapplication.work.enqueueHotWorkRequest
import com.example.myapplication.workManager

interface ChargingAlarmService {
    suspend fun snooze(): Result<Any>

    suspend fun restart(): Result<Any>

    companion object {
        fun getInstance(context: Context): ChargingAlarmService =
            ChargingAlarmServiceImpl(context)
    }
}

private class ChargingAlarmServiceImpl(
    private val context: Context,
) : ChargingAlarmService {
    private val notificationService = NotificationService.getInstance(context)

    override suspend fun snooze() =
        runCatchingAndLogIfError(
            TAG,
            "snooze: Error in cancelNotificationForOvercharging"
        ) {
            Log.d(TAG, "snooze")
            context.workManager.let {
                it.cancelColdWorkRequest()
                it.cancelHotWorkRequest()
            }
            notificationService.cancelNotificationForOvercharging()
            notificationService.cancelNotificationForChargingStarted()
        }

    override suspend fun restart() =
        runCatching {
            Log.d(TAG, "restart")
            snooze()
            context.workManager.enqueueHotWorkRequest()
        }.onFailure {
            Log.e(TAG, "restart: Error in enqueue", it)
        }

    companion object {
        private val TAG = this::class.java.enclosingClass.simpleName.take(23)
    }
}