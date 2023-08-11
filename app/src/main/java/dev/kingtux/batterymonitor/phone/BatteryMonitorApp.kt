package dev.kingtux.batterymonitor.phone

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BatteryMonitorApp : Application(){

    @Inject
    lateinit var devices: Devices

    private var localBatteryMonitor = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val currentWatchBattery: Int =
                intent?.getIntExtra("level", -1) ?: -1
            val device =  devices.phone;
            device.batteryLevel = currentWatchBattery;
            Log.d("BatteryLevelGetter-Phone", "Updated: $device")
            devices.phone = device;

        }
    }
    override fun onCreate() {
        super.onCreate()
        registerReceiver(localBatteryMonitor,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );
    }
}