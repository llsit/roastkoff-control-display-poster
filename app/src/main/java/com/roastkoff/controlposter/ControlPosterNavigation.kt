package com.roastkoff.controlposter

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.roastkoff.controlposter.data.model.AuthState
import com.roastkoff.controlposter.ui.screen.addItems.AddPlaylistItemScreen
import com.roastkoff.controlposter.ui.screen.addplaylist.AddPlaylistScreen
import com.roastkoff.controlposter.ui.screen.authen.AuthGateScreen
import com.roastkoff.controlposter.ui.screen.authen.AuthViewModel
import com.roastkoff.controlposter.ui.screen.dashboard.DashboardScreen
import com.roastkoff.controlposter.ui.screen.display.DisplayDetailScreen
import com.roastkoff.controlposter.ui.screen.group.AddGroupScreen
import com.roastkoff.controlposter.ui.screen.itemdetail.ItemDetailScreen
import com.roastkoff.controlposter.ui.screen.login.LoginScreen
import com.roastkoff.controlposter.ui.screen.pairscreen.PairManualScreen
import com.roastkoff.controlposter.ui.screen.playlist.PlaylistDetailScreen

@Composable
fun ControlPosterNavigation(
    paddingValues: PaddingValues,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val mainBackStack = remember { mutableStateListOf<Any>(RootRoute.AuthGate) }

    NavDisplay(
        backStack = mainBackStack,
        onBack = { mainBackStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is RootRoute.AuthGate -> NavEntry(key) {
                    AuthGateScreen(
                        viewModel = authViewModel,
                        onAuthenticated = {
                            mainBackStack.add(RootRoute.Main)
                        },
                        onUnauthenticated = {
                            mainBackStack.add(RootRoute.Login)
                        }
                    )
                }

                is RootRoute.Login -> NavEntry(key) {
                    LoginScreen(
                        onLoginSuccess = {
                            mainBackStack.add(RootRoute.AuthGate)
                        }
                    )
                }

                is RootRoute.Main -> NavEntry(key) {
                    if (authState is AuthState.Authorized) {
                        val currentTenantId = (authState as AuthState.Authorized).profile.tenantId
                        MainNavigation(
                            currentTenantId = currentTenantId,
                            onSignOut = {
                                authViewModel.signOut()
                                mainBackStack.add(RootRoute.Login)
                            }
                        )
                    }
                }

                else -> NavEntry(Unit) { Text("Unknown route") }
            }
        }
    )
}

@Composable
private fun MainNavigation(
    currentTenantId: String,
    onSignOut: () -> Unit
) {
    val backStack = remember { mutableStateListOf<Any>(MainRoute.Dashboard) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is MainRoute.AddGroup -> NavEntry(key) {
                    AddGroupScreen(
                        tenantId = currentTenantId,
                        onDone = { backStack.removeLastOrNull() },
                        onClickBack = { backStack.removeLastOrNull() }
                    )
                }

                is MainRoute.Dashboard -> NavEntry(key) {
                    DashboardScreen(
                        tenantId = currentTenantId,
                        onSignOut = onSignOut,
                        onTapDisplay = {
                            backStack.add(MainRoute.Display(it))
                        },
                        onOpenAddGroup = { backStack.add(MainRoute.AddGroup) },
                        onOpenPairDisplay = { backStack.add(MainRoute.PairDisplay) }
                    )
                }

                is MainRoute.Display -> NavEntry(key) {
                    DisplayDetailScreen(
                        displayId = key.displayId,
                        onClickBack = { backStack.removeLastOrNull() },
                        onTapPlaylist = { playlistId, playlistName ->
                            backStack.add(
                                MainRoute.Playlist(
                                    playlistId,
                                    playlistName
                                )
                            )
                        },
                        onAddPlaylist = { groupId, displayId ->
                            backStack.add(MainRoute.AddPlaylist(groupId, displayId))
                        }
                    )
                }

                is MainRoute.PairDisplay -> NavEntry(key) {
                    PairManualScreen(
                        tenantId = currentTenantId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onAddGroup = { backStack.add(MainRoute.AddGroup) }
                    )
                }

                is MainRoute.AddPlaylist -> NavEntry(key) {
                    AddPlaylistScreen(
                        groupId = key.groupId,
                        displayId = key.displayId,
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }

                is MainRoute.Playlist -> NavEntry(key) {
                    PlaylistDetailScreen(
                        playlistId = key.playlistId,
                        playlistName = key.playlistName,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onAddItem = { backStack.add(MainRoute.AddItemPlaylist(it)) },
                        onClickItem = { playlistId, itemId ->
                            backStack.add(
                                MainRoute.ItemPlaylistDetail(
                                    playlistId,
                                    itemId
                                )
                            )
                        }
                    )
                }

                is MainRoute.AddItemPlaylist -> NavEntry(key) {
                    AddPlaylistItemScreen(
                        playlistId = key.playlistId,
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }

                is MainRoute.ItemPlaylistDetail -> NavEntry(key) {
                    ItemDetailScreen(
                        playlistId = key.playlistId,
                        itemId = key.itemId,
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }

                else -> NavEntry(Unit) { Text("Unknown route") }
            }
        }
    )
}

sealed class RootRoute() {
    data object AuthGate : RootRoute()
    data object Login : RootRoute()
    data object Main : RootRoute()
}

sealed class MainRoute() {
    data object Dashboard : MainRoute()
    data object AddGroup : MainRoute()
    data object PairDisplay : MainRoute()
    data class Display(val displayId: String) : MainRoute()
    data class AddPlaylist(val groupId: String, val displayId: String) : MainRoute()
    data class Playlist(val playlistId: String, val playlistName: String) : MainRoute()
    data class AddItemPlaylist(val playlistId: String) : MainRoute()
    data class ItemPlaylistDetail(val playlistId: String, val itemId: String) : MainRoute()
}
