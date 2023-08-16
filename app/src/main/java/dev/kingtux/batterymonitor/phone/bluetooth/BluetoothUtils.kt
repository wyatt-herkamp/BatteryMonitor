package dev.kingtux.batterymonitor.phone.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import dev.kingtux.batterymonitor.phone.BatteryMonitorApp
import java.lang.Exception


fun BluetoothDevice.batteryLevel(): Int? {
    return try {
        val method = javaClass.getMethod("getBatteryLevel")
        method.invoke(this) as Int?
    } catch (e: Exception) {
        Log.w(
            "BluetoothDevice",
            "Unable to get the battery level. Blame Google and their infinite wisdom",
            e
        )
        null
    }
}

fun BluetoothDevice.isConnected(): Boolean {
    return try {
        val method = javaClass.getMethod("isConnected")
        method.invoke(this) as Boolean
    } catch (e: Exception) {
        Log.w(
            "BluetoothDevice",
            "Unable to get the isConnected. Blame Google and their infinite wisdom",
            e
        )
        false
    }
}

fun Intent.getBluetoothDevice(): BluetoothDevice? {
    return if (Build.VERSION.SDK_INT < 33) {
        getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return null
    } else {
        getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java) ?: return null
    }
}

fun Context.getBluetoothManager() =
    getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?;


fun Context.hasBluetoothPermission() =
    ActivityCompat.checkSelfPermission(
        this, Manifest.permission.BLUETOOTH_CONNECT
    ) == PackageManager.PERMISSION_GRANTED

class BluetoothUtils {
    companion object {
        private const val tag = "BluetoothUtils"
        fun getConnectedDevices(applicationContext: Context) =
            getPairedDevices(applicationContext).filter { it.isConnected }.toMutableList()
                .sortBy { it.priority }

        fun getDevicesWithBatteryLevel(applicationContext: Context) =
            getPairedDevices(applicationContext).filter { it.isConnected }
                .filter { it.batteryLevel != -1 }.toMutableList().sortBy { it.priority }

        @SuppressLint("MissingPermission") // Permission is checked by Context.hasBluetoothPermission()
        fun getPairedDevices(applicationContext: Context): MutableList<Device> {
            val devices = mutableListOf<Device>()

            val bluetoothManager: BluetoothManager? = applicationContext.getBluetoothManager()
            if (bluetoothManager != null) {
                if (applicationContext.hasBluetoothPermission()) {
                    Log.d(tag, "Looping through Bonded Devices")
                    // Get a list of currently connected devices
                    for (bondedDevice: BluetoothDevice in bluetoothManager.adapter.bondedDevices) {
                        Log.d(tag, "getDevices: $bondedDevice")
                        val device = Device(bondedDevice, applicationContext.filesDir.toPath())
                        devices.add(device)
                    }
                } else {
                    Log.w(
                        tag,
                        BatteryMonitorApp.NO_BLUETOOTH_PERMISSION
                    )
                }
            } else {
                Log.w(tag, BatteryMonitorApp.NO_BLUETOOTH_MANAGER)
            }
            return devices;
        }
    }
}