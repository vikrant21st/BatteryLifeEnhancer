package com.example.myapplication.previews.providers

import android.Manifest
import android.content.Intent
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.myapplication.PermissionErrorModel
import com.example.myapplication.PermissionErrorModelMini
import com.example.myapplication.UnusedAppRestrictionsRequirement

class PermissionErrorModelProvider : PreviewParameterProvider<PermissionErrorModel> {
    override val values =
        sequenceOf(
            PermissionErrorModel(
                permission = Manifest.permission.LOCATION_HARDWARE,
                missingPermissionMessage = "App needs the permission for something important",
            ) { _, _ -> }
        )
}

class PermissionErrorModelMiniProvider :
    PreviewParameterProvider<PermissionErrorModelMini> {
    override val values =
        sequenceOf(
            PermissionErrorModelMini(
                permission = UnusedAppRestrictionsRequirement,
                launchIntent = Intent(),
                missingPermissionMessage = "App has some restrictions by default" +
                        " which you should remove," +
                        " so that app will keep working properly",
            ) { _ -> }
        )
}