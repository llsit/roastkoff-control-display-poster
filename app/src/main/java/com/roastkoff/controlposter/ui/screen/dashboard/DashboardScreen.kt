package com.roastkoff.controlposter.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            DashboardTopBar(onSignOut = onSignOut)
        }
    ) { paddingValues ->
        DashboardContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(onSignOut: () -> Unit) {
    TopAppBar(
        title = { Text("Dashboard") },
        actions = {
            TextButton(onClick = onSignOut) {
                Text("Sign out")
            }
        }
    )
}

@Composable
private fun DashboardContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WelcomeSection()

        Spacer(Modifier.height(8.dp))

        // TODO: Add summary cards
        // TODO: Add "Create Playlist" button
        // TODO: Add "Pair Display" button
    }
}

@Composable
private fun WelcomeSection() {
    Column {
        Text(
            text = "Welcome to ControlPoster",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Manage your digital signage content",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}