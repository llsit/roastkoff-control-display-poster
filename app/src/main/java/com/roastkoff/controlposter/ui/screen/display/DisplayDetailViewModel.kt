package com.roastkoff.controlposter.ui.screen.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.data.DisplayRepository
import com.roastkoff.controlposter.data.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class DisplayDetailViewModel @Inject constructor(
    private val displayRepository: DisplayRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DisplayDetailUiState>(DisplayDetailUiState.Loading)
    val uiState: StateFlow<DisplayDetailUiState> = _uiState.asStateFlow()

    private var toggleJob: Job? = null

    fun loadDisplay(displayId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = DisplayDetailUiState.Loading
            try {
                displayRepository.getDisplayWithPlaylists(displayId)
                    .collect { (display, playlists) ->
                        _uiState.value = DisplayDetailUiState.Success(
                            display = display,
                            playlists = playlists
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = DisplayDetailUiState.Error(
                    message = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun setActivePlaylist(
        playlistId: String,
        groupId: String,
        displayId: String,
        isActive: Boolean
    ) {
        toggleJob?.cancel()
        toggleJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = _uiState.value

                if (current is DisplayDetailUiState.Success) {

                    val updatedPlaylists = current.playlists.map { playlist ->
                        when {
                            playlist.id == playlistId -> playlist.copy(isActive = isActive)
                            isActive -> playlist.copy(isActive = false)
                            else -> playlist
                        }
                    }

                    _uiState.value = current.copy(playlists = updatedPlaylists)
                }
                delay(250)
                playlistRepository.updateActivePlaylist(
                    playlistId = playlistId,
                    groupId = groupId,
                    displayId = displayId,
                    isActive = isActive
                )

                displayRepository.getDisplayWithPlaylists(displayId)
                    .collect { (display, playlists) ->
                        _uiState.value = DisplayDetailUiState.Success(
                            display = display,
                            playlists = playlists
                        )
                    }
            } catch (e: CancellationException) {

            } catch (e: Exception) {
                _uiState.value = DisplayDetailUiState.Error(
                    message = e.message ?: "Unknown error"
                )
            }
        }
    }
}