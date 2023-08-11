package dev.kingtux.batterymonitor.phone

import android.Manifest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

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
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import dev.kingtux.common.getSettings
import dev.kingtux.common.updateSettings

import java.io.File


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
        val file = File(applicationContext.filesDir, "devices.json")
        val settings = getSettings(file)
        val devices = BluetoothUtils.getDevices(applicationContext, settings).toMutableList()
        devices.sortByDescending {
            it.enabled
        }

        setContent {
            BatteryMonitorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Devices(devices, file)
                }
            }
        }
    }

}


@Composable
fun Devices(devices: List<Device>, file: File, modifier: Modifier = Modifier) {

    LazyColumn(
        modifier = modifier
    ) {
        items(devices,
            key = { item -> item.address }
        ) { item ->
            Device(item, file, modifier = modifier)
        }
    }
}


@Composable
fun Device(device: Device, file: File, modifier: Modifier = Modifier) {
    var enabled by remember { mutableStateOf(device.enabled) }
    Row(
        modifier = modifier
            .padding(all = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.secondary)
            .fillMaxWidth()
    ) {
        Column(
            modifier = modifier.width(25.dp)
        ) {
            Checkbox(checked = enabled, onCheckedChange = {
                device.enabled = it;
                enabled = it;
                updateSettings(device, file)
            })
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textWidth = if (device.isConnected) 100.dp else 150.dp
            Text(
                text = device.name,
                modifier = modifier
                    .padding(16.dp)
                    .width(textWidth)
            )
        }
        if (device.isConnected) {
            Spacer(modifier = Modifier.padding(8.dp))
            if (device.batteryLevel != -1) {
                Column {
                    Text(
                        text = String.format("%d%%", device.batteryLevel),
                        modifier = modifier
                            .wrapContentWidth(Alignment.Start)
                            .padding(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = modifier.weight(1f))
        var expanded by remember { mutableStateOf(false) }

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            // Dropdown
            Button(
                onClick = { expanded = true },
                modifier = modifier
                    .padding(8.dp)
                    .width(150.dp)
                    .wrapContentWidth(Alignment.Start)
            ) {
                Text(
                    text = device.deviceType.toString(),
                    modifier = modifier
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                for (type in DeviceType.values()) {
                    DropdownMenuItem(text = {
                        Text(type.toString(), modifier = modifier)
                    }, onClick = {
                        device.deviceType = type
                        expanded = false
                        updateSettings(device, file)
                    })
                }

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BatteryMonitorPreview() {
    BatteryMonitorTheme {
        Devices(
            listOf(
                Device(1, "Test", 100, DeviceType.Other, true, "00:00:00:00:00:10", true),
                Device(1, "Test2", 0, DeviceType.Headphones, false, "00:00:00:00:10:00", true),
                Device(1, "Test3", 50, DeviceType.Glasses, true, "00:00:00:10:00:00", true),
            ), File("test.json")
        )
    }
}
