package com.innosage.cmp.example.audiorecorderengine.recorderscreen

import android.content.Context
import androidx.lifecycle.ViewModel
import com.innosage.android.audiorecorderengine.AudioConstants
import com.innosage.android.audiorecorderengine.Recorder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecorderScreenViewModel(
    private val recorder: Recorder,
    private val context: Context
) : ViewModel() {

    private var audioFile: File? = null

    fun startRecording() {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "${sdf.format(Date())}${AudioConstants.AUDIO_FILE_EXTENSION}"
        audioFile = File(context.cacheDir, fileName)
        recorder.startRecording(audioFile!!)
    }

    fun stopRecording() {
        recorder.stopRecording()
    }
}