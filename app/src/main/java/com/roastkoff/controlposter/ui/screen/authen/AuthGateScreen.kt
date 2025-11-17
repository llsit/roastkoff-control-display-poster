package com.roastkoff.controlposter.ui.screen.authen

import AuthViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roastkoff.controlposter.data.model.AuthState

@Composable
fun AuthGateScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    AuthGateContent(
        authState = authState,
        onAuthenticated = onAuthenticated,
        onUnauthenticated = onUnauthenticated
    )
}

@Composable
private fun AuthGateContent(
    authState: AuthState,
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit
) {
    when (authState) {
        is AuthState.Uninitialized,
        is AuthState.Checking -> {
            LoadingScreen()
        }

        is AuthState.Unauthenticated -> {
            onUnauthenticated()
        }

        is AuthState.Authorized -> {
            onAuthenticated()
        }

        is AuthState.IncompleteProfile -> {
            ProfileErrorScreen(reason = authState.reason)
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ProfileErrorScreen(reason: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Profile Error",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}