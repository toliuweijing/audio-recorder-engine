package com.innosage.cmp.example.audiorecorderengine.recordingscreen

import java.io.File

data class AudioRecordingItem(
    val file: File,
    val duration: String,
    val fileSize: String
)