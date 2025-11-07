package com.roastkoff.controlposter.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.roastkoff.controlposter.data.model.Dashboard
import com.roastkoff.controlposter.data.model.DashboardStats
import com.roastkoff.controlposter.data.model.Display
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface DashRepository {
    fun branchesOfTenant(tenantId: String): Flow<DashboardStats>
}

class DashRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DashRepository {
    override fun branchesOfTenant(tenantId: String): Flow<DashboardStats> = flow {
        val displaysSnapshot = firestore.collection(COLLECTION_DISPLAYS)
            .whereEqualTo(FIELD_TENANT_ID, tenantId)
            .get()
            .await()

        val groupsSnapshot = firestore.collection(COLLECTION_GROUPS)
            .whereEqualTo(FIELD_TENANT_ID, tenantId)
            .get()
            .await()

        val displaysByGroup = displaysSnapshot.documents
            .mapNotNull { doc ->
                try {
                    Display(
                        displayId = doc.id,
                        name = doc.getString("name") ?: "",
                        status = doc.getString(FIELD_STATUS) ?: "offline",
                        location = doc.getString("location"),
                        groupId = doc.getString("groupId"),
                    )
                } catch (e: Exception) {
                    Log.e("DashRepo", "Error parsing display ${doc.id}", e)
                    null
                }
            }.groupBy { it.groupId ?: "" }

        val dashboards = groupsSnapshot.documents.map { groupDoc ->
            val groupId = groupDoc.id
            val groupName = groupDoc.getString("name") ?: "ไม่มีชื่อ"
            val displays = displaysByGroup[groupId] ?: emptyList()

            Dashboard(
                groupId = groupId,
                groupName = groupName,
                displayList = displays
            )
        }.toMutableList()

        emit(
            DashboardStats(
                groupTotal = groupsSnapshot.size(),
                dashboardDetail = dashboards
            )
        )
    }

    companion object {
        private const val COLLECTION_DISPLAYS = "displays"
        private const val COLLECTION_GROUPS = "groups"
        private const val FIELD_TENANT_ID = "tenantId"
        private const val FIELD_STATUS = "status"
        private const val STATUS_ONLINE = "online"
    }

}