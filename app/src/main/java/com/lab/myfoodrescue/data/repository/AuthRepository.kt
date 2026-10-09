package com.lab.myfoodrescue.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await


//  MODEL — user data + the single source of truth for auth ops


data class UserProfile(
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String? = null      // "RECIPIENT" | "COURIER" | "DONOR"
)

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun logout() = auth.signOut()

    /** Signs in with email/password and returns the signed-in user. */
    suspend fun login(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw IllegalStateException("Login failed: no user returned.")
    }

    /**
     * Creates the Auth account, sets the display name, then stores in Firestore
     */
    suspend fun signUp(user: UserProfile, password: String): FirebaseUser {
        val created = auth.createUserWithEmailAndPassword(user.email, password).await().user
            ?: throw IllegalStateException("Sign up failed: no user returned.")

        created.updateProfile(
            userProfileChangeRequest { displayName = user.username }
        ).await()

        db.collection("users").document(created.uid).set(
            mapOf(
                "username" to user.username,
                "email" to user.email,
                "phone" to user.phone,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()

        return created
    }

    /** Reads the saved profile from Firestore. */
    suspend fun getUserProfile(uid: String): UserProfile {
        val snapshot = db.collection("users").document(uid).get().await()
        return UserProfile(
            username = snapshot.getString("username").orEmpty(),
            email = snapshot.getString("email").orEmpty(),
            phone = snapshot.getString("phone").orEmpty(),
            role = snapshot.getString("role")
        )
    }

    /** Stores the chosen role on the user's profile (merge, so the other
     *  fields survive). Returns the refreshed profile. */
    suspend fun updateRole(uid: String, role: String): UserProfile {
        db.collection("users").document(uid)
            .set(mapOf("role" to role), SetOptions.merge()).await()
        return getUserProfile(uid)
    }
}