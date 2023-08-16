package dev.kingtux.batterymonitor.wear

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.Log
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.Wearable
import dev.kingtux.batterymonitor.DeviceRoute
import dev.kingtux.batterymonitor.DeviceType
import dev.kingtux.batterymonitor.SharedDevice
import dev.kingtux.batterymonitor.wear.tile.MainTileService
import kotlinx.parcelize.parcelableCreator

data class Devices(
    var watch: SharedDevice,
    var phone: SharedDevice?,
    var deviceOne: SharedDevice?,
    var deviceTwo: SharedDevice?
) {
    constructor(context: Context) : this(
        SharedDevice(DeviceType.Watch),
        null,
        null,
        null
    ) {
        updateAllFromDataStore(context)
    }


    private fun putDeviceByIndex(route: DeviceRoute, device: SharedDevice?) {
        Log.d("Devices", "putDeviceByIndex: $route Value: $device")

        when (route) {
            DeviceRoute.Phone -> {
                this.phone = device
            }

            DeviceRoute.DeviceOne -> {
                this.deviceOne = device
            }

            DeviceRoute.DeviceTwo -> {
                this.deviceTwo = device
            }

            else -> {
                Log.e("Devices", "Invalid Index: $route")
            }
        }

    }

    @SuppressLint("VisibleForTests")
    fun updateFromDataItem(
        dataItem: DataItem,
        creator: Parcelable.Creator<SharedDevice> = parcelableCreator()
    ) {
        Log.d(
            TAG,
            "pathSegments: ${dataItem.uri.pathSegments}"
        )
        val route = DeviceRoute.fromURI(dataItem.uri)
        if (route == null) {
            Log.e(TAG, "Invalid URI: ${dataItem.uri}")
            return
        }
        // The watch is already updated from a different method
        if (route == DeviceRoute.Watch) {
            return
        }
        val device = SharedDevice.fromDataItem(dataItem, creator)

        putDeviceByIndex(route, device)
        Log.d(TAG, "Debugging Devices: $device")
    }

    @SuppressLint("VisibleForTests")
    fun updateAllFromDataStore(context: Context) {
        val creator = parcelableCreator<SharedDevice>()
        val dataClient = Wearable.getDataClient(context)
        dataClient.dataItems.addOnSuccessListener {
            for (dataItem in it.iterator()) {
                val frozenDataItem = dataItem.freeze();
                Log.d(TAG, "Debugging Devices: $it")
                if (DeviceRoute.isDeviceRoute(frozenDataItem.uri)) {
                    updateFromDataItem(frozenDataItem, creator)
                }
            }
            it.release()
            Log.d(TAG, "New Devices: $this")
        }
        MainTileService.forceTileUpdate(context)
    }

    fun getComplicationDevice(): SharedDevice? {
        return if (deviceOne != null) {
            deviceOne
        } else if (phone != null) {
            phone
        } else {
            null;
        }
    }

    fun numberOfDevices(): Int {
        var count = 1
        if (phone != null) {
            count++
        }
        if (deviceOne != null) {
            count++
        }
        if (deviceTwo != null) {
            count++
        }
        return count
    }

    companion object {
        const val TAG = "Devices"
    }
}