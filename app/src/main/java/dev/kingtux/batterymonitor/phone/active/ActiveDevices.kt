package dev.kingtux.batterymonitor.phone.active

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Parcel
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dev.kingtux.batterymonitor.phone.BatteryMonitorApp
import dev.kingtux.batterymonitor.DeviceRoute
import dev.kingtux.batterymonitor.DeviceType
import dev.kingtux.batterymonitor.SharedDevice
import dev.kingtux.batterymonitor.phone.bluetooth.BluetoothUtils
import dev.kingtux.batterymonitor.phone.bluetooth.Device
import dev.kingtux.batterymonitor.phone.bluetooth.hasBluetoothPermission


class ActiveDevices {
    var phone: SharedDevice = SharedDevice(DeviceType.Phone)
    var watch: SharedDevice? = null
    var connectedDevices: MutableList<Device> = mutableListOf()

    constructor(
        phone: SharedDevice,
        watch: SharedDevice?,
        connectedDevices: MutableList<Device>
    ) {
        this.phone = phone
        this.watch = watch
        this.connectedDevices = connectedDevices
    }

    constructor(
        context: Context
    ) {
        if (!context.hasBluetoothPermission()) {
            Log.w(TAG, BatteryMonitorApp.NO_BLUETOOTH_PERMISSION)
            Log.w(TAG, "Starting with no devices")
            return
        }
        resetDevices(context)
        Log.d(TAG, "ActiveDevices: $connectedDevices")
    }

    fun getSharedDevices(): List<SharedDevice> {
        val connectedDevices =
            this.connectedDevices.toList().filter {
                it.batteryLevel != -1
            }.map {
                it.toSharedDevice()
            }.toMutableList();
        connectedDevices.add(this.phone)
        this.watch?.let {
            connectedDevices.add(it)
        }
        connectedDevices.sortBy {
            it.priority
        };
        return connectedDevices
    }

    fun resetDevices(context: Context) {
        this.connectedDevices = BluetoothUtils.getPairedDevices(context).filter {
            it.batteryLevel != -1
        }.filter {
            it.isConnected
        }.toMutableList();
        connectedDevices.sortBy {
            it.priority
        }
        updateDevices(Wearable.getDataClient(context))
    }

    fun updateDevices(dataClient: DataClient) {
        putDevice(phone, dataClient, DeviceRoute.Phone)

        val devicesToSend = connectedDevices.toMutableList().filter {
            it.batteryLevel != null && it.batteryLevel != -1
        }.toMutableList();
        devicesToSend.sortBy { it.priority }
        devicesToSend.getOrNull(0)?.let {
            putDevice(it.toSharedDevice(), dataClient, DeviceRoute.DeviceOne)
        } ?: noDevice(dataClient, DeviceRoute.DeviceOne)

        devicesToSend.getOrNull(1)?.let {
            putDevice(it.toSharedDevice(), dataClient, DeviceRoute.DeviceTwo)
        } ?: noDevice(dataClient, DeviceRoute.DeviceTwo)
    }

    @SuppressLint("VisibleForTests")
    fun putDevice(device: SharedDevice, dataClient: DataClient, deviceRoute: DeviceRoute) {

        Log.d(TAG, "putDevice: $device with path: $deviceRoute")
        val putDataMapReq = PutDataMapRequest.create(deviceRoute.toString())
        val parcel = Parcel.obtain()
        device.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        putDataMapReq.dataMap.putByteArray("device", bytes)
        parcel.recycle()
        Log.d(TAG, "putDevice: ${bytes.size}")
        dataClient.putDataItem(putDataMapReq.asPutDataRequest().apply {
            Log.d(TAG, "putDevice: $this")
        })
    }

    @SuppressLint("VisibleForTests")
    private fun noDevice(dataClient: DataClient, deviceRoute: DeviceRoute) {

        Log.d(TAG, "putDevice: null with path: $deviceRoute")
        val putDataMapReq = PutDataMapRequest.create(deviceRoute.toString())
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }

    fun refreshDevices(applicationContext: Context) {
        val bluetoothManager: BluetoothManager? =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        if (bluetoothManager != null) {
            var hasChanged = false;
            for (device in connectedDevices) {
                if (device.refreshDevice(bluetoothManager)) {
                    hasChanged = true;
                }
            }
            connectedDevices.removeIf() {
                !it.isConnected
            }
            connectedDevices.sortBy {
                it.priority
            }
            if (hasChanged) {
                updateDevices(Wearable.getDataClient(applicationContext))
            }
            Log.d(TAG, "refreshDevices: $connectedDevices")
        }
    }

    fun putPhone(applicationContext: Context) {
        putDevice(phone, Wearable.getDataClient(applicationContext), DeviceRoute.Phone)
    }

    companion object {
        const val TAG = "ActiveDevices";
    }

}