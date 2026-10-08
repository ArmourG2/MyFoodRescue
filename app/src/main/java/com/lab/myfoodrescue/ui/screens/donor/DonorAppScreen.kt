package com.lab.myfoodrescue.ui.screens.donor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lab.myfoodrescue.ui.screens.ProfileScreen
import com.lab.myfoodrescue.ui.screens.UserRole
import com.lab.myfoodrescue.viewmodel.AuthViewModel

// ============================================================
//  DONOR — app shell: bottom nav with
//  Home | Donate | History | Profile
//  Tab content comes in a later phase (placeholders for now).
// ============================================================

private data class DonorNavItem(
    val label: String,
    val icon: ImageVector
)

@Composable
fun DonorAppScreen(
    onLoggedOut: () -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val items = remember {
        listOf(
            DonorNavItem("Home", Icons.Filled.Home),
            DonorNavItem("Donate", Icons.Filled.AddCircle),
            DonorNavItem("History", Icons.Filled.History),
            DonorNavItem("Profile", Icons.Filled.Person)
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                0 -> DonorPlaceholder(
                    title = "Donor Home",
                    subtitle = "Your donations overview will live here."
                )
                1 -> DonorPlaceholder(
                    title = "Donate Food",
                    subtitle = "Create a new donation post (coming soon)."
                )
                2 -> DonorPlaceholder(
                    title = "Donation History",
                    subtitle = "Your past donations will be listed here."
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

@Composable
private fun DonorPlaceholder(
    title: String,
    subtitle: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
