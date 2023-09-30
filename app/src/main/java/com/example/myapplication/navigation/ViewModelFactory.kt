package com.example.myapplication.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.AppSettings
import com.example.myapplication.TransformFunction

class ViewModelFactory(
    private val chargingAlarmService: ChargingAlarmService,
    private val appSettings: AppSettings,
    private val updateAppSettings: suspend (TransformFunction<AppSettings>) -> Unit,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T = with(modelClass) {
        when {
            isAssignableFrom(ChargingAlarmViewModel::class.java) ->
                ChargingAlarmViewModel(
                    chargingAlarmService, appSettings,
                    updateAppSettings
                )

            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
