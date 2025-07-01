package com.innosage.android.audiorecorderengine

import android.content.Context
import java.io.File

interface RecordingFileManager {
    fun createRecordingFile(context: Context): File
    fun getRecordingFiles(context: Context): List<File>
}