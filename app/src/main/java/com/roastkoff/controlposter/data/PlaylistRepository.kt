package com.roastkoff.controlposter.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.roastkoff.controlposter.common.ControlPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface PlaylistRepository {
    suspend fun createPlaylist(
        name: String,
        loop: Boolean,
        shuffle: Boolean,
        defaultIntervalMs: Int,
        groupId: String,
        displayId: String
    ): String

    fun getPlaylist(playlistId: String): Flow<PlaylistData>
}

class PlaylistRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val prefs: ControlPreferences
) : PlaylistRepository {

    companion object {
        private const val COLLECTION_PLAYLISTS = "playlists"
    }

    override suspend fun createPlaylist(
        name: String,
        loop: Boolean,
        shuffle: Boolean,
        defaultIntervalMs: Int,
        groupId: String,
        displayId: String
    ): String {
        val tenantId = prefs.tenantId.first()
            ?: throw IllegalStateException("Tenant ID not found")

        val playlistData = PlaylistData(
            tenantId = tenantId,
            name = name,
            groupId = groupId,
            displayId = displayId,
            loop = loop,
            shuffle = shuffle,
            defaultIntervalMs = defaultIntervalMs,
            items = emptyList(),
            createdAt = FieldValue.serverTimestamp(),
            updatedAt = FieldValue.serverTimestamp(),
        )

        val docRef = firestore.collection(COLLECTION_PLAYLISTS)
            .add(playlistData)
            .await()

        return docRef.id
    }

    override fun getPlaylist(playlistId: String): Flow<PlaylistData> = flow {
        val playlistDoc = firestore.collection(COLLECTION_PLAYLISTS)
            .document(playlistId)
            .get()
            .await()

        val itemsRaw = playlistDoc.get("items") as? List<Map<String, Any>> ?: emptyList()

        val items = itemsRaw.map { itemMap ->
            PlaylistItem(
                id = itemMap["id"] as? String ?: "",
                durationMs = (itemMap["durationMs"] as? Long)?.toInt() ?: 7000,
                fit = itemMap["fit"] as? String ?: "cover",
                mute = itemMap["mute"] as? Boolean ?: true,
                src = itemMap["src"] as? String ?: "",
                type = itemMap["type"] as? String ?: "image"
            )
        }

        val data = PlaylistData(
            tenantId = playlistDoc.getString("tenantId") ?: "",
            groupId = playlistDoc.getString("groupId") ?: "",
            displayId = playlistDoc.getString("groupId") ?: "",
            name = playlistDoc.getString("groupId") ?: "",
            loop = playlistDoc.getBoolean("loop") ?: false,
            shuffle = playlistDoc.getBoolean("shuffle") ?: false,
            defaultIntervalMs = playlistDoc.getLong("defaultIntervalMs")?.toInt() ?: 0,
            items = items
        )

        emit(data)
    }
}

data class PlaylistData(
    val tenantId: String = "",
    val groupId: String = "",
    val displayId: String = "",
    val name: String = "",
    val loop: Boolean = false,
    val shuffle: Boolean = false,
    val defaultIntervalMs: Int = 0,
    val items: List<PlaylistItem> = emptyList(),
    val createdAt: FieldValue? = null,
    val updatedAt: FieldValue? = null
)

data class PlaylistItem(
    val id: String = "",
    val durationMs: Int = 7000,
    val fit: String = "cover",
    val mute: Boolean = true,
    val src: String = "",
    val type: String = "image"
)