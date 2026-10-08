package com.lab.myfoodrescue.ui.screens.courier

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
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.lab.myfoodrescue.data.repository.ReservationStatus
import com.lab.myfoodrescue.data.repository.ReservationStore
import com.lab.myfoodrescue.ui.screens.CourierDetailState
import com.lab.myfoodrescue.ui.screens.PostDetailScreen
import com.lab.myfoodrescue.ui.screens.ProfileScreen
import com.lab.myfoodrescue.ui.screens.SurplusPost
import com.lab.myfoodrescue.ui.screens.UserRole
import com.lab.myfoodrescue.ui.theme.FlashGreen
import com.lab.myfoodrescue.ui.theme.FlashGreenContainer
import com.lab.myfoodrescue.ui.theme.FlashGreenDark
import com.lab.myfoodrescue.viewmodel.AuthViewModel

// ============================================================
//  COURIER — hardcoded assigned pickups for the prototype
//  (matches the volunteer collector dashboard mock)
// ============================================================

private val ASSIGNED_PICKUPS = listOf(
    SurplusPost(
        id = "FR20260045",
        name = "Mixed Vegetables",
        expiryDate = "",
        distanceKm = "",
        foodType = "Vegetables",
        quantity = "2 kg",
        location = "Yogi 8 Canteen",
        donorName = "Recipient: Sitrad (UPTM Student)",
        pickupWindow = "2:00 PM - 2:30 PM",
        pickupPoint = "Yogi 8 Canteen",
        description = "Mixed vegetables packed by Yogi 8 Canteen for the food rescue " +
            "programme. Collect within the pickup window and deliver straight to the recipient."
    ),
    SurplusPost(
        id = "FR20260046",
        name = "Bread & Pastry",
        expiryDate = "",
        distanceKm = "",
        foodType = "Bakery",
        quantity = "3 packs",
        location = "Yogi 10 Canteen",
        donorName = "Recipient: Siti (UPTM Staff)",
        pickupWindow = "4:00 PM - 4:30 PM",
        pickupPoint = "Yogi 10 Canteen",
        description = "Assorted bread and pastry packs rescued from Yogi 10 Canteen. " +
            "Collect on time and deliver to the recipient right away."
    )
)

private data class CourierNavItem(
    val label: String,
    val icon: ImageVector
)

// One unit of courier work: a hardcoded demo pickup, or a live
// recipient reservation coming from ReservationStore
private data class CourierJob(
    val key: String,           // unique key: post id or reservation id
    val post: SurplusPost,
    val reservationId: String? // non-null when driven by a Reservation
)

// ============================================================
//  VIEW — Courier shell: bottom nav with
//  Pickup | History | Profile
//  Also hosts the courier version of the Post Detail screen.
// ============================================================

@Composable
fun CourierAppScreen(
    onLoggedOut: () -> Unit,
    onSwitchRole: (UserRole) -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val navItems = remember {
        listOf(
            CourierNavItem("Pickup", Icons.Filled.LocalShipping),
            CourierNavItem("History", Icons.Filled.History),
            CourierNavItem("Profile", Icons.Filled.Person)
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    // ---- Courier job state ----
    // Hardcoded demo pickups + live recipient reservations. Reservations
    // leave Assigned Pickups automatically once delivered.
    val hardcodedJobs = remember {
        ASSIGNED_PICKUPS.map { CourierJob(key = it.id, post = it, reservationId = null) }
    }
    val removedKeys = remember { mutableStateListOf<String>() }
    val deliveredJobs = remember { mutableStateListOf<CourierJob>() }
    val pickedUpMap = remember { mutableStateMapOf<String, Boolean>() }
    var selectedJob by remember { mutableStateOf<CourierJob?>(null) }

    val storeJobs = ReservationStore.reservations
        .filter { it.status != ReservationStatus.COLLECTED }
        .map { CourierJob(key = it.id, post = it.post, reservationId = it.id) }
    val assignedJobs = hardcodedJobs.filter { it.key !in removedKeys } + storeJobs

    // ---- Greeting name ("Hi, <name>!") ----
    val state by authViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { authViewModel.loadProfile() }
    val courierName = when {
        // Guests have no Firebase user — their ViewModel flag does not
        // survive navigation, so detect the guest by the missing user.
        FirebaseAuth.getInstance().currentUser == null -> "Guest"
        else -> state.profile?.username?.takeIf { it.isNotBlank() }
            ?: FirebaseAuth.getInstance().currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: "there"
    }

    val currentJob = selectedJob
    when {
        // Full-screen pickup detail (no bottom nav) — same layout as the
        // recipient post detail, but with side-by-side courier actions.
        // Pick Up -> "Pickup Scheduled"; Mark as Delivered -> "Collected".
        currentJob != null -> {
            val isPickedUp =
                if (currentJob.reservationId != null) {
                    ReservationStore.statusOf(currentJob.reservationId) !=
                        ReservationStatus.RESERVED
                } else {
                    pickedUpMap[currentJob.key] == true
                }
            PostDetailScreen(
                post = currentJob.post,
                isReserved = false,
                onReserve = {},
                onBack = { selectedJob = null },
                courier = CourierDetailState(
                    isPickedUp = isPickedUp,
                    onPickUp = {
                        if (currentJob.reservationId != null) {
                            ReservationStore.markPickedUp(currentJob.reservationId)
                        } else {
                            pickedUpMap[currentJob.key] = true
                        }
                    },
                    onDelivered = {
                        if (currentJob.reservationId != null) {
                            ReservationStore.markCollected(currentJob.reservationId)
                        } else {
                            removedKeys.add(currentJob.key)
                        }
                        deliveredJobs.add(currentJob)
                        selectedJob = null
                    }
                )
            )
        }

        else -> Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEachIndexed { index, item ->
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
                    0 -> CourierPickupScreen(
                        courierName = courierName,
                        jobs = assignedJobs,
                        pickedUpMap = pickedUpMap,
                        onOpenJob = { selectedJob = it },
                        onPickUp = { job ->
                            if (job.reservationId != null) {
                                ReservationStore.markPickedUp(job.reservationId)
                            } else {
                                pickedUpMap[job.key] = true
                            }
                        },
                        onDelivered = { job ->
                            if (job.reservationId != null) {
                                ReservationStore.markCollected(job.reservationId)
                            } else {
                                removedKeys.add(job.key)
                            }
                            deliveredJobs.add(job)
                        }
                    )
                    1 -> CourierHistoryScreen(deliveredJobs)
                    2 -> ProfileScreen(
                        viewModel = authViewModel,
                        onLoggedOut = onLoggedOut,
                        onRoleChanged = onSwitchRole
                    )
                }
            }
        }
    }
}

