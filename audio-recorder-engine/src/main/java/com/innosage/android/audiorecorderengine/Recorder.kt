package com.innosage.android.audiorecorderengine

import java.io.File

interface Recorder {
    fun startRecording(
        outputFile: File,
        autoSaveEnabled: Boolean = false,
        chunkDurationMillis: Long = AudioConstants.DEFAULT_CHUNK_DURATION_MILLIS,
        onChunkSaved: ((File) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    )
    fun stopRecording()
    fun isRecording(): Boolean
}
