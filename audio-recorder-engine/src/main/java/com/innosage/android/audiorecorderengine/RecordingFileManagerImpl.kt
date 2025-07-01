package com.innosage.android.audiorecorderengine

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingFileManagerImpl : RecordingFileManager {

    override fun createRecordingFile(context: Context): File {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "${sdf.format(Date())}${AudioConstants.AUDIO_FILE_EXTENSION}"
        return File(context.cacheDir, fileName)
    }

    override fun getRecordingFiles(context: Context): List<File> {
        return context.cacheDir.listFiles()?.filter { it.name.endsWith(AudioConstants.AUDIO_FILE_EXTENSION) } ?: emptyList()
    }
}