package com.example.myapplication

import android.Manifest
import android.os.Build
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    val requiredPermissions = mutableListOf<PermissionErrorModelIfc>().apply {
        if (isMinApiVersion(Build.VERSION_CODES.TIRAMISU)) {
            add(
                PermissionErrorModel(
                    permission = Manifest.permission.POST_NOTIFICATIONS,
                    missingPermissionMessage = "App won't work without 'Notification' permission",
                    onPermissionResult = ::onPermissionResult,
                )
            )
//            add(
//                PermissionErrorModel(
//                    permission = Manifest.permission.USE_EXACT_ALARM,
//                    missingPermissionMessage = "App won't work without 'Alarm' permission",
//                    onPermissionResult = ::onPermissionResult,
//                )
//            )
//        } else if (isMinApiVersion(Build.VERSION_CODES.S)) {
//            add(
//                PermissionErrorModel(
//                    permission = Manifest.permission.SCHEDULE_EXACT_ALARM,
//                    missingPermissionMessage = "App won't work without 'Alarm' permission",
//                    onPermissionResult = ::onPermissionResult,
//                )
//            )
        }
    }

    fun dismissPermissionDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }

    fun onPermissionResult(
        model: PermissionErrorModelIfc,
        isGranted: Boolean,
    ) {
        if (model is PermissionErrorModel) {
            model.hasPermission = isGranted
            if (!isGranted &&
                !visiblePermissionDialogQueue.contains(model.permission)
            ) {
                visiblePermissionDialogQueue.add(model.permission)
            }
        } else if (model is PermissionErrorModelMini) {
            model.setPermission(model)
        }
    }

    lateinit var saveSettings: suspend ((AppSettings) -> AppSettings) -> Unit

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

    fun initializePermissions(hasPermission: (String) -> Boolean) =
        viewModelScope.launch {
            requiredPermissions.forEach {
                when (it) {
                    is PermissionErrorModel -> {
                        it.hasPermission = hasPermission(it.permission)
                    }

                    is PermissionErrorModelMini -> {
                        it.setPermission(it)
                    }
                }
            }
        }
}
