package com.innosage.cmp.example.audiorecorderengine

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.innosage.android.audiorecorderengine.AudioPlayerImpl
import com.innosage.android.audiorecorderengine.RecordingFileManagerImpl
import com.innosage.cmp.example.audiorecorderengine.recordingscreen.RecordingListScreen
import com.innosage.cmp.example.audiorecorderengine.recordingscreen.RecordingListViewModel
import com.innosage.cmp.example.audiorecorderengine.recordingscreen.RecordingListViewModelFactory
import com.innosage.cmp.example.audiorecorderengine.recorderscreen.RecorderScreen
import com.innosage.cmp.example.audiorecorderengine.recorderscreen.RecorderScreenViewModel
import com.innosage.cmp.example.audiorecorderengine.recorderscreen.RecorderScreenViewModelFactory
import com.innosage.cmp.example.audiorecorderengine.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            0
        )
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var tabIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val recordingFileManager = RecordingFileManagerImpl()
    val recordingListViewModel: RecordingListViewModel =
        viewModel(
            factory = RecordingListViewModelFactory(
                recordingFileManager = recordingFileManager,
                audioPlayer = AudioPlayerImpl(context),
                context = context,
            )
        )
    val recorderScreenViewModel: RecorderScreenViewModel =
        viewModel(factory = RecorderScreenViewModelFactory(context, recordingFileManager))

    val tabs = listOf(
        TabItem("Recorder", Icons.Default.Mic) { RecorderScreen(recorderScreenViewModel) },
        TabItem("Recordings", Icons.Default.List) { RecordingListScreen(recordingListViewModel) }
    )

    Scaffold(
        bottomBar = {
            BottomAppBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = tabIndex == index,
                        onClick = {
                            tabIndex = index
                            if (index == 1) {
                                recordingListViewModel.updateRecordings()
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            tabs[tabIndex].screen()
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector, val screen: @Composable () -> Unit)
