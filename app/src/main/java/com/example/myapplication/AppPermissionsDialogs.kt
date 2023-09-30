package com.example.myapplication

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.core.content.IntentCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.content.UnusedAppRestrictionsConstants

@Composable
fun AppPermissionsDialogs(
    viewModel: MainViewModel,
    activity: Activity,
    permissionsToRequest: Set<String>,
) {
    remember {
        addPermissionRequestForUnusedAppRestrictions(viewModel, activity)
        0
    }

    PermissionDialogQueue(viewModel, activity, permissionsToRequest)
}

@Composable
fun PermissionsErrors(viewModel: MainViewModel) {
    Column(horizontalAlignment = Alignment.Start) {
        viewModel.requiredPermissions.forEach {
            when (it) {
                is PermissionErrorModelMini -> {
                    PermissionError(
                        model = it,
                        onClick = {
                            viewModel.visiblePermissionDialogQueue.add(
                                it.permission
                            )
                        },
                    )
                }

                is PermissionErrorModel -> {
                    PermissionError(it)
                }
            }
        }
    }
}

@Composable
private fun PermissionDialogQueue(
    viewModel: MainViewModel,
    activity: Activity,
    permissionsToRequest: Set<String>,
) {
    val multiplePermissionResultLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { perms ->
                viewModel.requiredPermissions.intersect(permissionsToRequest)
                    .forEach { permission ->
                        if (permission is PermissionErrorModel) {
                            viewModel.onPermissionResult(
                                model = permission,
                                isGranted =
                                perms[permission.permission] == true,
                            )
                        }
                    }
            }
        )

    viewModel.visiblePermissionDialogQueue
        .reversed()
        .mapNotNull { permissionName ->
            viewModel.requiredPermissions.find {
                it.permission == permissionName
            }
        }
        .forEach { permission ->
            if (permission is PermissionErrorModelMini) {
                val permissionResultLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = {
                            permission.setPermission(permission)
                        }
                    )

                SettingsChangeRequest(
                    onDismiss = viewModel::dismissPermissionDialog,
                    onOkClick = {
                        viewModel.dismissPermissionDialog()
                        permissionResultLauncher.launch(
                            permission.launchIntent
                        )
                    },
                )
            } else if (permission is PermissionErrorModel) {
                val permissionTextProvider =
                    PermissionTextProvider.getByName(permission.permission)
                        ?: return@forEach

                PermissionDialog(
                    permissionTextProvider = permissionTextProvider,
                    isPermanentlyDeclined =
                    !activity.shouldShowRequestPermissionRationale(
                        permission.permission
                    ),
                    onDismiss = viewModel::dismissPermissionDialog,
                    onOkClick = {
                        viewModel.dismissPermissionDialog()
                        multiplePermissionResultLauncher.launch(
                            arrayOf(permission.permission)
                        )
                    },
                    onGoToAppSettingsClick = activity::openAppSettings,
                )
            }
        }
}

private fun hasUnusedAppRestrictions(
    appRestrictionsStatus: Int,
): Boolean =
    when (appRestrictionsStatus) {
        // Couldn't fetch status. Check logs for details.
        UnusedAppRestrictionsConstants.ERROR,

            // Restrictions don't apply to your app on this device.
        UnusedAppRestrictionsConstants.FEATURE_NOT_AVAILABLE,

            // The user has disabled restrictions for your app.
        UnusedAppRestrictionsConstants.DISABLED ->
            false

        // If the user doesn't start your app for a few months, the system will
        // place restrictions on it. See the API_* constants for details.
        UnusedAppRestrictionsConstants.API_30_BACKPORT,
        UnusedAppRestrictionsConstants.API_30,
        UnusedAppRestrictionsConstants.API_31 ->
            true

        else ->
            false
    }

private fun addPermissionRequestForUnusedAppRestrictions(
    viewModel: MainViewModel,
    context: Context,
) {
    if (viewModel.requiredPermissions.find {
            it is PermissionErrorModelMini && it.permission == UnusedAppRestrictionsRequirement
        } == null
    ) {
        viewModel.requiredPermissions.add(
            PermissionErrorModelMini(
                permission = UnusedAppRestrictionsRequirement,
                missingPermissionMessage = "App has some restrictions" +
                        " by default, you should remove these" +
                        " so that app can work properly",
                launchIntent = IntentCompat.createManageUnusedAppRestrictionsIntent(
                    context,
                    context.packageName
                ),
                setPermission = { checkUnusedAppRestrictions(it, context) },
            )
        )
    }
}

private fun checkUnusedAppRestrictions(
    permissionErrorModel: PermissionErrorModelMini,
    context: Context,
) {
    val future =
        PackageManagerCompat.getUnusedAppRestrictionsStatus(context)

    future.addListener(
        {
            permissionErrorModel.hasPermission =
                !hasUnusedAppRestrictions(future.get())
        },
        context.mainExecutor,
    )
}