package dev.kingtux.batterymonitor.watch


import android.net.Uri
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.batterymonitor.watch.complication.MainComplicationService
import dev.kingtux.batterymonitor.watch.tile.MainTileService
import dev.kingtux.common.SmallDevice
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLevelGetter : WearableListenerService(), DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener{

    @Inject
    lateinit var currentBattery: Devices


    override fun onCreate() {
        super.onCreate()

        Wearable.getMessageClient(applicationContext).addListener(this)
        Wearable.getDataClient(applicationContext).addListener(this)
        Wearable.getCapabilityClient(applicationContext)
            .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        Log.d("BatteryLevelGetter-Service", "onCreate: ")
        getDevices()
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getMessageClient(applicationContext).removeListener(this)
        Wearable.getDataClient(applicationContext).removeListener(this)
        Wearable.getCapabilityClient(applicationContext).removeListener(this)
    }


    override fun onDataChanged(message: DataEventBuffer) {
        Log.d("BatteryLevelGetter-Service", "onMessageReceived: $message")
    }

    override fun onMessageReceived(message: MessageEvent) {
        Log.d("BatteryLevelGetter-Service", "onMessageReceived: $message")
        if (message.path == "devices") {
            val decodeToString = message.data.decodeToString()
            Log.d("BatteryLevelGetter-Service", "onMessageReceived: $decodeToString");
            val devices =
                Gson().fromJson(decodeToString, Array<SmallDevice>::class.java)
                    .toList()
            currentBattery.deviceOne = devices.getOrNull(0)
            currentBattery.deviceTwo  = devices.getOrNull(1)
            currentBattery.deviceThree = devices.getOrNull(2)
            Log.d("BatteryLevelGetter-Service", "onMessageReceived: $devices")
            MainTileService.forceTileUpdate(applicationContext)
            MainComplicationService.forceComplicationUpdate(applicationContext)
        }
    }

    override fun onCapabilityChanged(message: CapabilityInfo) {
        Log.d("BatteryLevelGetter-Service", "onMessageReceived: $message")
    }

    fun getDevices() {
        Wearable.getMessageClient(applicationContext)
            .sendMessage("battery", "get-devices", ByteArray(0))

    }

}