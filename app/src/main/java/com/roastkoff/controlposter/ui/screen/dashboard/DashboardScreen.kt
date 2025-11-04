package com.roastkoff.controlposter.ui.screen.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roastkoff.controlposter.data.model.DashboardStats
import kotlin.collections.isNotEmpty
import kotlin.collections.take

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    tenantId: String,
    viewModel: DashboardViewModel = hiltViewModel(),
    onSignOut: () -> Unit,
    onNavigateToDisplays: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onNavigateToBranches: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(tenantId) {
        viewModel.loadStats(tenantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dashboard")
                        Text(
                            "Control Poster",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {  }) {
                        Icon(Icons.Outlined.Refresh, "Refresh")
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Outlined.Logout, "Sign out")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    LoadingContent()
                }
                is DashboardUiState.Success -> {
                    DashboardContent(
                        stats = state.stats,
                        onNavigateToDisplays = onNavigateToDisplays,
                        onNavigateToPlaylists = onNavigateToPlaylists,
                        onNavigateToBranches = onNavigateToBranches
                    )
                }
                is DashboardUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadStats(tenantId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    stats: DashboardStats,
    onNavigateToDisplays: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onNavigateToBranches: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WelcomeCard()
        }

        item {
            Text(
                "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DisplayStatsCard(
                    stats = stats,
                    onClick = onNavigateToDisplays,
                    modifier = Modifier.weight(1f)
                )

                PlaylistStatsCard(
                    stats = stats,
                    onClick = onNavigateToPlaylists,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            BranchStatsCard(
                stats = stats,
                onClick = onNavigateToBranches,
                modifier = Modifier.fillMaxWidth()
            )
        }

//        if (stats.activePlaylist != null) {
//            item {
//                Text(
//                    "Active Playlist",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            item {
//                ActivePlaylistCard(playlist = stats.activePlaylist)
//            }
//        }

//        // Recent Activities
//        if (recentActivities.isNotEmpty()) {
//            item {
//                Text(
//                    "Recent Activities",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            items(recentActivities.take(5)) { activity ->
//                ActivityItem(activity)
//            }
//        }

//        // Alerts
//        if (stats.offlineDisplays.isNotEmpty()) {
//            item {
//                AlertsSection(offlineDisplays = stats.offlineDisplays)
//            }
//        }
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Welcome to ControlPoster",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Manage your digital signage content",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Icon(
                Icons.Outlined.Dashboard,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun DisplayStatsCard(
    stats: DashboardStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Displays",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    Icons.Outlined.Tv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "${stats.displaysOnline}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "/ ${stats.displaysTotal}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

//            LinearProgressIndicator(
//                progress = { stats.displaysOnlinePercentage / 100f },
//                modifier = Modifier.fillMaxWidth(),
//            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "🟢 ${stats.displaysOnline} Online",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

//                if (stats.displaysOffline > 0) {
//                    Surface(
//                        shape = MaterialTheme.shapes.small,
//                        color = MaterialTheme.colorScheme.errorContainer
//                    ) {
//                        Text(
//                            "⚫ ${stats.displaysOffline} Offline",
//                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//                }
            }
        }
    }
}

@Composable
private fun PlaylistStatsCard(
    stats: DashboardStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Playlists",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    Icons.Outlined.PlaylistPlay,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                "${stats.playlistsTotal}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Total playlists",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

//            if (stats.publishedPlaylists > 0) {
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(4.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        Icons.Outlined.CheckCircle,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Text(
//                        "${stats.publishedPlaylists} Published",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
        }
    }
}

@Composable
private fun BranchStatsCard(
    stats: DashboardStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(
                        Icons.Outlined.Store,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Column {
                    Text(
                        "Branches",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${stats.branchesTotal} locations",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

//@Composable
//private fun ActivePlaylistCard(playlist: ActivePlaylistInfo) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.secondaryContainer
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Surface(
//                shape = MaterialTheme.shapes.medium,
//                color = MaterialTheme.colorScheme.surface
//            ) {
//                Icon(
//                    Icons.Outlined.PlayCircle,
//                    contentDescription = null,
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .size(32.dp),
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//
//            Column(modifier = Modifier.weight(1f)) {
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Surface(
//                        shape = MaterialTheme.shapes.small,
//                        color = MaterialTheme.colorScheme.primary
//                    ) {
//                        Text(
//                            "LIVE",
//                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.onPrimary,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//
//                Spacer(Modifier.height(4.dp))
//
//                Text(
//                    playlist.name,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    modifier = Modifier.padding(top = 8.dp)
//                ) {
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(4.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            Icons.Outlined.Image,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp)
//                        )
//                        Text(
//                            "${playlist.itemCount} items",
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(4.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            Icons.Outlined.Tv,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp)
//                        )
//                        Text(
//                            "${playlist.displayCount} displays",
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun ActivityItem(activity: RecentActivity) {
//    Card(modifier = Modifier.fillMaxWidth()) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            horizontalArrangement = Arrangement.spacedBy(12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Surface(
//                shape = MaterialTheme.shapes.small,
//                color = activity.type.color.copy(alpha = 0.1f)
//            ) {
//                Icon(
//                    activity.type.icon,
//                    contentDescription = null,
//                    modifier = Modifier
//                        .padding(8.dp)
//                        .size(20.dp),
//                    tint = activity.type.color
//                )
//            }
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    activity.message,
//                    style = MaterialTheme.typography.bodyMedium
//                )
//                Text(
//                    activity.timestamp,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}

@Composable
private fun AlertsSection(offlineDisplays: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            offlineDisplays.forEach { displayName ->
                Text(
                    "• Display \"$displayName\" is offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Outlined.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                "Error",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}