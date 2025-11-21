package com.roastkoff.controlposter.ui.screen.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.common.BaseViewModel
import com.roastkoff.controlposter.data.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddGroupUi(
    val name: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val done: Boolean = false
)

@HiltViewModel
class AddGroupViewModel @Inject constructor(
    private val repository: GroupRepository
) : BaseViewModel() {
    private val _addGroupUi = MutableStateFlow(AddGroupUi())
    val addGroupUi: StateFlow<AddGroupUi> = _addGroupUi.asStateFlow()

    fun setName(name: String) {
        _addGroupUi.value = _addGroupUi.value.copy(name = name, error = null)
    }

    fun save(tenantId: String) {
        val current = _addGroupUi.value
        if (current.name.isBlank()) {
            _addGroupUi.value = current.copy(error = "Branch name required")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _addGroupUi.value = current.copy(loading = true, error = null)
            runCatching {
                repository.createGroup(tenantId, current.name.trim())
            }.onSuccess {
                _addGroupUi.value = _addGroupUi.value.copy(loading = false)
                onNavigationBack()
            }.onFailure { e ->
                _addGroupUi.value =
                    _addGroupUi.value.copy(loading = false, error = e.message ?: "Failed to save")
            }
        }
    }

    fun resetState() {
        _addGroupUi.value = AddGroupUi()
    }
}