
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun SpeechToTextScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    var speechResult by remember { mutableStateOf("Say something...") }
    var isSpeechToTextProcessing by remember { mutableStateOf(false) }

    // Text-to-Speech initialization
    val textToSpeech = remember { mutableStateOf<TextToSpeech?>(null) }
    val reversedText = remember { mutableStateOf("") }
    val outputAudioUri = remember { mutableStateOf("") }

    val permissionGranted = remember { mutableStateOf(false) }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted.value = isGranted
    }

    // Check for permissions at startup
    DisposableEffect(Unit) {
        permissionGranted.value =
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted.value) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        onDispose {}
    }

    LaunchedEffect(Unit) {
        textToSpeech.value = TextToSpeech(context, OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.value?.setLanguage(Locale.US)
            }
        })
    }

    // Handle speech-to-text functionality
    val startSpeechRecognition = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        speechRecognizer.startListening(intent)
        isSpeechToTextProcessing = true
    }

    // Convert Text to Speech and save the file
    fun convertTextToSpeech(text: String) {
        if (textToSpeech.value != null) {
            val fileName = "reversed_audio_${System.currentTimeMillis()}.mp3"
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.TITLE, fileName)
                put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            }
            val resolver: ContentResolver = context.contentResolver
            val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            outputAudioUri.value = audioUri?.toString() ?: ""
            // Generate the audio file here (use TextToSpeech API or other methods)
        }
    }
    // Handle Speech Recognizer callback
    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            isSpeechToTextProcessing = false
            speechResult = "Error occurred: $error"
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null && matches.isNotEmpty()) {
                speechResult = matches[0]
                reversedText.value = speechResult.reversed()
                isSpeechToTextProcessing = false
                convertTextToSpeech(reversedText.value)
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Speech-to-Text & Text-to-Speech Demo") })
        }
    ) { paddings ->
        Column(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (!permissionGranted.value) {
                Text(
                    text = "Permission for Recording Audio is required. Please grant it.",
                    style = TextStyle(color = Color.Red),
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                if (!isSpeechToTextProcessing) {
                    Button(onClick = startSpeechRecognition) {
                        Text("Start Speech to Text")
                    }
                } else {
                    Text(text = "Processing Speech...", modifier = Modifier.padding(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Speech Result: $speechResult",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Reversed Text: ${reversedText.value}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    outputAudioUri.value?.let {
                        Toast.makeText(context, "Audio saved at $it", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Save and Share Audio")
                }
            }
        }
    }
}

@Preview
@Composable
fun SpeechToTextScreenPreview() {
    SpeechToTextScreen()
}
