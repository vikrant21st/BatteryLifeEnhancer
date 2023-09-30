package com.example.myapplication.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.myapplication.navigation.TodoDestinationsArgs.APP_LIST_ID_ARG
import com.example.myapplication.navigation.TodoDestinationsArgs.TASK_ID_ARG
import com.example.myapplication.navigation.TodoDestinationsArgs.USER_MESSAGE_ARG
import com.example.myapplication.navigation.TodoDestinationsArgs.TITLE_ARG
import com.example.myapplication.navigation.TodoScreens.STATISTICS_SCREEN
import com.example.myapplication.navigation.TodoScreens.TASKS_SCREEN
import com.example.myapplication.navigation.TodoScreens.ADD_EDIT_TASK_SCREEN
import com.example.myapplication.navigation.TodoScreens.APP_LISTS_SCREEN
import com.example.myapplication.navigation.TodoScreens.BATTERY_SCREEN
import com.example.myapplication.navigation.TodoScreens.ADD_EDIT_APP_LIST_SCREEN
import com.example.myapplication.navigation.TodoScreens.APP_LIST_DETAIL_SCREEN
import com.example.myapplication.navigation.TodoScreens.TASK_DETAIL_SCREEN

/**
 * Screens used in [TodoDestinations]
 */
private object TodoScreens {
    const val TASKS_SCREEN = "tasks"
    const val STATISTICS_SCREEN = "statistics"
    const val TASK_DETAIL_SCREEN = "task"
    const val ADD_EDIT_TASK_SCREEN = "addEditTask"

    const val BATTERY_SCREEN = "battery"
    const val APP_LISTS_SCREEN = "appLists"
    const val APP_LIST_DETAIL_SCREEN = "appList"
    const val ADD_EDIT_APP_LIST_SCREEN = "addEditAppList"
}

/**
 * Arguments used in [TodoDestinations] routes
 */
object TodoDestinationsArgs {
    const val USER_MESSAGE_ARG = "userMessage"
    const val TASK_ID_ARG = "taskId"
    const val TITLE_ARG = "title"

    const val APP_LIST_ID_ARG = "appListId"
}

/**
 * Destinations used in the [TasksActivity]
 */
object TodoDestinations {
    const val TASKS_ROUTE =
        "$TASKS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val STATISTICS_ROUTE = STATISTICS_SCREEN
    const val TASK_DETAIL_ROUTE = "$TASK_DETAIL_SCREEN/{$TASK_ID_ARG}"
    const val ADD_EDIT_TASK_ROUTE =
        "$ADD_EDIT_TASK_SCREEN/{$TITLE_ARG}?$TASK_ID_ARG={$TASK_ID_ARG}"

    const val BATTERY_ROUTE = BATTERY_SCREEN
    const val APP_LISTS_ROUTE = APP_LISTS_SCREEN
    const val APP_LIST_DETAIL_ROUTE = "$APP_LIST_DETAIL_SCREEN/{$APP_LIST_ID_ARG}"
    const val ADD_EDIT_APP_LIST_ROUTE =
        "$ADD_EDIT_APP_LIST_SCREEN?$APP_LIST_ID_ARG={$APP_LIST_ID_ARG}"
}

/**
 * Models the navigation actions in the app.
 */
class TodoNavigationActions(private val navController: NavHostController) {

    fun navigateToTasks(userMessage: Int = 0) {
        val navigatesFromDrawer = userMessage == 0
        navController.navigate(
            TASKS_SCREEN.let {
                if (userMessage != 0)
                    "$it?$USER_MESSAGE_ARG=$userMessage"
                else
                    it
            }
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }

    fun navigateToStatistics() {
        navController.navigate(TodoDestinations.STATISTICS_ROUTE) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    fun navigateToTaskDetail(taskId: String) {
        navController.navigate("$TASK_DETAIL_SCREEN/$taskId")
    }

    fun navigateToAddEditTask(title: Int, taskId: String?) {
        navController.navigate(
            "$ADD_EDIT_TASK_SCREEN/$title".let {
                if (taskId != null) "$it?$TASK_ID_ARG=$taskId" else it
            }
        )
    }

    fun navigateToAppListDetail(appListId: String) {
        navController.navigate("$APP_LIST_DETAIL_SCREEN/$appListId")
    }

    fun navigateToChargingAlarm() {
        navController.navigate(TodoDestinations.BATTERY_ROUTE)
    }

    fun navigateToAppListDetail() {
        navController.navigate(TodoDestinations.APP_LISTS_ROUTE)
    }

    fun navigateToAddOrEditAppListDetail(appListId: String?) {
        navController.navigate(
            ADD_EDIT_APP_LIST_SCREEN.let {
                if (appListId != null) "$it?$APP_LIST_ID_ARG=$appListId" else it
            }
        )
    }
}