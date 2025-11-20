package com.roastkoff.controlposter.ui.screen.addItems

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.roastkoff.controlposter.common.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistItemScreen(
    playlistId: String,
    onNavigateBack: () -> Unit,
    viewModel: AddPlaylistItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var itemName by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("image") }
    var durationMs by remember { mutableStateOf("5000") }
    var fit by remember { mutableStateOf("cover") }
    var mute by remember { mutableStateOf(true) }

    var nameError by remember { mutableStateOf(false) }
    var durationError by remember { mutableStateOf(false) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedMediaUri = uri
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.NavigateBack -> {
                        onNavigateBack()
                    }

                    is UiEvent.ShowSnackbar -> {}
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("การเพิ่ม item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.Close, "Close")
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "ชื่อ item",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = itemName,
                        onValueChange = {
                            itemName = it
                            nameError = it.isBlank()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ใส่ชื่อ item") },
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("กรุณาใส่ชื่อ item") }
                        } else null,
                        singleLine = true,
                        enabled = uiState !is AddPlaylistItemUiState.Uploading
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "ประเภท",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = itemType == "image",
                                    onClick = { itemType = "image" },
                                    role = Role.RadioButton,
                                    enabled = uiState !is AddPlaylistItemUiState.Uploading
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = itemType == "image",
                                onClick = null,
                                enabled = uiState !is AddPlaylistItemUiState.Uploading
                            )
                            Text(
                                "รูปภาพ (Image)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = itemType == "video",
                                    onClick = { itemType = "video" },
                                    role = Role.RadioButton,
                                    enabled = uiState !is AddPlaylistItemUiState.Uploading
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = itemType == "video",
                                onClick = null,
                                enabled = uiState !is AddPlaylistItemUiState.Uploading
                            )
                            Text(
                                "วิดีโอ (Video)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "ระยะเวลาแสดง",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = durationMs,
                        onValueChange = {
                            durationMs = it
                            durationError = it.toIntOrNull() == null || it.toInt() < 1000
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("ระยะเวลา (ms)") },
                        placeholder = { Text("5000") },
                        isError = durationError,
                        supportingText = if (durationError) {
                            { Text("กรุณาใส่ตัวเลขมากกว่า 1000") }
                        } else {
                            { Text("${(durationMs.toIntOrNull() ?: 0) / 1000} วินาที") }
                        },
                        singleLine = true,
                        enabled = uiState !is AddPlaylistItemUiState.Uploading
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "เลือกไฟล์",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedButton(
                        onClick = {
                            val mimeType = if (itemType == "image") "image/*" else "video/*"
                            mediaPickerLauncher.launch(mimeType)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is AddPlaylistItemUiState.Uploading
                    ) {
                        Text(if (itemType == "image") "เลือกรูปภาพ" else "เลือกวิดีโอ")
                    }

                    selectedMediaUri?.let { uri ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "✓ ${uri.lastPathSegment ?: "ไฟล์ที่เลือก"}",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        "ขนาดไฟล์สูงสุด: 5 MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "รูปแบบการแสดงผล",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "cover" to "เต็มจอ (Cover)",
                            "contain" to "พอดี (Contain)",
                            "fill" to "ยืด (Fill)"
                        ).forEach { (value, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = fit == value,
                                        onClick = { fit = value },
                                        role = Role.RadioButton,
                                        enabled = uiState !is AddPlaylistItemUiState.Uploading
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = fit == value,
                                    onClick = null,
                                    enabled = uiState !is AddPlaylistItemUiState.Uploading
                                )
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            if (itemType == "video") {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "ปิดเสียง (Mute)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "เล่นวิดีโอแบบไม่มีเสียง",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = mute,
                            onCheckedChange = { mute = it },
                            enabled = uiState !is AddPlaylistItemUiState.Uploading
                        )
                    }
                }
            }

            if (uiState is AddPlaylistItemUiState.Uploading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "กำลังอัปโหลด...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (uiState is AddPlaylistItemUiState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        (uiState as AddPlaylistItemUiState.Error).message,
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
                    enabled = uiState !is AddPlaylistItemUiState.Uploading
                ) {
                    Text("ยกเลิก")
                }

                Button(
                    onClick = {
                        if (itemName.isNotBlank() &&
                            selectedMediaUri != null &&
                            durationMs.toIntOrNull() != null &&
                            durationMs.toInt() >= 1000
                        ) {
                            viewModel.addPlaylistItem(
                                playlistId = playlistId,
                                itemName = itemName,
                                itemType = itemType,
                                durationMs = durationMs.toInt(),
                                fit = fit,
                                mute = mute,
                                mediaUri = selectedMediaUri!!
                            )
                        } else {
                            nameError = itemName.isBlank()
                            durationError = durationMs.toIntOrNull() == null ||
                                    durationMs.toInt() < 1000
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is AddPlaylistItemUiState.Uploading &&
                            selectedMediaUri != null
                ) {
                    if (uiState is AddPlaylistItemUiState.Uploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("เพิ่ม Item")
                }
            }
        }
    }
}