package com.example.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MyViewModel(
    settings: AppSettings,
    private val saveSettings: suspend ((AppSettings) -> AppSettings) -> Unit,
    private val onSnooze: (() -> Unit)? = null,
    private val onUnSnooze: (() -> Unit)? = null,
) {
    var isCharging by mutableStateOf(false)
    var levelPercentage by mutableStateOf(0)
    var overchargingLimit by mutableStateOf("")
    var underchargingLimit by mutableStateOf("")
    var snooze by mutableStateOf(false)

//    fun readValues(connectionReceiver: PowerConnectionReceiver) {
//        isCharging = connectionReceiver.isCharging
//        levelPercentage = connectionReceiver.level
//    }

    fun updateChargingStatus(isCharging: Boolean, levelPercentage: Int) {
        this.isCharging = isCharging
        this.levelPercentage = levelPercentage
    }

    suspend fun snoozeUnSnooze() {
        if (snooze)
            onUnSnooze?.invoke()
        else
            onSnooze?.invoke()

        snooze = !snooze
        save()
    }

    suspend fun save() {
        saveSettings.invoke { settings ->
            settings.copy(
                overchargingLimit = overchargingLimit.toInt(),
                underchargingLimit = underchargingLimit.toInt(),
                snooze = snooze,
            )
        }
    }

    fun apply(newValue: AppSettings) {
        overchargingLimit = newValue.overchargingLimit.toString()
        underchargingLimit = newValue.underchargingLimit.toString()
        snooze = newValue.snooze
    }

    init {
        apply(settings)
    }
}
