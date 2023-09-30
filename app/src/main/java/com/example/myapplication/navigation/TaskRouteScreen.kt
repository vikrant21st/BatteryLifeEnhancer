package com.example.myapplication.navigation

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.navigation.addedittask.TasksScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Preview
@Composable
fun TaskRouteScreenPreview() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val navActions = remember(navController) {
        TodoNavigationActions(navController)
    }
    val coroutineScope = rememberCoroutineScope()
    TaskRouteScreen(
        drawerState,
        TodoDestinations.TASKS_ROUTE,
        navActions,
        coroutineScope,
        null,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRouteScreen(
    drawerState: DrawerState,
    currentRoute: String,
    navActions: TodoNavigationActions,
    coroutineScope: CoroutineScope,
    entry: NavBackStackEntry?,
) {
    AppModalDrawer(drawerState, currentRoute, navActions) {
        TasksScreen(
            userMessage =
            entry?.arguments?.getInt(TodoDestinationsArgs.USER_MESSAGE_ARG)
                ?: -1,
            onUserMessageDisplayed = {
                entry?.arguments?.putInt(
                    TodoDestinationsArgs.USER_MESSAGE_ARG,
                    0
                )
            },
            onAddTask = {
                navActions.navigateToAddEditTask(
                    R.string.add_task,
                    null
                )
            },
            onTaskClick = { task -> navActions.navigateToTaskDetail(task.id) },
            openDrawer = { coroutineScope.launch { drawerState.open() } }
        )
    }
}