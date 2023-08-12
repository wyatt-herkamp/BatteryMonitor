package dev.kingtux.batterymonitor.phone

import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.common.DeviceMessage
import dev.kingtux.common.SharedDevice
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLevelGetter : WearableListenerService(), DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    private val tag = "BatteryLevelGetter-Service"

    @Inject
    lateinit var devices: Devices

    override fun onCreate() {
        Wearable.getMessageClient(applicationContext).addListener(this)
        Wearable.getDataClient(applicationContext).addListener(this)
        Wearable.getCapabilityClient(applicationContext)
            .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getMessageClient(applicationContext).removeListener(this)
        Wearable.getDataClient(applicationContext).removeListener(this)
        Wearable.getCapabilityClient(applicationContext).removeListener(this)
    }


    override fun onMessageReceived(message: MessageEvent) {
        Log.d(tag, "onMessageReceived: ${message.path}")
        if (message.path == "get-devices") {
            val devices = devices.getSharedDevices()
            Log.d(tag, "Sending Devices: $devices")
            val json = Json.encodeToString(devices);
            Wearable.getMessageClient(applicationContext)
                .sendMessage(message.sourceNodeId, "devices", json.toByteArray())
        } else if (message.path == "refresh-devices") {
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
                    Wearable.getDataClient(applicationContext).putDataItem(devices.createDataMap())
                }
            }
        } else if (message.path == "reload-devices") {
            devices.devices = BluetoothUtils.getDevices(applicationContext).toMutableList();
            Wearable.getDataClient(applicationContext).putDataItem(devices.createDataMap())
        }
    }


}