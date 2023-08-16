package dev.kingtux.batterymonitor.phone

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.batterymonitor.DeviceRoute
import dev.kingtux.batterymonitor.SharedDevice
import dev.kingtux.batterymonitor.phone.active.ActiveDevices
import dev.kingtux.batterymonitor.phone.bluetooth.BluetoothUtils
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLevelGetter : WearableListenerService() {

    @Inject
    lateinit var devices: ActiveDevices

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)
        Log.d(TAG, "onDataChanged: $dataEventBuffer")

        val dataEventsValues = dataEventBuffer.map { it.dataItem };
        for (it in dataEventsValues) {
            if (DeviceRoute.isDeviceRoute(it.uri)) {
                val deviceRoute = DeviceRoute.fromURI(it.uri)
                if (deviceRoute == DeviceRoute.Watch) {
                    val watch = SharedDevice.fromDataItem(it)
                    devices.watch = watch
                }
            }
        }
    }

    override fun onMessageReceived(message: MessageEvent) {
        Log.d(TAG, "onMessageReceived: ${message.path}")
        if (message.path == "/batteryMonitor/refresh-devices") {
            devices.refreshDevices(applicationContext)
        } else if (message.path == "/batteryMonitor/reload-devices") {
            devices.resetDevices(applicationContext)
        }
    }

    companion object {
        const val TAG = "BatteryLevelGetter-Service"
    }
}