package com.innosage.android.audiorecorderengine

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RecorderImpl(
    private var recorder: MediaRecorder?,
    private val context: Context,
    private val recordingFileManager: RecordingFileManager
) : Recorder {

    private var _isRecording: Boolean = false
    private var currentOutputFile: File? = null
    private var autoSaveHandler: Handler? = null
    private var autoSaveRunnable: Runnable? = null
    private var onChunkSavedCallback: ((File) -> Unit)? = null
    private var onErrorCallback: ((Exception) -> Unit)? = null

    override fun startRecording(
        outputFile: File,
        autoSaveEnabled: Boolean,
        chunkDurationMillis: Long,
        onChunkSaved: ((File) -> Unit)?,
        onError: ((Exception) -> Unit)?
    ) {
        this.currentOutputFile = outputFile
        this.onChunkSavedCallback = onChunkSaved
        this.onErrorCallback = onError
        _isRecording = true

        startMediaRecorder(outputFile)

        if (autoSaveEnabled) {
            autoSaveHandler = Handler(Looper.getMainLooper())
            autoSaveRunnable = object : Runnable {
                override fun run() {
                    try {
                        switchRecordingChunk()
                        autoSaveHandler?.postDelayed(this, chunkDurationMillis)
                    } catch (e: Exception) {
                        onErrorCallback?.invoke(e)
                        Toast.makeText(context, "Recording auto-save error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            autoSaveHandler?.postDelayed(autoSaveRunnable!!, chunkDurationMillis)
        }
    }

    override fun stopRecording() {
        autoSaveHandler?.removeCallbacksAndMessages(null)
        autoSaveHandler = null
        autoSaveRunnable = null
        stopMediaRecorder()
        currentOutputFile = null
        _isRecording = false
    }

    override fun isRecording(): Boolean {
        return _isRecording
    }

    private fun startMediaRecorder(outputFile: File) {
        try {
            recorder?.apply {
                reset()
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setOutputFile(outputFile)
                } else {
                    setOutputFile(FileOutputStream(outputFile).fd)
                }
                prepare()
                start()
            }
        } catch (e: IOException) {
            onErrorCallback?.invoke(e)
            Toast.makeText(context, "Recording start error: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: IllegalStateException) {
            onErrorCallback?.invoke(e)
            Toast.makeText(context, "Recording start error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopMediaRecorder() {
        try {
            recorder?.stop()
            recorder?.reset()
            currentOutputFile?.let { onChunkSavedCallback?.invoke(it) }
        } catch (e: IllegalStateException) {
            onErrorCallback?.invoke(e)
            Toast.makeText(context, "Recording stop error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun switchRecordingChunk() {
        stopMediaRecorder()
        val newOutputFile = recordingFileManager.createRecordingFile(context)
        currentOutputFile = newOutputFile
        startMediaRecorder(newOutputFile)
    }
}
