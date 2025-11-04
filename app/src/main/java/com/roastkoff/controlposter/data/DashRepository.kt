package com.roastkoff.controlposter.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.roastkoff.controlposter.data.model.DashboardStats
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
        Log.d("DEBUG",
            "project=${firestore.app.options.projectId}, " +
                    "tenantFromUserDoc=<โหลดจาก /users/{uid}>, " +
                    "queryTenant=$tenantId"
        )
        val displays = firestore.collection(COLLECTION_DISPLAYS)
            .whereEqualTo(FIELD_TENANT_ID, tenantId)
            .get()
            .await()

        val playlists = firestore.collection(COLLECTION_PLAYLISTS)
            .whereEqualTo(FIELD_TENANT_ID, tenantId)
            .get()
            .await()

        val branches = firestore.collection(COLLECTION_GROUPS)
            .whereEqualTo(FIELD_TENANT_ID, tenantId)
            .get()
            .await()

        val onlineDisplays = displays.documents.count {
            it.getString(FIELD_STATUS) == STATUS_ONLINE
        }

        emit(
            DashboardStats(
                displaysTotal = displays.size(),
                displaysOnline = onlineDisplays,
                playlistsTotal = playlists.size(),
                branchesTotal = branches.size()
            )
        )
    }

    companion object {
        private const val COLLECTION_DISPLAYS = "displays"
        private const val COLLECTION_PLAYLISTS = "playlists"
        private const val COLLECTION_GROUPS = "groups"
        private const val FIELD_TENANT_ID = "tenantId"
        private const val FIELD_STATUS = "status"
        private const val STATUS_ONLINE = "online"
    }

}