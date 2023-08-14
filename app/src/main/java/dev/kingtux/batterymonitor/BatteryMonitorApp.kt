package dev.kingtux.batterymonitor

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.wearable.Wearable
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
    fun updateDevices(){
        val bluetoothManager: BluetoothManager? =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        if (bluetoothManager != null) {
            for (device in devices.devices) {
                if (device.refreshDevice(bluetoothManager)) {
                    if (!device.isConnected) {
                        devices.devices.remove(device)
                        continue
                    }
                }
            }
        }
        devices.updateDevices(Wearable.getDataClient(applicationContext))
    }
    private var bluetoothBroadcastReceiver = object: BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent == null) {
                return
            }
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("BatteryLevelGetter-Phone", "onReceive: No Permission")
                return
            }
            val action = intent.action
            val device: BluetoothDevice = if (Build.VERSION.SDK_INT < 33){
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return
            }else{
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java) ?: return
            }
            when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    Log.d("BatteryLevelGetter-Phone", "onReceive: $device")
                    devices.devices.find {
                        it.address == device.address
                    }?.let {
                        return
                    }
                    val deviceConfiguration = DeviceConfiguration.loadDevice(
                        applicationContext.filesDir.toPath(),
                        device.address
                    )
                    if (deviceConfiguration.enabled){
                        val newDevice =
                            Device(
                            deviceConfiguration.priority,
                            device.name,
                            device.batteryLevel,
                            deviceConfiguration.deviceType,
                            device.isConnected,
                            device.address,
                            deviceConfiguration.enabled
                        )
                        devices.devices.add(newDevice)
                        Log.d("BatteryLevelGetter-Phone", "newDevice: $newDevice")
                    }
                    updateDevices()

                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.d("BatteryLevelGetter-Phone", "onReceive: $device")
                    val appDevice = devices.devices.find {
                        it.address == device.address
                    } ?: return
                    if (devices.devices.remove(appDevice)){
                        Log.d("BatteryLevelGetter-Phone", "Removed: $appDevice")
                        appDevice.toDeviceConfiguration().saveDevice(applicationContext.filesDir.toPath())
                    }
                    updateDevices()

                }
                else -> {}
            }
        }

    }
    override fun onCreate() {
        super.onCreate()
        registerReceiver(localBatteryMonitor,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        registerReceiver(bluetoothBroadcastReceiver,
            filter
        );
    }
}