package dev.kingtux.batterymonitor.phone.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import dev.kingtux.batterymonitor.DeviceConfiguration
import dev.kingtux.batterymonitor.DeviceType
import dev.kingtux.batterymonitor.SharedDevice
import dev.kingtux.batterymonitor.randomMACAddress
import java.nio.file.Path


class Device {
    val address: String
    val name: String
    var priority: Int
    var batteryLevel: Int?
    var deviceType: DeviceType
    var isConnected: Boolean
    var enabled: Boolean

    @SuppressLint("MissingPermission")
    constructor(
        deviceConfiguration: DeviceConfiguration, bluetoothDevice: BluetoothDevice
    ) {
        val isConnected = bluetoothDevice.isConnected()
        this.batteryLevel = if (isConnected) {
            bluetoothDevice.batteryLevel()
        } else {
            null
        }

        this.address = deviceConfiguration.address
        this.name = bluetoothDevice.name
        this.priority = deviceConfiguration.priority
        this.deviceType = deviceConfiguration.deviceType
        this.isConnected = isConnected
        this.enabled = deviceConfiguration.enabled
    }

    constructor(bluetoothDevice: BluetoothDevice, directory: Path) : this(
        deviceConfiguration = DeviceConfiguration.loadDevice(directory, bluetoothDevice.address),
        bluetoothDevice = bluetoothDevice
    )

    constructor(
        address: String = randomMACAddress(),
        priority: Int = 1,
        name: String,
        batteryLevel: Int? = null,
        deviceType: DeviceType = DeviceType.Other,
        isConnected: Boolean = false,
        enabled: Boolean = true
    ) {
        this.address = address
        this.name = name
        this.priority = priority
        this.batteryLevel = batteryLevel
        this.deviceType = deviceType
        this.isConnected = isConnected
        this.enabled = enabled
    }

    /**
     * This is used to compare devices to each other
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Device) {
            return false
        }
        return other.address == address
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }

    fun toSharedDevice(): SharedDevice {
        return SharedDevice(priority, name, batteryLevel, deviceType)
    }

    fun toDeviceConfiguration(): DeviceConfiguration {
        return DeviceConfiguration(address, deviceType, enabled, priority)
    }

    fun getBluetoothDevice(bluetoothManager: BluetoothManager): BluetoothDevice {
        return bluetoothManager.adapter.getRemoteDevice(address)
    }

    fun hasChanged(bluetoothDevice: BluetoothDevice) =
        batteryLevel != bluetoothDevice.batteryLevel() || isConnected != bluetoothDevice.isConnected()

    fun refreshDevice(bluetoothDevice: BluetoothDevice): Boolean {
        val batteryLevel = bluetoothDevice.batteryLevel()
        val isConnected = bluetoothDevice.isConnected()
        return if (batteryLevel != this.batteryLevel || isConnected != this.isConnected) {
            this.batteryLevel = batteryLevel
            this.isConnected = isConnected
            true
        } else {
            false
        }
    }

    fun refreshDevice(bluetoothManager: BluetoothManager) =
        refreshDevice(getBluetoothDevice(bluetoothManager))

    override fun toString(): String {
        return "Device(address='$address', name='$name', priority=$priority, batteryLevel=$batteryLevel, deviceType=$deviceType, isConnected=$isConnected, enabled=$enabled)"
    }

    companion object {
        fun testDevices(): MutableList<Device> {
            return mutableListOf(
                Device(
                    name = "Soundcore Liberty 4",
                    enabled = true,
                    isConnected = false,
                    deviceType = DeviceType.Earbuds
                ),
                Device(
                    name = "Developer's WH-1000XM4",
                    enabled = true,
                    isConnected = true,
                    batteryLevel = 70,
                    deviceType = DeviceType.Headphones
                ),
                Device(
                    name = "Mazda",
                    enabled = false,
                    isConnected = false,
                ),
                Device(
                    name = "Soundcore Frames",
                    enabled = true,
                    isConnected = false,
                    deviceType = DeviceType.Earbuds
                ),
                Device(
                    name = "Galaxy Watch 6",
                    enabled = false,
                    isConnected = true,
                    deviceType = DeviceType.Watch
                ),
                Device(
                    name = "Soundcore Flare 2",
                    enabled = false,
                    isConnected = false,
                ),

                )

        }

        fun testEnabledDevices() = testDevices().filter { it.enabled }.toMutableList()
        fun testDisabledDevices() = testDevices().filter { !it.enabled }.toMutableList()
    }

}