package com.roastkoff.controlposter.ui.screen.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roastkoff.controlposter.data.PlaylistItem
import com.roastkoff.controlposter.ui.component.DeleteConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    playlistName: String = "playlist1",
    onNavigateBack: () -> Unit,
    onAddItem: (playlistId: String) -> Unit,
    onClickItem: (playlistId: String, itemId: String) -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {

    val state by viewModel.playlistUiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadPlaylistDetail(playlistId) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemId by remember { mutableStateOf("") }

    DeleteConfirmDialog(
        visible = showDeleteDialog,
        itemName = playlistName,
        onConfirm = {
            showDeleteDialog = false
            viewModel.deleteItem(playlistId, itemId)
        },
        onDismiss = {
            showDeleteDialog = false
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlistName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddItem(playlistId) }) {
                Icon(Icons.Outlined.Add, "Add Item")
            }
        }
    ) { padding ->
        when (state) {
            is PlaylistUiState.Error -> {}
            PlaylistUiState.Loading -> {
                CircularProgressIndicator()
            }

            is PlaylistUiState.Success -> {
                val items = (state as PlaylistUiState.Success).playlist.items
                if (items.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Image,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "ยังไม่มี Item",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items.size) { index ->
                            PlaylistItemCard(
                                item = items[index],
                                onClickItem = { onClickItem(playlistId, items[index].id) },
                                onDelete = { itemId = it; showDeleteDialog = true }
                            )
                        }
                    }
                }
            }

            PlaylistUiState.Deleted -> {}
        }
    }
}

@Composable
private fun PlaylistItemCard(
    item: PlaylistItem,
    onClickItem: () -> Unit,
    onDelete: (String) -> Unit
) {
    ElevatedCard(
        onClick = onClickItem,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (item.type == "image") Icons.Outlined.Image else Icons.Outlined.VideoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

//            Icon(
//                Icons.Outlined.Edit,
//                contentDescription = "Edit",
//                tint = MaterialTheme.colorScheme.primary
//            )

            IconButton(onClick = { onDelete(item.id) }) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}