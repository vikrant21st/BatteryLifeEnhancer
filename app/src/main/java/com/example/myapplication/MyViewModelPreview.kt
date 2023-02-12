package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.myapplication.previews.providers.MyViewModelProvider

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyViewModelPreview(
    @PreviewParameter(MyViewModelProvider::class)
    model: MainViewModel
) {
    ChargingStatusAndConfigurations(model)
}
