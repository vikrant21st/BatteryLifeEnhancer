package com.example.myapplication

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun SettingsChangeRequest(
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Change settings")
        },
        text = {
            DialogDescription()
        },
        modifier = modifier,

        confirmButton = {
            Text(
                text = "OK",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOkClick() }
                    .padding(16.dp)
            )
        },
    )
}

@Composable
fun DialogDescription() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "For app to work reliably, " +
                    "you must change the following settings"
        )

        if (isMinApiVersion(Build.VERSION_CODES.S)) {
            Text(
                text = buildAnnotatedString {
                    append("Go to")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" App info")
                    }

                    append(" and turn off")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        if (isMinApiVersion(Build.VERSION_CODES.TIRAMISU))
                            append(" Pause app activity if unused")
                        else
                            append(" Remove permissions and free up space")
                    }
                }
            )
        } else if (isMinApiVersion(Build.VERSION_CODES.R)) {
            Text(
                text = buildAnnotatedString {
                    append("Go to")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" App info")
                    }

                    append(" >")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" Permissions")
                    }

                    append(" and turn off")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" Remove permissions if app isn't used")
                    }
                }
            )
        } else {
            Text(
                text = buildAnnotatedString {
                    append("Go to")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" Play app")
                    }

                    append(" >")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" Menu")
                    }

                    append(" >")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" Play Protect")
                    }

                    append(" >")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" Permissions for Unused Apps")
                    }

                    append(" >")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" Remove permissions if app isn't used")
                    }
                }
            )
        }
    }
}
