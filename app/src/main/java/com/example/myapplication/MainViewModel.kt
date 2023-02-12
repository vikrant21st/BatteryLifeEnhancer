package com.example.myapplication

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainViewModel {
    lateinit var saveSettings: suspend ((AppSettings) -> AppSettings) -> Unit

    val notificationPermissionModel = PermissionsErrorModel(
        text = "App won't work without 'Notification' permission, " +
                "you might need to reinstall the application if you have denied permission previously.",
    )

    var chargingStatus by mutableStateOf(ChargingStatusModel())
    val snackBarHostState = SnackbarHostState()
    val configModel = ConfigModel(
        save = ::save,
        snackBarHostState = snackBarHostState,
    )

    fun updateChargingStatus(isCharging: Boolean, levelPercentage: Int) {
        chargingStatus = ChargingStatusModel(isCharging, levelPercentage)
    }

    private suspend fun save() {
        saveSettings.invoke { settings ->
            settings.copy(
                overchargingLimit = configModel.overchargingLimit.toInt(),
                underchargingLimit = configModel.underchargingLimit.toInt(),
                snooze = configModel.snooze,
            )
        }
    }
}
