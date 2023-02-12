package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class PermissionsErrorModel(
    val text: String,
) {
    var hasPermission by mutableStateOf(false)
    lateinit var requestPermission: () -> Unit
}

class PermissionsErrorModelProvider : PreviewParameterProvider<PermissionsErrorModel> {
    override val values = sequenceOf(
        PermissionsErrorModel(
            text = "Allow permission request for something, user may need to reinstall" +
                    "the app on some devices if permission is denied multiple times",
        ).apply {
            requestPermission = { hasPermission = true }
        }
    )
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionError(
    @PreviewParameter(PermissionsErrorModelProvider::class) model: PermissionsErrorModel,
) {
    if (model.hasPermission)
        return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.error,
        shadowElevation = 2.dp,
        onClick = model.requestPermission,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier.padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = model.requestPermission) {
                    Text("Allow")
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = model.text,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = TextStyle(),
                    )
                }
            }
        }
    }
}
