package dev.kingtux.batterymonitor.wear


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BatteryLevelGetter : WearableListenerService() {

    @Inject
    lateinit var currentBattery: Devices

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged: $dataEvents")

        val dataEventsValues = dataEvents.map { it.dataItem };
        for (it in dataEventsValues) {
            Log.d(TAG, "onDataChanged: ${it.uri}")
            if (it.uri.path?.startsWith("/batteryMonitor/device/") == true) {
                currentBattery.updateFromDataItem(it)
            }
        }
        Log.d(TAG, "onDataChanged: $currentBattery")
    }

    companion object {
        const val TAG = "BatteryLevelGetter-Service"

        fun refreshDevices(context: Context) {
            Log.d("BatteryLevelGetter-Service", "Refreshing Devices")
            Wearable.getMessageClient(context)
                .sendMessage("wear", "/batteryMonitor/refresh-devices", ByteArray(0))

        }

        fun reloadDevices(context: Context) {
            Log.d("BatteryLevelGetter-Service", "Reloading Devices")
            Wearable.getMessageClient(context)
                .sendMessage("wear", "/batteryMonitor/reload-devices", ByteArray(0))

        }
    }
}