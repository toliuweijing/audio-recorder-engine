package com.innosage.android.audiorecorderengine

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class WavRecorderImpl(
    private val context: Context,
    private val recordingFileManager: RecordingFileManager
) : Recorder {

    private var audioRecord: AudioRecord? = null
    private var wavFileWriter: WavFileWriter? = null
    private var isRecordingAtomic = AtomicBoolean(false)
    private var currentOutputFile: File? = null
    private var onChunkSavedCallback: ((File) -> Unit)? = null
    private var onErrorCallback: ((Exception) -> Unit)? = null
    private var autoSaveHandler: Handler? = null
    private var autoSaveRunnable: Runnable? = null

    private val bufferSize = AudioRecord.getMinBufferSize(
        AudioConstants.SAMPLE_RATE_HZ,
        AudioConstants.CHANNEL_CONFIG,
        AudioConstants.AUDIO_FORMAT
    )

    override fun startRecording(
        outputFile: File,
        autoSaveEnabled: Boolean,
        chunkDurationMillis: Long,
        onChunkSaved: ((File) -> Unit)?,
        onError: ((Exception) -> Unit)?
    ) {
        if (isRecordingAtomic.get()) {
            onErrorCallback?.invoke(IllegalStateException("Already recording"))
            return
        }

        this.currentOutputFile = outputFile
        this.onChunkSavedCallback = onChunkSaved
        this.onErrorCallback = onError

        try {
            setupAudioRecord()
            setupWavFileWriter(outputFile)

            audioRecord?.startRecording()
            isRecordingAtomic.set(true)

            Thread {
                audioRecordingLoop()
            }.start()

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
        } catch (e: Exception) {
            onErrorCallback?.invoke(e)
            Toast.makeText(context, "Recording start error: ${e.message}", Toast.LENGTH_LONG).show()
            releaseResources()
        }
    }

    override fun stopRecording() {
        if (!isRecordingAtomic.get()) {
            return
        }

        autoSaveHandler?.removeCallbacksAndMessages(null)
        autoSaveHandler = null
        autoSaveRunnable = null

        isRecordingAtomic.set(false)
        releaseResources()
        currentOutputFile?.let { onChunkSavedCallback?.invoke(it) }
        currentOutputFile = null
    }

    override fun isRecording(): Boolean {
        return isRecordingAtomic.get()
    }

    private fun setupAudioRecord() {
        audioRecord = AudioRecord(
            android.media.MediaRecorder.AudioSource.MIC,
            AudioConstants.SAMPLE_RATE_HZ,
            AudioConstants.CHANNEL_CONFIG,
            AudioConstants.AUDIO_FORMAT,
            bufferSize
        )
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            throw IllegalStateException("AudioRecord initialization failed")
        }
    }

    private fun setupWavFileWriter(outputFile: File) {
        wavFileWriter = WavFileWriter(
            AudioConstants.SAMPLE_RATE_HZ,
            AudioConstants.CHANNEL_CONFIG_WAV, // Use 1 for mono channel for WAV
            AudioConstants.BITS_PER_SAMPLE_WAV
        )
        wavFileWriter?.startNewWavFile(outputFile)
    }

    private fun audioRecordingLoop() {
        val audioBuffer = ByteArray(bufferSize)
        while (isRecordingAtomic.get()) {
            val bytesRead = audioRecord?.read(audioBuffer, 0, bufferSize) ?: 0
            if (bytesRead > 0) {
                try {
                    wavFileWriter?.writePcmData(audioBuffer, 0, bytesRead)
                } catch (e: IOException) {
                    onErrorCallback?.invoke(e)
                    Toast.makeText(context, "Error writing WAV data: ${e.message}", Toast.LENGTH_LONG).show()
                    stopRecording()
                }
            }
        }
    }

    private fun switchRecordingChunk() {
        wavFileWriter?.closeWavFile()
        currentOutputFile?.let { onChunkSavedCallback?.invoke(it) }

        val newOutputFile = recordingFileManager.createRecordingFile(context, AudioConstants.AUDIO_FILE_EXTENSION_WAV)
        currentOutputFile = newOutputFile
        setupWavFileWriter(newOutputFile)
    }

    private fun releaseResources() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            wavFileWriter?.closeWavFile()
            wavFileWriter = null
        } catch (e: Exception) {
            onErrorCallback?.invoke(e)
            Toast.makeText(context, "Error releasing resources: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}