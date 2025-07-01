package com.innosage.cmp.example.audiorecorderengine.recorderscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RecorderScreen(viewModel: RecorderScreenViewModel) {
    var isRecording by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            if (isRecording) {
                viewModel.stopRecording()
            } else {
                viewModel.startRecording()
            }
            isRecording = !isRecording
        }) {
            Text(text = if (isRecording) "Stop Recording" else "Start Recording")
        }
    }
}