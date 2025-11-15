package com.roastkoff.controlposter.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.roastkoff.controlposter.common.ControlPreferences
import kotlinx.coroutines.flow.first
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
}

class PlaylistRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val prefs: ControlPreferences
) : PlaylistRepository {

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

//        val playlistData = hashMapOf(
//            "tenantId" to tenantId,
//            "name" to name,
//            "loop" to loop,
//            "shuffle" to shuffle,
//            "defaultIntervalMs" to defaultIntervalMs,
//            "items" to emptyList<String>(),
//            "createdAt" to FieldValue.serverTimestamp(),
//            "updatedAt" to FieldValue.serverTimestamp(),
//            "groupId" to groupId,
//            ""
//        )

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

        val docRef = firestore.collection("playlists")
            .add(playlistData)
            .await()

        return docRef.id
    }
}

data class PlaylistData(
    val tenantId: String,
    val groupId: String,
    val displayId: String,
    val name: String,
    val loop: Boolean,
    val shuffle: Boolean,
    val defaultIntervalMs: Int,
    val items: List<String>,
    val createdAt: FieldValue,
    val updatedAt: FieldValue
)