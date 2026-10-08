package com.lab.myfoodrescue.ui.screens.donor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.lab.myfoodrescue.data.repository.DonorListing
import com.lab.myfoodrescue.data.repository.DonorStore
import com.lab.myfoodrescue.data.repository.Reservation
import com.lab.myfoodrescue.data.repository.ReservationStatus
import com.lab.myfoodrescue.data.repository.ReservationStore
import com.lab.myfoodrescue.data.repository.displayCode
import com.lab.myfoodrescue.ui.screens.ProfileScreen
import com.lab.myfoodrescue.ui.screens.ReservationDetailsScreen
import com.lab.myfoodrescue.ui.screens.UserRole
import com.lab.myfoodrescue.ui.theme.FlashGreen
import com.lab.myfoodrescue.ui.theme.FlashGreenContainer
import com.lab.myfoodrescue.ui.theme.FlashGreenDark
import com.lab.myfoodrescue.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================
//  DONOR — app shell: bottom nav with
//  Donate | Deliver | History | Profile  +  FAB (List Food)
//  Donate: the donor's listed food (click -> Cancel Listing).
//  Deliver: listings reserved by recipients (live status).
//  History: listings the courier marked as delivered.
// ============================================================

private data class DonorNavItem(
    val label: String,
    val icon: ImageVector
)

@Composable
fun DonorAppScreen(
    onLoggedOut: () -> Unit,
    onSwitchRole: (UserRole) -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val items = remember {
        listOf(
            DonorNavItem("Donate", Icons.Rounded.Eco),
            DonorNavItem("Deliver", Icons.Filled.LocalShipping),
            DonorNavItem("History", Icons.Filled.History),
            DonorNavItem("Profile", Icons.Filled.Person)
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showListFood by remember { mutableStateOf(false) }
    var cancelListing by remember { mutableStateOf<DonorListing?>(null) }
    var selectedReservationId by remember { mutableStateOf<String?>(null) }

    // ---- Greeting name ----
    val state by authViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { authViewModel.loadProfile() }
    val donorName = state.profile?.username?.takeIf { it.isNotBlank() }
        ?: FirebaseAuth.getInstance().currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: "Donor"

    // Donor listings that have a reservation, by status.
    // Reserved posts relocate from Donate to Deliver; delivered ones
    // relocate from Deliver to History. (Reservation.post.kind ==
    // "listing" marks donor listings, so restaurant posts never
    // show up in the donor's tabs.)
    val donorReservations = ReservationStore.reservations.filter {
        it.post.kind == "listing"
    }
    val deliverReservations = donorReservations.filter {
        it.status != ReservationStatus.COLLECTED
    }
    val historyReservations = donorReservations.filter {
        it.status == ReservationStatus.COLLECTED
    }
    val reservedIds = donorReservations.map { it.post.id }.toSet()
    val availableListings = DonorStore.listings.filter { it.id !in reservedIds }

    val listingToCancel = cancelListing
    val currentReservationId = selectedReservationId
    when {
        // ---- Full-screen List Food form (from the FAB) ----
        showListFood -> ListFoodScreen(
            onBack = { showListFood = false },
            onPosted = {
                showListFood = false
                selectedIndex = 0   // land on Donate tab to see the new listing
            }
        )

        // ---- Full-screen reservation details (Deliver / History) ----
        currentReservationId != null -> ReservationDetailsScreen(
            reservationId = currentReservationId,
            onBack = { selectedReservationId = null }
        )

        else -> Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showListFood = true },
                    containerColor = FlashGreen,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "List food"
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedIndex) {
                    0 -> DonorDonateScreen(
                        donorName = donorName,
                        listings = availableListings,
                        onOpenListing = { cancelListing = it }
                    )
                    1 -> DonorDeliverScreen(
                        reservations = deliverReservations,
                        onOpenReservation = { selectedReservationId = it }
                    )
                    2 -> DonorHistoryScreen(
                        reservations = historyReservations,
                        onOpenReservation = { selectedReservationId = it }
                    )
                    3 -> ProfileScreen(
                        viewModel = authViewModel,
                        onLoggedOut = onLoggedOut,
                        onRoleChanged = onSwitchRole
                    )
                }
            }
        }
    }

    // ---- Cancel listing dialog (delete the post) ----
    if (listingToCancel != null) {
        AlertDialog(
            onDismissRequest = { cancelListing = null },
            title = { Text("Cancel Listing?") },
            text = {
                Text("Remove \"${listingToCancel.name}\" from your listed food? " +
                    "This cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    DonorStore.remove(listingToCancel.id)
                    cancelListing = null
                }) {
                    Text("Cancel Listing", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelListing = null }) { Text("Keep") }
            }
        )
    }
}

