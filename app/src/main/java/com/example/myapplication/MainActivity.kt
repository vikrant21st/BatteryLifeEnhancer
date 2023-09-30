package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import com.example.myapplication.navigation.ChargingAlarmService
import com.example.myapplication.navigation.TodoNavGraph
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

const val UnusedAppRestrictionsRequirement = "UnusedAppRestrictionsRequirement"

fun isMinApiVersion(versionCode: Int) = Build.VERSION.SDK_INT >= versionCode

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

class MainActivity : ComponentActivity() {
    private val permissionsToRequest =
        buildSet<String> {
            if (isMinApiVersion(Build.VERSION_CODES.TIRAMISU))
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)

//            if (isMinApiVersion(Build.VERSION_CODES.TIRAMISU))
//                arrayOf(Manifest.permission.USE_EXACT_ALARM)
//            else if (isMinApiVersion(Build.VERSION_CODES.S))
//                arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM)

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val chargingAlarmService = ChargingAlarmService.getInstance(this)
        val savedAppSettings = runBlocking { savedAppSettings()!! }

        workManager.enqueueUniqueWork(
            /* uniqueWorkName = */ AppStartupProcess::class.java.simpleName,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<AppStartupProcess>()
                .addTag(AppStartupProcess::class.java.simpleName)
                .setInitialDelay(1.seconds.toJavaDuration())
                .build(),
        )

        setContent {
            MyApplicationTheme {
                val viewModel = viewModel<MainViewModel>()

                AppPermissionsDialogs(viewModel, this, permissionsToRequest)

                Surface(Modifier.fillMaxSize()) {
                    Column {
                        Row {
                            PermissionsErrors(viewModel)
                        }

                        Row {
                            TodoNavGraph(
                                chargingAlarmService,
                                savedAppSettings,
                                updateAppSettings = { transformFunc ->
                                    appSettingsDataStore.updateData(transformFunc)
                                },
                            )
                        }
                    }
                }

                LaunchedEffect(viewModel) {
                    launch {
                        repeatOnLifecycle(Lifecycle.State.RESUMED) {
                            viewModel.initializePermissions(
                                hasPermission = { hasPermission(it) },
                            )
                        }
                    }
                }
            }
        }
    }
}
