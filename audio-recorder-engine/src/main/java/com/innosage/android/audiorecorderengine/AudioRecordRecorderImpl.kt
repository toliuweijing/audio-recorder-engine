package com.innosage.android.audiorecorderengine

import android.content.Context
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class AudioRecordRecorderImpl(
    private val context: Context,
    private val recordingFileManager: RecordingFileManager
) : Recorder {

    private var audioRecord: AudioRecord? = null
    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private var audioTrackIndex = -1
    private var isRecordingAtomic = AtomicBoolean(false)
    private var currentOutputFile: File? = null
    private var onChunkSavedCallback: ((File) -> Unit)? = null
    private var onErrorCallback: ((Exception) -> Unit)? = null
    private var autoSaveHandler: Handler? = null
    private var autoSaveRunnable: Runnable? = null
    private var presentationTimeUs: Long = 0

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
        presentationTimeUs = 0

        try {
            setupAudioRecord()
            setupMediaCodec()
            setupMediaMuxer(outputFile)

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

    private fun setupMediaCodec() {
        val format = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            AudioConstants.SAMPLE_RATE_HZ,
            1 // mono
        )
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        format.setInteger(MediaFormat.KEY_BIT_RATE, AudioConstants.AUDIO_BIT_RATE)

        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec?.start()
    }

    private fun setupMediaMuxer(outputFile: File) {
        mediaMuxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        audioTrackIndex = -1 // Reset track index
    }

    private fun audioRecordingLoop() {
        val audioBuffer = ByteBuffer.allocateDirect(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()

        while (isRecordingAtomic.get()) {
            val bytesRead = audioRecord?.read(audioBuffer, bufferSize) ?: 0
            if (bytesRead <= 0) continue

            val inputBufferIndex = mediaCodec?.dequeueInputBuffer(-1) ?: -1
            if (inputBufferIndex >= 0) {
                val inputBuffer = mediaCodec?.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                inputBuffer?.put(audioBuffer)
                mediaCodec?.queueInputBuffer(inputBufferIndex, 0, bytesRead, presentationTimeUs, 0)
                presentationTimeUs += (bytesRead * 1_000_000L) / (AudioConstants.SAMPLE_RATE_HZ * 2) // 2 bytes per sample for 16-bit PCM
            }

            var outputBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
            while (outputBufferIndex >= 0) {
                val outputBuffer = mediaCodec?.getOutputBuffer(outputBufferIndex)
                if (outputBuffer != null && bufferInfo.size > 0) {
                    if (audioTrackIndex == -1) {
                        audioTrackIndex = mediaMuxer?.addTrack(mediaCodec!!.outputFormat) ?: -1
                        mediaMuxer?.start()
                    }
                    mediaMuxer?.writeSampleData(audioTrackIndex, outputBuffer, bufferInfo)
                }
                mediaCodec?.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
            }
        }
        drainEncoder(true)
    }

    private fun drainEncoder(endOfStream: Boolean) {
        val bufferInfo = MediaCodec.BufferInfo()
        if (endOfStream) {
            mediaCodec?.signalEndOfInputStream()
        }

        while (true) {
            val outputBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break
                } else {
                    // continue to drain
                }
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (audioTrackIndex == -1) {
                    audioTrackIndex = mediaMuxer?.addTrack(mediaCodec!!.outputFormat) ?: -1
                    mediaMuxer?.start()
                }
            } else if (outputBufferIndex < 0) {
                // ignore
            } else {
                val outputBuffer = mediaCodec?.getOutputBuffer(outputBufferIndex)
                if (outputBuffer == null) {
                    throw RuntimeException("codec.getOutputBuffer() returned null!")
                }
                if (bufferInfo.size != 0) {
                    mediaMuxer?.writeSampleData(audioTrackIndex, outputBuffer, bufferInfo)
                }
                mediaCodec?.releaseOutputBuffer(outputBufferIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }

    private fun switchRecordingChunk() {
        drainEncoder(true) // Ensure all pending data is written
        stopMediaMuxer()
        currentOutputFile?.let { onChunkSavedCallback?.invoke(it) }

        val newOutputFile = recordingFileManager.createRecordingFile(context, AudioConstants.AUDIO_FILE_EXTENSION_M4A)
        currentOutputFile = newOutputFile
        setupMediaMuxer(newOutputFile)
        presentationTimeUs = 0 // Reset presentation time for new chunk
    }

    private fun releaseResources() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            mediaCodec?.stop()
            mediaCodec?.release()
            mediaCodec = null

            stopMediaMuxer()
        } catch (e: Exception) {
            onErrorCallback?.invoke(e)
            Toast.makeText(context, "Error releasing resources: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopMediaMuxer() {
        try {
            mediaMuxer?.stop()
            mediaMuxer?.release()
            mediaMuxer = null
        } catch (e: Exception) {
            onErrorCallback?.invoke(e)
            Toast.makeText(context, "Error stopping MediaMuxer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
