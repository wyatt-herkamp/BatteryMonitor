package dev.kingtux.batterymonitor.phone.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.batterymonitor.phone.BatteryMonitorApp
import dev.kingtux.batterymonitor.phone.BatteryMonitorApp.Companion.NO_BLUETOOTH_MANAGER
import dev.kingtux.batterymonitor.DeviceConfiguration
import dev.kingtux.batterymonitor.phone.active.ActiveDevices
import javax.inject.Inject

@AndroidEntryPoint
class DeviceReceiver : BroadcastReceiver() {
    @Inject
    lateinit var activeDevices: ActiveDevices
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            return
        }
        if (context.hasBluetoothPermission()) {
            Log.w(tag, BatteryMonitorApp.NO_BLUETOOTH_PERMISSION)
            return
        }
        val action = intent.action
        val device: BluetoothDevice = intent.getBluetoothDevice() ?: return
        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                Log.d(tag, "onReceive: $device")
                val foundDevice = activeDevices.connectedDevices.find {
                    it.address == device.address
                }
                if (foundDevice != null) {
                    Log.d(tag, "Found Device: $foundDevice")
                    val bluetoothManager =
                        context.getBluetoothManager()
                    if (bluetoothManager == null) {
                        Log.w(tag, NO_BLUETOOTH_MANAGER)
                        return
                    }
                    foundDevice.refreshDevice(
                        bluetoothManager
                    )
                } else {
                    val deviceConfiguration = DeviceConfiguration.loadDevice(
                        context.filesDir.toPath(), device.address
                    )
                    if (deviceConfiguration.enabled) {
                        val newDevice = Device(deviceConfiguration, device)
                        activeDevices.connectedDevices.add(newDevice)
                        Log.d(tag, "New Device Connected: $newDevice")
                    }
                }
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                Log.d(tag, "onReceive: $device")
                val appDevice = activeDevices.connectedDevices.find {
                    it.address == device.address
                } ?: return
                if (activeDevices.connectedDevices.remove(appDevice)) {
                    Log.d(tag, "Removed: $appDevice")
                    appDevice.toDeviceConfiguration().saveDevice(context.filesDir.toPath())
                }
            }

            else -> {}
        }
        activeDevices.refreshDevices(context)
        activeDevices.putExtraDevices(
            Wearable.getDataClient(context)
        )
    }

    companion object {
        private const val tag = "DeviceReceiver"

        fun intentFilter(): IntentFilter {
            val filter = IntentFilter()
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            return filter
        }
    }
}