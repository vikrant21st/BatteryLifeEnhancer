package com.example.myapplication.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.AppSettings
import com.example.myapplication.R
import com.example.myapplication.TransformFunction
import com.example.myapplication.navigation.TodoDestinationsArgs.TASK_ID_ARG
import com.example.myapplication.navigation.TodoDestinationsArgs.TITLE_ARG
import com.example.myapplication.navigation.TodoDestinationsArgs.USER_MESSAGE_ARG
import com.example.myapplication.navigation.addedittask.AddEditTaskScreen
import com.example.myapplication.navigation.addedittask.AddEditTaskViewModel
import com.example.myapplication.navigation.addedittask.LoadingContent
import com.example.myapplication.navigation.addedittask.Task
import com.example.myapplication.navigation.addedittask.TaskDetailTopAppBar
import com.example.myapplication.navigation.addedittask.TaskDetailViewModel
import com.example.myapplication.navigation.addedittask.TasksScreen
import com.example.myapplication.navigation.addedittask.collectAsStateWithLifecycle
import com.example.myapplication.navigation.addedittask.getViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TodoNavGraph(
    chargingAlarmService: ChargingAlarmService,
    appSettings: AppSettings,
    updateAppSettings: suspend (TransformFunction<AppSettings>) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    startDestination: String = TodoDestinations.BATTERY_ROUTE,
    navActions: TodoNavigationActions = remember(navController) {
        TodoNavigationActions(navController)
    },
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute =
        currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            TodoDestinations.TASKS_ROUTE,
            arguments = listOf(
                navArgument(USER_MESSAGE_ARG) {
                    type = NavType.IntType; defaultValue = 0
                }
            )
        ) { entry ->
            TasksRouteScreen(
                drawerState,
                currentRoute,
                navActions,
                entry,
                coroutineScope
            )
        }
        composable(
            TodoDestinations.ADD_EDIT_TASK_ROUTE,
            arguments = listOf(
                navArgument(TITLE_ARG) { type = NavType.IntType },
                navArgument(TASK_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) { entry ->
            AddEditTaskRouteScreen(entry, navActions, navController)
        }
        composable(TodoDestinations.TASK_DETAIL_ROUTE) { entry ->
            navController.popBackStack()
            TaskDetailRouteScreen(entry, navActions, navController)
        }
        composable(TodoDestinations.BATTERY_ROUTE) {
            navController.popBackStack(
                TodoDestinations.BATTERY_ROUTE,
                inclusive = false,
                saveState = false,
            )
            HomeRouteScreen(TodoDestinations.BATTERY_ROUTE, navActions) {
                ChargingAlarmScreen(
                    chargingAlarmService,
                    appSettings,
                    updateAppSettings
                )
            }
        }
        composable(TodoDestinations.APP_LISTS_ROUTE) {
            navController.popBackStack(
                TodoDestinations.APP_LISTS_ROUTE,
                inclusive = false,
                saveState = false,
            )
            HomeRouteScreen(TodoDestinations.APP_LISTS_ROUTE, navActions) {
                AppListsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDetailRouteScreen(
    entry: NavBackStackEntry,
    navActions: TodoNavigationActions,
    navController: NavHostController
) {
    val viewModel: TaskDetailViewModel = viewModel(
        factory = getViewModelFactory(entry.arguments)
    )
    TaskDetailScreen(
        viewModel = viewModel,
        onEditTask = { taskId ->
            navActions.navigateToAddEditTask(R.string.edit_task, taskId)
        },
        onBack = { navController.popBackStack() },
        onDeleteTask = { navActions.navigateToTasks(DELETE_RESULT_OK) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditTaskRouteScreen(
    entry: NavBackStackEntry,
    navActions: TodoNavigationActions,
    navController: NavHostController
) {
    val taskId = entry.arguments?.getString(TASK_ID_ARG)
    val viewModel: AddEditTaskViewModel = viewModel(
        factory = getViewModelFactory(entry.arguments)
    )
    AddEditTaskScreen(
        viewModel = viewModel,
        topBarTitle = entry.arguments?.getInt(TITLE_ARG)!!,
        onTaskUpdate = {
            navActions.navigateToTasks(
                if (taskId == null) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
            )
        },
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksRouteScreen(
    drawerState: DrawerState,
    currentRoute: String,
    navActions: TodoNavigationActions,
    entry: NavBackStackEntry,
    coroutineScope: CoroutineScope
) {
    AppModalDrawer(drawerState, currentRoute, navActions) {
        TasksScreen(
            userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
            onUserMessageDisplayed = {
                entry.arguments?.putInt(
                    USER_MESSAGE_ARG,
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

// Keys for navigation
const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onEditTask: (String) -> Unit,
    onBack: () -> Unit,
    onDeleteTask: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState()
) {
    Scaffold(
//        scaffoldState = scaffoldState,
        modifier = modifier.fillMaxSize(),
        topBar = {
            TaskDetailTopAppBar(
                onBack = onBack,
                onDelete = viewModel::deleteTask
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEditTask(viewModel.taskId) }) {
                Icon(Icons.Filled.Edit, "Edit task")
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        EditTaskContent(
            loading = uiState.isLoading,
            empty = uiState.task == null && !uiState.isLoading,
            task = uiState.task,
            onRefresh = viewModel::refresh,
            onTaskCheck = viewModel::setCompleted,
            modifier = Modifier.padding(paddingValues)
        )

        // Check for user messages to display on the screen
        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(
                scaffoldState,
                viewModel,
                userMessage,
                snackbarText
            ) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        // Check if the task is deleted and call onDeleteTask
        LaunchedEffect(uiState.isTaskDeleted) {
            if (uiState.isTaskDeleted) {
                onDeleteTask()
            }
        }
    }
}

@Composable
private fun EditTaskContent(
    loading: Boolean,
    empty: Boolean,
    task: Task?,
    onTaskCheck: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenPadding = Modifier.padding(
        horizontal = 16.dp,
        vertical = 16.dp,
    )
    val commonModifier = modifier
        .fillMaxWidth()
        .then(screenPadding)

    LoadingContent(
        loading = loading,
        empty = empty,
        emptyContent = {
            Text(
                text = "No data",
                modifier = commonModifier
            )
        },
        onRefresh = onRefresh
    ) {
        Column(commonModifier.verticalScroll(rememberScrollState())) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .then(screenPadding),

                ) {
                if (task != null) {
                    Checkbox(task.isCompleted, onTaskCheck)
                    Column {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}
