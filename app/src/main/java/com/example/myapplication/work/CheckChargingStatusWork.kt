package com.example.myapplication.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.myapplication.runCatchingAndLogIfError
import com.example.myapplication.savedAppSettings
import java.time.Duration

sealed class CheckChargingStatusWork(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), ActionOnCheckChargingStatus {
    override suspend fun doWork(): Result {
        val appSettings = applicationContext.savedAppSettings()
            ?: return Result.success()

        val chargingStatus = ChargingStatus(applicationContext)

        when {
            chargingStatus.isCharging -> {
                Log.d(
                    TAG, "Charged ${chargingStatus.levelPercentage}%"
                )
                when {
                    chargingStatus.levelPercentage >=
                            appSettings.overchargingLimit ->
                        onOvercharging()

                    else -> onCharging()
                }
            }

            // not charging
            else -> {
                Log.d(TAG, "Not charging")
                onNotCharging()
            }
        }
        return Result.success()
    }

    class Cold(
        context: Context,
        params: WorkerParameters,
    ) : CheckChargingStatusWork(context, params),
        ActionOnCheckChargingStatus by ActionOnCheckColdChargingStatus(context)

    class Hot(
        context: Context,
        params: WorkerParameters,
    ) : CheckChargingStatusWork(context, params),
        ActionOnCheckChargingStatus by ActionOnCheckHotChargingStatus(context)
}

private val TAG = CheckChargingStatusWork::class.simpleName!!.take(23)
private const val WORK_TAG_PARAM = "TAG"
private const val HOT_WORK_TAG = "HOT_WORK"
private const val COLD_WORK_TAG = "COLD_WORK"
private const val APP_REVIVER_WORK_TAG = "APP_REVIVER_WORK"
private const val COLD_PERIOD = 60L
private const val HOT_PERIOD = 5L

fun WorkManager.cancelColdWorkRequest() =
    runCatchingAndLogIfError(TAG, "cancelColdWorkRequest: Error") {
        cancelUniqueWork(CheckChargingStatusWork.Cold::class.simpleName!!)
    }

fun WorkManager.cancelHotWorkRequest() =
    runCatchingAndLogIfError(TAG, "cancelHotWorkRequest: Error") {
        cancelUniqueWork(CheckChargingStatusWork.Hot::class.simpleName!!)
    }

fun WorkManager.cancelAppReviverWorkRequest() =
    runCatchingAndLogIfError(TAG, "cancelAppReviverWorkRequest: Error") {
        cancelUniqueWork(AppReviverWork::class.simpleName!!)
    }

fun WorkManager.enqueueColdWorkRequest() =
    runCatchingAndLogIfError(TAG, "enqueueColdWorkRequest: Error") {
        enqueueUniqueWork(
            TAG,
            ExistingWorkPolicy.REPLACE,
            getColdWorkRequest(),
        )
    }

fun WorkManager.enqueueHotWorkRequest() =
    runCatchingAndLogIfError(TAG, "enqueueHotWorkRequest: Error") {
        enqueueUniqueWork(
            TAG,
            ExistingWorkPolicy.REPLACE,
            getHotWorkRequest(),
        )
    }

fun WorkManager.enqueueAppReviverWorkRequest(period: Duration) =
    runCatchingAndLogIfError(TAG, "enqueueAppReviverWorkRequest: Error") {
        enqueueUniquePeriodicWork(
            TAG,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            getAppReviverWorkRequest(period),
        )
    }

private fun getColdWorkRequest() =
    OneTimeWorkRequestBuilder<CheckChargingStatusWork.Cold>()
        .setInitialDelay(Duration.ofSeconds(COLD_PERIOD))
        .setTagAndInputData(COLD_WORK_TAG)
        .setConstraints()
        .build()

private fun getHotWorkRequest() =
    OneTimeWorkRequestBuilder<CheckChargingStatusWork.Hot>()
        .setInitialDelay(Duration.ofSeconds(HOT_PERIOD))
        .setTagAndInputData(HOT_WORK_TAG)
        .build()

private fun getAppReviverWorkRequest(period: Duration) =
    PeriodicWorkRequestBuilder<AppReviverWork>(period)
        .addTag(APP_REVIVER_WORK_TAG)
        .build()

private fun OneTimeWorkRequest.Builder.setConstraints() =
    this.setConstraints(
        Constraints.Builder()
            .setRequiresCharging(true)
            .build()
    )

private fun OneTimeWorkRequest.Builder.setTagAndInputData(
    tag: String,
) =
    this.addTag(tag)
        .setInputData(
            Data.Builder()
                .putString(WORK_TAG_PARAM, tag)
                .build()
        )
