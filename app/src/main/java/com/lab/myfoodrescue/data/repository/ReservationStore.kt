package com.lab.myfoodrescue.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.lab.myfoodrescue.ui.screens.SurplusPost

// ============================================================
//  Reservation flow — a recipient reserves a post (status
//  RESERVED), the post appears in the courier's Assigned
//  Pickups. Courier taps "Pick Up" -> PICKUP_SCHEDULED.
//  Courier taps "Mark as Delivered" -> COLLECTED (recipient
//  history). In-memory for the prototype; swap for Firestore later.
// ============================================================

enum class ReservationStatus { RESERVED, PICKUP_SCHEDULED, COLLECTED }

data class CollectorInfo(
    val name: String = "Ahmad Rahman",
    val title: String = "Volunteer Collector",
    val phone: String = "+60 12-345 6789"
)

data class Reservation(
    val id: String,
    val post: SurplusPost,
    val status: ReservationStatus = ReservationStatus.RESERVED,
    val reservedAt: Long = System.currentTimeMillis(),
    val pickedUpAt: Long? = null,
    val collectedAt: Long? = null,
    val collector: CollectorInfo = CollectorInfo()
)

object ReservationStore {

    // Reservation IDs are generated automatically: FR20260047, FR20260048, ...
    // (starts after the two hardcoded courier pickups FR20260045/46)
    private var idCounter = 46

    val reservations = mutableStateListOf<Reservation>()

    fun nextId(): String = "FR2026" + (++idCounter).toString().padStart(4, '0')

    /** Recipient taps Reserve: creates a reservation and shows it in the courier feed. */
    fun reserve(post: SurplusPost): Reservation {
        val reservation = Reservation(id = nextId(), post = post)
        reservations.add(reservation)
        return reservation
    }

    /** Current status of a reservation ("COLLECTED" if it no longer exists). */
    fun statusOf(id: String): ReservationStatus =
        reservations.firstOrNull { it.id == id }?.status ?: ReservationStatus.COLLECTED

    /** Courier taps Pick Up. */
    fun markPickedUp(id: String) = update(id) {
        it.copy(
            status = ReservationStatus.PICKUP_SCHEDULED,
            pickedUpAt = System.currentTimeMillis()
        )
    }

    /** Courier taps Mark as Delivered. */
    fun markCollected(id: String) = update(id) {
        it.copy(
            status = ReservationStatus.COLLECTED,
            collectedAt = System.currentTimeMillis()
        )
    }

    private inline fun update(id: String, transform: (Reservation) -> Reservation) {
        val index = reservations.indexOfFirst { it.id == id }
        if (index >= 0) reservations[index] = transform(reservations[index])
    }
}
