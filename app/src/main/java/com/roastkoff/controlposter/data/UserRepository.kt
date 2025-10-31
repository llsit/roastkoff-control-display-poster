package com.roastkoff.controlposter.data

import com.google.firebase.firestore.FirebaseFirestore
import com.roastkoff.controlposter.data.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface UserRepository {
    suspend fun ensureProfile(uid: String): Result<UserProfile>
}

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun ensureProfile(uid: String): Result<UserProfile> {
        return try {
            val document = firestore.collection(COLLECTION_USERS)
                .document(uid)
                .get()
                .await()

            if (!document.exists()) {
                return Result.failure(ProfileException.NotFound)
            }

            val data = document.data ?: return Result.failure(ProfileException.InvalidData)

            validateProfileData(data)?.let { error ->
                return Result.failure(error)
            }

            val profile = UserProfile(
                uid = uid,
                email = data[FIELD_EMAIL] as? String ?: "",
                displayName = data[FIELD_DISPLAY_NAME] as? String,
                tenantId = data[FIELD_TENANT_ID] as String,
                role = data[FIELD_ROLE] as? String ?: DEFAULT_ROLE
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validateProfileData(data: Map<String, Any>): ProfileException? {
        return when {
            data[FIELD_DISABLED] == true -> ProfileException.AccountDisabled
            data[FIELD_TENANT_ID] == null -> ProfileException.NoTenant
            else -> null
        }
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val FIELD_EMAIL = "email"
        private const val FIELD_DISPLAY_NAME = "displayName"
        private const val FIELD_TENANT_ID = "tenantId"
        private const val FIELD_ROLE = "role"
        private const val FIELD_DISABLED = "disabled"
        private const val DEFAULT_ROLE = "staff"
    }
}

sealed class ProfileException(message: String) : Exception(message) {
    object NotFound : ProfileException("This account has no profile. Please contact admin.")
    object AccountDisabled : ProfileException("This account is disabled.")
    object NoTenant : ProfileException("No tenant assigned to this account.")
    object InvalidData : ProfileException("Invalid profile data.")
}