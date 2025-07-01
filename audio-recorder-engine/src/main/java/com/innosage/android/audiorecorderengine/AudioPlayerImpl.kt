package com.innosage.android.audiorecorderengine

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File

class AudioPlayerImpl(private val context: Context) : AudioPlayer {

//    private var mediaPlayer: MediaPlayer? = null
    var mediaPlayer: MediaPlayer? = null

    override fun play(file: File) {
        MediaPlayer.create(context, file.toUri()).apply {
            mediaPlayer = this
            start()
        }
    }

    override fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
