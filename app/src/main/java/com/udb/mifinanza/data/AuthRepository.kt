package com.udb.mifinanza.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val isSignedIn: Boolean get() = auth.currentUser != null

    suspend fun register(name: String, email: String, password: String, currency: String): Result<Unit> =
        runCatching {
            val user = auth.createUserWithEmailAndPassword(email, password).await().user
                ?: error("No se pudo crear el usuario")
            user.updateProfile(userProfileChangeRequest { displayName = name }).await()
            val profile = UserProfile(name = name, email = email, currency = currency)
            db.collection(USERS).document(user.uid).set(profile.toMap()).await()
        }

    suspend fun login(email: String, password: String): Result<Unit> =
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await()
            Unit
        }

    suspend fun sendPasswordReset(email: String): Result<Unit> =
        runCatching { auth.sendPasswordResetEmail(email).await() }

    /** Lee `users/{uid}`; si el documento aún no existe usa los datos de la cuenta. */
    suspend fun loadProfile(): Result<UserProfile> = runCatching {
        val user = auth.currentUser ?: error("Sin sesión")
        val snapshot = db.collection(USERS).document(user.uid).get().await()
        val data = snapshot.data
        if (data != null) UserProfile.fromMap(data)
        else UserProfile(name = user.displayName.orEmpty(), email = user.email.orEmpty())
    }

    fun signOut() = auth.signOut()

    private companion object {
        const val USERS = "users"
    }
}
