package com.innosage.cmp.example.audiorecorderengine.recordingscreen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.innosage.android.audiorecorderengine.RecordingFileManager
import com.innosage.android.audiorecorderengine.RecordingFileManagerImpl

class RecordingListViewModelFactory(
    private val context: Context,
    private val recordingFileManager: RecordingFileManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordingListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordingListViewModel(recordingFileManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}