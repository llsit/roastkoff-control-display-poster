package com.roastkoff.controlposter.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.roastkoff.controlposter.data.model.Display
import com.roastkoff.controlposter.data.model.DisplayDto
import com.roastkoff.controlposter.data.model.Playlist
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface DisplayRepository {
    suspend fun createDisplay(
        tenantId: String,
        groupId: String?,
        name: String,
        location: String?,
        code: String?
    ): String

    fun displaysByTenant(tenantId: String): Flow<List<Pair<String, DisplayDto>>>
    fun displaysByGroup(tenantId: String, groupId: String?): Flow<List<Pair<String, DisplayDto>>>

    fun getDisplayWithPlaylists(displayId: String): Flow<Pair<Display, List<Playlist>>>
}

class DisplayRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DisplayRepository {

    override suspend fun createDisplay(
        tenantId: String,
        groupId: String?,
        name: String,
        location: String?,
        code: String?
    ): String {
        val ref = firestore.collection("displays").document()
        val data = hashMapOf(
            "tenantId" to tenantId,
            "groupId" to groupId,
            "name" to name,
            "location" to location,
            "status" to "offline",
            "activePlaylistId" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        ref.set(data).await()

        if (!code.isNullOrBlank()) {
            firestore.collection("pairingLogs").document()
                .set(
                    mapOf(
                        "displayId" to ref.id,
                        "tenantId" to tenantId,
                        "groupId" to groupId,
                        "code" to code,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        }
        return ref.id
    }

    override fun displaysByTenant(tenantId: String): Flow<List<Pair<String, DisplayDto>>> =
        callbackFlow {
            val reg = firestore.collection("displays")
                .whereEqualTo("tenantId", tenantId)
                .orderBy("branchId", Query.Direction.ASCENDING)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        close(err); return@addSnapshotListener
                    }
                    val list = snap?.documents.orEmpty().map { data ->
                        data.id to DisplayDto(
                            tenantId = data.getString("tenantId") ?: "",
                            branchId = data.getString("branchId"),
                            name = data.getString("name") ?: "",
                            location = data.getString("location"),
                            activePlaylistId = data.getString("activePlaylistId"),
                            status = data.getString("status") ?: "offline",
                            createdAt = data.getTimestamp("createdAt"),
                            updatedAt = data.getTimestamp("updatedAt")
                        )
                    }
                    trySend(list)
                }
            awaitClose { reg.remove() }
        }

    override fun displaysByGroup(
        tenantId: String,
        groupId: String?
    ): Flow<List<Pair<String, DisplayDto>>> = callbackFlow {
        var query = firestore.collection("displays").whereEqualTo("tenantId", tenantId)
        if (groupId != null) query = query.whereEqualTo("groupId", groupId)
        val reg = query.orderBy("name").addSnapshotListener { snap, err ->
            if (err != null) {
                close(err); return@addSnapshotListener
            }
            val list = snap?.documents.orEmpty().map { data ->
                data.id to DisplayDto(
                    tenantId = data.getString("tenantId") ?: "",
                    branchId = data.getString("branchId"),
                    name = data.getString("name") ?: "",
                    location = data.getString("location"),
                    activePlaylistId = data.getString("activePlaylistId"),
                    status = data.getString("status") ?: "offline",
                    createdAt = data.getTimestamp("createdAt"),
                    updatedAt = data.getTimestamp("updatedAt")
                )
            }
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    override fun getDisplayWithPlaylists(displayId: String): Flow<Pair<Display, List<Playlist>>> =
        flow {
            val displayDoc = firestore.collection("displays")
                .document(displayId)
                .get()
                .await()

            val display = Display(
                displayId = displayDoc.id,
                name = displayDoc.getString("name") ?: "",
                status = displayDoc.getString("status") ?: "offline",
                location = displayDoc.getString("location"),
                groupId = displayDoc.getString("groupId")
            )

            // ดึง playlists ที่ active บนจอนี้
            val activePlaylistId = displayDoc.getString("activePlaylistId")

            val playlists = if (activePlaylistId != null) {
                val playlistDoc = firestore.collection("playlists")
                    .document(activePlaylistId)
                    .get()
                    .await()

                if (playlistDoc.exists()) {
                    listOf(
                        Playlist(
                            id = playlistDoc.id,
                            name = playlistDoc.getString("name") ?: "",
                            items = playlistDoc.get("items") as? List<String> ?: emptyList(),
                            loop = playlistDoc.getBoolean("loop") ?: false,
                            shuffle = playlistDoc.getBoolean("shuffle") ?: false,
                            defaultIntervalMs = playlistDoc.getLong("defaultIntervalMs")?.toInt()
                                ?: 5000,
                            groupId = playlistDoc.getString("groupId"),
                            tenantId = playlistDoc.getString("tenantId") ?: ""
                        )
                    )
                } else emptyList()
            } else emptyList()

            emit(display to playlists)
        }
}
