package com.innosage.cmp.example.audiorecorderengine.recordingscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.innosage.cmp.example.audiorecorderengine.recordingscreen.AudioRecordingItem

@Composable
fun RecordingListScreen(viewModel: RecordingListViewModel) {
    val recordings by viewModel.recordings.collectAsState()

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(recordings) { recordingItem ->
            val currentlyPlayingFile by viewModel.currentlyPlayingFile.collectAsState()
            val isPlaying = currentlyPlayingFile == recordingItem

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { /* Handle item click if needed */ },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recordingItem.file.name,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${recordingItem.duration} | ${recordingItem.fileSize}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                viewModel.stopPlaying()
                            } else {
                                viewModel.playRecording(recordingItem)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Stop Playback" else "Start Playback"
                        )
                    }
                }
            }
        }
    }
}