package com.roastkoff.controlposter.data

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.roastkoff.controlposter.common.ControlPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
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

    suspend fun addItemToPlaylist(
        playlistId: String,
        itemName: String,
        itemType: String,
        durationMs: Int,
        fit: String,
        mute: Boolean,
        mediaUri: Uri,
    )

    fun getPlaylistItem(playlistId: String, itemId: String): Flow<PlaylistItem>
    suspend fun deletePlaylistItem(playlistId: String, itemId: String)

    suspend fun updateActivePlaylist(
        playlistId: String,
        groupId: String,
        displayId: String,
        isActive: Boolean
    )
}

class PlaylistRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val prefs: ControlPreferences,
    private val storage: FirebaseStorage
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
            isActive = false
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
        val items = itemsRaw.map { mapPlaylistItem(it) }

        val data = PlaylistData(
            tenantId = playlistDoc.getString("tenantId") ?: "",
            groupId = playlistDoc.getString("groupId") ?: "",
            displayId = playlistDoc.getString("groupId") ?: "",
            name = playlistDoc.getString("groupId") ?: "",
            loop = playlistDoc.getBoolean("loop") ?: false,
            shuffle = playlistDoc.getBoolean("shuffle") ?: false,
            defaultIntervalMs = playlistDoc.getLong("defaultIntervalMs")?.toInt() ?: 0,
            items = items,
            isActive = playlistDoc.getBoolean("active") ?: false
        )

        emit(data)
    }

    override suspend fun addItemToPlaylist(
        playlistId: String,
        itemName: String,
        itemType: String,
        durationMs: Int,
        fit: String,
        mute: Boolean,
        mediaUri: Uri
    ) {
        val fileExtension = when (itemType) {
            "image" -> "jpg"
            "video" -> "mp4"
            else -> "bin"
        }

        val fileName = "${UUID.randomUUID()}.$fileExtension"
//        val storageRef = storage.reference
//            .child("playlists")
//            .child(playlistId)
//            .child(fileName)
//
//        storageRef.putFile(mediaUri).await()
//
//        val downloadUrl = storageRef.downloadUrl.await().toString()

        val newItem = PlaylistItem(
            id = "item_${System.currentTimeMillis()}",
            durationMs = durationMs,
            name = itemName,
            fit = fit,
            mute = mute,
            src = "downloadUrl",
            type = itemType
        )

        firestore.collection("playlists")
            .document(playlistId)
            .update(
                mapOf(
                    "items" to FieldValue.arrayUnion(newItem),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    override fun getPlaylistItem(
        playlistId: String,
        itemId: String
    ): Flow<PlaylistItem> = flow {
        val playlistDoc = firestore.collection(COLLECTION_PLAYLISTS)
            .document(playlistId)
            .get()
            .await()

        val itemsRaw = playlistDoc.get("items") as? List<Map<String, Any>> ?: emptyList()
        val items = itemsRaw.map { mapPlaylistItem(it) }

        val item = items.firstOrNull { it.id == itemId }
            ?: throw NoSuchElementException("Playlist item $itemId not found in playlist $playlistId")

        emit(item)
    }

    override suspend fun deletePlaylistItem(playlistId: String, itemId: String) {
        val playlistRef = firestore.collection(COLLECTION_PLAYLISTS)
            .document(playlistId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(playlistRef)

            val itemsRaw = snapshot.get("items") as? List<Map<String, Any>> ?: emptyList()
            val items = itemsRaw.map { mapPlaylistItem(it) }

            val updatedItems = items.filterNot { it.id == itemId }

            transaction.update(
                playlistRef,
                mapOf(
                    "items" to updatedItems,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            null
        }.await()
    }

    override suspend fun updateActivePlaylist(
        playlistId: String,
        groupId: String,
        displayId: String,
        isActive: Boolean
    ) {
        val tenantId = prefs.tenantId.first()
            ?: throw IllegalStateException("Tenant ID not found")
        val ref = firestore.collection("playlists")

        if (!isActive) {
            ref.document(playlistId).update("active", false).await()
            return
        }
        val activeSnapshot = ref
            .whereEqualTo("tenantId", tenantId)
            .whereEqualTo("displayId", displayId)
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("active", true)
            .get()
            .await()
        activeSnapshot.documents.forEach { doc ->
            ref.document(doc.id).update("active", false).await()
        }

        ref.document(playlistId).update("active", true).await()
    }
}

private fun mapPlaylistItem(itemMap: Map<String, Any>): PlaylistItem {
    return PlaylistItem(
        id = itemMap["id"] as? String ?: "",
        durationMs = (itemMap["durationMs"] as? Long)?.toInt() ?: 7000,
        name = itemMap["name"] as? String ?: "",
        fit = itemMap["fit"] as? String ?: "cover",
        mute = itemMap["mute"] as? Boolean ?: true,
        src = itemMap["src"] as? String ?: "",
        type = itemMap["type"] as? String ?: "image"
    )
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
    val updatedAt: FieldValue? = null,
    val isActive: Boolean = false
)

data class PlaylistItem(
    val id: String = "",
    val durationMs: Int = 7000,
    val name: String = "",
    val fit: String = "cover",
    val mute: Boolean = true,
    val src: String = "",
    val type: String = "image"
)