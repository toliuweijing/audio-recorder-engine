package com.innosage.android.audiorecorderengine

import android.content.Context
import java.io.File

interface RecordingFileManager {
    fun createRecordingFile(context: Context, extension: String = AudioConstants.AUDIO_FILE_EXTENSION_M4A): File
    fun getRecordingFiles(context: Context): List<File>
}
