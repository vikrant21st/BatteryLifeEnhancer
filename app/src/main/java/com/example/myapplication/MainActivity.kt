package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    private val model = MyViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val registerForActivityResult =
            if (isTiramisuApi)
                registerForActivityResult(RequestPermission()) {
                    model.notificationPermission = it
                }
            else
                null

        lifecycleScope.launchWhenCreated {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                resumeAction()
                requestNotificationPermission(registerForActivityResult)
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
                        requestNotificationPermission = {
                            requestNotificationPermission(registerForActivityResult)
                        }
                        requestBatteryOptimizePermission = ::requestBatteryOptimizePermission
                    }

                    scope.launch {
                        launch { dataStore.data.collectLatest(model::apply) }
                        startNotificationsWork()
                    }
                }

                ChargingStatusAndConfigurations(model)
            }
        }
    }

    private fun resumeAction() {
        registerReceiver(connectionReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        checkIfIgnoringBatteryOptimizations()
    }

    private fun requestNotificationPermission(registerForActivityResult: ActivityResultLauncher<String>?) {
        if (registerForActivityResult != null && isTiramisuApi) {
            model.notificationPermission = hasPermission(Manifest.permission.POST_NOTIFICATIONS)

            if (!model.notificationPermission)
                registerForActivityResult.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            model.notificationPermission = true
        }
    }

    private fun requestBatteryOptimizePermission() {
        checkIfIgnoringBatteryOptimizations()
        if (model.batteryOptimizePermission)
            return

        startActivity(
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:$packageName"),
            )
        )
    }

    private fun checkIfIgnoringBatteryOptimizations() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        model.batteryOptimizePermission = powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private suspend fun startNotificationsWork() {
        with(BackgroundAction) {
            createNotificationChannel()
            cancelPrevious()
            val appSettings = dataStore.data.firstOrNull() ?: return
            if (model.notificationPermission && !appSettings.snooze)
                scheduleNextWork(
                    NotificationStage(model.isCharging, model.levelPercentage, appSettings)
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
fun ChargingStatusAndConfigurations(model: MyViewModel) {
    Column(horizontalAlignment = Alignment.Start) {
        PermissionError(
            model.notificationPermission,
            text = "App won't work without 'Notification' permission",
            requestPermission = model.requestNotificationPermission,
        )

        PermissionError(
            model.batteryOptimizePermission,
            text = "Allow app to run unrestricted in background",
            requestPermission = model.requestBatteryOptimizePermission,
        )

        Row(
            modifier = Modifier
                .height(80.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.fillMaxHeight()
            )
            Column(Modifier.padding(start = 10.dp)) {
                PairRow("Charging", if (model.isCharging) "Yes" else "No")
                PairRow("Level", "${model.levelPercentage} %")
            }
        }

        Row(Modifier.padding(horizontal = 10.dp)) {
            ConfigForm(model)
        }
    }
}

private val noOp = {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionError(
    hasPermission: Boolean,
    text: String,
    requestPermission: () -> Unit = noOp,
) {
    if (!hasPermission) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error,
            shadowElevation = 2.dp,
            onClick = requestPermission,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = text,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = TextStyle(),
                    fontStyle = FontStyle.Italic,
                )

                if (requestPermission != noOp)
                    Column {
                        TextButton(onClick = requestPermission) {
                            Text("Allow")
                        }
                    }
            }

            Divider(
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private val underChargingLimitRange = 10..70
private val overChargingLimitRange = 50..99

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigForm(model: MyViewModel) {
    Column {
        val scope = rememberCoroutineScope()
        val snackBarHostState = remember { SnackbarHostState() }

        val isUnderchargingLimitValid = model.underchargingLimit.toIntOrNull().let {
            it != null && underChargingLimitRange.contains(it) &&
                    (model.overchargingLimit.toIntOrNull()
                        ?: overChargingLimitRange.min()) - 5 > it
        }

        val isOverchargingLimitValid = model.overchargingLimit.toIntOrNull().let {
            it != null && overChargingLimitRange.contains(it) &&
                    (model.underchargingLimit.toIntOrNull()
                        ?: underChargingLimitRange.min()) + 5 < it
        }

        Divider()
        Spacer(Modifier.height(20.dp))

        Text(
            "Select charging range (${underChargingLimitRange.min()} < ${overChargingLimitRange.max()})",
            style = MaterialTheme.typography.titleSmall,
        )

        Text(
            "Note: There should be a gap at least 5%",
            style = MaterialTheme.typography.bodySmall,
        )

        val numericKeyboard = remember { KeyboardOptions(keyboardType = KeyboardType.Number) }

        Row(
            modifier = Modifier.height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(0.45f),
                contentAlignment = Alignment.TopCenter,
            ) {
                OutlinedTextField(
                    value = model.underchargingLimit,
                    onValueChange = { model.underchargingLimit = it },
                    isError = !isUnderchargingLimitValid,
                    trailingIcon = { Text("%") },
                    enabled = !model.snooze,
                    label = {
                        Text(
                            "(${underChargingLimitRange.min()} < ${underChargingLimitRange.max()})",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = numericKeyboard,
                    modifier = Modifier.width(100.dp),
                )
            }

            Box(Modifier.fillMaxWidth(0.1f), contentAlignment = Alignment.Center) {
                Text("-")
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                OutlinedTextField(
                    value = model.overchargingLimit,
                    onValueChange = { model.overchargingLimit = it },
                    isError = !isOverchargingLimitValid,
                    trailingIcon = { Text("%") },
                    enabled = !model.snooze,
                    label = {
                        Text(
                            "(${overChargingLimitRange.min()} < ${overChargingLimitRange.max()})",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = numericKeyboard,
                    modifier = Modifier.width(100.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    model.save()
                    snackBarHostState.showSnackbar("Saved", duration = SnackbarDuration.Short)
                }
            },
            enabled = !model.snooze && isOverchargingLimitValid && isUnderchargingLimitValid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save")
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                scope.launch {
                    model.snoozeUnSnooze()
                    snackBarHostState.showSnackbar(
                        if (model.snooze)
                            "Saved and snoozed"
                        else
                            "Saved and snooze is off",
                        duration = SnackbarDuration.Short,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (model.snooze) "Snoozing (Tap to stop snoozing)"
                else "Snooze temporarily"
            )
        }
        SnackbarHost(hostState = snackBarHostState) {
            Snackbar(it)
        }
    }
}

@Composable
fun PairRow(
    key: @Composable () -> Unit,
    value: @Composable () -> Unit,
    colon: Boolean = true,
) {
    Row(
        Modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.fillMaxWidth(if (colon) 0.45f else 0.5f),
        ) {
            key()
        }

        if (colon)
            Column(Modifier.fillMaxWidth(0.1f)) {
                Text(":", textAlign = TextAlign.Center)
            }

        Column(Modifier.widthIn()) {
            value()
        }
    }
}

@Composable
fun PairRow(key: String, value: String) {
    PairRow(
        key = { Text(key) },
        value = { Text(value) },
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        val model = remember {
            MyViewModel().apply {
                saveSettings = {}
                requestNotificationPermission = { true }
                requestBatteryOptimizePermission = { true }
            }
        }
        ChargingStatusAndConfigurations(model)
    }
}