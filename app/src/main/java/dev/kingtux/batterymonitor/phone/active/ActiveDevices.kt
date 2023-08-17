package dev.kingtux.batterymonitor.phone.active

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import dev.kingtux.batterymonitor.DeviceConfiguration
import dev.kingtux.batterymonitor.DeviceRoute
import dev.kingtux.batterymonitor.DeviceType
import dev.kingtux.batterymonitor.LocalBatteryMonitor
import dev.kingtux.batterymonitor.SharedDevice
import dev.kingtux.batterymonitor.phone.BatteryMonitorApp
import dev.kingtux.batterymonitor.phone.bluetooth.BluetoothUtils
import dev.kingtux.batterymonitor.phone.bluetooth.Device
import dev.kingtux.batterymonitor.phone.bluetooth.getBluetoothDevice
import dev.kingtux.batterymonitor.phone.bluetooth.getBluetoothManager
import dev.kingtux.batterymonitor.phone.bluetooth.hasBluetoothPermission


class ActiveDevices : LocalBatteryMonitor {
    var phone: SharedDevice = SharedDevice(DeviceType.Phone)
    var watch: SharedDevice? = null
    var connectedDevices: MutableList<Device> = mutableListOf()

    constructor(
        phone: SharedDevice, watch: SharedDevice?, connectedDevices: MutableList<Device>
    ) {
        this.phone = phone
        this.watch = watch
        this.connectedDevices = connectedDevices
    }

    constructor(
        context: Context, initLocalMonitor: Boolean = true
    ) {
        Log.d(TAG, "Starting ActiveDevices")
        if (initLocalMonitor) {
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            context.registerReceiver(this, intentFilter)
        }
        if (!context.hasBluetoothPermission()) {
            Log.w(TAG, BatteryMonitorApp.NO_BLUETOOTH_PERMISSION)
            Log.w(TAG, "Starting with no devices")
            return
        }
        resetDevices(context)
        Log.d(TAG, "ActiveDevices: $connectedDevices")
    }

    fun getSharedDevices(): List<SharedDevice> {
        val connectedDevices = this.connectedDevices.toList().filter {
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
        Log.d(TAG, "resetDevices: $connectedDevices, $phone")
        putPhone(context)
        putExtraDevices(Wearable.getDataClient(context))
    }

    fun putExtraDevices(dataClient: DataClient) {
        val devicesToSend = connectedDevices.toList().filter {
            it.batteryLevel != null && it.batteryLevel != -1
        }.toMutableList();
        devicesToSend.sortBy { it.priority }

        Log.d(TAG, "putExtraDevices: $devicesToSend")

        devicesToSend.getOrNull(0)?.toSharedDevice()?.putDevice(dataClient, DeviceRoute.DeviceOne)
            ?: SharedDevice.noDevice(dataClient, DeviceRoute.DeviceOne)

        devicesToSend.getOrNull(1)?.toSharedDevice()?.putDevice(dataClient, DeviceRoute.DeviceTwo)
            ?: SharedDevice.noDevice(dataClient, DeviceRoute.DeviceTwo)
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
                putExtraDevices(Wearable.getDataClient(applicationContext))
            }
            Log.d(TAG, "refreshDevices: $connectedDevices")
        }

    }

    fun putPhone(applicationContext: Context) {
        Log.d(TAG, "putPhone: $phone")
        phone.putDevice(Wearable.getDataClient(applicationContext), DeviceRoute.Phone)
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: $intent")
        super.onReceive(context, intent)
        if (intent?.action != BluetoothDevice.ACTION_ACL_CONNECTED && intent?.action != BluetoothDevice.ACTION_ACL_DISCONNECTED) {
            Log.d(TAG, "Not a Bluetooth Action")
            return
        }
        if (context == null) {
            Log.w(TAG, "Context is null")
            return
        }
        if (!context.hasBluetoothPermission()) {
            Log.w(TAG, BatteryMonitorApp.NO_BLUETOOTH_PERMISSION)
            return
        }
        Log.d(TAG, "Resetting Devices")
        /// Wait 5 seconds and then reset devices
        Thread.sleep(5000)
        resetDevices(context)
    }

    override fun updateBatteryLevel(context: Context, currentBattery: Int) {
        if (phone.batteryLevel != currentBattery) {
            phone.batteryLevel = currentBattery
            putPhone(context)
        }
    }

    companion object {
        const val TAG = "ActiveDevices";
    }

}