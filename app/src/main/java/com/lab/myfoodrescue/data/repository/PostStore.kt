package com.lab.myfoodrescue.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.lab.myfoodrescue.ui.screens.SURPLUS_POSTS
import com.lab.myfoodrescue.ui.screens.SurplusPost

// ============================================================
//  Restaurant feed posts — Firestore-backed. One snapshot
//  listener keeps `posts` (a Compose-observable list) in sync
//  across devices. Seeded once with the built-in demo posts
//  (AppSync). Photos sync as stable keys (see FoodPhotos).
// ============================================================

/** Serializes a SurplusPost into a Firestore document map. */
fun SurplusPost.toFirestore(): Map<String, Any?> = mapOf(
    "name" to name,
    "expiryDate" to expiryDate,
    "distanceKm" to distanceKm,
    "foodType" to foodType,
    "quantity" to quantity,
    "location" to location,
    "donorName" to donorName,
    "pickupWindow" to pickupWindow,
    "pickupPoint" to pickupPoint,
    "description" to description,
    "photoKey" to photoKeyFor(photoRes),
    "kind" to kind
)

/** Rebuilds a SurplusPost from a Firestore document map. */
fun Map<String, Any?>.toSurplusPost(id: String = ""): SurplusPost = SurplusPost(
    name = get("name") as? String ?: "",
    expiryDate = get("expiryDate") as? String ?: "",
    distanceKm = get("distanceKm") as? String ?: "",
    foodType = get("foodType") as? String ?: "",
    quantity = get("quantity") as? String ?: "",
    location = get("location") as? String ?: "",
    donorName = get("donorName") as? String ?: "",
    pickupWindow = get("pickupWindow") as? String ?: "",
    pickupPoint = get("pickupPoint") as? String ?: "",
    description = get("description") as? String ?: "",
    photoRes = photoResFor(get("photoKey") as? String),
    id = id,
    kind = get("kind") as? String ?: "post"
)

object PostStore {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    // Optimistic initial content: the built-in demo posts.
    // Replaced wholesale by the Firestore listener on the first snapshot.
    val posts = mutableStateListOf<SurplusPost>().apply { addAll(SURPLUS_POSTS) }

    /** Attach the sync listener (idempotent). Call after sign-in. */
    fun start() {
        if (listener != null) return
        listener = db.collection("posts")
            .orderBy("sortOrder", Query.Direction.ASCENDING) // keep catalogue order
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                val fresh = snap.documents.mapNotNull { doc ->
                    doc.data?.let { it.toSurplusPost(id = doc.id) }
                }
                posts.clear()
                posts.addAll(fresh)
            }
    }

    /** Detach the listener and drop back to the local demo posts. */
    fun stop() {
        listener?.remove()
        listener = null
        posts.clear()
        posts.addAll(SURPLUS_POSTS)
    }

    fun restart() {
        stop()
        start()
    }
}
