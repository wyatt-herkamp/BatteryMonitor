package dev.kingtux.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.FileReader
import java.nio.file.Path
import kotlin.io.path.exists
@Parcelize
data class DeviceMessage(
    val deviceOne: SharedDevice?,
    val deviceTwo: SharedDevice?,
    val phone: SharedDevice?
) :  Parcelable
@Serializable
data class DeviceConfiguration(
    val address: String,
    var deviceType: DeviceType,
    var enabled: Boolean,
    var priority: Int
){
    override fun equals(other: Any?): Boolean {
        if (other !is DeviceConfiguration) {
            return false
        }
        return other.address == address
    }
    fun saveDevice(directory: Path) {
        val devicesPath = directory.resolve("devices");
        if (!devicesPath.exists()) {
            devicesPath.toFile().mkdirs()
        }
        val file = devicesPath.resolve("$address.json").toFile()
        val encodeToString = Json.encodeToString(this)
        file.writeText(encodeToString)
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + deviceType.hashCode()
        result = 31 * result + enabled.hashCode()
        result = 31 * result + priority
        return result
    }

    companion object {
        fun loadDevice(
            directory: Path,
            address: String,
        ): DeviceConfiguration {
            val path = directory.resolve("devices").resolve("$address.json")
            return if (!path.exists()) {
                DeviceConfiguration(
                    address,
                    DeviceType.Other,
                    true,
                    2
                )
            } else {
                val file = path.toFile()
                Json.decodeFromString<DeviceConfiguration>(file.readText())
            }

        }
    }
}

data class Device(
    val priority: Int,
    val name: String,
    var batteryLevel: Int,
    var deviceType: DeviceType,
    var isConnected: Boolean,
    val address: String,
    var enabled: Boolean
){
    override fun equals(other: Any?): Boolean {
        if (other !is Device) {
            return false
        }
        return other.address == address
    }

    fun toSharedDevice(): SharedDevice{
        return SharedDevice(priority, name, batteryLevel, deviceType)
    }
    fun toDeviceConfiguration(): DeviceConfiguration{
        return DeviceConfiguration(address, deviceType, enabled, priority)
    }
}
@Parcelize
data class SharedDevice(
    val priority: Int,
    val name: String,
    var batteryLevel: Int,
    val deviceType: DeviceType,
) : Parcelable