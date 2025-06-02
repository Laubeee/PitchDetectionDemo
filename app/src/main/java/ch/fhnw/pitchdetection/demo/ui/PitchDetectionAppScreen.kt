package ch.fhnw.pitchdetection.demo.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel // Import this
import ch.fhnw.pitchdetection.demo.viewmodel.PitchDetectionViewModel

// This file contains...
// State variables for UI (e.g., statusText, resultsText, isRecording)
// Permission launchers (storage, audio)
// File picker launchers

// UI Layout (Buttons, Display areas for status and results)
// Observe ViewModel LiveData/StateFlow for status and results updates

@Composable
fun PitchDetectionAppScreen(viewModel: PitchDetectionViewModel = viewModel()) {
    val context = LocalContext.current
    var statusText by remember { mutableStateOf("Status: Idle") }
    val resultsTextFromViewModel by viewModel.resultsText.collectAsState() // Observe results here

    val requestAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                statusText = "Audio permission granted. Starting live detection..."
            } else {
                statusText = "Audio permission denied."
                Toast.makeText(context, "Audio permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun checkAndRequestAudioPermission(onPermissionGranted: () -> Unit) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)) {
            PackageManager.PERMISSION_GRANTED -> {
                onPermissionGranted()
            }

            else -> {
                // You could show a rationale dialog here before launching if
                // ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.READ_MEDIA_AUDIO)
                Toast.makeText(context, "Audio permission denied", Toast.LENGTH_SHORT).show()
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val requestStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                statusText = "Storage permission granted. Opening file picker..."
            } else {
                statusText = "Storage permission denied."
                Toast.makeText(context, "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun checkAndRequestStoragePermission(onPermissionGranted: () -> Unit) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO)) {
            PackageManager.PERMISSION_GRANTED -> {
                onPermissionGranted()
            }
            else -> {
                // You could show a rationale dialog here before launching if
                // ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.READ_MEDIA_AUDIO)
                Toast.makeText(context, "Audio permission denied", Toast.LENGTH_SHORT).show()
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }
    }

    // Launcher for picking (multiple) audio files
    val pickAudioFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),   // ActivityResultContracts.GetContent() for single file -> onResult = { uri: Uri -> ...
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                statusText = "${uris.size} files selected."
                viewModel.runBenchmark(uris)
            } else {
                statusText = "No files selected."
            }
        }
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = { // Check and request storage permission
            checkAndRequestStoragePermission { pickAudioFilesLauncher.launch(arrayOf("audio/*")) }   // Pass mime type as an array
        }) {
            Text("Benchmark Files")
        }

        Button(onClick = { // Check and request audio permission
            checkAndRequestAudioPermission { viewModel.startLiveDetection() }
        }) {
            Text("Live Detection")
        }

        Text(text = statusText, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = resultsTextFromViewModel,
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
        )
    }
}