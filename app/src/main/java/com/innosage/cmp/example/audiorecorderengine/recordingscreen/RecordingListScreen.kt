package com.innosage.cmp.example.audiorecorderengine.recordingscreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecordingListScreen(viewModel: RecordingListViewModel) {
    val recordings by viewModel.recordings.collectAsState()

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(recordings) { recording ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = recording.name,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}