package com.example.myapplication

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Permission required")
        },
        text = {
            Text(
                text = permissionTextProvider.getDescription(isPermanentlyDeclined)
            )
        },
        modifier = modifier,

        confirmButton = {
            Text(
                text =
                if (isPermanentlyDeclined)
                    "Grant permission"
                else
                    "OK",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isPermanentlyDeclined) {
                            onGoToAppSettingsClick()
                        } else {
                            onOkClick()
                        }
                    }
                    .padding(16.dp)
            )
        },
    )
}

interface PermissionTextProvider {
    fun getDescription(isPermanentlyDeclined: Boolean): String

    companion object {
        fun getByName(permissionName: String): PermissionTextProvider? =
            when (permissionName) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    NotificationPermissionTextProvider()
                }

                Manifest.permission.USE_EXACT_ALARM,
                Manifest.permission.SCHEDULE_EXACT_ALARM -> {
                    AlarmPermissionTextProvider()
                }

                Manifest.permission.CAMERA -> {
                    CameraPermissionTextProvider()
                }

                Manifest.permission.RECORD_AUDIO -> {
                    RecordAudioPermissionTextProvider()
                }

                Manifest.permission.CALL_PHONE -> {
                    PhoneCallPermissionTextProvider()
                }

                else -> null
            }
    }
}

class CameraPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String =
        if (isPermanentlyDeclined) {
            "It seems you permanently declined camera permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs access to your camera so that your friends " +
                    "can see you in a call."
        }
}

class NotificationPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String =
        if (isPermanentlyDeclined) {
            "It seems you permanently declined notifications permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs notifications permission so that you can be " +
                    "notified when phone is overcharging or undercharging"
        }
}

class AlarmPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String =
        if (isPermanentlyDeclined) {
            "It seems you permanently declined alarms permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs alarms permission in order to keep chekcing " +
                    "on battery conditions in background"
        }
}

class RecordAudioPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String =
        if (isPermanentlyDeclined) {
            "It seems you permanently declined microphone permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs access to your microphone so that your friends " +
                    "can hear you in a call."
        }
}

class PhoneCallPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String =
        if (isPermanentlyDeclined) {
            "It seems you permanently declined phone calling permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs phone calling permission so that you can talk " +
                    "to your friends."
        }
}