// ============================================================
//  Donate tab — greeting + Your Listed Food (Available)
// ============================================================

@Composable
private fun DonorDonateScreen(
    donorName: String,
    listings: List<DonorListing>,
    onOpenListing: (DonorListing) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ---- Greeting ----
        Text(
            text = "Hello, $donorName!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Help reduce food waste today!",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        // ---- Your Listed Food ----
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Your Listed Food",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${listings.size} listed",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))

        if (listings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No listed food yet.\nTap + to list your surplus food.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(listings) { listing ->
                    ListingCard(
                        listing = listing,
                        onClick = { onOpenListing(listing) }
                    )
                }
            }
        }
    }
}

// ============================================================
//  Listing card — weight, food type, expiry, location, range
// ============================================================

@Composable
private fun ListingCard(
    listing: DonorListing,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            ListingImage(listing.photoRes, listing.name, 64.dp)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${listing.weight} • ${listing.foodType}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listing.expiryDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2574C)
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${listing.location} • ${listing.distanceKm}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FlashGreenContainer
            ) {
                Text(
                    text = "Available",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FlashGreenDark
                )
            }
        }
    }
}

// ============================================================
//  Deliver tab — listings reserved by recipients
// ============================================================

@Composable
private fun DonorDeliverScreen(
    reservations: List<Reservation>,
    onOpenReservation: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Reserved Listings",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        if (reservations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Nothing to deliver yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Listings reserved by a recipient will show up here.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(reservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onClick = { onOpenReservation(reservation.id) }
                    )
                }
            }
        }
    }
}

// ============================================================
//  History tab — listings the courier marked as delivered
// ============================================================

@Composable
private fun DonorHistoryScreen(
    reservations: List<Reservation>,
    onOpenReservation: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "History",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        if (reservations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No delivered food yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Listings marked as delivered will show up here.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(reservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onClick = { onOpenReservation(reservation.id) }
                    )
                }
            }
        }
    }
}

// ============================================================
//  Shared reservation card (Deliver + History tabs)
// ============================================================

@Composable
private fun ReservationCard(
    reservation: Reservation,
    onClick: () -> Unit
) {
    val statusLabel = when (reservation.status) {
        ReservationStatus.RESERVED -> "Reserved"
        ReservationStatus.PICKUP_SCHEDULED -> "Pickup Scheduled"
        ReservationStatus.COLLECTED -> "Collected"
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            ListingImage(reservation.post.photoRes, reservation.post.name, 56.dp)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${reservation.displayCode}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${reservation.post.name} • ${reservation.post.quantity}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
                if (reservation.status == ReservationStatus.RESERVED) {
                    Text(
                        text = "Reserved by ${reservation.recipientName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (reservation.status == ReservationStatus.PICKUP_SCHEDULED &&
                    reservation.scheduledAt != null
                ) {
                    PickupInfoRow(
                        Icons.Rounded.Schedule,
                        "Pickup: " + SimpleDateFormat("d MMM", Locale.getDefault())
                            .format(Date(reservation.scheduledAt)) +
                            ", ${reservation.scheduledTime}"
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (reservation.status == ReservationStatus.COLLECTED)
                    FlashGreenContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = statusLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (reservation.status == ReservationStatus.COLLECTED)
                        FlashGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PickupInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun ListingImage(
    photoRes: Int?,
    name: String,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFB9C2C9)),
        contentAlignment = Alignment.Center
    ) {
        if (photoRes != null) {
            Image(
                painter = painterResource(photoRes),
                contentDescription = name,
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
