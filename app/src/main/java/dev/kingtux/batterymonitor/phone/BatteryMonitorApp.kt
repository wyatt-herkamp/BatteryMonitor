package dev.kingtux.batterymonitor.phone

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import dev.kingtux.batterymonitor.phone.active.ActiveDevices
import javax.inject.Inject


@HiltAndroidApp
class BatteryMonitorApp : Application() {

    @Inject
    lateinit var activeDevices: ActiveDevices


    companion object {
        const val NO_BLUETOOTH_PERMISSION =
            "Missing BLUETOOTH_CONNECT permission. This app is useless without it.";
        const val NO_BLUETOOTH_MANAGER =
            "No BluetoothManager found. This app is useless without it.";
    }
}