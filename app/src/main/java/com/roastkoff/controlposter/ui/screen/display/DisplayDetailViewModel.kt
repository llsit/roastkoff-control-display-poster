package com.roastkoff.controlposter.ui.screen.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.data.DisplayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DisplayDetailViewModel @Inject constructor(
    private val displayRepository: DisplayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DisplayDetailUiState>(DisplayDetailUiState.Loading)
    val uiState: StateFlow<DisplayDetailUiState> = _uiState.asStateFlow()

    fun loadDisplay(displayId: String) {
        viewModelScope.launch {
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
}