package com.lab.myfoodrescue.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.lab.myfoodrescue.ui.screens.SurplusPost

// ============================================================
//  Reservation flow — a recipient reserves a post (RESERVED),
//  the post appears in the courier's Assigned Pickups. Courier
//  taps "Pick Schedule", sets date + time -> PICKUP_SCHEDULED
//  (the reservation shows the real courier's info and the set
//  schedule). Courier taps "Mark as Delivered" -> COLLECTED
//  (recipient history). In-memory for the prototype.
// ============================================================

enum class ReservationStatus { RESERVED, PICKUP_SCHEDULED, COLLECTED }

data class Reservation(
    val id: String,
    val post: SurplusPost,
    val recipientName: String = "",     // account name of who reserved
    val status: ReservationStatus = ReservationStatus.RESERVED,
    val reservedAt: Long = System.currentTimeMillis(),
    val scheduledAt: Long? = null,      // courier-scheduled pickup date
    val scheduledTime: String = "",     // courier-scheduled time, e.g. "10:00 AM"
    val collectedAt: Long? = null,
    val courierName: String? = null,    // actual courier account name
    val courierPhone: String? = null    // courier's profile phone (optional)
)

object ReservationStore {

    // Reservation IDs are generated automatically: FR20260047, FR20260048, ...
    // (starts after the two hardcoded courier pickups FR20260045/46)
    private var idCounter = 46

    val reservations = mutableStateListOf<Reservation>()

    fun nextId(): String = "FR2026" + (++idCounter).toString().padStart(4, '0')

    /** Recipient taps Reserve: creates a reservation (carrying the
     *  recipient's account name) and shows it in the courier feed. */
    fun reserve(post: SurplusPost, recipientName: String): Reservation {
        val reservation = Reservation(
            id = nextId(),
            post = post,
            recipientName = recipientName
        )
        reservations.add(reservation)
        return reservation
    }

    /** Current status of a reservation ("COLLECTED" if it no longer exists). */
    fun statusOf(id: String): ReservationStatus =
        reservations.firstOrNull { it.id == id }?.status ?: ReservationStatus.COLLECTED

    /** Courier sets the pickup date + time on the scheduling screen. */
    fun schedulePickup(
        id: String,
        scheduledAtMillis: Long,
        time: String,
        courierName: String,
        courierPhone: String?
    ) = update(id) {
        it.copy(
            status = ReservationStatus.PICKUP_SCHEDULED,
            scheduledAt = scheduledAtMillis,
            scheduledTime = time,
            courierName = courierName,
            courierPhone = courierPhone
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
