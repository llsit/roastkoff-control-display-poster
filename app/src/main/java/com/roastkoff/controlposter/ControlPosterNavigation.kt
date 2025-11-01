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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roastkoff.controlposter.data.model.AuthState
import com.roastkoff.controlposter.ui.auth.AuthViewModel
import com.roastkoff.controlposter.ui.screen.authen.AuthGateScreen
import com.roastkoff.controlposter.ui.screen.dashboard.DashboardScreen
import com.roastkoff.controlposter.ui.screen.login.LoginScreen

@Composable
fun ControlPosterNavigation(
    paddingValues: PaddingValues,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Route.AUTH_GATE,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Route.AUTH_GATE) {
            AuthGateScreen(
                viewModel = authViewModel,
                onAuthenticated = {
                    navController.navigate(Route.MAIN) {
                        popUpTo(Route.AUTH_GATE) { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(Route.LOGIN) {
                        popUpTo(Route.AUTH_GATE) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.AUTH_GATE) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.MAIN) {
            if (authState is AuthState.Authorized) {
                MainNavigation(
                    authViewModel = authViewModel,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Route.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MainNavigation(
    authViewModel: AuthViewModel,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
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
                DashboardScreen(onSignOut = onSignOut)
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

object Route {
    const val AUTH_GATE = "auth_gate"
    const val LOGIN = "login"
    const val MAIN = "main"
}

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