package com.roastkoff.controlposter.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.roastkoff.controlposter.common.ControlPreferences
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface PlaylistRepository {
    suspend fun createPlaylist(
        name: String,
        loop: Boolean,
        shuffle: Boolean,
        defaultIntervalMs: Int,
        groupId: String
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
        groupId: String
    ): String {
        val playlistData = hashMapOf(
            "tenantId" to prefs.tenantId,
            "name" to name,
            "loop" to loop,
            "shuffle" to shuffle,
            "defaultIntervalMs" to defaultIntervalMs,
            "items" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "groupId" to groupId
        )

        val docRef = firestore.collection("playlists")
            .add(playlistData)
            .await()

        return docRef.id
    }
}