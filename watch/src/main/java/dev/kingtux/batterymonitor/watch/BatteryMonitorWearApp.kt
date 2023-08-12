package dev.kingtux.batterymonitor.watch

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import dev.kingtux.batterymonitor.watch.tile.MainTileService
import javax.inject.Inject

@HiltAndroidApp
class BatteryMonitorWearApp : Application(){
    @Inject
    lateinit var currentBattery: Devices

    private var watchBatteryReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val currentWatchBattery: Int =
                intent?.getIntExtra("level", -1) ?: -1
            val device =  currentBattery.watch;
            device.batteryLevel = currentWatchBattery;
            Log.d("BatteryLevelGetter-Watch", "Updated Watch: $device")
            currentBattery.watch = device;
            MainTileService.forceTileUpdate(applicationContext)

        }
    }
    override fun onCreate() {
        super.onCreate()
        registerReceiver(watchBatteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );
    }
}