// ============================================================
//  Pickup tab — greeting, thank-you message, Assigned Pickups
// ============================================================

@Composable
private fun CourierPickupScreen(
    courierName: String,
    jobs: List<CourierJob>,
    pickedUpMap: Map<String, Boolean>,
    onOpenJob: (CourierJob) -> Unit,
    onPickUp: (CourierJob) -> Unit,
    onDelivered: (CourierJob) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ---- Greeting (avatar intentionally omitted per design) ----
        Text(
            text = "Hi, $courierName!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Thank you for helping the community!",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))
        Text(
            text = "Assigned Pickups",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (jobs.isEmpty()) {
                item {
                    Text(
                        text = "No assigned pickups right now.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
            items(jobs) { job ->
                val isPickedUp =
                    if (job.reservationId != null) {
                        ReservationStore.statusOf(job.reservationId) !=
                            ReservationStatus.RESERVED
                    } else {
                        pickedUpMap[job.key] == true
                    }
                PickupCard(
                    code = job.key,
                    post = job.post,
                    isPickedUp = isPickedUp,
                    onClick = { onOpenJob(job) },
                    onPickUp = { onPickUp(job) },
                    onDelivered = { onDelivered(job) }
                )
            }
        }
    }
}

// ============================================================
//  Assigned pickup card — image placeholder, code, title, place,
//  time, recipient, and the Pick Up / Mark as Delivered buttons.
// ============================================================

@Composable
private fun PickupCard(
    code: String,
    post: SurplusPost,
    isPickedUp: Boolean,
    onClick: () -> Unit,
    onPickUp: () -> Unit,
    onDelivered: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FlashGreenContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                // ---- Image placeholder ----
                Box(
                    modifier = Modifier
                        .size(76.dp)
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
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // ---- Info ----
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#$code",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${post.name}  •  ${post.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(6.dp))
                    PickupInfoRow(Icons.Rounded.LocationOn, post.location)
                    Spacer(Modifier.height(4.dp))
                    PickupInfoRow(Icons.Rounded.Schedule, post.pickupWindow)
                    Spacer(Modifier.height(4.dp))
                    PickupInfoRow(
                        Icons.Filled.Person,
                        post.donorName,
                        tint = FlashGreenDark,
                        textColor = FlashGreenDark
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Side-by-side actions ----
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onPickUp,
                    enabled = !isPickedUp,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlashGreen,
                        contentColor = Color.White,
                        disabledContainerColor = FlashGreenContainer,
                        disabledContentColor = FlashGreenDark
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text(
                        text = if (isPickedUp) "Picked Up ✓" else "Pick Up",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onDelivered,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlashGreenDark,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text(
                        text = "Mark as Delivered",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PickupInfoRow(
    icon: ImageVector,
    text: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1
        )
    }
}

// ============================================================
//  History tab — pickups already marked as delivered
// ============================================================

@Composable
private fun CourierHistoryScreen(jobs: List<CourierJob>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Delivery History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        if (jobs.isEmpty()) {
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
                        text = "No deliveries yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Pickups you mark as delivered will show up here.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(jobs) { job ->
                    DeliveredCard(code = job.key, post = job.post)
                }
            }
        }
    }
}

@Composable
private fun DeliveredCard(code: String, post: SurplusPost) {
    Card(
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
            Box(
                modifier = Modifier
                    .size(56.dp)
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

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#$code",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${post.name} • ${post.quantity}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = post.pickupPoint,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FlashGreenContainer
            ) {
                Text(
                    text = "Delivered",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FlashGreenDark
                )
            }
        }
    }
}
