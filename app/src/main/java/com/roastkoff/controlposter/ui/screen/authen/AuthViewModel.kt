package com.roastkoff.controlposter.ui.screen.authen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.data.AuthRepository
import com.roastkoff.controlposter.data.UserRepository
import com.roastkoff.controlposter.data.model.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Uninitialized)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.observeAuthState().collect { uid ->
                if (uid == null) {
                    _authState.value = AuthState.Unauthenticated
                } else {
                    loadUserProfile(uid)
                }
            }
        }
    }

    private suspend fun loadUserProfile(uid: String) {
        _authState.value = AuthState.Checking

        userRepository.ensureProfile(uid).fold(
            onSuccess = { profile ->
                _authState.value = AuthState.Authorized(profile)
            },
            onFailure = { error ->
                _authState.value = AuthState.IncompleteProfile(
                    reason = error.message ?: "Failed to load profile"
                )
            }
        )
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}