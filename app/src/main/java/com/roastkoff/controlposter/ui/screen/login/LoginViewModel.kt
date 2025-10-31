package com.roastkoff.controlposter.ui.screen.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.controlposter.data.AuthRepository
import com.roastkoff.controlposter.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState()); private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, error = null)
    }

    fun onTogglePasswordVisibility() {
        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
    }

    fun onLogin() {
        if (!validateInput()) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            authRepository.signIn(
                email = uiState.email.trim(),
                password = uiState.password
            ).fold(
                onSuccess = { uid -> handleLoginSuccess(uid) },
                onFailure = { error -> handleLoginFailure(error) }
            )
        }
    }

    fun onResetPassword() {
        val email = uiState.email.trim()

        if (email.isBlank()) {
            uiState = uiState.copy(error = "Please enter your email address")
            return
        }

        viewModelScope.launch {
            authRepository.sendPasswordReset(email).fold(
                onSuccess = {
                    uiState = uiState.copy(
                        error = "Password reset email sent. Please check your inbox."
                    )
                },
                onFailure = {
                    uiState = uiState.copy(
                        error = "Failed to send reset email. Please try again."
                    )
                }
            )
        }
    }

    private fun validateInput(): Boolean {
        val email = uiState.email.trim()
        val password = uiState.password

        return when {
            email.isBlank() -> {
                uiState = uiState.copy(error = "Email is required")
                false
            }
            password.isBlank() -> {
                uiState = uiState.copy(error = "Password is required")
                false
            }
            else -> true
        }
    }

    private suspend fun handleLoginSuccess(uid: String) {
        userRepository.ensureProfile(uid).fold(
            onSuccess = {
                uiState = uiState.copy(
                    isLoading = false,
                    isLoggedIn = true
                )
            },
            onFailure = { error ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load user profile"
                )
            }
        )
    }

    private fun handleLoginFailure(error: Throwable) {
        uiState = uiState.copy(
            isLoading = false,
            error = getErrorMessage(error)
        )
    }

    private fun getErrorMessage(error: Throwable): String {
        return when {
            error.message?.contains("password") == true -> "Invalid email or password"
            error.message?.contains("network") == true -> "Network error. Please check your connection"
            else -> error.message ?: "Login failed. Please try again"
        }
    }
}