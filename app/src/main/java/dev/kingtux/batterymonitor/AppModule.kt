package dev.kingtux.batterymonitor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Parcel
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DevicesStore {

    @Singleton
    @Provides
    fun providesDevices(@ApplicationContext application: Context): Devices {
        val phone = SharedDevice(
            0, android.os.Build.PRODUCT, 100, DeviceType.Phone
        );
        if (ActivityCompat.checkSelfPermission(
                application,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val value = Devices(
                phone,
                mutableListOf()
            )
            Log.d("DeviceStore", "Starting with no devices")
            return value;
        } else {
            Log.d("DeviceStore", "Starting with no devices")
            val devices = BluetoothUtils.getDevices(application).toMutableList();
            Log.d("DeviceStore", "Starting with $devices")
            return Devices(
                phone,
                devices
            )
        }
    }
}

data class Devices(
    var phone: SharedDevice,
    var devices: MutableList<Device>
) {
    fun getSharedDevices(): List<SharedDevice> {
        val connectedDevices =
            this.devices.toList().filter {
                it.batteryLevel != -1
            }.map {
                it.toSharedDevice()
            }.toMutableList();
        connectedDevices.add(this.phone)
        connectedDevices.sortBy {
            it.priority
        };
        return connectedDevices
    }

    fun resetDevices(context: Context) {
        this.devices = BluetoothUtils.getDevices(context).filter {
            it.batteryLevel != -1
        }.filter {
            it.isConnected
        }.toMutableList();
        updateDevices(Wearable.getDataClient(context))
    }

    fun updateDevices(dataClient: DataClient) {
        putDevice(phone, dataClient, 0)

        for (i in 0 until 2) {
            devices.getOrNull(
                i
            )?.let {
                putDevice(devices[i].toSharedDevice(), dataClient, i + 1)
            } ?: noDevice(dataClient, i+1)
        }


    }

    @SuppressLint("VisibleForTests")
    private fun putDevice(device: SharedDevice, dataClient: DataClient, index: Int) {
        val url = "/batteryMonitor/device/$index"

        Log.d("Devices", "putDevice: $device with path: $url")
        val putDataMapReq = PutDataMapRequest.create(url)
        val parcel = Parcel.obtain()
        device.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        putDataMapReq.dataMap.putByteArray("device", bytes)
        parcel.recycle()
        Log.d("Devices", "putDevice: ${bytes.size}")
        dataClient.putDataItem(putDataMapReq.asPutDataRequest().apply {
            setUrgent()
            Log.d("Devices", "putDevice: $this")
        })
    }

    @SuppressLint("VisibleForTests")
    private fun noDevice(dataClient: DataClient, index: Int) {
        val url = "/batteryMonitor/device/$index"

        Log.d("Devices", "putDevice: null with path: $url")
        val putDataMapReq = PutDataMapRequest.create(url)
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }
}