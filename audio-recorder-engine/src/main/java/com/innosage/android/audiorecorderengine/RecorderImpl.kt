package com.innosage.android.audiorecorderengine

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class RecorderImpl(
    private val recorder: MediaRecorder,
    private val context: Context
) : Recorder {

    override fun startRecording(outputFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(outputFile)
        } else {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(FileOutputStream(outputFile).fd)
        }
        recorder.prepare()
        recorder.start()
    }

    override fun stopRecording() {
        recorder.stop()
        recorder.reset()
    }

    override fun getRecordings(): List<File> {
        return context.cacheDir.listFiles()?.filter { it.name.endsWith(".mp4") } ?: emptyList()
    }
}