package com.example.myapplication.previews.providers

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.myapplication.ConfigModel

class ConfigModelProvider: PreviewParameterProvider<ConfigModel> {
    override val values = sequenceOf(
        ConfigModel(
            save = {},
            snackBarHostState = SnackbarHostState(),
        ).apply {
            snooze = true
        },

        ConfigModel(
            save = {},
            snackBarHostState = SnackbarHostState(),
        ).apply {
            underchargingLimit = "50"
            overchargingLimit = "60"
        },

        ConfigModel(
            save = {},
            snackBarHostState = SnackbarHostState(),
        ).apply {
            underchargingLimit = "80"
            overchargingLimit = "20"
        }
    )
}
