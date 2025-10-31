package com.roastkoff.controlposter.data.model

sealed interface AuthState {
    data object Uninitialized : AuthState
    data object Checking : AuthState
    data object Unauthenticated : AuthState
    data class IncompleteProfile(val reason: String) : AuthState
    data class Authorized(val profile: UserProfile) : AuthState
}

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String?,
    val tenantId: String,
    val role: String
)