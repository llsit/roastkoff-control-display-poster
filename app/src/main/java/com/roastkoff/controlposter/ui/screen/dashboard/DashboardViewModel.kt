package com.roastkoff.controlposter.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.common.BaseViewModel
import com.roastkoff.controlposter.data.DashRepository
import com.roastkoff.controlposter.data.model.DashboardStats
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val stats: DashboardStats) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashRepository: DashRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadStats(tenantId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = DashboardUiState.Loading

            dashRepository.branchesOfTenant(tenantId).collect { stats ->
                _uiState.value = DashboardUiState.Success(stats)
            }
        }
    }
}