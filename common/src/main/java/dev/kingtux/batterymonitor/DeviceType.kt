package dev.kingtux.batterymonitor;

import android.os.Parcelable
import kotlinx.parcelize.Parcelize;

@Parcelize
enum class DeviceType : Parcelable {
    Phone {
        override fun iconResource(): Int  = R.drawable.cellphone
        override fun nameResource(): Int = R.string.phone
    },
    Watch {
        override fun iconResource(): Int  = R.drawable.watch
        override fun nameResource(): Int = R.string.watch
    },
    Headphones {
        override fun iconResource(): Int  = R.drawable.headphones
        override fun nameResource(): Int = R.string.headphones
    },
    Earbuds {
        override fun iconResource(): Int  = R.drawable.earbuds
        override fun nameResource(): Int = R.string.earbuds
    },
    Glasses {
        override fun iconResource(): Int  = R.drawable.glasses
        override fun nameResource(): Int = R.string.glasses
    },
    Other {
        override fun iconResource(): Int  = R.drawable.bluetooth
        override fun nameResource(): Int = R.string.other
    };

    abstract fun iconResource(): Int

    fun resourceName(): String = name.lowercase();

    abstract fun nameResource(): Int
}