package com.innosage.cmp.example.audiorecorderengine.recordingscreen

import androidx.lifecycle.ViewModel
import com.innosage.android.audiorecorderengine.Recorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class RecordingListViewModel(
    private val recorder: Recorder
) : ViewModel() {

    private val _recordings = MutableStateFlow<List<File>>(emptyList())
    val recordings = _recordings.asStateFlow()

    fun updateRecordings() {
        _recordings.value = recorder.getRecordings()
    }
}