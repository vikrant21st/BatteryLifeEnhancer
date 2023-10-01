package com.example.myapplication.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.NotificationService

class AppReviverWork(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.i(TAG, "doWork: Sending notification")
        NotificationService.getInstance(applicationContext)
            .sendNotificationForAppRevival()
        return Result.success()
    }

    companion object {
        private val TAG = AppReviverWork::class.java.simpleName
    }
}
