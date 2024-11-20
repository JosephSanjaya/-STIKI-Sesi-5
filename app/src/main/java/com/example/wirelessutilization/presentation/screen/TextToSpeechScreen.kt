package com.example.wirelessutilization.presentation.screen

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToSpeechScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tts = remember {
        TextToSpeech(context) { status ->
            // Handle TTS initialization status
            if (status == TextToSpeech.SUCCESS) {
                // If successful, you can initialize your TTS-related state here
                // For example, you can choose a language or prepare it for speaking.
            }
        }
    }

    // List of predefined phrases
    val phrases = listOf(
        "Hello! How are you?",
        "Welcome to the Text to Speech example.",
        "Compose makes UI development simple.",
        "This is a dynamic TTS implementation.",
        "Hope you're having a great day!"
    )

    // State to manage TTS status and custom input
    var statusText by remember { mutableStateOf("Tap to Speak!") }
    var customText by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Text to Speech Example") })
        }
    ) { paddings ->
        Column(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Predefined Phrase List (Selectable)
            Text(
                text = "Select a Phrase:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(phrases.size) { index ->
                    ListItem(
                        modifier = Modifier.padding(vertical = 8.dp),
                        headlineContent = {
                            Text(phrases[index])
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                tts.speak(phrases[index], TextToSpeech.QUEUE_FLUSH, null, null)
                                statusText = "Speaking: ${phrases[index]}"
                            }) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "Speak")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Text Input
            TextField(
                value = customText,
                onValueChange = { customText = it },
                label = { Text("Enter Custom Text") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (customText.text.isNotEmpty()) {
                        tts.speak(customText.text, TextToSpeech.QUEUE_FLUSH, null, null)
                        statusText = "Speaking: ${customText.text}"
                    } else {
                        statusText = "Please enter some text."
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Speak Custom Text")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Text showing current action
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Cleanup TTS instance when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown() // Properly shutdown TTS instance when no longer needed
        }
    }
}

@Preview
@Composable
fun TextToSpeechScreenPreview() {
    TextToSpeechScreen()
}
