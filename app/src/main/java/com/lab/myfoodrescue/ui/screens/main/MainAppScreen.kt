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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lab.myfoodrescue.ui.screens.FilterScreen
import com.lab.myfoodrescue.ui.screens.HomeScreen
import com.lab.myfoodrescue.ui.screens.PostDetailScreen
import com.lab.myfoodrescue.ui.screens.ProfileScreen
import com.lab.myfoodrescue.ui.screens.SearchFilter
import com.lab.myfoodrescue.ui.screens.SearchScreen
import com.lab.myfoodrescue.ui.screens.SurplusPost
import com.lab.myfoodrescue.viewmodel.AuthViewModel

// ============================================================
//  VIEW — Main app shell after login: bottom nav with
//  Home | Search | Booking | History | Profile
//  Also hosts the Post Detail screen and the Filter panel.
// ============================================================

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainAppScreen(
    onLoggedOut: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val items = remember {
        listOf(
            BottomNavItem("Home", Icons.Filled.Home),
            BottomNavItem("Search", Icons.Filled.Search),
            BottomNavItem("Booking", Icons.Filled.Event),
            BottomNavItem("History", Icons.Filled.History),
            BottomNavItem("Profile", Icons.Filled.Person)
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    // ---- Search + Filter state (hoisted so it survives navigation) ----
    var searchText by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(SearchFilter()) }
    var showFilter by remember { mutableStateOf(false) }

    // ---- Post detail navigation + reserved state (in-memory) ----
    var selectedPost by remember { mutableStateOf<SurplusPost?>(null) }
    val reservedMap = remember { mutableStateMapOf<String, Boolean>() }

    val currentPost = selectedPost
    when {
        // Full-screen post detail (no bottom nav, like the mock)
        currentPost != null -> PostDetailScreen(
            post = currentPost,
            isReserved = reservedMap[currentPost.name] == true,
            onReserve = { reservedMap[currentPost.name] = true },
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
                        reservedMap = reservedMap,
                        onPostClick = { selectedPost = it }
                    )
                    1 -> SearchScreen(
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        filter = filter,
                        onFilterChange = { filter = it },
                        onOpenFilter = { showFilter = true },
                        reservedMap = reservedMap,
                        onPostClick = { selectedPost = it },
                        onBackToHome = { selectedIndex = 0 }
                    )
                    2 -> PlaceholderScreen("Booking")
                    3 -> PlaceholderScreen("History")
                    4 -> ProfileScreen(
                        viewModel = authViewModel,
                        onLoggedOut = onLoggedOut
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "$name — coming soon",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}