package com.example.wirelessutilization.presentation.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.wirelessutilization.presentation.component.RadarAnimation
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val wifiManager = remember {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    var presenceMessage by remember { mutableStateOf("No presence detected.") }
    var isScanning by remember { mutableStateOf(false) }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.all { it.value }) {
                presenceMessage = "Location permission is required for Wi-Fi scanning."
            }
        }
    )

    // Wi-Fi BroadcastReceiver
    DisposableEffect(context) {
        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    analyzeWifiSignals(wifiManager, onPresenceDetected = {
                        presenceMessage = it
                    })
                } else {
                    presenceMessage = "Wi-Fi scan failed. Retrying..."
                }
                isScanning = false
            }
        }

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiReceiver, intentFilter)

        onDispose {
            context.unregisterReceiver(wifiReceiver)
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeContent,
        topBar = {
            TopAppBar(
                title = { Text(text = "Wireless Utilization") }
            )
        }
    ) { paddings ->
        Column(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Radar animation during scanning
            if (isScanning) {
                RadarAnimation(modifier = Modifier.size(200.dp))
            } else {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                    contentDescription = "Wi-Fi Icon",
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start/Stop Scanning button
            Button(onClick = {
                if (isScanning) {
                    // Stop scanning
                    isScanning = false
                    presenceMessage = "Scanning stopped."
                } else {
                    // Check permissions and start scanning
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        isScanning = true
                        wifiManager.startScan()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_WIFI_STATE,
                            )
                        )
                    }
                }
            }) {
                Text(text = if (isScanning) "Stop Scanning" else "Start Scanning")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display detection message
            Text(
                text = presenceMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@SuppressLint("MissingPermission")
private fun analyzeWifiSignals(
    wifiManager: WifiManager,
    onPresenceDetected: (String) -> Unit
) {
    val scanResults: List<ScanResult> = wifiManager.scanResults
    if (scanResults.isNotEmpty()) {
        val targetWifi = scanResults.maxByOrNull { it.level }
        targetWifi?.let {
            val rssiDifference = abs(it.level - (-50)) // Example baseline
            val message = if (rssiDifference > 5) {
                "Presence Detected: Significant change in Wi-Fi signal strength!"
            } else {
                "No significant presence detected."
            }
            onPresenceDetected(message)
        }
    }
}

@Preview
@Composable
fun WiFiScreenPreview() {
    WiFiScreen()
}
