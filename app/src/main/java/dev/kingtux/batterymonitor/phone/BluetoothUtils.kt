package dev.kingtux.batterymonitor.phone

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import dev.kingtux.common.Device
import dev.kingtux.common.DeviceConfiguration


val BluetoothDevice.batteryLevel
    get() = this.let { device ->
        val method = device.javaClass.getMethod("getBatteryLevel")
        method.invoke(device) as Int?
    } ?: -1
val BluetoothDevice.isConnected
    get() = this.let { device ->
        val method = device.javaClass.getMethod("isConnected")
        method.invoke(device) as Boolean
    } ?: false

@SuppressLint("MissingPermission")
fun DeviceConfiguration.toDevice(bluetoothDevice: BluetoothDevice): Device {
    return Device(
        priority = priority,
        name = bluetoothDevice.name,
        batteryLevel = bluetoothDevice.batteryLevel,
        deviceType = deviceType,
        isConnected = bluetoothDevice.isConnected,
        address = bluetoothDevice.address,
        enabled = enabled
    )
}

fun Device.refreshDevice(bluetoothManager: BluetoothManager): Boolean {

    val remoteDevice = bluetoothManager.adapter.getRemoteDevice(address)
    val batteryLevel = remoteDevice.batteryLevel
    val isConnected = remoteDevice.isConnected
    return if (batteryLevel != this.batteryLevel || isConnected != this.isConnected) {
        this.batteryLevel = batteryLevel
        this.isConnected = isConnected
        true
    }else{
        false
    }
}

class BluetoothUtils {
    companion object {
        private const val tag = "BluetoothUtils"

        fun getDevices(applicationContext: Context): List<Device> {
            val devices = mutableListOf<Device>()

            val systemService: BluetoothManager? =
                applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?;
            if (systemService != null) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(tag, "Grabbing devices")
                    // Get a list of currently connected devices
                    for (bondedDevice: BluetoothDevice in systemService.adapter.bondedDevices) {
                        Log.d(tag, "getDevices: $bondedDevice")
                        val deviceConfiguration =
                            DeviceConfiguration.loadDevice(
                                applicationContext.filesDir.toPath(),
                                bondedDevice.address
                            )
                        val device = deviceConfiguration.toDevice(bondedDevice)
                        devices.add(device)
                    }
                } else {
                    Log.d(tag, "onCreate: No permission")
                }

            } else {
                Log.d(tag, "onCreate: systemService is null")
            }
            return devices;
        }
    }
}