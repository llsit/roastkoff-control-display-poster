package com.roastkoff.controlposter.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.roastkoff.controlposter.data.model.BranchDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface BranchRepository {
    suspend fun createBranch(tenantId: String, name: String, address: String?): String
    fun branchesOfTenant(tenantId: String): Flow<List<Pair<String, BranchDto>>>
}

class BranchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BranchRepository {

    override suspend fun createBranch(
        tenantId: String,
        name: String,
        address: String?
    ): String {
        require(tenantId.isNotBlank()) { "tenantId required" }
        require(name.isNotBlank()) { "name required" }

        val document = firestore.collection("branches").document()
        val data = mapOf(
            "tenantId" to tenantId,
            "name" to name,
            "address" to address,
            "createdAt" to FieldValue.serverTimestamp()
        )
        document.set(data).await()
        return document.id
    }

    override fun branchesOfTenant(tenantId: String): Flow<List<Pair<String, BranchDto>>> =
        callbackFlow {
            val reg = firestore.collection("branches")
                .whereEqualTo("tenantId", tenantId)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        close(err); return@addSnapshotListener
                    }
                    val list = snap?.documents.orEmpty().map { doc ->
                        doc.id to BranchDto(
                            tenantId = doc.getString("tenantId") ?: "",
                            name = doc.getString("name") ?: "",
                            address = doc.getString("address"),
                            createdAt = doc.getTimestamp("createdAt")
                                ?: Timestamp.now()
                        )
                    }
                    trySend(list)
                }
            awaitClose { reg.remove() }
        }
}
