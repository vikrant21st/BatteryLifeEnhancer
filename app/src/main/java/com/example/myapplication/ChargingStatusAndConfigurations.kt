package com.example.myapplication

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp

@Composable
fun ChargingStatusAndConfigurations(
    model: MainViewModel,
) {
    Column(horizontalAlignment = Alignment.Start) {
        model.requiredPermissions.forEach {
            when (it) {
                is PermissionErrorModelMini -> {
                    PermissionError(
                        model = it,
                        onClick = {
                            model.visiblePermissionDialogQueue.add(it.permission)
                        },
                    )
                }

                is PermissionErrorModel -> {
                    PermissionError(it)
                }
            }
        }

        ChargingStatus(model.chargingStatus)

        Divider()

        Spacer(Modifier.height(20.dp))

        Row(Modifier.fillMaxHeight()) {
            ConfigView(model.configModel)
        }
    }

    Box(
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