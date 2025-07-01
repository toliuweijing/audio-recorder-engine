package com.innosage.cmp.example.audiorecorderengine.recordingscreen

import android.content.Context
import androidx.lifecycle.ViewModel
import com.innosage.android.audiorecorderengine.RecordingFileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class RecordingListViewModel(
    private val recordingFileManager: RecordingFileManager,
    private val context: Context
) : ViewModel() {

    private val _recordings = MutableStateFlow<List<File>>(emptyList())
    val recordings = _recordings.asStateFlow()

    fun updateRecordings() {
        _recordings.value = recordingFileManager.getRecordingFiles(context)
    }
}