package com.lab.myfoodrescue.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.lab.myfoodrescue.ui.screens.SurplusPost

// ============================================================
//  Reservation flow — a recipient reserves a post (RESERVED),
//  the post appears in the courier's Assigned Pickups. Courier
//  taps "Pick Schedule", sets date + time -> PICKUP_SCHEDULED
//  (the reservation shows the real courier's info and the set
//  schedule). Courier taps "Mark as Delivered" -> COLLECTED
//  (recipient history).
//  Firestore-backed: one snapshot listener syncs `reservations`
//  across devices; writes are targeted updates so two devices
//  never stomp on each other's fields.
// ============================================================

enum class ReservationStatus { RESERVED, PICKUP_SCHEDULED, COLLECTED }

data class Reservation(
    val id: String = "",                // Firestore document id
    val code: String = "",              // display code, e.g. "FR20260047"
    val post: SurplusPost,
    val recipientName: String = "",     // account name of who reserved
    val recipientId: String? = null,    // uid of who reserved (used by rules)
    val status: ReservationStatus = ReservationStatus.RESERVED,
    val reservedAt: Long = System.currentTimeMillis(),
    val scheduledAt: Long? = null,      // courier-scheduled pickup date
    val scheduledTime: String = "",     // courier-scheduled time, e.g. "10:00 AM"
    val collectedAt: Long? = null,
    val courierName: String? = null,    // actual courier account name
    val courierPhone: String? = null    // courier's profile phone (optional)
)

// ============================================================
//  Firestore serialization (post embedded, photo as stable key)
// ============================================================

private fun Reservation.toFirestore(): Map<String, Any?> = mapOf(
    "code" to code,
    "recipientId" to recipientId,
    "recipientName" to recipientName,
    "status" to status.name,
    "reservedAt" to reservedAt,
    "scheduledAt" to scheduledAt,
    "scheduledTime" to scheduledTime,
    "collectedAt" to collectedAt,
    "courierName" to courierName,
    "courierPhone" to courierPhone,
    "post" to post.toFirestore()
)

private fun Map<String, Any?>.toReservation(): Reservation {
    val postMap = get("post") as? Map<String, Any?> ?: emptyMap()
    return Reservation(
        code = get("code") as? String ?: "",
        recipientId = get("recipientId") as? String,
        recipientName = get("recipientName") as? String ?: "",
        status = try {
            ReservationStatus.valueOf(get("status") as? String ?: "RESERVED")
        } catch (_: IllegalArgumentException) {
            ReservationStatus.RESERVED
        },
        reservedAt = (get("reservedAt") as? Number)?.toLong() ?: 0L,
        scheduledAt = (get("scheduledAt") as? Number)?.toLong(),
        scheduledTime = get("scheduledTime") as? String ?: "",
        collectedAt = (get("collectedAt") as? Number)?.toLong(),
        courierName = get("courierName") as? String,
        courierPhone = get("courierPhone") as? String,
        post = postMap.toSurplusPost(id = "")
    )
}

/** "#FR20260047" display code, falling back to the document id. */
val Reservation.displayCode: String get() = code.ifBlank { id }

object ReservationStore {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    // Display codes (FR20260047, FR20260048, ...) continue after the
    // courier demo pickups FR20260045/46. Cosmetic only — the real
    // identity is the Firestore document id.
    private var codeCounter = 46

    val reservations = mutableStateListOf<Reservation>()

    private fun nextCode(): String = "FR2026" + (++codeCounter).toString().padStart(4, '0')

    /** Attach the sync listener (idempotent). Call after sign-in. */
    fun start() {
        if (listener != null) return
        listener = db.collection("reservations")
            .orderBy("reservedAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                val fresh = snap.documents.mapNotNull { doc ->
                    doc.data?.let { it.toReservation().copy(id = doc.id) }
                }
                reservations.clear()
                reservations.addAll(fresh)
            }
    }

    /** Detach the listener and clear local state. */
    fun stop() {
        listener?.remove()
        listener = null
        reservations.clear()
    }

    fun restart() {
        stop()
        start()
    }

    /** Recipient taps Reserve: creates the reservation in Firestore so it
     *  appears in the courier feed on every device. */
    fun reserve(post: SurplusPost, recipientName: String): Reservation {
        val docRef = db.collection("reservations").document()
        val reservation = Reservation(
            id = docRef.id,
            code = nextCode(),
            post = post,
            recipientName = recipientName,
            recipientId = FirebaseAuth.getInstance().currentUser?.uid
        )
        reservations.add(reservation)        // optimistic; snapshot confirms it
        docRef.set(reservation.toFirestore()) // fire-and-forget
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
    ) {
        updateLocal(id) {
            it.copy(
                status = ReservationStatus.PICKUP_SCHEDULED,
                scheduledAt = scheduledAtMillis,
                scheduledTime = time,
                courierName = courierName,
                courierPhone = courierPhone
            )
        }
        updateRemote(
            id,
            mapOf(
                "status" to ReservationStatus.PICKUP_SCHEDULED.name,
                "scheduledAt" to scheduledAtMillis,
                "scheduledTime" to time,
                "courierName" to courierName,
                "courierPhone" to courierPhone
            )
        )
    }

    /** Courier taps Mark as Delivered. */
    fun markCollected(id: String) {
        updateLocal(id) {
            it.copy(
                status = ReservationStatus.COLLECTED,
                collectedAt = System.currentTimeMillis()
            )
        }
        updateRemote(
            id,
            mapOf(
                "status" to ReservationStatus.COLLECTED.name,
                "collectedAt" to System.currentTimeMillis()
            )
        )
    }

    private inline fun updateLocal(id: String, transform: (Reservation) -> Reservation) {
        val index = reservations.indexOfFirst { it.id == id }
        if (index >= 0) reservations[index] = transform(reservations[index])
    }

    private fun updateRemote(id: String, fields: Map<String, Any?>) {
        db.collection("reservations").document(id).update(fields)
    }
}
