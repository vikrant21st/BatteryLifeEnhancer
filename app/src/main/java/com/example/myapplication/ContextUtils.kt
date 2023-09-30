package com.example.myapplication

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.datastore.dataStore
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

val Context.appSettingsDataStore by dataStore("app-settings.json", AppSettingsSerializer)

suspend fun Context.savedAppSettings() = appSettingsDataStore.data.firstOrNull()

val Context.workManager get() = WorkManager.getInstance(this)
val Context.alarmManager get() = getSystemService(AlarmManager::class.java)!!
fun Context.hasPermission(permission: String) =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED