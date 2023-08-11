package dev.kingtux.batterymonitor.watch

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.kingtux.common.DeviceType
import dev.kingtux.common.SmallDevice
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DevicesStore {

    @Singleton
    @Provides
    fun providesDevices(@ApplicationContext application: Context): Devices {
        return Devices(
            SmallDevice(0,android.os.Build.PRODUCT,50, DeviceType.Watch),
            null,
            null,
            null
        )
    }
}

data class Devices (
     var watch: SmallDevice,
     var deviceOne: SmallDevice?,
     var deviceTwo: SmallDevice?,
     var deviceThree: SmallDevice?
){

    fun getComplicationDevice(): SmallDevice?{
        return if (deviceTwo != null){
            deviceTwo
        }else if (deviceOne != null){
            deviceOne
        }else{
            null;
        }

    }
}