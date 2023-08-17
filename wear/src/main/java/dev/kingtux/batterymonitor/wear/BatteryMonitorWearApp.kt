package dev.kingtux.batterymonitor.wear

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.HiltAndroidApp
import dev.kingtux.batterymonitor.LocalBatteryMonitor
import dev.kingtux.batterymonitor.wear.complication.MainComplicationService
import dev.kingtux.batterymonitor.wear.tile.MainTileService
import javax.inject.Inject
class BatteryMonitorWearApp : Application() {

    override fun onCreate() {
        super.onCreate()
        refreshDevices(applicationContext)
    }

    companion object{
        fun refreshRenders(context: Context){
            MainTileService.forceTileUpdate(context)
            MainComplicationService.forceComplicationUpdate(context)
        }
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