package com.innosage.android.audiorecorderengine

import java.io.File
import java.io.FileOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavFileWriter(
    private val sampleRate: Int,
    private val channels: Int,
    private val bitsPerSample: Int
) {
    private var fileOutputStream: FileOutputStream? = null
    private var dataOutputStream: DataOutputStream? = null
    private var totalAudioLen: Long = 0
    private var totalDataLen: Long = 0
    private var headerWritten: Boolean = false

    @Throws(IOException::class)
    fun startNewWavFile(file: File) {
        fileOutputStream = FileOutputStream(file)
        dataOutputStream = DataOutputStream(fileOutputStream)
        totalAudioLen = 0
        totalDataLen = 0
        headerWritten = false
        writeWavHeader()
    }

    @Throws(IOException::class)
    fun writePcmData(buffer: ByteArray, offset: Int, size: Int) {
        if (dataOutputStream == null) {
            throw IllegalStateException("WavFileWriter not initialized. Call startNewWavFile first.")
        }
        dataOutputStream?.write(buffer, offset, size)
        totalAudioLen += size
        totalDataLen = totalAudioLen + 36 // 36 bytes for header before data chunk
    }

    @Throws(IOException::class)
    fun closeWavFile() {
        if (dataOutputStream == null) return

        dataOutputStream?.flush()
        updateWavHeader()
        dataOutputStream?.close()
        fileOutputStream?.close()
        dataOutputStream = null
        fileOutputStream = null
    }

    @Throws(IOException::class)
    private fun writeWavHeader() {
        val longSampleRate = sampleRate.toLong()
        val byteRate = (bitsPerSample / 8 * channels * sampleRate).toLong()
        val totalAudioLenPlusHeader = totalAudioLen + 36 // 36 bytes for header before data chunk

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF chunk
        header.putInt(0x46464952) // "RIFF"
        header.putInt(totalAudioLenPlusHeader.toInt()) // ChunkSize
        header.putInt(0x45564157) // "WAVE"

        // FMT chunk
        header.putInt(0x20746D66) // "fmt "
        header.putInt(16) // Subchunk1Size (16 for PCM)
        header.putShort(1) // AudioFormat (1 for PCM)
        header.putShort(channels.toShort()) // NumChannels
        header.putInt(longSampleRate.toInt()) // SampleRate
        header.putInt(byteRate.toInt()) // ByteRate
        header.putShort((channels * bitsPerSample / 8).toShort()) // BlockAlign
        header.putShort(bitsPerSample.toShort()) // BitsPerSample

        // DATA chunk
        header.putInt(0x61746164) // "data"
        header.putInt(totalAudioLen.toInt()) // Subchunk2Size

        dataOutputStream?.write(header.array())
        headerWritten = true
    }

    @Throws(IOException::class)
    private fun updateWavHeader() {
        if (fileOutputStream == null) return

        val finalTotalAudioLen = totalAudioLen
        val finalTotalDataLen = finalTotalAudioLen + 36

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF chunk
        header.putInt(0x46464952) // "RIFF"
        header.putInt(finalTotalDataLen.toInt()) // ChunkSize
        header.putInt(0x45564157) // "WAVE"

        // FMT chunk
        header.putInt(0x20746D66) // "fmt "
        header.putInt(16) // Subchunk1Size (16 for PCM)
        header.putShort(1) // AudioFormat (1 for PCM)
        header.putShort(channels.toShort()) // NumChannels
        header.putInt(sampleRate) // SampleRate
        header.putInt((bitsPerSample / 8 * channels * sampleRate)) // ByteRate
        header.putShort((channels * bitsPerSample / 8).toShort()) // BlockAlign
        header.putShort(bitsPerSample.toShort()) // BitsPerSample

        // DATA chunk
        header.putInt(0x61746164) // "data"
        header.putInt(finalTotalAudioLen.toInt()) // Subchunk2Size

        // Seek to the beginning of the file and rewrite the header
        fileOutputStream?.channel?.position(0)
        fileOutputStream?.write(header.array())
    }
}