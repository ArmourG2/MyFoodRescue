package com.lab.myfoodrescue.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.lab.myfoodrescue.R
import com.lab.myfoodrescue.ui.screens.SurplusPost

// ============================================================
//  Donor listings — what the donor has posted for rescue.
//  Flow: donor posts (Donate tab, "Available") -> recipient
//  reserves (moves to donor's Deliver tab) -> courier marks
//  delivered (moves to donor's History tab). In-memory prototype.
// ============================================================

data class DonorListing(
    val id: String,              // "DL2026001", ...
    val name: String,
    val foodType: String,
    val weight: String,          // e.g. "5 kg"
    val expiryDate: String,      // e.g. "Exp: 12 Aug 2026"
    val location: String,
    val distanceKm: String,      // e.g. "2.4 km"
    val description: String,
    val photoRes: Int? = null,
    val postedAt: Long = System.currentTimeMillis()
)

private val SEED_LISTINGS = listOf(
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

object DonorStore {

    private var idCounter = 0

    val listings = mutableStateListOf<DonorListing>().apply {
        addAll(SEED_LISTINGS)
    }

    fun nextId(): String = "DL2026" + (++idCounter).toString().padStart(3, '0')

    /** New listings appear at the top of the Donate tab. */
    fun add(listing: DonorListing) {
        listings.add(0, listing)
    }

    /** "Cancel Listing" — permanently removes the post. */
    fun remove(id: String) {
        listings.removeAll { it.id == id }
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
    id = id
)
