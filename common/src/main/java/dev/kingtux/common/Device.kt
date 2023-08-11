package dev.kingtux.common

import com.google.gson.Gson
import java.io.File
data class SavedDevice(
    val address: String,
    var deviceType: DeviceType,
    var enabled: Boolean,
    var priority: Int
)


data class Device(
    val priority: Int,
    val name: String,
    val batteryLevel: Int,
    var deviceType: DeviceType,
    val isConnected: Boolean,
    val address: String,
    var enabled: Boolean
)



data class Settings(
    var sendPhone: Boolean,
    val devices: List<SavedDevice>
){
    init {
        // Only The Phone can be priority 0
        devices.forEach {
            if(it.priority == 0 && it.deviceType != DeviceType.Phone){
                it.priority = 1
            }
        }
    }
}

 fun getSettings(file: File): Settings{
    val settings: Settings = if (file.exists()) {
        val json = file.readText()
        val gson = Gson()
        gson.fromJson(json, Settings::class.java)
    } else {
        Settings(true, emptyList())
    }
    return settings
}
fun updateSettings(device: Device, file: File) {
    val settings: Settings = if (file.exists()) {
        val json = file.readText()
        val gson = Gson()
        gson.fromJson(json, Settings::class.java).let { settings ->
            val savedDevices = settings.devices.toMutableList()
            savedDevices.removeIf { it.address == device.address }
            savedDevices.add(
                SavedDevice(
                    device.address,
                    device.deviceType,
                    device.enabled,
                    device.priority
                )
            );
            Settings(settings.sendPhone, savedDevices)
        }
    } else {
        Settings(true, listOf(
            SavedDevice(
                device.address,
                device.deviceType,
                device.enabled,
                device.priority
            )
        ))
    }
    val gson = Gson()
    val json = gson.toJson(settings)
    file.writeText(json)
}

data class SmallDevice(
    val priority: Int,
    val name: String,
    var batteryLevel: Int,
    val deviceType: DeviceType,
)
