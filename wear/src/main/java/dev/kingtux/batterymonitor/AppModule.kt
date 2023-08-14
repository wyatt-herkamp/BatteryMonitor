package dev.kingtux.batterymonitor

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable.Creator
import android.util.Log
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.kingtux.batterymonitor.tile.BatteryPercentTileRenderer
import dev.kingtux.batterymonitor.tile.MainTileService
import kotlinx.parcelize.parcelableCreator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DevicesStore {

    @Singleton
    @Provides
    fun providesDevices(@ApplicationContext application: Context): Devices {
        return Devices(
            SharedDevice(0, Build.PRODUCT,50, DeviceType.Watch),
            null,
            null,
            null
        )
    }
}

data class Devices (
    var watch: SharedDevice,
    var deviceOne: SharedDevice?,
    var deviceTwo: SharedDevice?,
    var deviceThree: SharedDevice?
){
    fun putDeviceByIndex(index: Int, deviceOne: SharedDevice?){
        Log.d("Devices", "putDeviceByIndex: $index Value: $deviceOne")
        when(index){
            0 -> {
                this.deviceOne = deviceOne
            }
            1 -> {
                this.deviceTwo = deviceOne
            }
            2 -> {
                this.deviceThree = deviceOne
            }
            else -> {
                Log.e("Devices", "Invalid Index: $index")
            }
        }
    }

    @SuppressLint("VisibleForTests")
    fun updateFromDataItem(dataItem: DataItem, creator: Creator<SharedDevice> = parcelableCreator()){
        Log.d(
            TAG,
            "pathSegments: ${dataItem.uri.pathSegments}"
        )
        val deviceIndex = if (dataItem.uri.pathSegments.size > 2) {
            dataItem.uri.pathSegments[2].toInt()
        } else {
            Log.d(BatteryLevelGetter.TAG, "onDataChanged: Unknown Path: ${dataItem.uri.path}")
            return
        }
        val fromDataItem = DataMapItem.fromDataItem(
            dataItem
        )
        val deviceBytes = fromDataItem.dataMap.getByteArray("device")
        if (deviceBytes == null || deviceBytes.isEmpty()) {
            Log.d(BatteryLevelGetter.TAG, "onDataChanged: Empty Device Asset")
            putDeviceByIndex(deviceIndex, null)
            return
        }

        val parcel = Parcel.obtain().apply {
            unmarshall(deviceBytes, 0, deviceBytes.size)
            setDataPosition(0)
        }

        val device = creator.createFromParcel(parcel)

        parcel.recycle()

        putDeviceByIndex(deviceIndex, device)

        Log.d(TAG, "Debugging Devices: $device")
    }
    @SuppressLint("VisibleForTests")
    fun update(context: Context){
        val creator = parcelableCreator<SharedDevice>()
        val dataClient = Wearable.getDataClient(context)
        dataClient.dataItems.addOnSuccessListener {
            for (dataItem in it.iterator()) {

                Log.d(TAG, "Debugging Devices: $it")
                if (dataItem.uri.path?.startsWith("/batteryMonitor/device/") == true) {
                    updateFromDataItem(dataItem,creator)
                }
            }
            Log.d(TAG, "New Devices: $this")
        }
        MainTileService.forceTileUpdate(context)
    }

    fun getComplicationDevice(): SharedDevice?{
        return if (deviceTwo != null){
            deviceTwo
        }else if (deviceOne != null){
            deviceOne
        }else{
            null;
        }
    }
    companion object{
        const val TAG = "Devices"
    }
}