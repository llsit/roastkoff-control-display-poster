package com.roastkoff.controlposter

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roastkoff.controlposter.data.model.AuthState
import com.roastkoff.controlposter.ui.screen.authen.AuthGateScreen
import com.roastkoff.controlposter.ui.screen.authen.AuthViewModel
import com.roastkoff.controlposter.ui.screen.dashboard.DashboardScreen
import com.roastkoff.controlposter.ui.screen.display.PairManualScreen
import com.roastkoff.controlposter.ui.screen.group.AddGroupScreen
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
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.Dashboard.path,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainRoute.Dashboard.path) {
                DashboardScreen(
                    tenantId = currentTenantId,
                    onSignOut = onSignOut,
                    onTapDisplay = {},
                    onOpenAddBranch = { navController.navigate(MainRoute.AddGroup.path) },
                    onOpenPairDisplay = { navController.navigate(MainRoute.PairDisplay.path) }
                )
            }

            composable(MainRoute.AddGroup.path) {
                AddGroupScreen(
                    tenantId = currentTenantId,
                    onSaved = { navController.navigateUp() },
                    onClickBack = { navController.navigateUp() }
                )
            }

            composable(MainRoute.PairDisplay.path) {
                PairManualScreen(
                    tenantId = currentTenantId,
                    onDone = {},
                    onClickBack = { navController.popBackStack() }
                )
            }
        }
    }
}

object Route {
    const val AUTH_GATE = "auth_gate"
    const val LOGIN = "login"
    const val MAIN = "main"
}

sealed class MainRoute(val path: String) {
    data object Dashboard : MainRoute("dashboard")
    data object AddGroup : MainRoute("add_group")
    data object PairDisplay : MainRoute("pair_display")
}
