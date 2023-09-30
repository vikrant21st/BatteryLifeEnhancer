package com.example.myapplication.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppSettings
import com.example.myapplication.TransformFunction
import kotlinx.coroutines.launch

class ChargingAlarmViewModel(
    private val chargingAlarmService: ChargingAlarmService,
    appSettings: AppSettings,
    private val updateAppSettings: suspend (TransformFunction<AppSettings>) -> Unit,
) : ViewModel() {
    var snooze by mutableStateOf(appSettings.snooze)
    var chargingLimit by mutableStateOf(appSettings.overchargingLimit.toString())

    fun updateChargingLimit() {
        viewModelScope.launch {
            val limit = chargingLimit.toInt()
            updateAppSettings { old ->
                old.copy(overchargingLimit = limit)
            }
            chargingAlarmService.restart()
        }
    }

    fun toggleSnooze() {
        viewModelScope.launch {
            snooze = !snooze
            updateAppSettings { old ->
                old.copy(snooze = snooze)
            }
            if (snooze)
                chargingAlarmService.snooze()
            else
                chargingAlarmService.restart()
        }
    }
}