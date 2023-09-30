package com.example.myapplication.previews.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.myapplication.ChargingStatusModel
import com.example.myapplication.MainViewModel

class MyViewModelProvider : PreviewParameterProvider<MainViewModel> {
    override val values = sequenceOf(
        MainViewModel().apply {
            saveSettings = {}
            chargingStatus = ChargingStatusModel(true, 50)
        }
    )
}