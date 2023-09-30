package com.example.myapplication

import android.app.Application
import androidx.work.Configuration
import com.example.myapplication.navigation.addedittask.TasksRepository
import com.example.myapplication.navigation.addedittask.TasksRepositoryImpl

/**
 * An application that lazily provides a repository. Note that this Service Locator pattern is
 * used to simplify the sample. Consider a Dependency Injection framework.
 *
 * Also, sets up Timber in the DEBUG BuildConfig. Read Timber's documentation for production setups.
 */
class TodoApplication : Application() {

    // Depends on the flavor,
    val taskRepository: TasksRepository
        get() = TasksRepositoryImpl()
}
