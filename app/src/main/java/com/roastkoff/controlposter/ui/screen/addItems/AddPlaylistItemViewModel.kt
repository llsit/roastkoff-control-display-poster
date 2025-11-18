package com.roastkoff.controlposter.ui.screen.addItems

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.data.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AddPlaylistItemUiState {
    data object Idle : AddPlaylistItemUiState
    data object Uploading : AddPlaylistItemUiState  // เอา progress ออก
    data object Success : AddPlaylistItemUiState
    data class Error(val message: String) : AddPlaylistItemUiState
}

@HiltViewModel
class AddPlaylistItemViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddPlaylistItemUiState>(AddPlaylistItemUiState.Idle)
    val uiState: StateFlow<AddPlaylistItemUiState> = _uiState.asStateFlow()

    fun addPlaylistItem(
        playlistId: String,
        itemName: String,
        itemType: String,
        durationMs: Int,
        fit: String,
        mute: Boolean,
        mediaUri: Uri
    ) {
        viewModelScope.launch {
            try {
                // Check file size
                val fileSize = getFileSize(mediaUri)
                if (fileSize > 5 * 1024 * 1024) { // 5 MB
                    _uiState.value = AddPlaylistItemUiState.Error(
                        "ไฟล์มีขนาดใหญ่เกิน 5 MB (${fileSize / 1024 / 1024} MB)"
                    )
                    return@launch
                }

                _uiState.value = AddPlaylistItemUiState.Uploading

                playlistRepository.addItemToPlaylist(
                    playlistId = playlistId,
                    itemName = itemName,
                    itemType = itemType,
                    durationMs = durationMs,
                    fit = fit,
                    mute = mute,
                    mediaUri = mediaUri
                )

                _uiState.value = AddPlaylistItemUiState.Success
            } catch (e: Exception) {
                _uiState.value = AddPlaylistItemUiState.Error(
                    message = e.message ?: "เกิดข้อผิดพลาดในการเพิ่ม Item"
                )
            }
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}