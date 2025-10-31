package com.roastkoff.controlposter.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface AuthRepository {
    fun observeAuthState(): Flow<String?>
    suspend fun signIn(email: String, password: String): Result<String>
    fun signOut()
    suspend fun sendPasswordReset(email: String): Result<Unit>
}

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun observeAuthState(): Flow<String?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val uid = authResult.user?.uid

            if (uid != null) {
                Result.success(uid)
            } else {
                Result.failure(AuthException("User ID not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            require(email.isNotBlank()) { "Email cannot be blank" }

            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class AuthException(message: String) : Exception(message)