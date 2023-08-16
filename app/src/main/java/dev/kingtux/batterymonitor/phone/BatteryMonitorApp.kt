package dev.kingtux.batterymonitor.phone

import android.app.Application
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.HiltAndroidApp
import dev.kingtux.batterymonitor.LocalBatteryMonitor
import dev.kingtux.batterymonitor.phone.bluetooth.DeviceReceiver
import dev.kingtux.batterymonitor.phone.active.ActiveDevices
import javax.inject.Inject


@HiltAndroidApp
class BatteryMonitorApp : Application() {

    @Inject
    lateinit var activeDevices: ActiveDevices

    private var deviceReceiver = DeviceReceiver()

    private var localBatteryMonitor = LocalBatteryMonitor(
        updateBatteryLevel = { batteryLevel ->
            if (activeDevices.phone.batteryLevel != batteryLevel){
                activeDevices.phone.batteryLevel = batteryLevel
                activeDevices.putPhone(applicationContext);
                Log.d("BatteryLevelGetter-Local", "Updated Phone: ${activeDevices.phone}")
            }
        }
    )

    override fun onCreate() {
        super.onCreate()

        localBatteryMonitor.register(this)

        ContextCompat.registerReceiver(
            this,
            deviceReceiver,
            DeviceReceiver.intentFilter(),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    companion object {
        const val NO_BLUETOOTH_PERMISSION =
            "Missing BLUETOOTH_CONNECT permission. This app is useless without it.";
        const val NO_BLUETOOTH_MANAGER =
            "No BluetoothManager found. This app is useless without it.";
    }
}