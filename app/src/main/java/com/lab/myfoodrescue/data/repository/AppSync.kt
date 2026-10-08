package com.lab.myfoodrescue.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.lab.myfoodrescue.ui.screens.SURPLUS_POSTS

// ============================================================
//  AppSync — single bootstrap for cross-device sync:
//   1. signs guests in anonymously so Firestore rules accept them
//   2. seeds the demo catalogue once (only if it doesn't exist)
//   3. attaches the store snapshot listeners (idempotent)
//  Stores expose Compose-observable lists, so screens keep
//  working unchanged — the data is just shared now.
// ============================================================

object AppSync {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /** Ensures a (possibly anonymous) account so Firestore rules accept reads. */
    fun ensureUser(onReady: (FirebaseUser?) -> Unit = {}) {
        val current = auth.currentUser
        if (current != null) {
            onReady(current)
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener { onReady(it.user) }
            .addOnFailureListener { onReady(null) }
    }

    /**
     * Glue called once from MainActivity: authenticates, seeds demo
     * data, then attaches the live listeners.
     */
    fun start() {
        ensureUser { _ ->
            seedIfNeeded {
                PostStore.start()
                DonorStore.start()
                ReservationStore.start()
            }
        }
    }

    /** Call when the user signs out: stop syncing so stale shared
     *  data doesn't linger, then reset for the next sign-in. */
    fun stop() {
        PostStore.stop()
        DonorStore.stop()
        ReservationStore.stop()
    }

    /** Restart the listeners after a login/account change. */
    fun onUserChanged() {
        PostStore.restart()
        DonorStore.restart()
        ReservationStore.restart()
    }

    // ---------------------------------------------------------
    //  One-time seeding of the demo catalogue
    // ---------------------------------------------------------

    private fun seedIfNeeded(onDone: () -> Unit) {
        // Marker document acts as the "already seeded" flag.
        val marker = db.collection("meta").document("seed")
        marker.get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                onDone()
            } else {
                seed()
                marker.set(mapOf("at" to System.currentTimeMillis()))
                onDone()
            }
        }
    }

    private fun seed() {
        val posts = db.collection("posts")
        val listings = db.collection("listings")

        // ---- Restaurant feed posts (keep catalogue order) ----
        SURPLUS_POSTS.forEachIndexed { index, post ->
            posts.document(post.name)
                .set(post.toFirestore() + mapOf("sortOrder" to index))
        }

        // ---- Donor demo listings ----
        SEED_LISTINGS.forEach { listing ->
            listings.document(listing.id)
                .set(listing.copy(photoKey = photoKeyFor(listing.photoRes)).toFirestore())
        }
    }
}
