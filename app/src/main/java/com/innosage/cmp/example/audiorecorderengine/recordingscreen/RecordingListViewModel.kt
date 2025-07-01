package com.innosage.cmp.example.audiorecorderengine.recordingscreen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.innosage.android.audiorecorderengine.AudioPlayer
import com.innosage.android.audiorecorderengine.AudioPlayerImpl
import com.innosage.android.audiorecorderengine.RecordingFileManager
import com.innosage.cmp.example.audiorecorderengine.utils.AudioFileUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class RecordingListViewModel(
    private val recordingFileManager: RecordingFileManager,
    private val audioPlayer: AudioPlayer,
    private val context: Context
) : ViewModel() {

    private val _recordings = MutableStateFlow<List<AudioRecordingItem>>(emptyList())
    val recordings = _recordings.asStateFlow()

    private val _currentlyPlayingFile = MutableStateFlow<AudioRecordingItem?>(null)
    val currentlyPlayingFile = _currentlyPlayingFile.asStateFlow()

    init {
        // Set up a listener for when the media player finishes
        if (audioPlayer is AudioPlayerImpl) { // Assuming AudioPlayerImpl has access to MediaPlayer
            audioPlayer.mediaPlayer?.setOnCompletionListener {
                _currentlyPlayingFile.value = null
            }
        }
    }

    fun updateRecordings() {
        _recordings.value = recordingFileManager.getRecordingFiles(context).map { file ->
            AudioRecordingItem(
                file = file,
                duration = AudioFileUtil.getAudioDuration(file),
                fileSize = AudioFileUtil.getFileSize(file)
            )
        }
    }

    fun playRecording(recordingItem: AudioRecordingItem) {
        viewModelScope.launch {
            audioPlayer.play(recordingItem.file)
            _currentlyPlayingFile.value = recordingItem
        }
    }

    fun stopPlaying() {
        viewModelScope.launch {
            audioPlayer.stop()
            _currentlyPlayingFile.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
