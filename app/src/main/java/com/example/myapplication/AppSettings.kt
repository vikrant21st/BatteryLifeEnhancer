package com.example.myapplication

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@kotlinx.serialization.Serializable
data class AppSettings(
    val overchargingLimit: Int = 80,
    val underchargingLimit: Int = 30,
    val snooze: Boolean = false,
    val appLists: List<App> = emptyList(),
    val isAppInitialized: Boolean = false,
    val reviveAppInDays: Int = 3,
)

@kotlinx.serialization.Serializable
data class App(
    val packageName: String,
)

@Suppress("BlockingMethodInNonBlockingContext")
object AppSettingsSerializer : Serializer<AppSettings> {

    override val defaultValue: AppSettings
        get() = AppSettings()

    override suspend fun readFrom(input: InputStream): AppSettings {
        return try {
            Json.decodeFromString(
                deserializer = AppSettings.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            AppSettings()
        }
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = AppSettings.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}