package com.roastkoff.controlposter.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.roastkoff.controlposter.common.ControlPreferences
import com.roastkoff.controlposter.data.model.Display
import com.roastkoff.controlposter.data.model.DisplayDto
import com.roastkoff.controlposter.data.model.Playlist
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface DisplayRepository {
    suspend fun createDisplay(
        groupId: String,
        name: String,
        location: String?,
        code: String
    ): String

    fun displaysByTenant(tenantId: String): Flow<List<Pair<String, DisplayDto>>>
    fun displaysByGroup(tenantId: String, groupId: String?): Flow<List<Pair<String, DisplayDto>>>

    fun getDisplayWithPlaylists(displayId: String): Flow<Pair<Display, List<Playlist>>>

    suspend fun setActivePlaylist(displayId: String, playlistId: String)
}

class DisplayRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val preferences: ControlPreferences
) : DisplayRepository {

    override suspend fun createDisplay(
        groupId: String,
        name: String,
        location: String?,
        code: String
    ): String {
        val sessionRef = firestore.collection("pairingSessions").document(code)
        val sessionSnap = sessionRef.get().await()
        require(sessionSnap.exists()) { "ยังไม่มีโค้ดนี้ในระบบ" }
        val status = sessionSnap.getString("status") ?: "pending"
        if (status != "pending") {
            throw IllegalStateException("โค้ดนี้ถูกใช้แล้วหรือหมดอายุแล้ว")
        }
        val displayRef = firestore.collection("displays").document()
        val tenantId = preferences.tenantId.firstOrNull()
        val data = hashMapOf(
            "tenantId" to tenantId,
            "groupId" to groupId,
            "name" to name,
            "location" to location.orEmpty(),
            "status" to "offline",
            "activePlaylistId" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        displayRef.set(data).await()

        sessionRef.update(
            mapOf(
                "displayId" to displayRef.id,
                "tenantId" to tenantId,
                "groupId" to groupId,
                "status" to "claimed",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        return displayRef.id
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

            val tenantId = displayDoc.getString("tenantId")

            val playlists = if (!display.groupId.isNullOrBlank()) {
                val playlistsSnapshot = firestore.collection("playlists")
                    .whereEqualTo("tenantId", tenantId)
                    .whereEqualTo("groupId", display.groupId)
                    .get()
                    .await()

                playlistsSnapshot.documents.mapNotNull { doc ->
                    try {
                        Playlist(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            loop = doc.getBoolean("loop") ?: false,
                            shuffle = doc.getBoolean("shuffle") ?: false,
                            defaultIntervalMs = doc.getLong("defaultIntervalMs")?.toInt() ?: 5000,
                            groupId = doc.getString("groupId"),
                            tenantId = doc.getString("tenantId") ?: "",
                            isActive = doc.getBoolean("active") ?: false
                        )
                    } catch (e: Exception) {
                        Log.e("DisplayRepo", "Error parsing playlist ${doc.id}", e)
                        null
                    }
                }
            } else {
                emptyList()
            }

            emit(display to playlists)
        }

    override suspend fun setActivePlaylist(displayId: String, playlistId: String) {
        firestore.collection("displays")
            .document(displayId)
            .update(
                mapOf(
                    "activePlaylistId" to playlistId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }
}
