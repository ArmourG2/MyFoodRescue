package com.lab.myfoodrescue.data.repository

import com.lab.myfoodrescue.R

// ============================================================
//  Photo keys — photos are local drawable resources, so a raw
//  resource id is meaningless on another device. Instead we
//  store a stable key string in Firestore and resolve it back
//  to the drawable on every device.
// ============================================================

val PHOTO_BY_KEY: Map<String, Int> = mapOf(
    "fresh_veg" to R.drawable.fresh_veg,
    "chicken_rice" to R.drawable.chicken_rice,
    "butter_crois" to R.drawable.butter_crois,
    "apple" to R.drawable.apple,
    "milk" to R.drawable.milk,
    "veg_curry" to R.drawable.veg_curry,
    "steam_bun" to R.drawable.steam_bun,
    "yogurt" to R.drawable.yogurt,
    "banana" to R.drawable.banana,
    "beras" to R.drawable.beras,
    "burger" to R.drawable.burger,
    "lekor" to R.drawable.lekor,
    "meggi_kari" to R.drawable.meggi_kari,
    "roti_canai" to R.drawable.roti_canai,
    "sardine" to R.drawable.sardine,
    "tepung" to R.drawable.tepung,
    "banana_bread" to R.drawable.banana_bread,
    "sushi_platter" to R.drawable.sushi_platter,
    "sour_bread" to R.drawable.sour_bread,
    "roasted_chick" to R.drawable.roasted_chick,
    "noodle" to R.drawable.noodle
)

/** Reverse lookup: drawable resource -> stable key stored in Firestore. */
fun photoKeyFor(res: Int?): String? =
    res?.let { r -> PHOTO_BY_KEY.entries.firstOrNull { it.value == r }?.key }

/** Resolve a stored key back to a drawable resource (null = placeholder). */
fun photoResFor(key: String?): Int? = key?.let { PHOTO_BY_KEY[it] }
