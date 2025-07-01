package com.innosage.cmp.example.audiorecorderengine

import android.content.Context
import androidx.lifecycle.ViewModel
import com.innosage.android.audiorecorderengine.Recorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.*

class MainViewModel(
    private val recorder: Recorder,
    private val context: Context
) : ViewModel() {

    private val _recordings = MutableStateFlow<List<File>>(emptyList())
    val recordings = _recordings.asStateFlow()

    private var audioFile: File? = null

    fun startRecording() {
        val fileName = "audio_${Date().time}.mp4"
        audioFile = File(context.cacheDir, fileName)
        recorder.startRecording(audioFile!!)
    }

    fun stopRecording() {
        recorder.stopRecording()
        updateRecordings()
    }

    fun updateRecordings() {
        _recordings.value = recorder.getRecordings()
    }
}