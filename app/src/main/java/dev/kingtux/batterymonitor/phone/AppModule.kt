package dev.kingtux.batterymonitor.phone

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
            SmallDevice(
                0, android.os.Build.PRODUCT, 100, DeviceType.Phone
            )
        )
    }
}

data class Devices(var phone: SmallDevice)