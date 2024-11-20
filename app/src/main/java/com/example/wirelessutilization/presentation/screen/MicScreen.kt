package com.example.wirelessutilization.presentation.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wirelessutilization.presentation.component.RadarAnimation
import java.io.File

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    var audioFilePath by remember { mutableStateOf("") }

    val mediaRecorder = remember { MediaRecorder() }
    var mediaPlayer = remember { MediaPlayer() }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        permissionGranted = isGranted.all { it.value }
    }

    // Check permissions at startup
    DisposableEffect(Unit) {
        permissionGranted = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                )
            )
        }

        onDispose {}
    }

    // Handle recording
    fun startRecording() {
        try {
            val outputDir = context.cacheDir
            val outputFile = File(outputDir, "recorded_audio.3gp")
            audioFilePath = outputFile.absolutePath

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder.setOutputFile(audioFilePath)
            mediaRecorder.prepare()
            mediaRecorder.start()

            isRecording = true
        } catch (e: Exception) {
            Log.e("MicrophoneScreen", "Error starting recording", e)
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder.stop()
            mediaRecorder.reset()
            isRecording = false
        } catch (e: Exception) {
            Log.e("MicrophoneScreen", "Error stopping recording", e)
        }
    }

    // Handle playback
    fun startPlayback() {
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioFilePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlaying = true

            mediaPlayer.setOnCompletionListener {
                isPlaying = false
            }
        } catch (e: Exception) {
            Log.e("MicrophoneScreen", "Error starting playback", e)
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer.stop()
            mediaPlayer.reset()
            isPlaying = false
        } catch (e: Exception) {
            Log.e("MicrophoneScreen", "Error stopping playback", e)
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeContent,
        topBar = {
            TopAppBar(title = { Text("Microphone Utilization") })
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
                    text = "Permission for microphone is required. Please grant it.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (isRecording) {
                Text(
                    text = "Microphone is recording... Tap to stop.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
            } else if (isPlaying) {
                Text(
                    text = "Playing the recording... Tap to stop.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    text = "Microphone is not recording. Tap to start.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recording and Playback Animation or Visual Feedback
            if (isRecording || isPlaying) {
                MicRecordingAnimation(modifier = Modifier.size(200.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display status or instructions
            Text(
                text = "Press the button to toggle recording or playback.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons for recording and playback
            Row {
                // Record/Stop button
                Button(onClick = {
                    if (isRecording) {
                        stopRecording()
                    } else {
                        startRecording()
                    }
                }) {
                    Text(text = if (isRecording) "Stop Recording" else "Start Recording")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Play/Stop button
                Button(onClick = {
                    if (audioFilePath.isNotEmpty()) {
                        if (isPlaying) {
                            stopPlayback()
                        } else {
                            startPlayback()
                        }
                    }
                }) {
                    Text(text = if (isPlaying) "Stop Playback" else "Play Recording")
                }
            }
        }
    }
}

// Mic Recording Animation Example
@Composable
fun MicRecordingAnimation(modifier: Modifier = Modifier) {
    // Placeholder for mic animation, could be a custom animated drawable or simple circle expansion
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        RadarAnimation()
    }
}

@Preview
@Composable
fun MicrophoneScreenPreview() {
    MicScreen()
}
