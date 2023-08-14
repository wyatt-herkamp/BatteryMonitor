package dev.kingtux.batterymonitor.complication

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.wear.watchface.complications.data.ColorRamp
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.batterymonitor.Devices
import dev.kingtux.batterymonitor.presentation.MainActivity
import dev.kingtux.batterymonitor.SharedDevice
import javax.inject.Inject

@AndroidEntryPoint
class MainComplicationService : SuspendingComplicationDataSourceService() {
    @Inject
    lateinit var currentBattery: Devices
    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        when (type) {

            ComplicationType.SHORT_TEXT -> {
                return deviceText(
                    SharedDevice(
                        0, "Ear Buds", 100, dev.kingtux.batterymonitor.DeviceType.Earbuds
                    )
                )
            }

            ComplicationType.RANGED_VALUE -> {
                return deviceRange(
                    SharedDevice(
                        0, "Ear Buds", 100, dev.kingtux.batterymonitor.DeviceType.Earbuds
                    )
                )

            }

            else -> {
                return null;
            }
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val complicationDevice = currentBattery.getComplicationDevice()
        Log.d("BatteryLevelGetter-Complication", "Request: $request. Device: $complicationDevice")
        when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                return if (complicationDevice != null) {
                    Log.d(
                        "BatteryLevelGetter-Complication",
                        "Updated Complication: $complicationDevice"
                    )
                    deviceRange(complicationDevice)

                } else {
                    Log.d("BatteryLevelGetter-Complication", "Updated Complication: No Device")
                    noDeviceRange()
                }
            }

            ComplicationType.RANGED_VALUE -> return if (complicationDevice != null) {
                Log.d(
                    "BatteryLevelGetter-Complication", "Updated Complication: $complicationDevice"
                )
                deviceRange(complicationDevice)
            } else {
                Log.d("BatteryLevelGetter-Complication", "Updated Complication: No Device")
                noDeviceRange()
            }

            else -> {
                return noDeviceText()
            }
        }
    }

    private fun noDeviceText() = ShortTextComplicationData.Builder(
        PlainComplicationText.Builder(
            "No Device"
        ).build(), PlainComplicationText.Builder("No Device").build()
    ).setTapAction(applicationContext.tapAction()).build();

    private fun deviceText(device: SharedDevice) = ShortTextComplicationData.Builder(
        PlainComplicationText.Builder("${device.name} ${device.batteryLevel}%").build(),
        PlainComplicationText.Builder("${device.name} ${device.batteryLevel}%").build()
    ).setTapAction(applicationContext.tapAction()).build();

    private fun noDeviceRange() = RangedValueComplicationData.Builder(
        0f, 0f, 100f, PlainComplicationText.Builder("No Device").build()
    ).apply {
        setTitle(PlainComplicationText.Builder("No Device").build())
        setTapAction(applicationContext.tapAction())
    }.build()

    private fun deviceRange(device: SharedDevice) = RangedValueComplicationData.Builder(
        device.batteryLevel.toFloat(),
        0f,
        100f,
        PlainComplicationText.Builder("${device.name} ${device.batteryLevel}%").build()
    ).apply {
        setTitle(
            PlainComplicationText.Builder("${device.name} ${device.batteryLevel}%").build()
        )
        if (device.batteryLevel < 20) {
            setColorRamp(
                ColorRamp(
                    intArrayOf(
                        0xffff0000.toInt()
                    ),
                    false
                )
            )
        } else {
            setColorRamp(
                ColorRamp(
                    intArrayOf(
                        0xff00ff00.toInt()
                    ),
                    false
                )
            )
        }
        setTapAction(applicationContext.tapAction())
    }.build()

    companion object {
        fun Context.tapAction(): PendingIntent? {
            val deepLinkIntent = Intent(
                this,
                MainActivity::class.java
            )

            return PendingIntent.getActivity(
                this,
                0,
                deepLinkIntent,
                PendingIntent.FLAG_MUTABLE
            )
        }

        fun forceComplicationUpdate(applicationContext: Context) {
            val request = ComplicationDataSourceUpdateRequester.create(
                applicationContext, ComponentName(
                    applicationContext, MainComplicationService::class.java
                )
            )
            request.requestUpdateAll()
        }
    }

}