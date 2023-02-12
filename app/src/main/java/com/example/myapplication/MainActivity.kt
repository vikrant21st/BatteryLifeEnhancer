package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.datastore.dataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.WorkManager
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

val Context.dataStore by dataStore("app-settings.json", AppSettingsSerializer)

val Context.workManager get() = WorkManager.getInstance(this)

val isTiramisuApi get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

fun Context.hasPermission(permission: String) =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

class MainActivity : ComponentActivity() {
    private val connectionReceiver = PowerConnectionReceiver(this::readChargingStatus)
    private val model = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val registerForActivityResult =
            if (isTiramisuApi)
                registerForActivityResult(RequestPermission()) {
                    model.notificationPermissionModel.hasPermission = it
                }
            else
                null

        lifecycleScope.launchWhenCreated {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                resumeAction()
                requestNotificationPermission1(registerForActivityResult)
            }
        }

        setContent {
            MyApplicationTheme {
                val scope = rememberCoroutineScope()
                remember {
                    with(model) {
                        saveSettings = { transformFn ->
                            dataStore.updateData(transformFn)
                            scope.launch {
                                startNotificationsWork()
                            }
                        }

                        notificationPermissionModel.requestPermission = {
                            requestNotificationPermission1(registerForActivityResult)
                        }
                    }

                    scope.launch {
                        launch { dataStore.data.collectLatest(model.configModel::apply) }
                        startNotificationsWork()
                    }
                }

                Surface(Modifier.fillMaxSize()) {
                    ChargingStatusAndConfigurations(model)
                }
            }
        }
    }

    private fun resumeAction() {
        registerReceiver(connectionReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun requestNotificationPermission1(registerForActivityResult: ActivityResultLauncher<String>?) {
        if (registerForActivityResult != null && isTiramisuApi) {
            model.notificationPermissionModel.hasPermission = hasPermission(Manifest.permission.POST_NOTIFICATIONS)

            if (!model.notificationPermissionModel.hasPermission)
                registerForActivityResult.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            model.notificationPermissionModel.hasPermission = true
        }
    }

    private suspend fun startNotificationsWork() {
        with(BackgroundAction) {
            createNotificationChannel()
            cancelPrevious()
            val appSettings = dataStore.data.firstOrNull() ?: return
            if (model.notificationPermissionModel.hasPermission && !appSettings.snooze)
                scheduleNextWork(
                    NotificationStage(model.chargingStatus.isCharging, model.chargingStatus.levelPercentage, appSettings)
                )
        }
    }

    private fun readChargingStatus(isCharging: Boolean, levelPercentage: Int) =
        model.updateChargingStatus(isCharging, levelPercentage)

    override fun onStop() {
        super.onStop()
        unregisterReceiver(connectionReceiver)
    }
}

@Composable
fun ChargingStatusAndConfigurations(model: MainViewModel) {
    Column(horizontalAlignment = Alignment.Start) {
        PermissionError(model.notificationPermissionModel)

        ChargingStatus(model.chargingStatus)

        Divider()

        Spacer(Modifier.height(20.dp))

        Row(Modifier.fillMaxHeight()) {
            ConfigView(model.configModel)
        }
    }

    Box (
        modifier = Modifier
           .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        SnackbarHost(hostState = model.snackBarHostState) {
            Snackbar(
                snackbarData = it,
                modifier = Modifier.padding(20.dp),
            )
        }

        if (LocalInspectionMode.current)
            Snackbar(
                modifier = Modifier.padding(20.dp),
                dismissAction = {
                    TextButton({}) { Text("Close") }
                },
            ) {
                Text(text = "This is a Snack bar")
            }
    }
}
