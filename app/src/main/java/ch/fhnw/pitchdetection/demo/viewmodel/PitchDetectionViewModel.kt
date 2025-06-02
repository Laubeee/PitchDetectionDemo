package ch.fhnw.pitchdetection.demo.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive


// Assuming these processor classes exist and are injectable or instantiable
// import com.example.yourproject.domain.AudioProcessor
// import com.example.yourproject.domain.BenchmarkProcessor
// import com.example.yourproject.domain.model.BenchmarkResult
class PitchDetectionViewModel(
    application: Application
    // You would typically inject these using Hilt or Koin:
    // private val audioProcessor: AudioProcessor,
    // private val benchmarkProcessor: BenchmarkProcessor
) : AndroidViewModel(application) {

    // Internal mutable state flows
    private val _statusText = MutableStateFlow("Status: Idle")
    private val _resultsText = MutableStateFlow("")
    private val _isRecording = MutableStateFlow(false)
    private val _detectedPitch = MutableStateFlow<Float?>(null) // Nullable if no pitch detected

    // Publicly exposed immutable state flows for the UI to observe
    val statusText: StateFlow<String> = _statusText.asStateFlow()
    val resultsText: StateFlow<String> = _resultsText.asStateFlow()
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    val detectedPitch: StateFlow<Float?> = _detectedPitch.asStateFlow()

    // Example instances (replace with actual injected or properly initialized instances)
    private val benchmarkProcessor = BenchmarkProcessorPlaceholder(application) // Placeholder
    private val audioProcessor = AudioProcessorPlaceholder(application)         // Placeholder

    fun runBenchmark(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _statusText.value = "Benchmarking ${uris.size} files..."
        _resultsText.value = "" // Clear previous results
        viewModelScope.launch(Dispatchers.IO) {
            val allResults = StringBuilder()
            uris.forEachIndexed { index, uri ->
                try {
                    // val result = benchmarkProcessor.processFile(uri) // Real call
                    val result = benchmarkProcessor.processFilePlaceholder(uri) // Placeholder call
                    withContext(Dispatchers.Main) { // Update UI progressively
                        _statusText.value = "Benchmarking file ${index + 1} of ${uris.size}..."
                        allResults.append(formatBenchmarkResult(result)).append("\n\n")
                        _resultsText.value = allResults.toString()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        allResults.append("Error processing ${uri.lastPathSegment}: ${e.message}\n\n")
                        _resultsText.value = allResults.toString()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                _statusText.value = "Benchmark of ${uris.size} files complete."
            }
        }
    }

    fun startLiveDetection() {
        if (_isRecording.value) return
        _statusText.value = "Starting live detection..."
        _detectedPitch.value = null // Clear previous pitch
        _resultsText.value = "" // Clear benchmark results if any

        viewModelScope.launch(Dispatchers.IO) {
            // audioProcessor.startRecording { pitch -> // Real call
            audioProcessor.startRecordingPlaceholder { pitch -> // Placeholder call
                _detectedPitch.value = pitch // This will be collected by the UI
            }
            // This part of the coroutine will continue after startRecording potentially sets up its own loop
            withContext(Dispatchers.Main) {
                _isRecording.value = true
                _statusText.value = "Status: Live Recording..."
            }
        }
    }

    fun stopLiveDetection() {
        if (!_isRecording.value) return
        audioProcessor.stopRecordingPlaceholder() // audioProcessor.stopRecording() // Real call
        _isRecording.value = false
        _statusText.value = "Status: Idle"
        // _detectedPitch.value = null // Optionally clear the last detected pitch
    }

    private fun formatBenchmarkResult(result: BenchmarkResultPlaceholder): String {
        return "File: ${result.fileName}\n" +
                "  Accuracy: ${result.accuracy?.let { "%.2f".format(it) } ?: "N/A"}\n" +
                "  F1 Score: ${result.f1Score?.let { "%.2f".format(it) } ?: "N/A"}\n" +
                "  Inference Time: ${result.inferenceTimeMs} ms"
    }

    override fun onCleared() {
        super.onCleared()
        // Release resources held by processors if necessary, e.g., if they manage TFLite interpreters directly
        // audioProcessor.release()
        // benchmarkProcessor.release()
        stopLiveDetection() // Ensure recording is stopped
    }
}

// --- Placeholder Data class and Processor classes for this example ---
// Replace these with your actual implementations
data class BenchmarkResultPlaceholder(
    val fileName: String,
    val accuracy: Double?,
    val f1Score: Double?,
    val inferenceTimeMs: Long
)

class BenchmarkProcessorPlaceholder(private val application: Application) {
    suspend fun processFilePlaceholder(uri: Uri): BenchmarkResultPlaceholder {
        delay(1000) // Simulate work
        return BenchmarkResultPlaceholder(
            fileName = uri.lastPathSegment ?: "Unknown File",
            accuracy = (80..95).random() / 100.0,
            f1Score = (75..90).random() / 100.0,
            inferenceTimeMs = (50..200).random().toLong()
        )
    }
    // fun release() { /* Release resources if any */ }
}

class AudioProcessorPlaceholder(private val application: Application) {
    private var isSimulatingRecording = false
    private var simulationJob: Job? = null

    fun startRecordingPlaceholder(onNewPitchDetected: (pitch: Float) -> Unit) {
        if (isSimulatingRecording) return
        isSimulatingRecording = true
        simulationJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && isSimulatingRecording) {
                delay(500) // Simulate audio chunk interval
                if (!isActive) break // check again, in case it was cancelled during processing

                val simulatedPitch = (60..80).random() + (0..99).random() / 100f // e.g., 65.34 Hz
                withContext(Dispatchers.Main) { // Callback on Main for UI updates
                    // Check isActive again before calling back, especially if onNewPitchDetected could be slow
                    if (isActive && isSimulatingRecording) {
                        onNewPitchDetected(simulatedPitch)
                    }
                }
            }
        }
    }

    fun stopRecordingPlaceholder() {
        if (!isSimulatingRecording) return
        isSimulatingRecording = false
        simulationJob?.cancel()
        simulationJob = null
    }
    // fun release() { /* Release resources if any */ }
}