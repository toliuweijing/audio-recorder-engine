package com.innosage.android.audiorecorderengine

import android.content.Context
import android.media.AudioRecord
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class WavRecorderImpl(
    private val context: Context,
    private val recordingFileManager: RecordingFileManager
) : Recorder {

    private var audioRecord: AudioRecord? = null
    private var wavFileWriter: WavFileWriter? = null
    private val isRecordingAtomic = AtomicBoolean(false)
    private var currentOutputFile: File? = null
    private var onChunkSavedCallback: ((File) -> Unit)? = null
    private var onErrorCallback: ((Exception) -> Unit)? = null

    private var recorderScope: CoroutineScope? = null
    private val audioEventFlow = MutableSharedFlow<AudioEvent>(replay = 1)

    private var autoSaveHandler: Handler? = null
    private var autoSaveRunnable: Runnable? = null

    private val bufferSize by lazy {
        AudioRecord.getMinBufferSize(
            AudioConstants.SAMPLE_RATE_HZ,
            AudioConstants.CHANNEL_CONFIG,
            AudioConstants.AUDIO_FORMAT
        )
    }

    override fun startRecording(
        outputFile: File,
        autoSaveEnabled: Boolean,
        chunkDurationMillis: Long,
        onChunkSaved: ((File) -> Unit)?,
        onError: ((Exception) -> Unit)?
    ) {
        if (isRecordingAtomic.getAndSet(true)) {
            onErrorCallback?.invoke(IllegalStateException("Already recording"))
            return
        }

        this.currentOutputFile = outputFile
        this.onChunkSavedCallback = onChunkSaved
        this.onErrorCallback = onError

        recorderScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        try {
            setupAudioRecord()
            setupWavFileWriter(outputFile)

            recorderScope?.launch { writerLoop() }
            recorderScope?.launch { audioRecordingLoop() }

            if (autoSaveEnabled) {
                autoSaveHandler = Handler(Looper.getMainLooper())
                autoSaveRunnable = object : Runnable {
                    override fun run() {
                        recorderScope?.launch {
                            audioEventFlow.emit(AudioEvent.SwitchChunk)
                        }
                        autoSaveHandler?.postDelayed(this, chunkDurationMillis)
                    }
                }
                autoSaveHandler?.postDelayed(autoSaveRunnable!!, chunkDurationMillis)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override fun stopRecording() {
        if (!isRecordingAtomic.getAndSet(false)) {
            return
        }
        autoSaveHandler?.removeCallbacksAndMessages(null)
        autoSaveHandler = null
        autoSaveRunnable = null

        recorderScope?.cancel() // Cancels all coroutines in the scope
    }

    override fun isRecording(): Boolean {
        return isRecordingAtomic.get()
    }

    private suspend fun writerLoop() = withContext(Dispatchers.IO) {
        audioEventFlow.collectLatest { event ->
            when (event) {
                is AudioEvent.PcmData -> {
                    try {
                        wavFileWriter?.writePcmData(event.data, 0, event.size)
                    } catch (e: IOException) {
                        handleError(e)
                    }
                }
                is AudioEvent.SwitchChunk -> {
                    switchRecordingChunk()
                }
            }
        }
    }

    private suspend fun audioRecordingLoop() = withContext(Dispatchers.IO) {
        audioRecord?.startRecording()
        val audioBuffer = ByteArray(bufferSize)

        while (isActive && isRecordingAtomic.get()) {
            val bytesRead = audioRecord?.read(audioBuffer, 0, bufferSize) ?: 0
            if (bytesRead > 0) {
                // Emit a copy of the buffer to avoid race conditions with the next read
                audioEventFlow.emit(AudioEvent.PcmData(audioBuffer.clone(), bytesRead))
            }
        }
        // Final cleanup after loop finishes
        releaseResources()
        currentOutputFile?.let { onChunkSavedCallback?.invoke(it) }
        currentOutputFile = null
    }

    private fun switchRecordingChunk() {
        try {
            wavFileWriter?.closeWavFile()
            currentOutputFile?.let { onChunkSavedCallback?.invoke(it) }

            val newOutputFile = recordingFileManager.createRecordingFile(context, AudioConstants.AUDIO_FILE_EXTENSION_WAV)
            currentOutputFile = newOutputFile
            setupWavFileWriter(newOutputFile)
        } catch (e: IOException) {
            handleError(e)
        }
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
        val numChannels = if (AudioConstants.CHANNEL_CONFIG == android.media.AudioFormat.CHANNEL_IN_MONO) 1 else 2
        val bitsPerSample = if (AudioConstants.AUDIO_FORMAT == android.media.AudioFormat.ENCODING_PCM_16BIT) 16 else 8

        wavFileWriter = WavFileWriter(
            sampleRate = AudioConstants.SAMPLE_RATE_HZ,
            channels = numChannels,
            bitsPerSample = bitsPerSample
        )
        wavFileWriter?.startNewWavFile(outputFile)
    }

    private fun releaseResources() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            wavFileWriter?.closeWavFile()
            wavFileWriter = null
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun handleError(e: Exception) {
        // Switch to main thread to safely call the error callback
        MainScope().launch {
            onErrorCallback?.invoke(e)
        }
        stopRecording()
    }
}