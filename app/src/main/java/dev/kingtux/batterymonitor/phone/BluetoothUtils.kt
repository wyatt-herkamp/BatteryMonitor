package dev.kingtux.batterymonitor.phone

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import dev.kingtux.common.Device
import dev.kingtux.common.DeviceType
import dev.kingtux.common.Settings

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

class BluetoothUtils {

    companion object {
        private const val tag = "BluetoothUtils"

        fun getDevices(applicationContext: Context, settings: Settings): List<Device> {
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
                        val connectionState = bondedDevice.isConnected;
                        val deviceName = bondedDevice.name;
                        val blueToothBatteryLevel = bondedDevice.batteryLevel;
                        settings.devices.find { it.address == bondedDevice.address }.let {
                            if (it != null) {
                                devices.add(
                                    Device(
                                        priority = it.priority,
                                        deviceName,
                                        blueToothBatteryLevel,
                                        it.deviceType,
                                        connectionState,
                                        bondedDevice.address,
                                        it.enabled

                                    )
                                )
                            } else {
                                devices.add(
                                    Device(
                                        priority = 1,
                                        deviceName,
                                        blueToothBatteryLevel,
                                        DeviceType.Other,
                                        connectionState,
                                        bondedDevice.address,
                                        true
                                    )
                                )
                            }
                        }
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