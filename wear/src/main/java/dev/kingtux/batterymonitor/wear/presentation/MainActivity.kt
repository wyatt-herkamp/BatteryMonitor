/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package dev.kingtux.batterymonitor.wear.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource

import androidx.compose.ui.tooling.preview.Devices as ComposeDevices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.batterymonitor.wear.Devices
import dev.kingtux.batterymonitor.wear.presentation.theme.BatteryMonitorTheme
import dev.kingtux.batterymonitor.DeviceType
import dev.kingtux.batterymonitor.SharedDevice
import dev.kingtux.batterymonitor.wear.TRACK_BACKGROUND
import dev.kingtux.batterymonitor.wear.getColor
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    private var currentBattery: Devices = Devices()
    init {
        currentBattery.updateOccurred = {
            runOnUiThread {
                setContent {
                    WearApp(currentBattery)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentBattery.onCreate(applicationContext)

        setContent {
            WearApp(currentBattery)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentBattery.onDestroy(applicationContext)
    }
}

@Composable
fun WearApp(devices: Devices) {
    val numberOfDevices = devices.numberOfDevices()
    BatteryMonitorTheme {
        if (numberOfDevices == 1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                BatteryLevel(devices.watch, numberOfDevices)
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(2),
                modifier = Modifier.apply {
                    padding(0.dp, 10.dp)
                },
                verticalArrangement = Arrangement.Center,
                content = {
                    item {
                        BatteryLevel(devices.watch, numberOfDevices)
                    }
                    devices.phone?.let {
                        item {
                            BatteryLevel(it, numberOfDevices)
                        }
                    }
                    devices.deviceOne?.let {
                        item {
                            BatteryLevel(it, numberOfDevices)
                        }
                    }
                    devices.deviceTwo?.let {
                        item {
                            BatteryLevel(it, numberOfDevices)
                        }
                    }

                })
        }

    }
}

@Composable
fun BatteryLevel(device: SharedDevice, numberOfDevices: Int = 1) {
    val sizes = when (numberOfDevices) {
        4 -> {
            Pair(50.dp, 20.dp)
        }

        3, 2 -> {
            Pair(60.dp, 30.dp)
        }

        else -> {
            Pair(100.dp, 50.dp)
        }
    }

    val color = device.getColor();
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(Color.Transparent),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {

        CircularProgressIndicator(
            progress = device.batteryLevelOutOf100(),
            modifier = Modifier
                .height(sizes.first)
                .width(sizes.first)
                .background(Color.Transparent),
            indicatorColor = color,
            trackColor = Color(TRACK_BACKGROUND),
            strokeWidth = 4.dp
        )
        Column {
            Icon(
                imageVector = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = device.deviceType.iconResource()),
                contentDescription = null,
                modifier = Modifier
                    .height(sizes.second)
                    .width(sizes.second)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "${device.batteryLevel}%",
                color = color,
                style = MaterialTheme.typography.caption3
            )
        }
    }
}

@Preview(device = ComposeDevices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(
        Devices(
            SharedDevice(1, "Watch", 100, DeviceType.Watch), null, null, null
        )
    )
}

@Preview(device = ComposeDevices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun TwoDevices() {
    WearApp(
        Devices(
            SharedDevice(1, "Watch", 100, DeviceType.Watch),
            SharedDevice(1, "Phone", 80, DeviceType.Phone),
            null,
            null
        )
    )
}

@Preview(device = ComposeDevices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun ThreeDevices() {
    WearApp(
        Devices(
            SharedDevice(1, "Watch", 100, DeviceType.Watch),
            SharedDevice(1, "Phone", 80, DeviceType.Phone),
            SharedDevice(0, "Glasses", 20, DeviceType.Glasses),
            null
        )
    )
}

@Preview(device = ComposeDevices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun FourDevices() {
    WearApp(
        Devices(
            SharedDevice(0, "Watch", 100, DeviceType.Watch),
            SharedDevice(0, "Phone", 80, DeviceType.Phone),
            SharedDevice(0, "Headphones", 80, DeviceType.Headphones),
            SharedDevice(0, "Glasses", 20, DeviceType.Glasses),
        )
    )
}
