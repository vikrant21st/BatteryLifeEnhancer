package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.myapplication.previews.providers.ConfigModelProvider
import kotlinx.coroutines.launch

private val underChargingLimitRange = 10..70
private val overChargingLimitRange = 50..99

class ConfigModel(
    val save: suspend () -> Unit,
    val snackBarHostState: SnackbarHostState,
) {
    var overchargingLimit by mutableStateOf("")
    var underchargingLimit by mutableStateOf("")
    var snooze by mutableStateOf(false)

    suspend fun snoozeUnSnooze() {
        snooze = !snooze
        save()
    }

    fun apply(newValue: AppSettings) {
        overchargingLimit = newValue.overchargingLimit.toString()
        underchargingLimit = newValue.underchargingLimit.toString()
        snooze = newValue.snooze
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigView(
    @PreviewParameter(ConfigModelProvider::class) model: ConfigModel,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 10.dp),
    ) {
        val scope = rememberCoroutineScope()

        if (!model.snooze) {
            Row {
                ConfigForm(model)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    model.snoozeUnSnooze()
                    model.snackBarHostState.showSnackbar(
                        if (model.snooze)
                            "Saved and snoozed"
                        else
                            "Saved and snooze is off",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(snoozeIcon(model), contentDescription = null)

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                if (model.snooze) "Turn on"
                else "Turn off",
            )
        }
    }
}

@Composable
private fun snoozeIcon(model: ConfigModel) =
    if (model.snooze)
        painterResource(R.drawable.notifications_active)
    else
        painterResource(R.drawable.notifications_inactive)

val numericKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ConfigForm(
    model: ConfigModel,
) {
    Column {
        val scope = rememberCoroutineScope()

        Text(
            "Select charging range (${underChargingLimitRange.min()} < ${overChargingLimitRange.max()})",
            style = MaterialTheme.typography.titleSmall,
        )

        Text(
            "Note: There should be a gap at least 5%",
            style = MaterialTheme.typography.bodySmall,
        )

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

            Box(
                Modifier.fillMaxWidth(0.1f),
                contentAlignment = Alignment.Center,
            ) {
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
                    label = {
                        Text(
                            text = "(${overChargingLimitRange.min()} < ${overChargingLimitRange.max()})",
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
                    model.snackBarHostState.showSnackbar(
                        message = "Saved",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true,
                    )
                }
            },
            enabled = !model.snooze && isOverchargingLimitValid && isUnderchargingLimitValid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text("Save")
        }
    }
}