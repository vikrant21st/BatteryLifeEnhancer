package com.example.myapplication

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.preferences.core.Preferences
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object MainWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appSettings = context.savedAppSettings()!!

        provideContent {
//            currentState<Preferences>()[""]
            WidgetContent(
                appSettings = appSettings,
                onUpdate = { context.appSettingsDataStore.updateData(it) },
            )
        }
    }
}

@Composable
private fun WidgetContent(
    appSettings: AppSettings,
    onUpdate: suspend (TransformFunction<AppSettings>) -> Unit,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val scope = rememberCoroutineScope {Dispatchers.IO}

        Text(text = "Snooze ${if (appSettings.snooze) "ON" else "OFF"}")

        Button(text = "Increment", onClick = {
            scope.launch {
                onUpdate { it.copy(snooze = !it.snooze) }
            }
        })
    }
}