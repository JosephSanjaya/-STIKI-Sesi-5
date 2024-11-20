package com.example.wirelessutilization.presentation.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingerprintSensorScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val biometricManager = BiometricManager.from(context)
    var isFingerprintEnabled by remember { mutableStateOf(biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) }
    var fingerprintData by remember { mutableStateOf("Waiting for fingerprint interaction...") }
    var permissionGranted by remember { mutableStateOf(false) }

    // Permission Checker
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    // Check permission at startup
    DisposableEffect(Unit) {
        permissionGranted =
            context.checkSelfPermission(Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.USE_BIOMETRIC)
        }

        onDispose {}
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeContent,
        topBar = {
            TopAppBar(title = { Text("Fingerprint Sensor Utilization") })
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
                    text = "Permission for fingerprint sensor is required. Please grant it.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (isFingerprintEnabled) {
                Text(
                    text = "Fingerprint sensor is enabled. Press the button to authenticate.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    text = "Fingerprint sensor is disabled. Please enable it in settings.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fingerprint Animation
            if (permissionGranted) {
                FingerprintAnimation(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Button to trigger authentication
            if (permissionGranted && isFingerprintEnabled) {
                Button(onClick = {
                    startFingerprintAuthentication(context) { result ->
                        fingerprintData = result
                    }
                }) {
                    Text("Authenticate with Fingerprint")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display Fingerprint Sensor Data
            if (permissionGranted) {
                Text(
                    text = fingerprintData,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

private fun startFingerprintAuthentication(context: Context, onFingerprintData: (String) -> Unit) {
    val executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(
        context as androidx.fragment.app.FragmentActivity, // Replace with appropriate FragmentActivity reference
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onFingerprintData("Authentication Succeeded: $result")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFingerprintData("Authentication Failed")
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFingerprintData("Authentication Error: $errString")
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Fingerprint Authentication")
        .setSubtitle("Authenticate using your fingerprint")
        .setNegativeButtonText("Cancel")
        .build()

    biometricPrompt.authenticate(promptInfo)
}

@Composable
fun FingerprintAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier.fillMaxSize(), // Ensures the Box takes the full available space
        contentAlignment = Alignment.Center // Ensures the Canvas is centered
    ) {
        Canvas(modifier = Modifier.size(200.dp)) { // Adjust size as needed
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 3f

            // Draw fingerprint pattern (simplified for demo)
            drawCircle(
                color = Color.Gray.copy(alpha = alpha),
                radius = radius * scale,
                center = center
            )

            // Draw fingerprint ridges
            drawCircle(
                color = Color.Gray,
                radius = radius,
                center = center,
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = Color.Gray,
                radius = radius * 0.8f,
                center = center,
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = Color.Gray,
                radius = radius * 0.6f,
                center = center,
                style = Stroke(width = 6f)
            )
        }
    }
}
