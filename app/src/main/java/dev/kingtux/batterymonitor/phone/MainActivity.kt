package dev.kingtux.batterymonitor.phone

import android.Manifest
import android.content.Context
import android.content.res.Configuration

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.layout.Column

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.MutableState

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.batterymonitor.phone.bluetooth.BluetoothUtils
import dev.kingtux.batterymonitor.phone.bluetooth.Device
import dev.kingtux.batterymonitor.phone.bluetooth.DevicesAsCards
import dev.kingtux.batterymonitor.phone.active.ActiveDevices
import dev.kingtux.batterymonitor.phone.active.DevicesScreen
import dev.kingtux.batterymonitor.phone.settings.Settings
import dev.kingtux.batterymonitor.phone.settings.SettingsScreen

import java.nio.file.Path
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var activeDevicesRef: ActiveDevices

    @Inject
    lateinit var settings: Settings

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("Permission-Request", "${it.key} = ${it.value}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT
            )
        )
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                Log.d("BatteryLevelGetter", "onCreate: Denied")
                setContent {
                    val settings = remember { mutableStateOf(settings) }
                    ContentWrap(settings) {
                        NoPermission()
                    }
                }
            }
        }


        val pairedBluetoothDevices = BluetoothUtils.getPairedDevices(applicationContext)
        val enabledDevices = pairedBluetoothDevices.filter {
            it.enabled
        }
        val disabledDevices = pairedBluetoothDevices.filter {
            !it.enabled
        }
        setContent {
            val settings = remember { mutableStateOf(settings) }
            val devices = remember { mutableStateOf(activeDevicesRef) }
            ContentWrap(settings) {
                MainContent(
                    enabledDevicesValue = enabledDevices,
                    disabledDevicesValue = disabledDevices,
                    activeDevices = devices,
                    context = applicationContext,
                    settings = settings
                )
            }
        }

    }

}

@Composable
fun ContentWrap(
    settings: MutableState<Settings> = mutableStateOf(Settings()), content: @Composable () -> Unit
) {
    BatteryMonitorTheme(settings.value.theme) {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

fun updateDevice(device: Device, directory: Path) {
    device.toDeviceConfiguration().saveDevice(directory)
}

@Composable
fun NoPermission() {
    Text(text = "No Permission", textAlign = TextAlign.Center)
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    enabledDevicesValue: List<Device>,
    disabledDevicesValue: List<Device>,
    context: Context,
    activeDevices: MutableState<ActiveDevices>? = null,
    settings: MutableState<Settings> = mutableStateOf(Settings())
) {
    val enabledDevices = remember { mutableStateOf(enabledDevicesValue) }
    val disabledDevices = remember { mutableStateOf(disabledDevicesValue) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val updateDevice: (Device) -> Unit = {
        updateDevice(
            it,
            context.dataDir.toPath()
        )
    }
    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTab,
            contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                )
            }) {
            Tab(selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Enabled") })
            Tab(selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Disabled") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = {
                Text("Settings")
            })
            Tab(
                selected = selectedTab == 3,
                enabled = activeDevices != null,
                onClick = { selectedTab = 3 },
                text = {
                    Text("Devices")
                })
        }
        when (selectedTab) {
            0 -> {
                DevicesAsCards(
                    modifier = modifier,
                    enabledDevices,
                    updateDevice = updateDevice,
                    addDeviceToOtherList = {
                        val newList = disabledDevices.value.toMutableList()
                        newList.add(it)
                        disabledDevices.value = newList
                    })
            }

            1 -> {
                DevicesAsCards(
                    modifier = modifier,
                    disabledDevices,
                    updateDevice = updateDevice,
                    addDeviceToOtherList = {
                        val newList = enabledDevices.value.toMutableList()
                        newList.add(it)
                        enabledDevices.value = newList
                    })
            }

            2 -> {
                SettingsScreen(settings = settings, context = context)
            }

            3 -> {
                if (activeDevices == null) {
                    Text(text = "No Devices", textAlign = TextAlign.Center)
                    selectedTab = 2
                    return@Column
                }
                DevicesScreen(
                    settings = settings,
                    activeDevices = activeDevices,
                    context = context
                )
            }
        }

    }


}

@Preview(
    showBackground = true,
    device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun FullTest() {
    val context = androidx.compose.ui.platform.LocalContext.current

    val devices = Device.testDevices();
    val enabledDevices = devices.filter {
        it.enabled
    }
    val disableDevices = devices.filter {
        !it.enabled
    }
    ContentWrap {
        MainContent(
            enabledDevicesValue = enabledDevices,
            disabledDevicesValue = disableDevices,
            context = context
        )
    }

}
