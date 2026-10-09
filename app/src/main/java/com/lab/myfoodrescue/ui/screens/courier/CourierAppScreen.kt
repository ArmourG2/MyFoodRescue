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
import androidx.compose.material.icons.filled.EventAvailable
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
import com.lab.myfoodrescue.R
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


//  COURIER — hardcoded assigned pickups for the prototype
//  (matches the volunteer collector dashboard mock)


private val ASSIGNED_PICKUPS = listOf(
    SurplusPost(
        id = "FR20260045",
        name = "Mixed Vegetables",
        expiryDate = "",
        distanceKm = "",

        foodType = "Vegetables",
        quantity = "2 kg",
        location = "Yogi 8 Canteen",
        donorName = "Recipient: Sitrad ",
        pickupWindow = "2:00 PM - 2:30 PM",
        pickupPoint = "Yogi 8 Canteen",
        description = "Mixed vegetables packed by Yogi 8 Canteen for the food rescue " +
            "programme. Collect within the pickup window and deliver straight to the recipient.",
        photoRes = R.drawable.fresh_veg
    ),
    SurplusPost(
        id = "FR20260046",
        name = "Bread & Pastry",
        expiryDate = "",
        distanceKm = "",
        foodType = "Bakery",
        quantity = "3 packs",
        location = "Yogi 10 Canteen",
        donorName = "Recipient: Siti ",
        pickupWindow = "4:00 PM - 4:30 PM",

        pickupPoint = "Yogi 10 Canteen",
        description = "Assorted bread and pastry packs rescued from Yogi 10 Canteen. " +
            "Collect on time and deliver to the recipient right away.",
        photoRes = R.drawable.bread_and_pastry,

    )
)

private data class CourierNavItem(
    val label: String,
    val icon: ImageVector
)

// One unit of courier work: a hardcoded demo pickup, or a live
// recipient reservation coming from ReservationStore
private data class CourierJob(
    val key: String,              // unique key: post id or reservation id
    val post: SurplusPost,
    val reservationId: String?,   // non-null when driven by a Reservation
    val recipientName: String = ""
)

