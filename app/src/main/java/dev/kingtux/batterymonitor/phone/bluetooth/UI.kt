package dev.kingtux.batterymonitor.phone.bluetooth

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.kingtux.batterymonitor.DeviceType
import dev.kingtux.batterymonitor.phone.swipe.LeftSwipeActionCard
import dev.kingtux.batterymonitor.phone.swipe.swipeAction
import dev.kingtux.batterymonitor.phone.BatteryMonitorTheme


@Composable
fun DevicesAsCards(
    modifier: Modifier = Modifier,
    devices: MutableState<List<Device>>,
    updateDevice: (Device) -> Unit = { },
    addDeviceToOtherList: ((Device) -> Unit) = { }
) {

    LazyColumn(
        modifier = modifier
    ) {
        items(devices.value,
            key = { item -> item.address }
        ) { item ->
            DeviceAsCard(
                item,
                modifier = modifier,
                enableDisableChange = { device, newStatus ->
                    val newList = devices.value.toMutableList()
                    newList.remove(device)
                    devices.value = newList
                    addDeviceToOtherList(device)
                },
                updateDevice = { innerDevice ->
                    updateDevice(innerDevice)
                    val newList = devices.value.toMutableList()
                    newList.remove(innerDevice)
                    newList.add(innerDevice)
                    devices.value = newList
                }
            )
        }
    }
}

@Composable
fun DeviceAsCard(
    device: Device, modifier: Modifier = Modifier,
    enableDisableChange: (
        device: Device,
        newStatus: Boolean
    ) -> Unit,
    updateDevice: ((Device) -> Unit) = { }
) {

    val swipeAction = if (device.enabled) {
        swipeAction(
            icon = rememberVectorPainter(Icons.TwoTone.Warning),
            text = "Disable",
            background = MaterialTheme.colorScheme.error,
            onClick = {
                device.enabled = false
                updateDevice(device)
                enableDisableChange(device, false)
            }
        )
    } else {
        swipeAction(
            icon = rememberVectorPainter(Icons.TwoTone.Warning),
            background = MaterialTheme.colorScheme.secondary,
            text = "Enable",
            onClick = {
                device.enabled = true
                updateDevice(device)
                enableDisableChange(device, true)
            }
        )
    }
    LeftSwipeActionCard(
        actions = listOf(swipeAction)
    ) {

        var dropDownMenuExpanded by remember { mutableStateOf(false) }

        Row(
            modifier = modifier
                .padding(all = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,

            ) {
            Column(
                modifier = modifier
                    .align(Alignment.CenterVertically)
            ) {
                val modifierForText = if (device.isConnected) {
                    modifier
                        .padding(16.dp)
                        .width(100.dp)
                } else {
                    modifier
                        .padding(16.dp)
                };
                Text(
                    text = device.name,
                    modifier = modifierForText
                )
            }
            if (device.isConnected) {
                Spacer(modifier = Modifier.weight(1f))
                if (device.batteryLevel != -1) {
                    Column(
                        modifier = modifier
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = String.format("%d%%", device.batteryLevel),
                            modifier = modifier
                                .padding(16.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.padding(8.dp, 0.dp))
            }

            Spacer(modifier = modifier.weight(1f))

            Column(
                modifier = modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 4.dp)
            ) {
                Button(
                    onClick = { dropDownMenuExpanded = true },

                    modifier = modifier
                        .requiredWidth(150.dp)
                ) {
                    Text(
                        text = stringResource(id = device.deviceType.nameResource()),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = modifier.width(150.dp)
                    )
                }
                DropdownMenu(
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = { dropDownMenuExpanded = false }) {
                    for (type in DeviceType.values()) {
                        DropdownMenuItem(text = {
                            Text(
                                stringResource(id = type.nameResource()),
                                modifier = modifier
                            )
                        }, onClick = {
                            device.deviceType = type
                            dropDownMenuExpanded = false
                            updateDevice(device)
                        })
                    }

                }
            }
        }

    }
}

@SuppressLint("UnrememberedMutableState")
@Preview(
    showBackground = true, device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun EnabledDevicesPreviewCard() {
    BatteryMonitorTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            DevicesAsCards(
                devices = mutableStateOf(
                    Device.testEnabledDevices()
                )
            )
        }

    }
}

@SuppressLint("UnrememberedMutableState")
@Preview(
    showBackground = true, device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun DisabledDevicesPreviewCard() {
    BatteryMonitorTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            DevicesAsCards(
                devices = mutableStateOf(
                    Device.testDisabledDevices()
                )
            )
        }

    }
}
