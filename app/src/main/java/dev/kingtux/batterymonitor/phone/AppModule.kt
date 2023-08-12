package dev.kingtux.batterymonitor.phone

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Parcel
import androidx.core.app.ActivityCompat
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.kingtux.common.Device
import dev.kingtux.common.DeviceMessage
import dev.kingtux.common.DeviceType
import dev.kingtux.common.SharedDevice
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DevicesStore {

    @Singleton
    @Provides
    fun providesDevices(@ApplicationContext application: Context): Devices {
        val phone =         SharedDevice(
            0, android.os.Build.PRODUCT, 100, DeviceType.Phone
        );
        if (ActivityCompat.checkSelfPermission(
                application,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Devices(
                phone,
                mutableListOf()
            )
        }else{
            val devices = BluetoothUtils.getDevices(application).toMutableList();
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
){
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
    fun createDataMap(): PutDataRequest {
        val devices = DeviceMessage(
            this.phone,
            this.devices.getOrNull(0)?.toSharedDevice(),
            this.devices.getOrNull(1)?.toSharedDevice()
        );
        val putDataMapReq = PutDataMapRequest.create("/devices")

        val parcel = Parcel.obtain()
        devices.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        putDataMapReq.dataMap.putByteArray("devices", bytes)
        parcel.recycle()
        return putDataMapReq.asPutDataRequest()
    }
}