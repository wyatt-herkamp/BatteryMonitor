package dev.kingtux.batterymonitor

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.Log
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.exists

@Serializable
data class DeviceConfiguration(
    val address: String,
    var deviceType: DeviceType,
    var enabled: Boolean,
    var priority: Int
) {
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
                Json.decodeFromString(file.readText())
            }

        }
    }
}

enum class DeviceRoute {
    Phone("phone"),
    Watch("watch"),
    DeviceOne(1),
    DeviceTwo(2);

    private val route: String

    constructor(route: String) {
        this.route = route
    }

    constructor(index: Int) {
        this.route = "extra/$index"
    }

    override fun toString(): String {
        return "/batteryMonitor/device/$route"
    }

    companion object {
        fun isDeviceRoute(uri: Uri?): Boolean {
            if (uri == null) {
                return false
            }
            return uri.path?.contains("/batteryMonitor/device/") == true
        }

        fun fromURI(uri: Uri?): DeviceRoute? {
            val path = uri?.path ?: return null
            return when (path.split("/batteryMonitor/device/").last()) {
                "phone" -> Phone
                "watch" -> Watch
                "extra/1" -> DeviceOne
                "extra/2" -> DeviceTwo
                else -> null
            }
        }
    }
}

@Parcelize
data class SharedDevice(
    val priority: Int,
    val name: String,
    var batteryLevel: Int?,
    val deviceType: DeviceType,
) : Parcelable {
    /**
     * @param deviceType The type of device
     *
     * Creates a new SharedDevice for the current device
     */
    constructor(deviceType: DeviceType, name: String = Build.PRODUCT) : this(0, name, 100, deviceType)

    fun batteryLevelOrZero() = batteryLevel ?: 0

    fun batteryLevelOutOf100() = batteryLevelOrZero() / 100f

    companion object {
        @SuppressLint("VisibleForTests")
        fun fromDataItem(
            dataItem: DataItem,
            creator: Creator<SharedDevice> = parcelableCreator()
        ): SharedDevice? {
            val fromDataItem = DataMapItem.fromDataItem(
                dataItem
            )
            val deviceBytes = fromDataItem.dataMap.getByteArray("device")
            if (deviceBytes == null || deviceBytes.isEmpty()) {
                Log.d("SharedDevice", "onDataChanged: Empty Device Asset")
                return null;
            }

            val parcel = Parcel.obtain().apply {
                unmarshall(deviceBytes, 0, deviceBytes.size)
                setDataPosition(0)
            }

            val device = creator.createFromParcel(parcel)

            parcel.recycle()

            return device
        }
    }
}
