package dev.kingtux.batterymonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

abstract class LocalBatteryMonitor : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BATTERY_CHANGED) {
            return
        }
        if (context == null) {
            return
        }
        val currentBattery: Int = intent.getIntExtra("level", -1)
        Log.d(TAG, "Local Device Battery: $currentBattery")
        updateBatteryLevel(context, currentBattery)
    }

    abstract fun updateBatteryLevel(context: Context, currentBattery: Int)

    fun registerLocalMonitor(context: Context) {
        context.registerReceiver(this, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
    fun unregisterLocalMonitor(context: Context) {
        context.unregisterReceiver(this)
    }
    companion object {
        const val TAG = "BatteryMonitor-LocalMonitor"
    }
}