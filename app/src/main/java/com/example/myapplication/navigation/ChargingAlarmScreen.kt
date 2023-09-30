package com.example.myapplication.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.AppSettings
import com.example.myapplication.R
import com.example.myapplication.TransformFunction
import com.example.myapplication.numericKeyboard
import kotlinx.coroutines.flow.flowOf

@Preview(showBackground = true)
@Composable
fun ChargingAlarmScreenPreview() {
    val datastore = remember {
        object : DataStore<AppSettings> {
            private var appSettings = AppSettings()

            override val data get() = flowOf(appSettings)

            override suspend fun updateData(
                transform: suspend (t: AppSettings) -> AppSettings,
            ) =
                transform(appSettings).also { appSettings = it }
        }
    }

    val chargingAlarmService = remember {
        object : ChargingAlarmService {
            override suspend fun snooze() = runCatching {
                println("Snoozing")
            }

            override suspend fun restart() = runCatching {
                println("restarting")
            }
        }
    }

    Box(
        Modifier
            .width(1080.dp)
            .height(500.dp),
    ) {
        ChargingAlarmScreen(
            chargingAlarmService,
            AppSettings(),
            updateAppSettings = { transformFunc ->
                datastore.updateData(transformFunc)
            },
        )
    }
}

private val chargingLimitRange = 51..100

@Composable
fun ChargingAlarmScreen(
    chargingAlarmService: ChargingAlarmService,
    appSettings: AppSettings,
    updateAppSettings: suspend (TransformFunction<AppSettings>) -> Unit,
) {
    val model: ChargingAlarmViewModel =
        viewModel(
            factory = ViewModelFactory(
                chargingAlarmService, appSettings,
                updateAppSettings
            )
        )

    val isChargingLimitValid =
        model.chargingLimit.toIntOrNull()?.let { it in chargingLimitRange }
            ?: false

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = model.chargingLimit,
            onValueChange = { model.chargingLimit = it },
            isError = !isChargingLimitValid,
            label = { Text(text = "Charging limit") },
            suffix = { Text(text = "%") },
            supportingText = {
                Text(
                    text = "${chargingLimitRange.min()}% - " +
                            "${chargingLimitRange.max()}%",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
            singleLine = true,
            keyboardOptions = numericKeyboard,
            modifier = Modifier.width(150.dp),
            enabled = !model.snooze,
        )

        Button(
            onClick = { model.updateChargingLimit() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !model.snooze,
        ) {
            Text("Save")
        }

        Button(
            onClick = { model.toggleSnooze() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = snoozeIcon(snooze = model.snooze),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                if (model.snooze) "Turn on"
                else "Turn off",
            )
        }
    }
}

@Composable
private fun snoozeIcon(snooze: Boolean) =
    if (snooze)
        painterResource(R.drawable.notifications_active)
    else
        painterResource(R.drawable.notifications_inactive)
