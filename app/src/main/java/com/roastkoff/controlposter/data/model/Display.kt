package com.roastkoff.controlposter.data.model

import com.google.firebase.Timestamp

data class DisplayDto(
    val displayId: String = "",
    val tenantId: String = "",
    val branchId: String? = null,
    val name: String = "",
    val location: String? = null,
    val activePlaylistId: String? = null,
    val status: String = "offline",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class Display(
    val displayId: String = "",
    val tenantId: String = "",
    val groupId: String? = null,
    val name: String = "",
    val location: String? = null,
    val activePlaylistId: String? = null,
    val status: String = "offline",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)