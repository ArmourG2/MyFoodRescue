package com.lab.myfoodrescue.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.lab.myfoodrescue.R
import com.lab.myfoodrescue.ui.screens.SurplusPost

// ============================================================
//  Donor listings — what the donor has posted for rescue.
//  Firestore-backed: one snapshot listener keeps `listings`
//  (a Compose-observable list) in sync across devices.
//  Flow: donor posts (Donate tab, "Available") -> recipient
//  reserves (moves to donor's Deliver tab) -> courier marks
//  delivered (moves to donor's History tab).
// ============================================================

data class DonorListing(
    val id: String = "",             // Firestore document id
    val name: String,
    val foodType: String,
    val weight: String,              // e.g. "5 kg"
    val expiryDate: String,          // e.g. "Exp: 12 Aug 2026"
    val location: String,
    val distanceKm: String,          // e.g. "2.4 km"
    val description: String,
    val photoRes: Int? = null,       // local drawable (resolved from photoKey)
    val postedAt: Long = System.currentTimeMillis(),
    val ownerId: String? = null,     // signed-in donor's uid (used by rules)
    val photoKey: String? = null     // stable photo key stored in Firestore
)

val SEED_LISTINGS = listOf(
    DonorListing(
        "DL2026001", "Fresh Vegetables", "Vegetables", "5 kg", "Exp: 12 Aug 2026",
        "Bangsar, KL", "2.4 km",
        "Leafy greens, carrots and broccoli from a neighbourhood grocer.",
        R.drawable.fresh_veg
    ),
    DonorListing(
        "DL2026002", "Bread & Pastries", "Bakery", "3 kg", "Exp: 28 Aug 2026",
        "Ampang, KL", "3.1 km",
        "Assorted butter croissants and wholemeal loaves from a bakery.",
        R.drawable.butter_crois
    ),
    DonorListing(
        "DL2026003", "Fresh Apples", "Fruits", "10 kg", "Exp: 20 Aug 2026",
        "Pudu, KL", "1.2 km",
        "Crisp red apples, gently bruised but perfectly edible.",
        R.drawable.apple
    ),
    DonorListing(
        "DL2026004", "Meals", "Prepared Meals", "6 packs", "Exp: 12 Aug 2026",
        "Klang Lama, KL", "2.8 km",
        "Vegetable curry packs from a mamak restaurant, mild spice.",
        R.drawable.veg_curry
    ),
    DonorListing(
        "DL2026005", "Fresh Milk", "Dairy", "15 cartons", "Exp: 15 Aug 2026",
        "Damansara, KL", "2.2 km",
        "Full-cream milk cartons, kept refrigerated at all times.",
        R.drawable.milk
    ),
    DonorListing(
        "DL2026006", "Steamed Buns", "Bakery", "4 kg", "Exp: 10 Aug 2026",
        "Setiawangsa, KL", "2.9 km",
        "Soft buns with kaya, red bean and char siu fillings.",
        R.drawable.steam_bun
    ),
    DonorListing(
        "DL2026007", "Fruit Yogurt Cups", "Dairy", "2 kg", "Exp: 14 Aug 2026",
        "SS2, PJ", "0.6 km",
        "Chilled strawberry and mango yogurt cups from a supermarket.",
        R.drawable.yogurt
    )
)

// ============================================================
//  Firestore serialization (photo stored as a stable key)
// ============================================================

fun DonorListing.toFirestore(): Map<String, Any?> = mapOf(
    "name" to name,
    "foodType" to foodType,
    "weight" to weight,
    "expiryDate" to expiryDate,
    "location" to location,
    "distanceKm" to distanceKm,
    "description" to description,
    "photoKey" to photoKey,
    "postedAt" to postedAt,
    "ownerId" to ownerId
)

private fun Map<String, Any?>.toListing(): DonorListing = DonorListing(
    name = get("name") as? String ?: "",
    foodType = get("foodType") as? String ?: "",
    weight = get("weight") as? String ?: "",
    expiryDate = get("expiryDate") as? String ?: "",
    location = get("location") as? String ?: "",
    distanceKm = get("distanceKm") as? String ?: "",
    description = get("description") as? String ?: "",
    photoKey = get("photoKey") as? String,
    photoRes = photoResFor(get("photoKey") as? String),
    postedAt = (get("postedAt") as? Number)?.toLong() ?: 0L,
    ownerId = get("ownerId") as? String
)

object DonorStore {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    // Optimistic initial content: the seeded demo listings.
    // Replaced wholesale by the Firestore listener on the first snapshot.
    val listings = mutableStateListOf<DonorListing>().apply { addAll(SEED_LISTINGS) }

    /** Attach the sync listener (idempotent). Call after sign-in. */
    fun start() {
        if (listener != null) return
        listener = db.collection("listings")
            .orderBy("postedAt", Query.Direction.DESCENDING) // newest first
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                val fresh = snap.documents.mapNotNull { doc ->
                    doc.data?.let { it.toListing().copy(id = doc.id) }
                }
                listings.clear()
                listings.addAll(fresh)
            }
    }

    /** Detach the listener and drop back to the local seed content. */
    fun stop() {
        listener?.remove()
        listener = null
        listings.clear()
        listings.addAll(SEED_LISTINGS)
    }

    fun restart() {
        stop()
        start()
    }

    /** New listings appear at the top of the Donate tab (all devices). */
    fun add(listing: DonorListing) {
        val docRef = db.collection("listings").document()
        val withMeta = listing.copy(
            id = docRef.id,
            ownerId = FirebaseAuth.getInstance().currentUser?.uid,
            photoKey = photoKeyFor(listing.photoRes),
            postedAt = System.currentTimeMillis()
        )
        listings.add(0, withMeta)          // optimistic; snapshot confirms it
        docRef.set(withMeta.toFirestore()) // fire-and-forget
    }

    /** "Cancel Listing" — permanently removes the post (all devices). */
    fun remove(id: String) {
        listings.removeAll { it.id == id } // optimistic
        db.collection("listings").document(id).delete()
    }
}

/** Donor listings show up in the recipient feed as normal surplus posts. */
fun DonorListing.toSurplusPost(): SurplusPost = SurplusPost(
    name = name,
    expiryDate = expiryDate,
    distanceKm = distanceKm,
    foodType = foodType,
    quantity = weight,
    location = location,
    donorName = "Flash Food Donor",
    pickupWindow = "",
    pickupPoint = location,
    description = description.ifBlank {
        "Rescued surplus food posted by a Flash Food donor."
    },
    photoRes = photoRes,
    id = id,
    kind = "listing"
)
