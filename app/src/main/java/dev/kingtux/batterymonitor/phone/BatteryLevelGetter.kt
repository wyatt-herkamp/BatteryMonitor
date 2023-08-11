package dev.kingtux.batterymonitor.phone

import android.net.Uri
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.common.SmallDevice
import dev.kingtux.common.getSettings
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLevelGetter : WearableListenerService(), DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    private val tag = "BatteryLevelGetter-Service"

    @Inject
    lateinit var devices: Devices

    private fun getDevices(): List<SmallDevice> {
        val file = File(applicationContext.filesDir, "devices.json")
        val settings = getSettings(file)
        val connectedDevices =
            BluetoothUtils.getDevices(applicationContext, settings).asSequence().filter {
                it.enabled
            }.filter {
                it.isConnected
            }.filter {
                it.batteryLevel != -1
            }.map {
                SmallDevice(
                    it.priority,
                    it.name,
                    it.batteryLevel,
                    it.deviceType
                )
            }.toMutableList();
        if (settings.sendPhone) {
            connectedDevices.add(
                devices.phone
            )
        }
        connectedDevices.sortBy {
            it.priority
        };

        return connectedDevices.map {
            SmallDevice(it.priority, it.name, it.batteryLevel, it.deviceType)
        }
    }

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
            val devices = getDevices();
            Log.d(tag, "Sending Devices: $devices")
            val gson = Gson()
            val json = gson.toJson(devices)
            Wearable.getMessageClient(applicationContext)
                .sendMessage(message.sourceNodeId, "devices", json.toByteArray())
        }
    }
}