package dev.kingtux.batterymonitor.watch

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.kingtux.common.DeviceType
import dev.kingtux.common.SharedDevice
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DevicesStore {

    @Singleton
    @Provides
    fun providesDevices(@ApplicationContext application: Context): Devices {
        return Devices(
            SharedDevice(0,android.os.Build.PRODUCT,50, DeviceType.Watch),
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

    fun getComplicationDevice(): SharedDevice?{
        return if (deviceTwo != null){
            deviceTwo
        }else if (deviceOne != null){
            deviceOne
        }else{
            null;
        }

    }
}