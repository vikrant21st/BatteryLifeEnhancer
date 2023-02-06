package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.myapplication.BackgroundAction.Companion.cancelPrevious
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

val Context.dataStore by dataStore("app-settings.json", AppSettingsSerializer)
val Context.workManager get() = WorkManager.getInstance(this)
fun Context.hasPermission(permission: String) =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

class MainActivity : ComponentActivity() {
    private val connectionReceiver = PowerConnectionReceiver(this::readChargingStatus)
    private lateinit var model: MyViewModel
    private var permissionGiven by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val registerForActivityResult = registerForActivityResult(RequestPermission()) {
            permissionGiven = it
        }

        lifecycleScope.launchWhenCreated {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                resumeAction(registerForActivityResult)
            }
        }

        setContent {
            MyApplicationTheme {
                val appSettings = dataStore.data.collectAsState(initial = AppSettings()).value

                val scope = rememberCoroutineScope()
                remember {
                    model = MyViewModel(
                        settings = appSettings,
                        saveSettings = { transformFn -> dataStore.updateData(transformFn) },
                        onSnooze = { cancelPrevious() },
                        onUnSnooze = {
                            scope.launch { startNotificationsWork() }
                        },
                    )
                    scope.launch {
                        launch { dataStore.data.collectLatest(model::apply) }
                        startNotificationsWork()
                    }
                }

                ChargingStatusAndConfigurations(model, permissionGiven)
            }
        }
    }

    private fun resumeAction(registerForActivityResult: ActivityResultLauncher<String>) {
        registerReceiver(
            connectionReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        permissionGiven =
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED

        if (!permissionGiven)
            registerForActivityResult.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private suspend fun startNotificationsWork() {
        with(BackgroundAction) {
            createNotificationChannel()
            cancelPrevious()
            val appSettings = dataStore.data.firstOrNull() ?: return
            if (permissionGiven)
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
fun ChargingStatusAndConfigurations(model: MyViewModel, permissionGiven: Boolean) {
    Column(horizontalAlignment = Alignment.Start) {
        if (!permissionGiven)
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error,
            ) {
                Text(
                    "App won't work without 'Notification' permission",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    style = TextStyle(),
                    fontStyle = FontStyle.Italic,
                )
            }

        Row(Modifier.padding(horizontal = 10.dp)) {
            Column {
                PairRow("Charging", if (model.isCharging) "Yes" else "No")
                PairRow("Level", "${model.levelPercentage} %")
            }
        }

        Row(Modifier.padding(horizontal = 10.dp)) {
            ConfigForm(model)
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
                    (model.overchargingLimit.toIntOrNull() ?: overChargingLimitRange.min()) - 5 > it
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
private fun labelTextColor(enabled: Boolean) =
    if (enabled) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.secondary

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
            MyViewModel(settings = AppSettings(), saveSettings = {})
        }
        ChargingStatusAndConfigurations(model, false)
    }
}