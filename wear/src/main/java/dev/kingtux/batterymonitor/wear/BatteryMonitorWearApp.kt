package dev.kingtux.batterymonitor.wear

import android.app.Application
import android.content.Context
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import dev.kingtux.batterymonitor.LocalBatteryMonitor
import dev.kingtux.batterymonitor.wear.tile.MainTileService
import javax.inject.Inject

@HiltAndroidApp
class BatteryMonitorWearApp : Application() {
    @Inject
    lateinit var currentBattery: Devices

    private var watchBatteryMonitor = LocalBatteryMonitor(
        updateBatteryLevel = { batteryLevel ->
            currentBattery.watch.batteryLevel = batteryLevel
            Log.d("BatteryLevelGetter-Local", "Updated Watch: ${currentBattery.watch}")
        }
    )

    override fun onCreate() {
        super.onCreate()
        watchBatteryMonitor.register(this)
        BatteryLevelGetter.refreshDevices(applicationContext)
    }

    companion object{
        fun refreshRenders(context: Context){
            MainTileService.forceTileUpdate(context)
        }
    }
}