package dev.kingtux.batterymonitor.phone

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.kingtux.batterymonitor.phone.active.ActiveDevices
import dev.kingtux.batterymonitor.phone.settings.Settings
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DevicesStore {

    @Singleton
    @Provides
    fun providesDevices(@ApplicationContext application: Context): ActiveDevices {
        return ActiveDevices(application)
    }

    @Singleton
    @Provides
    fun providesSettings(
        @ApplicationContext application: Context
    ): Settings {
        val settingsFile = application.dataDir.resolve("settings.json");
        return if (!settingsFile.exists()) {
            Settings()
        } else {
            val settingsString = settingsFile.readText();
            try {
                Json.decodeFromString(Settings.serializer(), settingsString)
            } catch (e: Exception) {
                Log.e("Settings", "Error loading settings", e)
                Files.move(
                    settingsFile.toPath(),
                    application.dataDir.resolve("settings.json.old").toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                Settings()
            }
        }
    }
}

