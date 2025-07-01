package com.innosage.cmp.example.audiorecorderengine.utils

import android.media.MediaMetadataRetriever
import java.io.File
import java.util.concurrent.TimeUnit

object AudioFileUtil {

    fun getAudioDuration(file: File): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val milliseconds = durationString?.toLongOrNull() ?: 0

        retriever.release()

        return formatDuration(milliseconds)
    }

    fun getFileSize(file: File): String {
        val bytes = file.length()
        return formatFileSize(bytes)
    }

    private fun formatDuration(milliseconds: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}