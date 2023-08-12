package dev.kingtux.batterymonitor.phone

import android.Manifest
import android.content.res.Configuration

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.kingtux.batterymonitor.phone.ui.theme.BatteryMonitorTheme
import dev.kingtux.common.Device
import dev.kingtux.common.DeviceType

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.contentColorFor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import dev.kingtux.batterymonitor.phone.swipe.SwipeAction
import dev.kingtux.batterymonitor.phone.swipe.SwipeableActionsBox
import dev.kingtux.batterymonitor.phone.swipe.rememberSwipeableActionsState


import java.io.File
import java.nio.file.Path


class MainActivity : ComponentActivity() {
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        )

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    Log.d("BatteryLevelGetter", "onCreate: Denied")
                    setContent {
                        BatteryMonitorTheme {
                            // A surface container using the 'background' color from the theme
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                Text("Denied")
                            }
                        }
                    }
                }
            }

        val appDirectory = applicationContext.filesDir.toPath();

        val devices = BluetoothUtils.getDevices(applicationContext)
        val enabledDevices = devices.filter {
            it.enabled
        }
        val disabledDevices = devices.filter {
            !it.enabled
        }
        setContent {
            BatteryMonitorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(enabledDevices, disabledDevices, appDirectory)
                }
            }
        }
    }

}

@Composable
fun MainContent(
    enabledDevices: List<Device>,
    disabledDevices: List<Device>,
    applicationDirectory: Path,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    Column(modifier = modifier) {
        TabRow(
            selectedTabIndex = selectedTab,
            contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Enabled") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Disabled") }
            )
        }
        if (selectedTab == 0) {
            DevicesAsCards(enabledDevices, applicationDirectory, modifier = modifier)
        } else if (selectedTab == 1) {
            DevicesAsCards(disabledDevices, applicationDirectory, modifier = modifier)
        }
    }


}


@Composable
fun DevicesAsCards(devices: List<Device>, applicationDirectory: Path, modifier: Modifier = Modifier,) {

    LazyColumn(
        modifier = modifier
    ) {
        items(devices,
            key = { item -> item.address }
        ) { item ->
            DeviceAsCard(item, applicationDirectory, modifier = modifier, enableDisableChange = { device, newStatus ->
                // TODO update
            })
        }
    }
}
@Composable
fun DeviceAsCard(
    device: Device,applicationDirectory: Path, modifier: Modifier = Modifier,
    enableDisableChange: (
        device: Device,
        newStatus: Boolean
    ) -> Unit
){

    val swipeAction = if (device.enabled){
        SwipeAction(
            icon = rememberVectorPainter(Icons.TwoTone.Warning),
            text = "Disable",
            background = MaterialTheme.colorScheme.error,
            onSwipe = {
                device.enabled = false
                enableDisableChange(device, false)
            }
        )
    }else{
        SwipeAction(
            icon = rememberVectorPainter(Icons.TwoTone.Warning),
            background = MaterialTheme.colorScheme.secondary,
            text = "Enable",
            onSwipe = {
                device.enabled = true
                enableDisableChange(device, true)
            }
        )
    }
    SwipeableActionsBox(
        leftActions = listOf(swipeAction)
    ){

        Card(
            modifier = modifier
                .padding(all = 8.dp),
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
                    val modifierForText= if (device.isConnected){
                        modifier
                            .padding(16.dp)
                            .width(100.dp)
                    }else{
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
                }else{
                    Spacer(modifier = Modifier.padding(8.dp,0.dp))
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
                            text = stringResource(id =  device.deviceType.nameResource()),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = modifier.width(150.dp)
                        )
                    }
                    DropdownMenu(expanded = dropDownMenuExpanded, onDismissRequest = { dropDownMenuExpanded = false }) {
                        for (type in DeviceType.values()) {
                            DropdownMenuItem(text = {
                                Text(stringResource(id =  device.deviceType.nameResource()), modifier = modifier)
                            }, onClick = {
                                device.deviceType = type
                                dropDownMenuExpanded = false
                                device.toDeviceConfiguration().saveDevice(applicationDirectory)
                            })
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun Device(device: Device,applicationDirectory: Path, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(device.enabled) }
    var dropDownMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .padding(all = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.secondary),
        horizontalArrangement = Arrangement.SpaceBetween,

    ) {
        Column(
            modifier = modifier
                .align(Alignment.CenterVertically)
        ) {
            val modifierForText= if (device.isConnected){
                modifier
                    .padding(16.dp)
                    .width(100.dp)
            }else{
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
        }else{
            Spacer(modifier = Modifier.padding(8.dp,0.dp))
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
                    text = stringResource(id =  device.deviceType.nameResource()),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = modifier.width(150.dp)
                )
            }
            DropdownMenu(expanded = dropDownMenuExpanded, onDismissRequest = { dropDownMenuExpanded = false }) {
                for (type in DeviceType.values()) {
                    DropdownMenuItem(text = {
                        Text(stringResource(id =  device.deviceType.nameResource()), modifier = modifier)
                    }, onClick = {
                        device.deviceType = type
                        dropDownMenuExpanded = false
                        device.toDeviceConfiguration().saveDevice(applicationDirectory)
                    })
                }

            }
        }
    }
}



@Preview(showBackground = true, device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun EnabledDevicesPreviewCard() {
    BatteryMonitorTheme {
        val enabledDevices = listOf(
            Device(1, "Earbuds", 100, DeviceType.Earbuds, true, "00:00:00:00:00:10", true),
            Device(1, "Developer's WH-1000XM4", 0, DeviceType.Headphones, false, "00:00:00:00:10:00", true),
            Device(1, "Glasses", 50, DeviceType.Glasses, true, "00:00:00:10:00:00", true),
        )
        DevicesAsCards(enabledDevices, applicationDirectory = File("test").toPath())
    }
}
@Preview(showBackground = true, device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun DisabledDevicesPreviewCard() {
    BatteryMonitorTheme {
        val disableDevices = listOf(
            Device(1, "Car", 0, DeviceType.Other, false, "00:00:10:00:00:00", false),
            Device(1, "Watch", 0, DeviceType.Watch, false, "00:10:00:00:00:00", false),
        )
        DevicesAsCards(disableDevices, applicationDirectory = File("test").toPath())
    }
}
@Preview(showBackground = true, device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun FullTest() {
    BatteryMonitorTheme {
        val enabledDevices = listOf(
            Device(1, "Earbuds", 100, DeviceType.Earbuds, true, "00:00:00:00:00:10", true),
            Device(1, "Developer's WH-1000XM4", 0, DeviceType.Headphones, false, "00:00:00:00:10:00", true),
            Device(1, "Glasses", 50, DeviceType.Glasses, true, "00:00:00:10:00:00", true),
        )
        val disableDevices = listOf(
            Device(1, "Car", 0, DeviceType.Other, false, "00:00:10:00:00:00", false),
            Device(1, "Watch", 0, DeviceType.Watch, false, "00:10:00:00:00:00", false),
        )
        MainContent(enabledDevices, disableDevices, applicationDirectory = File("test").toPath())
    }
}
