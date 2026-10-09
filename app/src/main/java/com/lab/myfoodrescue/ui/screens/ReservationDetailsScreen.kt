package com.lab.myfoodrescue.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab.myfoodrescue.data.repository.Reservation
import com.lab.myfoodrescue.data.repository.ReservationStatus
import com.lab.myfoodrescue.data.repository.ReservationStore
import com.lab.myfoodrescue.ui.theme.FlashGreen
import com.lab.myfoodrescue.ui.theme.FlashGreenContainer
import com.lab.myfoodrescue.ui.theme.FlashGreenDark
import com.lab.myfoodrescue.ui.theme.FlashSlate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================
//  VIEW — Reservation Details (recipient). Shown right after
//  tapping Reserve, or when opening a reserved post. Live
//  status: Reserved -> Pickup Scheduled (courier sets the
//  schedule) -> Collected (courier marks delivered).
//  Collector Information and Pickup Schedule only appear once
//  the courier has scheduled a pickup, and then show the real
//  courier's account name and the chosen date/time.
// ============================================================

@Composable
fun ReservationDetailsScreen(
    reservationId: String,
    onBack: () -> Unit
) {
    val reservation = ReservationStore.reservations.firstOrNull { it.id == reservationId }
    var showStatusDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (reservation == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reservation not found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(8.dp))

            //Header: back arrow + title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Reservation Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(16.dp))

            // Reservation ID + food + progress card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reservation ID",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "#${reservation.id}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = FlashGreenContainer) {
                            Text(
                                text = statusLabel(reservation.status),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FlashGreenDark
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(12.dp))

                    //Food summary
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PostThumb(post = reservation.post, size = 64.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reservation.post.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${reservation.post.quantity} • ${reservation.post.foodType}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = reservation.post.pickupPoint,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ---- Progress steps: Reserved / Pickup Scheduled / Collected ----
                    ProgressSteps(reservation)
                }
            }

            // ---- Collector information (only after the courier scheduled) ----
            if (reservation.courierName != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Collector Information",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(FlashGreenContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = FlashGreenDark,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = reservation.courierName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Volunteer Collector",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!reservation.courierPhone.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Phone,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = reservation.courierPhone,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ---- Pickup schedule (only after the courier scheduled) ----
            if (reservation.scheduledAt != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pickup Schedule",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = formatDate(reservation.scheduledAt, "d MMM yyyy") +
                                    ", " + reservation.scheduledTime,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = reservation.post.pickupPoint,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---- Update Status ----
            Button(
                onClick = { showStatusDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FlashSlate,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = "Status", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showStatusDialog && reservation != null) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Reservation Status") },
            text = { Text(statusMessage(reservation.status)) },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) { Text("OK") }
            }
        )
    }
}

// ============================================================
//  Progress steps — Reserved (always checked) / Pickup
//  Scheduled (courier set the schedule) / Collected (courier
//  marked as delivered).
// ============================================================

@Composable
private fun ProgressSteps(reservation: Reservation) {
    val labels = listOf("Reserved", "Pickup Scheduled", "Collected")
    val done = listOf(
        true,
        reservation.status != ReservationStatus.RESERVED,
        reservation.status == ReservationStatus.COLLECTED
    )
    val dates = listOf(
        formatDatePair(reservation.reservedAt),
        if (reservation.scheduledAt != null) {
            formatDate(reservation.scheduledAt, "d MMM") to reservation.scheduledTime
        } else {
            "" to ""
        },
        reservation.collectedAt?.let { formatDatePair(it) } ?: ("" to "")
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        StepDot(done[0], Modifier.weight(1f))
        StepConnector(done[1], Modifier.weight(1f))
        StepDot(done[1], Modifier.weight(1f))
        StepConnector(done[2], Modifier.weight(1f))
        StepDot(done[2], Modifier.weight(1f))
    }

    Spacer(Modifier.height(10.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (done[index]) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (done[index]) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = dates[index].first,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = dates[index].second,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StepDot(done: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (done) FlashGreen else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StepConnector(active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (active) FlashGreen else MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Composable
private fun PostThumb(post: SurplusPost, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFB9C2C9)),
        contentAlignment = Alignment.Center
    ) {
        if (post.photoRes != null) {
            Image(
                painter = painterResource(post.photoRes),
                contentDescription = post.name,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = null,
                tint = Color(0xFFE4E9EC),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun statusLabel(status: ReservationStatus) = when (status) {
    ReservationStatus.RESERVED -> "Reserved"
    ReservationStatus.PICKUP_SCHEDULED -> "Pickup Scheduled"
    ReservationStatus.COLLECTED -> "Collected"
}

private fun statusMessage(status: ReservationStatus) = when (status) {
    ReservationStatus.RESERVED ->
        "Your reservation is confirmed. A volunteer collector will schedule " +
            "the pickup soon."
    ReservationStatus.PICKUP_SCHEDULED ->
        "The courier has scheduled your pickup. Check the schedule below."
    ReservationStatus.COLLECTED ->
        "Your food has been delivered. Enjoy!"
}

private fun formatDate(millis: Long, pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

private fun formatDatePair(millis: Long): Pair<String, String> =
    formatDate(millis, "d MMM") to formatDate(millis, "h:mm a")
