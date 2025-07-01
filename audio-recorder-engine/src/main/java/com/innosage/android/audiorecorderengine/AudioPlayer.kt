package com.innosage.android.audiorecorderengine

import java.io.File

interface AudioPlayer {
    fun play(file: File)
    fun stop()
    fun release()
}