package dev.kingtux.batterymonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

data class LocalBatteryMonitor(
    val updateBatteryLevel: (Int) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val currentBattery: Int = intent?.getIntExtra("level", -1) ?: -1
        Log.d(TAG, "Local Device Battery: $currentBattery")
        updateBatteryLevel(currentBattery)
    }

    fun register(context: Context){
        context.registerReceiver(this, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
    companion object {
        const val TAG = "BatteryMonitor-LocalMonitor"
    }
}