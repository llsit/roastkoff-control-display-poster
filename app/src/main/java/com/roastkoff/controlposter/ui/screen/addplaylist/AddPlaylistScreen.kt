package com.roastkoff.controlposter.ui.screen.addplaylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.roastkoff.controlposter.common.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistScreen(
    groupId: String,
    displayId: String,
    onNavigateBack: () -> Unit,
    viewModel: AddPlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var playlistName by remember { mutableStateOf("") }
    var defaultIntervalMs by remember { mutableStateOf("5000") }
    var loop by remember { mutableStateOf(true) }
    var shuffle by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var intervalError by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.NavigateBack -> {
                        onNavigateBack()
                        viewModel.resetState()
                    }

                    is UiEvent.ShowSnackbar -> {}
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("สร้าง Playlist ใหม่") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "ข้อมูล Playlist",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = {
                            playlistName = it
                            nameError = it.isBlank()
                        },
                        label = { Text("ชื่อ Playlist") },
                        placeholder = { Text("เช่น Summer Sale 2024") },
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("กรุณาใส่ชื่อ Playlist") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = uiState !is AddPlaylistUiState.Loading
                    )

                    OutlinedTextField(
                        value = defaultIntervalMs,
                        onValueChange = {
                            defaultIntervalMs = it
                            intervalError = it.toIntOrNull() == null || it.toInt() < 1000
                        },
                        label = { Text("ระยะเวลาแสดงต่อภาพ (ms)") },
                        placeholder = { Text("5000") },
                        isError = intervalError,
                        supportingText = if (intervalError) {
                            { Text("กรุณาใส่ตัวเลขมากกว่า 1000") }
                        } else {
                            { Text("${(defaultIntervalMs.toIntOrNull() ?: 0) / 1000} วินาที") }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = uiState !is AddPlaylistUiState.Loading
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "การตั้งค่า",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "เล่นวนซ้ำ (Loop)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "เล่นภาพวนไปเรื่อยๆ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = loop,
                            onCheckedChange = { loop = it },
                            enabled = uiState !is AddPlaylistUiState.Loading
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "สุ่มลำดับ (Shuffle)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "แสดงภาพแบบสุ่ม",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = shuffle,
                            onCheckedChange = { shuffle = it },
                            enabled = uiState !is AddPlaylistUiState.Loading
                        )
                    }
                }
            }

            if (uiState is AddPlaylistUiState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        (uiState as AddPlaylistUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is AddPlaylistUiState.Loading
                ) {
                    Text("ยกเลิก")
                }

                Button(
                    onClick = {
                        if (playlistName.isNotBlank() &&
                            defaultIntervalMs.toIntOrNull() != null &&
                            defaultIntervalMs.toInt() >= 1000
                        ) {
                            viewModel.createPlaylist(
                                groupId = groupId,
                                displayId = displayId,
                                formData = PlaylistFormData(
                                    name = playlistName,
                                    loop = loop,
                                    shuffle = shuffle,
                                    defaultIntervalMs = defaultIntervalMs.toInt()
                                )
                            )
                        } else {
                            nameError = playlistName.isBlank()
                            intervalError = defaultIntervalMs.toIntOrNull() == null ||
                                    defaultIntervalMs.toInt() < 1000
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is AddPlaylistUiState.Loading
                ) {
                    if (uiState is AddPlaylistUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("สร้าง Playlist")
                }
            }
        }
    }
}

data class PlaylistFormData(
    val name: String,
    val loop: Boolean,
    val shuffle: Boolean,
    val defaultIntervalMs: Int
)