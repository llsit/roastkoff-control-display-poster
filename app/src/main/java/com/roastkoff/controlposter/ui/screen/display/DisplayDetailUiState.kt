package com.roastkoff.controlposter.ui.screen.display

import com.roastkoff.controlposter.data.model.Display
import com.roastkoff.controlposter.data.model.Playlist

sealed interface DisplayDetailUiState {
    data object Loading : DisplayDetailUiState
    data class Success(
        val display: Display,
        val playlists: List<Playlist>
    ) : DisplayDetailUiState

    data class Error(val message: String) : DisplayDetailUiState
}