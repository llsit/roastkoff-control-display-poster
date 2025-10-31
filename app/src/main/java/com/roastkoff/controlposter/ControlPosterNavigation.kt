package com.roastkoff.controlposter

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roastkoff.controlposter.ui.screen.dashboard.DashboardScreen

@Composable
fun ControlPosterNavigation(
    paddingValues: PaddingValues,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        modifier = Modifier.padding(paddingValues),
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onSignOut = { /* Handle sign out */ }
                )
            }

            composable(Screen.Playlists.route) {
                // PlaylistListScreen(navController)
            }

            composable(Screen.Displays.route) {
                // DisplayListScreen(navController)
            }

            composable(Screen.Branches.route) {
                // BranchListScreen(navController)
            }

            composable(Screen.Settings.route) {
                // SettingsScreen()
            }

            // Detail screens
            composable("${Screen.PlaylistEditor.route}/{id}") { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("id")
                // PlaylistEditorScreen(id = playlistId!!)
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any {
                    it.route == screen.route
                } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label
                    )
                },
                label = { Text(screen.label) }
            )
        }
    }
}

// Screen definitions
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Outlined.Dashboard)
    data object Playlists : Screen("playlists", "Playlists", Icons.AutoMirrored.Outlined.List)
    data object Displays : Screen("displays", "Displays", Icons.Outlined.DevicesOther)
    data object Branches : Screen("branches", "Branches", Icons.Outlined.Store)
    data object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)

    // Detail screens
    data object PlaylistEditor :
        Screen("playlist/edit", "Edit Playlist", Icons.AutoMirrored.Outlined.List)
}

private val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Playlists,
    Screen.Displays,
    Screen.Branches,
    Screen.Settings
)