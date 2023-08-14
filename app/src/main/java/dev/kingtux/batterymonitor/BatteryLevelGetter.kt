package dev.kingtux.batterymonitor

import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLevelGetter : WearableListenerService() {
    private val tag = "BatteryLevelGetter-Service"

    @Inject
    lateinit var devices: Devices



    override fun onMessageReceived(message: MessageEvent) {
        Log.d(tag, "onMessageReceived: ${message.path}")
         if (message.path == "/batteryMonitor/refresh-devices") {
            val bluetoothManager: BluetoothManager? =
                applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
            if (bluetoothManager != null) {
                var hasChanged = false;
                for (device in devices.devices) {
                    if (device.refreshDevice(bluetoothManager)) {
                        if (!device.isConnected) {
                            devices.devices.remove(device)
                            continue
                        }
                        hasChanged = true;
                    }
                }
                if (hasChanged) {
                    devices.updateDevices(Wearable.getDataClient(applicationContext))
                }
            }
        } else if (message.path == "/batteryMonitor/reload-devices") {
            devices.devices = BluetoothUtils.getDevices(applicationContext).toMutableList();

             devices.updateDevices(Wearable.getDataClient(applicationContext))
        }
    }


}