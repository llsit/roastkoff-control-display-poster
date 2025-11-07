package com.roastkoff.controlposter.data.model

data class DashboardStats(
    val groupTotal: Int = 0,
    val dashboardDetail: List<Dashboard> = emptyList()
)

data class Dashboard(
    val groupId: String = "",
    val groupName: String = "",
    val displayList: List<Display> = emptyList(),
)