// ============================================================
//  VIEW — Courier shell: bottom nav with
//  Pickup | Scheduled | History | Profile
//  Pickup tab: assigned pickups -> "Pick Schedule" opens the
//  scheduling screen. After scheduling, the job moves to the
//  Scheduled tab where "Mark as Delivered" completes it.
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
            CourierNavItem("Scheduled", Icons.Filled.EventAvailable),
            CourierNavItem("History", Icons.Filled.History),
            CourierNavItem("Profile", Icons.Filled.Person)
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    // Courier job state
    val hardcodedJobs = remember {
        ASSIGNED_PICKUPS.map { CourierJob(key = it.id, post = it, reservationId = null) }
    }
    val removedKeys = remember { mutableStateListOf<String>() }
    val scheduledDemoJobs = remember { mutableStateListOf<CourierJob>() }
    val deliveredJobs = remember { mutableStateListOf<CourierJob>() }
    var selectedJob by remember { mutableStateOf<CourierJob?>(null) }
    var schedulingJob by remember { mutableStateOf<CourierJob?>(null) }

    //  Courier identity (real account info for reservations)
    val state by authViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { authViewModel.loadProfile() }
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val courierName = state.profile?.username?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }
        ?: "there"
    val courierPhone = state.profile?.phone?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.phoneNumber

    //  Live recipient reservations by status
    val reservedJobs = ReservationStore.reservations
        .filter { it.status == ReservationStatus.RESERVED }
        .map {
            CourierJob(it.id, it.post, it.id, it.recipientName)
        }
    val scheduledReservationJobs = ReservationStore.reservations
        .filter { it.status == ReservationStatus.PICKUP_SCHEDULED }
        .map {
            CourierJob(it.id, it.post, it.id, it.recipientName)
        }

    val pickupJobs = hardcodedJobs.filter { it.key !in removedKeys } + reservedJobs
    val scheduledJobs = scheduledDemoJobs + scheduledReservationJobs

    val currentScheduling = schedulingJob
    val currentJob = selectedJob
    when {
        // ---- Full-screen pickup scheduling (date + time) ----
        currentScheduling != null -> PickupSchedulingScreen(
            onBack = { schedulingJob = null },
            onConfirm = { dateMillis, time ->
                if (currentScheduling.reservationId != null) {
                    ReservationStore.schedulePickup(
                        id = currentScheduling.reservationId,
                        scheduledAtMillis = dateMillis,
                        time = time,
                        courierName = courierName,
                        courierPhone = courierPhone
                    )
                } else {
                    // Demo job: move it from Pickup to Scheduled in-memory
                    removedKeys.add(currentScheduling.key)
                    scheduledDemoJobs.add(currentScheduling)
                }
                schedulingJob = null
                selectedJob = null
                selectedIndex = 1   // show the Scheduled tab
            }
        )

        // ---- Full-screen job detail ----
        currentJob != null -> {
            val isScheduled = if (currentJob.reservationId != null) {
                ReservationStore.statusOf(currentJob.reservationId) !=
                    ReservationStatus.RESERVED
            } else {
                scheduledDemoJobs.any { it.key == currentJob.key }
            }
            PostDetailScreen(
                post = currentJob.post,
                isReserved = false,
                onReserve = {},
                onBack = { selectedJob = null },
                courier = CourierDetailState(
                    isScheduled = isScheduled,
                    onSchedule = {
                        schedulingJob = currentJob
                        selectedJob = null
                    },
                    onDelivered = {
                        if (currentJob.reservationId != null) {
                            ReservationStore.markCollected(currentJob.reservationId)
                        }
                        scheduledDemoJobs.removeAll { it.key == currentJob.key }
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
                        jobs = pickupJobs,
                        onOpenJob = { selectedJob = it },
                        onSchedule = { schedulingJob = it }
                    )
                    1 -> CourierScheduledScreen(
                        jobs = scheduledJobs,
                        onOpenJob = { selectedJob = it },
                        onDelivered = { job ->
                            if (job.reservationId != null) {
                                ReservationStore.markCollected(job.reservationId)
                            }
                            scheduledDemoJobs.removeAll { it.key == job.key }
                            deliveredJobs.add(job)
                        }
                    )
                    2 -> CourierHistoryScreen(deliveredJobs)
                    3 -> ProfileScreen(
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
//  Pickup tab — greeting + Assigned Pickups ("Pick Schedule")
// ============================================================

@Composable
private fun CourierPickupScreen(
    courierName: String,
    jobs: List<CourierJob>,
    onOpenJob: (CourierJob) -> Unit,
    onSchedule: (CourierJob) -> Unit
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
                PickupCard(
                    code = job.key,
                    post = job.post,
                    recipientLine = recipientLineFor(job),
                    onClick = { onOpenJob(job) },
                    onSchedule = { onSchedule(job) }
                )
            }
        }
    }
}

/** "Recipient: <actual account name>" for reservations, demo text otherwise. */
private fun recipientLineFor(job: CourierJob): String =
    if (job.reservationId != null && job.recipientName.isNotBlank()) {
        "Recipient: ${job.recipientName}"
    } else {
        job.post.donorName
    }

// ============================================================
//  Assigned pickup card — image placeholder, code, title, place,
//  time, recipient, and the Pick Schedule button.
// ============================================================

@Composable
private fun PickupCard(
    code: String,
    post: SurplusPost,
    recipientLine: String,
    onClick: () -> Unit,
    onSchedule: () -> Unit
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
                        recipientLine,
                        tint = FlashGreenDark,
                        textColor = FlashGreenDark
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Pick Schedule (opens the scheduling screen) ----
            Button(
                onClick = onSchedule,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FlashGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    text = "Pick Schedule",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

// ============================================================
//  Scheduled tab — pickups with a set schedule. "Mark as
//  Delivered" completes the job (recipient status -> Collected).
// ============================================================

@Composable
private fun CourierScheduledScreen(
    jobs: List<CourierJob>,
    onOpenJob: (CourierJob) -> Unit,
    onDelivered: (CourierJob) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Scheduled Pickups",
            fontSize = 17.sp,
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
                        imageVector = Icons.Filled.EventAvailable,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No scheduled pickups yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Pickups you schedule will show up here.",
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
                    ScheduledCard(
                        code = job.key,
                        post = job.post,
                        recipientLine = recipientLineFor(job),
                        onClick = { onOpenJob(job) },
                        onDelivered = { onDelivered(job) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduledCard(
    code: String,
    post: SurplusPost,
    recipientLine: String,
    onClick: () -> Unit,
    onDelivered: () -> Unit
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
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
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
                        recipientLine,
                        tint = FlashGreenDark,
                        textColor = FlashGreenDark
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Mark as Delivered ----
            Button(
                onClick = onDelivered,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FlashGreenDark,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
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
