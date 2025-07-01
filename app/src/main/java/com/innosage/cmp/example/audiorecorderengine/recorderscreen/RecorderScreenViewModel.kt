package com.innosage.cmp.example.audiorecorderengine.recorderscreen

import android.content.Context
import androidx.lifecycle.ViewModel
import android.widget.Toast // Added for error toasts
import com.innosage.android.audiorecorderengine.AudioConstants
import com.innosage.android.audiorecorderengine.Recorder
import com.innosage.android.audiorecorderengine.RecordingFileManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecorderScreenViewModel(
    private val recorder: Recorder,
    private val recordingFileManager: RecordingFileManager,
    private val context: Context
) : ViewModel() {

    private var audioFile: File? = null

    fun startRecording() {
        audioFile = recordingFileManager.createRecordingFile(context)
        recorder.startRecording(
            outputFile = audioFile!!,
            autoSaveEnabled = true, // Default to enabled as per user's request
            onChunkSaved = { file ->
                // Handle chunk saved, e.g., update UI or log
                // For now, no specific UI feedback is required
            },
            onError = { exception ->
                Toast.makeText(context, "Recording error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    fun stopRecording() {
        recorder.stopRecording()
    }
}