package com.example.wirelessutilization.presentation.screen

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wirelessutilization.presentation.component.RadarAnimation

@SuppressLint("MissingPermission", "InlinedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var isScanning by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf(listOf<BluetoothDevice>()) }
    var message by remember { mutableStateOf("Press 'Start Scanning' to find Bluetooth devices.") }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val scanGranted = permissions[Manifest.permission.BLUETOOTH_SCAN] ?: false
        if (!scanGranted) {
            message = "Permission is required to scan for Bluetooth devices."
        }
    }

    // Bluetooth BroadcastReceiver
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            if (!devices.contains(it)) {
                                devices = devices + it
                            }
                        }
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        isScanning = false
                        message = "Scan complete. Found ${devices.size} devices."
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeContent,
        topBar = {
            TopAppBar(title = { Text("Bluetooth Utilization") })
        }
    ) { paddings ->
        Column(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isScanning) RadarAnimation(modifier = Modifier.size(200.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (bluetoothAdapter?.isEnabled == true) {
                    if (isScanning) {
                        bluetoothAdapter.cancelDiscovery()
                        isScanning = false
                        message = "Scanning stopped."
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                        )
                        try {
                            if (bluetoothAdapter.startDiscovery()) {
                                isScanning = true
                                devices = emptyList()
                                message = "Scanning for devices..."
                            } else {
                                message = "Failed to start scanning."
                            }
                        } catch (e: SecurityException) {
                            message = "Permission denied for scanning."
                        }
                    }
                } else {
                    message = "Please enable Bluetooth to start scanning."
                }
            }) {
                Text(text = if (isScanning) "Stop Scanning" else "Start Scanning")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = message, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(devices.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(devices) { device ->
                        BluetoothDeviceCard(device = device)
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDeviceCard(device: BluetoothDevice) {
    var isConnecting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = device.name ?: "Unknown Device",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Address: ${device.address}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    isConnecting = true
                    try {
                        device.createBond() // Attempt to pair with the device
                        Toast.makeText(context, "Pairing initiated with ${device.name}", Toast.LENGTH_SHORT).show()
                    } catch (e: SecurityException) {
                        Toast.makeText(context, "Permission denied for pairing.", Toast.LENGTH_SHORT).show()
                    } finally {
                        isConnecting = false
                    }
                },
                enabled = !isConnecting
            ) {
                Text(text = if (isConnecting) "Connecting..." else "Connect")
            }
        }
    }
}

@Preview
@Composable
fun BluetoothScreenPreview() {
    BluetoothScreen()
}
