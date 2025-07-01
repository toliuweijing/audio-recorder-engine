package com.innosage.cmp.example.audiorecorderengine.recorderscreen

import android.content.Context
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.innosage.android.audiorecorderengine.RecorderImpl
import com.innosage.android.audiorecorderengine.RecordingFileManager
import com.innosage.android.audiorecorderengine.RecordingFileManagerImpl

class RecorderScreenViewModelFactory(
    private val context: Context,
    private val recordingFileManager: RecordingFileManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecorderScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecorderScreenViewModel(
                RecorderImpl(MediaRecorder(), context, recordingFileManager),
                recordingFileManager,
                context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
