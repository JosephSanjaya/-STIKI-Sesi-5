package com.example.wirelessutilization.presentation.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
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


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
    var isNfcEnabled by remember { mutableStateOf(nfcAdapter?.isEnabled == true) }
    var tagData by remember { mutableStateOf("Waiting for NFC interaction...") }
    var permissionGranted by remember { mutableStateOf(false) }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    // Check permission at startup
    DisposableEffect(Unit) {
        permissionGranted =
            context.checkSelfPermission(Manifest.permission.NFC) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.NFC)
        }

        onDispose {}
    }

    DisposableEffect(Unit) {
        if (permissionGranted && nfcAdapter != null) {
            nfcAdapter.enableReaderMode(
                context as androidx.activity.ComponentActivity,
                { tag: Tag ->
                    tagData = processTagData(tag)
                },
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V,
                null
            )
        }

        onDispose {
            if (permissionGranted) {
                nfcAdapter?.disableReaderMode(context as androidx.activity.ComponentActivity)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeContent,
        topBar = {
            TopAppBar(title = { Text("NFC Utilization") })
        }
    ) { paddings ->
        Column(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (!permissionGranted) {
                Text(
                    text = "Permission for NFC is required. Please grant it.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (isNfcEnabled) {
                Text(
                    text = "NFC is enabled. Bring a tag close to the device.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    text = "NFC is disabled. Please enable it in settings.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // NFC Interaction Animation
            if (permissionGranted) {
                RadarAnimation(modifier = Modifier.size(200.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display NFC Tag Data
            if (permissionGranted) {
                Text(
                    text = tagData,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

private fun processTagData(tag: Tag): String {
    val id = tag.id?.joinToString("") { String.format("%02X", it) } ?: "Unknown"
    return "Tag Detected:\nID: $id"
}

@Preview
@Composable
fun NfcScreenPreview() {
    NFCScreen()
}