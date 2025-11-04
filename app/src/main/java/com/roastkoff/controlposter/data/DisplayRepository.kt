package com.roastkoff.controlposter.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DisplayDto(
    val tenantId: String = "",
    val branchId: String? = null,
    val name: String = "",
    val location: String? = null,
    val activePlaylistId: String? = null,
    val status: String = "offline",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

interface DisplayRepository {
    suspend fun createDisplay(
        tenantId: String,
        branchId: String?,
        name: String,
        location: String?,
        code: String?
    ): String

    fun displaysByTenant(tenantId: String): Flow<List<Pair<String, DisplayDto>>>
    fun displaysByBranch(tenantId: String, branchId: String?): Flow<List<Pair<String, DisplayDto>>>
}

class DisplayRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : DisplayRepository {

    override suspend fun createDisplay(
        tenantId: String,
        branchId: String?,
        name: String,
        location: String?,
        code: String?
    ): String {
        val ref = db.collection("displays").document()
        val data = hashMapOf(
            "tenantId" to tenantId,
            "branchId" to branchId,
            "name" to name,
            "location" to location,
            "status" to "offline",
            "activePlaylistId" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        ref.set(data).await()

        if (!code.isNullOrBlank()) {
            // log ไว้เป็นหลักฐานจับคู่แบบ manual
            db.collection("pairingLogs").document()
                .set(
                    mapOf(
                        "displayId" to ref.id,
                        "tenantId" to tenantId,
                        "branchId" to branchId,
                        "code" to code,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        }
        return ref.id
    }

    override fun displaysByTenant(tenantId: String): Flow<List<Pair<String, DisplayDto>>> =
        callbackFlow {
            val reg = db.collection("displays")
                .whereEqualTo("tenantId", tenantId)
                .orderBy("branchId", Query.Direction.ASCENDING)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        close(err); return@addSnapshotListener
                    }
                    val list = snap?.documents.orEmpty().map { d ->
                        d.id to DisplayDto(
                            tenantId = d.getString("tenantId") ?: "",
                            branchId = d.getString("branchId"),
                            name = d.getString("name") ?: "",
                            location = d.getString("location"),
                            activePlaylistId = d.getString("activePlaylistId"),
                            status = d.getString("status") ?: "offline",
                            createdAt = d.getTimestamp("createdAt"),
                            updatedAt = d.getTimestamp("updatedAt")
                        )
                    }
                    trySend(list)
                }
            awaitClose { reg.remove() }
        }

    override fun displaysByBranch(
        tenantId: String,
        branchId: String?
    ): Flow<List<Pair<String, DisplayDto>>> = callbackFlow {
        var q = db.collection("displays").whereEqualTo("tenantId", tenantId)
        if (branchId != null) q = q.whereEqualTo("branchId", branchId)
        val reg = q.orderBy("name").addSnapshotListener { snap, err ->
            if (err != null) {
                close(err); return@addSnapshotListener
            }
            val list = snap?.documents.orEmpty().map { d ->
                d.id to DisplayDto(
                    tenantId = d.getString("tenantId") ?: "",
                    branchId = d.getString("branchId"),
                    name = d.getString("name") ?: "",
                    location = d.getString("location"),
                    activePlaylistId = d.getString("activePlaylistId"),
                    status = d.getString("status") ?: "offline",
                    createdAt = d.getTimestamp("createdAt"),
                    updatedAt = d.getTimestamp("updatedAt")
                )
            }
            trySend(list)
        }
        awaitClose { reg.remove() }
    }
}
