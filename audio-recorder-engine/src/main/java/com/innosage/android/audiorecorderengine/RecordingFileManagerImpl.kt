package com.innosage.android.audiorecorderengine

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingFileManagerImpl : RecordingFileManager {

    override fun createRecordingFile(context: Context, extension: String): File {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val existingChunks = context.cacheDir.listFiles()?.filter {
            it.name.startsWith(timestamp) && it.name.endsWith(extension)
        }?.size ?: 0
        val fileName = "${timestamp}_chunk${existingChunks + 1}${extension}"
        return File(context.cacheDir, fileName)
    }

    override fun getRecordingFiles(context: Context): List<File> {
        return context.cacheDir.listFiles()?.filter { it.name.endsWith(AudioConstants.AUDIO_FILE_EXTENSION_M4A) } ?: emptyList()
    }
}
