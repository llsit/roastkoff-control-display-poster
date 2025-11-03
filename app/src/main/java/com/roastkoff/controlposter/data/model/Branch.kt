package com.roastkoff.controlposter.data.model

import com.google.firebase.Timestamp

data class BranchDto(
    val tenantId: String = "",
    val name: String = "",
    val address: String? = null,
    val createdAt: Timestamp? = null
)

data class Branch(
    val tenantId: String = "",
    val name: String = "",
    val address: String? = null,
    val createdAt: Timestamp? = null
)