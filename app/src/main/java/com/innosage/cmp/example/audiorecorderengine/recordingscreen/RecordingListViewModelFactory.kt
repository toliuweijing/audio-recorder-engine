package com.innosage.cmp.example.audiorecorderengine.recordingscreen

import android.content.Context
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.innosage.android.audiorecorderengine.RecorderImpl

class RecordingListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(com.innosage.cmp.example.audiorecorderengine.recordingscreen.RecordingListViewModel::class.java)) {
            val recorder = RecorderImpl(MediaRecorder(context), context)
            return com.innosage.cmp.example.audiorecorderengine.recordingscreen.RecordingListViewModel(recorder) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}