package com.innosage.cmp.example.audiorecorderengine.recorderscreen

import android.content.Context
import androidx.lifecycle.ViewModel
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
        recorder.startRecording(audioFile!!)
    }

    fun stopRecording() {
        recorder.stopRecording()
    }
}