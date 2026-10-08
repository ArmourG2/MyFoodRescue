package com.lab.myfoodrescue.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lab.myfoodrescue.data.repository.ReservationStatus
import com.lab.myfoodrescue.data.repository.ReservationStore
import com.lab.myfoodrescue.ui.screens.FilterScreen
import com.lab.myfoodrescue.ui.screens.HistoryScreen
import com.lab.myfoodrescue.ui.screens.HomeScreen
import com.lab.myfoodrescue.ui.screens.PostDetailScreen
import com.lab.myfoodrescue.ui.screens.ProfileScreen
import com.lab.myfoodrescue.ui.screens.ReservationDetailsScreen
import com.lab.myfoodrescue.ui.screens.ReservedScreen
import com.lab.myfoodrescue.ui.screens.SearchFilter
import com.lab.myfoodrescue.ui.screens.SearchScreen
import com.lab.myfoodrescue.ui.screens.SurplusPost
import com.lab.myfoodrescue.ui.screens.UserRole
import com.lab.myfoodrescue.viewmodel.AuthViewModel

// ============================================================
//  VIEW — Recipient app shell (after choosing a role): bottom
//  nav Home | Search | Reserved | History | Profile.
//  Reserving a post creates a Reservation (auto ID) that shows
//  up in the courier's Assigned Pickups; delivered food lands
//  in the recipient History tab. Also hosts the Post Detail
//  screen, Reservation Details and the Filter panel.
// ============================================================

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainAppScreen(
    onLoggedOut: () -> Unit,
    onSwitchRole: (UserRole) -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val items = remember {
        listOf(
            BottomNavItem("Home", Icons.Filled.Home),
            BottomNavItem("Search", Icons.Filled.Search),
            BottomNavItem("Reserved", Icons.Filled.Event),
            BottomNavItem("History", Icons.Filled.History),
            BottomNavItem("Profile", Icons.Filled.Person)
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    // ---- Search + Filter state (hoisted so it survives navigation) ----
    var searchText by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(SearchFilter()) }
    var showFilter by remember { mutableStateOf(false) }

    // ---- Navigation + reservation state (in-memory) ----
    var selectedPost by remember { mutableStateOf<SurplusPost?>(null) }
    var selectedReservationId by remember { mutableStateOf<String?>(null) }

    // Active (not yet collected) reservations drive the Reserved badges
    val activeMap = ReservationStore.reservations
        .filter { it.status != ReservationStatus.COLLECTED }
        .associate { it.post.name to true }

    val currentPost = selectedPost
    val currentReservationId = selectedReservationId
    when {
        // Full-screen reservation details (the recipient's view of a
        // reserved post, with live status steps)
        currentReservationId != null -> ReservationDetailsScreen(
            reservationId = currentReservationId,
            onBack = { selectedReservationId = null }
        )

        // Full-screen post detail (no bottom nav, like the mock).
        // Reserving creates a reservation and jumps straight into
        // its Reservation Details screen.
        currentPost != null -> PostDetailScreen(
            post = currentPost,
            isReserved = activeMap.containsKey(currentPost.name),
            onReserve = {
                val reservation = ReservationStore.reserve(currentPost)
                selectedPost = null
                selectedReservationId = reservation.id
            },
            onBack = { selectedPost = null }
        )

        // Full-screen filter panel
        showFilter -> FilterScreen(
            filter = filter,
            onFilterChange = { filter = it },
            onBack = { showFilter = false }
        )

        // Tabs with bottom nav
        else -> Scaffold(
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
                    0 -> HomeScreen(
                        reservedMap = activeMap,
                        onPostClick = { selectedPost = it }
                    )
                    1 -> SearchScreen(
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        filter = filter,
                        onFilterChange = { filter = it },
                        onOpenFilter = { showFilter = true },
                        reservedMap = activeMap,
                        onPostClick = { selectedPost = it },
                        onBackToHome = { selectedIndex = 0 }
                    )
                    2 -> ReservedScreen(
                        reservations = ReservationStore.reservations.filter {
                            it.status != ReservationStatus.COLLECTED
                        },
                        onOpenReservation = { selectedReservationId = it }
                    )
                    3 -> HistoryScreen(
                        reservations = ReservationStore.reservations.filter {
                            it.status == ReservationStatus.COLLECTED
                        },
                        onOpenReservation = { selectedReservationId = it }
                    )
                    4 -> ProfileScreen(
                        viewModel = authViewModel,
                        onLoggedOut = onLoggedOut,
                        onRoleChanged = onSwitchRole
                    )
                }
            }
        }
    }
}
