package com.example.myapplication.navigation.addedittask

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

interface TasksRepository {

    fun getTasksStream(): Flow<Result<List<Task>>>

    suspend fun getTasks(forceUpdate: Boolean = false): Result<List<Task>>

    suspend fun refreshTasks()

    fun getTaskStream(taskId: String): Flow<Result<Task>>

    suspend fun getTask(taskId: String, forceUpdate: Boolean = false): Result<Task>

    suspend fun refreshTask(taskId: String)

    suspend fun saveTask(task: Task)

    suspend fun completeTask(task: Task)

    suspend fun completeTask(taskId: String)

    suspend fun activateTask(task: Task)

    suspend fun activateTask(taskId: String)

    suspend fun clearCompletedTasks()

    suspend fun deleteAllTasks()

    suspend fun deleteTask(taskId: String)
}

//@Entity(tableName = "tasks")
data class Task @JvmOverloads constructor(
//    @ColumnInfo(name = "title")
    var title: String = "",
//    @ColumnInfo(name = "description")
    var description: String = "",
//    @ColumnInfo(name = "completed")
    var isCompleted: Boolean = false,
//    @PrimaryKey @ColumnInfo(name = "entryid")
    var id: String = UUID.randomUUID().toString()
) {

    val titleForList: String
        get() = if (title.isNotEmpty()) title else description

    val isActive
        get() = !isCompleted

    val isEmpty
        get() = title.isEmpty() || description.isEmpty()
}

sealed class Result<out R> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}

class TasksRepositoryImpl: TasksRepository {
    override fun getTasksStream(): Flow<Result<List<Task>>> = flow { }

    override suspend fun getTasks(forceUpdate: Boolean): Result<List<Task>> =
        Result.Success(emptyList())

    override suspend fun refreshTasks() {
    }

    override fun getTaskStream(taskId: String): Flow<Result<Task>> = flow { }

    override suspend fun getTask(
        taskId: String,
        forceUpdate: Boolean
    ): Result<Task> = Result.Success(Task(taskId))

    override suspend fun refreshTask(taskId: String) {
    }

    override suspend fun saveTask(task: Task) {
    }

    override suspend fun completeTask(task: Task) {
    }

    override suspend fun completeTask(taskId: String) {
    }

    override suspend fun activateTask(task: Task) {
    }

    override suspend fun activateTask(taskId: String) {
    }

    override suspend fun clearCompletedTasks() {
    }

    override suspend fun deleteAllTasks() {
    }

    override suspend fun deleteTask(taskId: String) {
    }
}
