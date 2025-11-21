package com.roastkoff.controlposter.ui.screen.itemdetail

import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.common.BaseViewModel
import com.roastkoff.controlposter.data.PlaylistItem
import com.roastkoff.controlposter.data.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlaylistItemUiState {
    data object Loading : PlaylistItemUiState
    data class Success(val item: PlaylistItem) : PlaylistItemUiState
    data object Deleting : PlaylistItemUiState
    data object Deleted : PlaylistItemUiState
    data class Error(val message: String) : PlaylistItemUiState
}

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : BaseViewModel() {

    private val _uiState =
        MutableStateFlow<PlaylistItemUiState>(PlaylistItemUiState.Loading)
    val uiState: StateFlow<PlaylistItemUiState> = _uiState.asStateFlow()

    fun loadItem(playlistId: String, itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PlaylistItemUiState.Loading
            try {
                playlistRepository.getPlaylistItem(playlistId, itemId).collect { item ->
                    _uiState.value = PlaylistItemUiState.Success(item)
                }
            } catch (e: Exception) {
                _uiState.value = PlaylistItemUiState.Error(
                    message = e.message ?: "เกิดข้อผิดพลาด"
                )
            }
        }
    }

    fun deleteItem(playlistId: String, itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PlaylistItemUiState.Deleting
            try {
                playlistRepository.deletePlaylistItem(playlistId, itemId)
                _uiState.value = PlaylistItemUiState.Deleted
            } catch (e: Exception) {
                _uiState.value = PlaylistItemUiState.Error(
                    message = e.message ?: "ไม่สามารถลบ Item ได้"
                )
            }
        }
    }
}