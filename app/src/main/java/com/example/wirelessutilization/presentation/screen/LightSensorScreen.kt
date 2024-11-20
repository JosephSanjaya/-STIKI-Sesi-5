package com.example.wirelessutilization.presentation.screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightSensorScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    var lightLevel by remember { mutableStateOf("Waiting for sensor data...") }
    var isSensorAvailable by remember { mutableStateOf(lightSensor != null) }

    DisposableEffect(Unit) {
        if (isSensorAvailable) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null) {
                        lightLevel = "Light Level: ${event.values[0]} lx"
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // No-op
                }
            }

            sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }

        onDispose {}
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeContent,
        topBar = {
            TopAppBar(title = { Text("Light Sensor Demonstration") })
        }
    ) { paddings ->
        Column(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isSensorAvailable) {
                Text(
                    text = lightLevel,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LightSensorAnimation(lightLevel = lightLevel, modifier = Modifier.size(200.dp))
            } else {
                Text(
                    text = "Light sensor is not available on this device.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun LightSensorAnimation(lightLevel: String, modifier: Modifier = Modifier) {
    val lightValue = lightLevel.filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
    val brightness = (lightValue / 1000).coerceIn(0f, 1f) // Normalize light value for animation

    val animatedBrightness by animateFloatAsState(
        targetValue = brightness,
        animationSpec = tween(durationMillis = 500)
    )

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 3f
        drawCircle(
            color = Color.Magenta.copy(alpha = animatedBrightness),
            radius = radius,
            center = Offset(size.width / 2, size.height / 2)
        )
    }
}
