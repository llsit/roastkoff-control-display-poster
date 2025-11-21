package com.roastkoff.controlposter.data.model

data class Playlist(
    val id: String = "",
    val name: String = "",
    val items: List<String> = emptyList(),
    val loop: Boolean = false,
    val shuffle: Boolean = false,
    val defaultIntervalMs: Int = 5000,
    val groupId: String? = null,
    val tenantId: String = "",
    val isActive: Boolean = false
)