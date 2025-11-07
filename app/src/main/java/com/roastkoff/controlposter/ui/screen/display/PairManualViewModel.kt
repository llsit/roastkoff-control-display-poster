package com.roastkoff.controlposter.ui.screen.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.data.DisplayRepository
import com.roastkoff.controlposter.data.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PairManualUi(
    val groups: List<Pair<String, String>> = emptyList(), // (id,name)
    val groupId: String? = null,
    val displayName: String = "",
    val location: String = "",
    val code: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val done: Boolean = false
)

@HiltViewModel
class PairManualViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val displayRepository: DisplayRepository
) : ViewModel() {

    private val _pairManualUi = MutableStateFlow(PairManualUi())
    val pairManualUi: StateFlow<PairManualUi> = _pairManualUi

    fun loadGroups(tenantId: String) {
        viewModelScope.launch {
            groupRepository.groupsOfTenant(tenantId).collect { list ->
                _pairManualUi.value = _pairManualUi.value.copy(
                    groups = list.map { it.first to it.second.name }
                )
            }
        }
    }

    fun setGroup(id: String?) {
        _pairManualUi.value = _pairManualUi.value.copy(groupId = id)
    }

    fun setName(s: String) {
        _pairManualUi.value = _pairManualUi.value.copy(displayName = s, error = null)
    }

    fun setLocation(s: String) {
        _pairManualUi.value = _pairManualUi.value.copy(location = s)
    }

    fun setCode(s: String) {
        _pairManualUi.value = _pairManualUi.value.copy(code = s)
    }

    fun submit(tenantId: String) {
        val cur = _pairManualUi.value
        if (cur.displayName.isBlank()) {
            _pairManualUi.value = cur.copy(error = "กรุณากรอกชื่อจอ"); return
        }
        viewModelScope.launch {
            _pairManualUi.value = cur.copy(loading = true, error = null)
            runCatching {
                displayRepository.createDisplay(
                    tenantId = tenantId,
                    groupId = cur.groupId,
                    name = cur.displayName.trim(),
                    location = cur.location.ifBlank { null },
                    code = cur.code.ifBlank { null }
                )
            }.onSuccess {
                _pairManualUi.value = _pairManualUi.value.copy(loading = false, done = true)
            }.onFailure { e ->
                _pairManualUi.value = _pairManualUi.value.copy(loading = false, error = e.message ?: "บันทึกล้มเหลว")
            }
        }
    }
}