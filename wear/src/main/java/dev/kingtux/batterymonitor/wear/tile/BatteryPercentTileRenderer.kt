package dev.kingtux.batterymonitor.wear.tile

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.DpProp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.ColorFilter
import androidx.wear.protolayout.LayoutElementBuilders.Image


import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.ProgressIndicatorColors

import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import com.google.android.horologist.compose.tools.TileLayoutPreview
import dev.kingtux.batterymonitor.DeviceType
import dev.kingtux.batterymonitor.SharedDevice
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.ResourceBuilders.Resources
import dev.kingtux.batterymonitor.wear.TRACK_BACKGROUND
import dev.kingtux.batterymonitor.wear.getTileColor
import dev.kingtux.batterymonitor.wear.Devices as TileData

class BatteryPercentTileRenderer(context: Context) :
    SingleTileLayoutRenderer<TileData, Unit>(context) {


    override fun Resources.Builder.produceRequestedResources(
        resourceState: Unit,
        deviceParameters: DeviceParameters,
        resourceIds: MutableList<String>,
    ) {
        addIcons()
    }


    private fun Resources.Builder.addIcons(
    ) {
        for (value in DeviceType.values()) {
            addIdToImageMapping(
                value.resourceName(),
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(value.iconResource())
                            .build()
                    ).build()
            )
        }

    }

    override fun renderTile(state: TileData, deviceParameters: DeviceParameters): LayoutElement {
        return PrimaryLayout.Builder(deviceParameters)
            .setContent(bodyLayout(state)).build()
    }

    private fun bodyLayout(
        state: TileData,
    ) = LayoutElementBuilders.Column.Builder().apply {
        val numberOfDevices = state.numberOfDevices()
        addContent(
            deviceRow(state.watch, state.phone, numberOfDevices).build()
        )
        if (state.deviceOne != null) {
            addContent(
                deviceRow(state.deviceOne, state.deviceTwo, numberOfDevices).build()
            )
        }

    }.build()

    private fun deviceRow(
        deviceOne: SharedDevice?, deviceTwo: SharedDevice?, numberOfDevices: Int
    ) = LayoutElementBuilders.Row.Builder().apply {

        deviceOne?.let {
            addContent(batteryPercentage(it, numberOfDevices).build())
        }
        addContent(
            LayoutElementBuilders.Spacer.Builder().setWidth(DpProp.Builder(8f).build()).build()
        )
        deviceTwo?.let {
            addContent(batteryPercentage(it, numberOfDevices).build())
        }

    }

    private fun batteryPercentage(
        device: SharedDevice, numberOfDevices: Int = 4
    ) = LayoutElementBuilders.Box.Builder().apply {
        val circleSize = when (numberOfDevices) {
            4 -> {
                DpProp.Builder(70f).build()
            }

            3, 2 -> {
                DpProp.Builder(80f).build()
            }
            else -> {
                DpProp.Builder(100f).build()
            }
        }

        val colorProp = device.getTileColor()
        setModifiers(
            ModifiersBuilders.Modifiers.Builder().setPadding(
                ModifiersBuilders.Padding.Builder().setAll(DpProp.Builder(2f).build()).build()
            ).build()
        )
        addContent(CircularProgressIndicator.Builder().apply {
            setProgress(device.batteryLevelOutOf100())
            setCircularProgressIndicatorColors(
                ProgressIndicatorColors(
                    colorProp,
                    ColorProp.Builder(TRACK_BACKGROUND.toInt()).build(),
                )
            )
            setStrokeWidth(DpProp.Builder(4f).build())
            setHeight(circleSize)
            setWidth(circleSize)
        }.build())
        addContent(
            LayoutElementBuilders.Column.Builder().apply {
                addContent(
                    Image.Builder().apply {
                        setModifiers(
                            ModifiersBuilders.Modifiers.Builder().setPadding(
                                ModifiersBuilders.Padding.Builder()
                                    .setAll(DpProp.Builder(2f).build())
                                    .build()
                            ).build()
                        )
                        // Change to white
                        setColorFilter(
                            ColorFilter.Builder().apply {
                                setTint(ColorProp.Builder(0xffffffff.toInt()).build())
                            }.build()
                        )
                        setResourceId(device.deviceType.resourceName())
                        setHeight(DpProp.Builder(24f).build())
                        setWidth(DpProp.Builder(24f).build())
                    }.build()
                )
                addContent(
                    Text.Builder(context, "${device.batteryLevel}%").apply {
                        setTypography(
                            Typography.TYPOGRAPHY_CAPTION3
                        );
                        setModifiers(
                            ModifiersBuilders.Modifiers.Builder().setPadding(
                                ModifiersBuilders.Padding.Builder()
                                    .setAll(DpProp.Builder(2f).build())
                                    .build()
                            ).build()
                        )
                        setColor(
                            colorProp
                        )
                    }.build()
                )
            }.build()
        )
    }
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun PreviewOne() {
    val context = LocalContext.current

    val renderer = remember {
        BatteryPercentTileRenderer(context)
    }
    TileLayoutPreview(
        TileData(
            SharedDevice(0, "Watch", 70, DeviceType.Watch),
            null, null,
            null
        ), Unit, renderer
    )

}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun PreviewTwoDevices() {
    val context = LocalContext.current

    val renderer = remember {
        BatteryPercentTileRenderer(context)
    }
    TileLayoutPreview(
        TileData(
            SharedDevice(0, "Watch", 70, DeviceType.Watch),
            SharedDevice(0, "Phone", 80, DeviceType.Phone),
            null,
            null
        ), Unit, renderer
    )

}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun PreviewFourDevices() {
    val context = LocalContext.current

    val renderer = remember {
        BatteryPercentTileRenderer(context)
    }
    TileLayoutPreview(
        TileData(
            SharedDevice(0, "Watch", 100, DeviceType.Watch),
            SharedDevice(0, "Phone", 80, DeviceType.Phone),
            SharedDevice(0, "Headphones", 80, DeviceType.Headphones),
            SharedDevice(0, "Glasses", 20, DeviceType.Glasses),
        ), Unit, renderer
    )

}

@Preview(
    device = Devices.WEAR_OS_SQUARE,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun PreviewFourDevicesSquare() {
    val context = LocalContext.current

    val renderer = remember {
        BatteryPercentTileRenderer(context)
    }
    TileLayoutPreview(
        TileData(
            SharedDevice(0, "Watch", 100, DeviceType.Watch),
            SharedDevice(0, "Phone", 80, DeviceType.Phone),
            SharedDevice(0, "Headphones", 80, DeviceType.Headphones),
            SharedDevice(0, "Glasses", 20, DeviceType.Glasses),
        ), Unit, renderer
    )

}