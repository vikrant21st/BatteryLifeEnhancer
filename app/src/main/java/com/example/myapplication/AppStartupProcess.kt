package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.navigation.ChargingAlarmService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class AppStartupProcess(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        coroutineScope {
            if (applicationContext.savedAppSettings()?.isAppInitialized != true) {
                launch {
                    initializeNotificationChannels(applicationContext)
                }

                launch {
                    initializeChargingAlarmService(applicationContext)
                }

                joinAll()
                applicationContext.appSettingsDataStore.updateData {
                    it.copy(isAppInitialized = true)
                }
            }
            Result.success()
        }

    private suspend fun initializeChargingAlarmService(context: Context) =
        runCatchingAndLogIfError(
            tag = TAG,
            message = "initializeChargingAlarmService: Error "
        ) {
            ChargingAlarmService.getInstance(context).restart()
        }

    private fun initializeNotificationChannels(context: Context) =
        runCatchingAndLogIfError(
            tag = TAG,
            message = "initializeNotificationChannels: Error "
        ) {
            NotificationService.getInstance(context).createNotificationChannel()
        }

    companion object {
        private val TAG = this::class.java.enclosingClass.simpleName.take(23)
    }
}
