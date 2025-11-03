package com.roastkoff.controlposter.ui.screen.branch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.data.BranchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddBranchUi(
    val name: String = "",
    val address: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val done: Boolean = false
)

@HiltViewModel
class AddBranchViewModel @Inject constructor(
    private val repository: BranchRepository
) : ViewModel() {
    private val _addBranchUi = MutableStateFlow(AddBranchUi())
    val addBranchUi: StateFlow<AddBranchUi> = _addBranchUi

    fun setName(name: String) {
        _addBranchUi.value = _addBranchUi.value.copy(name = name, error = null)
    }

    fun setAddress(address: String) {
        _addBranchUi.value = _addBranchUi.value.copy(address = address, error = null)
    }

    fun save(tenantId: String) {
        val cur = _addBranchUi.value
        if (cur.name.isBlank()) {
            _addBranchUi.value = cur.copy(error = "Branch name required")
            return
        }
        viewModelScope.launch {
            _addBranchUi.value = cur.copy(loading = true, error = null)
            runCatching {
                repository.createBranch(tenantId, cur.name.trim(), cur.address.ifBlank { null })
            }.onSuccess {
                _addBranchUi.value = _addBranchUi.value.copy(loading = false, done = true)
            }.onFailure { e ->
                _addBranchUi.value =
                    _addBranchUi.value.copy(loading = false, error = e.message ?: "Failed to save")
            }
        }
    }
}