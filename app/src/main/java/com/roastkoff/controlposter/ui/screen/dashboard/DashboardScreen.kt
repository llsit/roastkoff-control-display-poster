package com.roastkoff.controlposter.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    tenantId: String,
    onSignOut: () -> Unit,
    onCreatePoster: () -> Unit,
    onGoPair: () -> Unit,
    onAddBranch: () -> Unit
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val stats by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(tenantId) { viewModel.loadStats(tenantId) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (stats is DashboardUiState.Success){
                (stats as DashboardUiState.Success).stats.let { stats ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("Displays", "${stats.displaysOnline}/${stats.displaysTotal}")
                        StatCard("Playlists", "${stats.playlistsTotal}")
                        StatCard("Branches", "${stats.branchesTotal}")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onGoPair, modifier = Modifier.weight(1f)) { Text("Pair Display") }
                OutlinedButton(
                    onClick = onAddBranch,
                    modifier = Modifier.weight(1f)
                ) { Text("Add Branch") }
            }
        }
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
private fun StatCard(title: String, value: String) {
    Box(Modifier.fillMaxWidth()) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(value, style = MaterialTheme.typography.headlineSmall)
            }
        }
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