package com.innosage.android.audiorecorderengine

import java.io.File

interface Recorder {
    fun startRecording(outputFile: File)
    fun stopRecording()
}