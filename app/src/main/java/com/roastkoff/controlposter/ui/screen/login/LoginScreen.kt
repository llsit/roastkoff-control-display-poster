package com.roastkoff.controlposter.ui.screen.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onLogin = viewModel::onLogin,
        onForgotPassword = viewModel::onResetPassword
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTitle()

            Spacer(Modifier.height(32.dp))

            EmailTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                enabled = !uiState.isLoading,
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )

            Spacer(Modifier.height(12.dp))

            PasswordTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                isPasswordVisible = uiState.isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility,
                enabled = !uiState.isLoading,
                onDone = { onLogin() }
            )

            Spacer(Modifier.height(24.dp))

            LoginButton(
                onClick = onLogin,
                enabled = !uiState.isLoading &&
                        uiState.email.isNotBlank() &&
                        uiState.password.isNotBlank(),
                isLoading = uiState.isLoading
            )

            Spacer(Modifier.height(8.dp))

            ForgotPasswordButton(
                onClick = onForgotPassword,
                enabled = !uiState.isLoading
            )

            uiState.error?.let { errorMessage ->
                Spacer(Modifier.height(16.dp))
                ErrorMessage(message = errorMessage)
            }
        }
    }
}

@Composable
private fun AppTitle() {
    Text(
        text = "ControlPoster",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onNext: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email") },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() }
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    enabled: Boolean,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password") },
        singleLine = true,
        enabled = enabled,
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        trailingIcon = {
            IconButton(
                onClick = onToggleVisibility,
                enabled = enabled
            ) {
                Icon(
                    imageVector = if (isPasswordVisible) {
                        Icons.Outlined.VisibilityOff
                    } else {
                        Icons.Outlined.Visibility
                    },
                    contentDescription = if (isPasswordVisible) {
                        "Hide password"
                    } else {
                        "Show password"
                    }
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LoginButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Login")
        }
    }
}

@Composable
private fun ForgotPasswordButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text("Forgot password?")
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
}