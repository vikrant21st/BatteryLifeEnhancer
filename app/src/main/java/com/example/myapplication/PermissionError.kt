package com.example.myapplication

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.myapplication.previews.providers.PermissionErrorModelMiniProvider
import com.example.myapplication.previews.providers.PermissionErrorModelProvider

typealias OnPermissionResult = (model: PermissionErrorModel, isGranted: Boolean) -> Unit

interface PermissionErrorModelIfc {
    val permission: String
}

class PermissionErrorModel(
    override val permission: String,
    val missingPermissionMessage: String,
    val onPermissionResult: OnPermissionResult,
): PermissionErrorModelIfc {
    var hasPermission by mutableStateOf(false)
}

class PermissionErrorModelMini(
    override val permission: String,
    val missingPermissionMessage: String,
    val launchIntent: Intent,
    val setPermission: (PermissionErrorModelMini) -> Unit,
): PermissionErrorModelIfc {
    var hasPermission by mutableStateOf(true)
}

@Preview(showBackground = true)
@Composable
fun PermissionError(
    @PreviewParameter(PermissionErrorModelMiniProvider::class)
    model: PermissionErrorModelMini,
    onClick: () -> Unit = {},
) {
    if (model.hasPermission)
        return

    PermissionErrorMessage(
        text = model.missingPermissionMessage,
        onClick = onClick,
    )
}

@Preview(showBackground = true)
@Composable
fun PermissionError(
    @PreviewParameter(PermissionErrorModelProvider::class)
    model: PermissionErrorModel,
) {
    if (model.hasPermission)
        return

    val permissionResultLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                model.onPermissionResult(model, isGranted)
            }
        )

    PermissionErrorMessage(
        text = model.missingPermissionMessage,
        onClick = {
            permissionResultLauncher.launch(model.permission)
        },
    )
}

@Composable
fun PermissionErrorMessage(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.error,
        shadowElevation = 2.dp,
        onClick = onClick,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier.padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onClick) {
                    Text("Grant")
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        style = TextStyle(),
                    )
                }
            }
        }
    }
}