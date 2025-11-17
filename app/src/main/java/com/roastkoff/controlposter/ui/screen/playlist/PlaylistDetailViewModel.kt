package com.roastkoff.controlposter.ui.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.data.PlaylistData
import com.roastkoff.controlposter.data.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PlaylistUiState {
    data object Loading : PlaylistUiState
    data class Success(val playlist: PlaylistData) : PlaylistUiState
    data class Error(val message: String) : PlaylistUiState
}

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _playlistUiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)
    val playlistUiState: StateFlow<PlaylistUiState> = _playlistUiState.asStateFlow()

    fun loadPlaylistDetail(playlistId: String) {
        viewModelScope.launch {
            _playlistUiState.value = PlaylistUiState.Loading
            try {
                playlistRepository.getPlaylist(playlistId = playlistId).collect { item ->
                    _playlistUiState.value = PlaylistUiState.Success(item)
                }
            } catch (e: Exception) {
                _playlistUiState.value = PlaylistUiState.Error(
                    message = e.message ?: "Unknown error"
                )
            }
        }
    }
}