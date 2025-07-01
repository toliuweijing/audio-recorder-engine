package com.innosage.cmp.example.audiorecorderengine

import android.content.Context
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.innosage.android.audiorecorderengine.RecorderImpl

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val recorder = RecorderImpl(MediaRecorder(context), context)
            return MainViewModel(recorder, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}