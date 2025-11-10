package com.roastkoff.controlposter.ui.screen.addplaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.data.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AddPlaylistUiState {
    data object Idle : AddPlaylistUiState
    data object Loading : AddPlaylistUiState
    data object Success : AddPlaylistUiState
    data class Error(val message: String) : AddPlaylistUiState
}

@HiltViewModel
class AddPlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddPlaylistUiState>(AddPlaylistUiState.Idle)
    val uiState: StateFlow<AddPlaylistUiState> = _uiState.asStateFlow()

    fun createPlaylist(groupId: String, formData: PlaylistFormData) {
        viewModelScope.launch {
            _uiState.value = AddPlaylistUiState.Loading
            try {
                playlistRepository.createPlaylist(
                    groupId = groupId,
                    name = formData.name,
                    loop = formData.loop,
                    shuffle = formData.shuffle,
                    defaultIntervalMs = formData.defaultIntervalMs
                )
                _uiState.value = AddPlaylistUiState.Success
            } catch (e: Exception) {
                _uiState.value = AddPlaylistUiState.Error(
                    message = e.message ?: "เกิดข้อผิดพลาดในการสร้าง Playlist"
                )
            }
        }
    }
}