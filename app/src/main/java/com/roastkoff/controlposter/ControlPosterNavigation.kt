package com.roastkoff.controlposter

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.roastkoff.controlposter.data.model.AuthState
import com.roastkoff.controlposter.ui.screen.authen.AuthGateScreen
import com.roastkoff.controlposter.ui.screen.authen.AuthViewModel
import com.roastkoff.controlposter.ui.screen.dashboard.DashboardScreen
import com.roastkoff.controlposter.ui.screen.display.DisplayDetailScreen
import com.roastkoff.controlposter.ui.screen.group.AddGroupScreen
import com.roastkoff.controlposter.ui.screen.login.LoginScreen
import com.roastkoff.controlposter.ui.screen.pairscreen.PairManualScreen

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
                val currentTenantId = (authState as AuthState.Authorized).profile.tenantId
                MainNavigation(
                    currentTenantId = currentTenantId,
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
    currentTenantId: String,
    onSignOut: () -> Unit
) {
    val backStack = remember { mutableStateListOf<MainRoute>(MainRoute.Dashboard) }

    Scaffold {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { key ->
                when (key) {
                    MainRoute.AddGroup -> NavEntry(key) {
                        AddGroupScreen(
                            tenantId = currentTenantId,
                            onSaved = { backStack.removeLastOrNull() },
                            onClickBack = { backStack.removeLastOrNull() }
                        )
                    }

                    MainRoute.Dashboard -> NavEntry(key) {
                        DashboardScreen(
                            tenantId = currentTenantId,
                            onSignOut = onSignOut,
                            onTapDisplay = { backStack.add(MainRoute.Display) },
                            onOpenAddBranch = { backStack.add(MainRoute.PairDisplay) },
                            onOpenPairDisplay = { backStack.add(MainRoute.PairDisplay) }
                        )
                    }

                    MainRoute.Display -> NavEntry(key) {
                        DisplayDetailScreen(
                            tenantId = currentTenantId,
                            onClickBack = { backStack.removeLastOrNull() },
                            onTapPlaylist = { }
                        )
                    }

                    MainRoute.PairDisplay -> NavEntry(key) {
                        PairManualScreen(
                            tenantId = currentTenantId,
                            onDone = { backStack.removeLastOrNull() },
                            onClickBack = { backStack.removeLastOrNull() },
                            onAddGroup = { backStack.add(MainRoute.AddGroup) }
                        )
                    }
                }
            }
        )
    }
}

object Route {
    const val AUTH_GATE = "auth_gate"
    const val LOGIN = "login"
    const val MAIN = "main"
}

sealed class MainRoute() {
    data object Dashboard : MainRoute()
    data object AddGroup : MainRoute()
    data object PairDisplay : MainRoute()
    data object Display : MainRoute()
}
