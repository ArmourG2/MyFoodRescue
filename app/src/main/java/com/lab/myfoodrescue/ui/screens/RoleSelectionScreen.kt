package com.lab.myfoodrescue.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lab.myfoodrescue.viewmodel.AuthViewModel

// ============================================================
//  MODEL — the three app roles (Donor arrives in a later phase)
// ============================================================

enum class UserRole(val label: String) {
    RECIPIENT("Recipient"),
    DONOR("Donor"),
    COURIER("Courier")
}

// Simple in-memory session for the chosen role (read by the
// Profile screen; resets when the app process restarts)
object RoleSession {
    var currentRole: UserRole? = null
}

// ============================================================
//  VIEW — Role selection ("Set a role"): shown after sign up.
//  Recipient -> main app, Courier -> courier dashboard,
//  Donor -> donor app. The chosen role is saved to Firestore.
// ============================================================

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(110.dp))

            Text(
                text = "Set a role",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(48.dp))

            // ---- Recipient + Donor side by side ----
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RoleCard(
                    role = UserRole.RECIPIENT,
                    icon = Icons.Rounded.ShoppingBasket,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        // Persist the role so future log-ins skip this screen
                        viewModel.saveRole(UserRole.RECIPIENT.name)
                        onRoleSelected(UserRole.RECIPIENT)
                    }
                )
                RoleCard(
                    role = UserRole.DONOR,
                    icon = Icons.Rounded.VolunteerActivism,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.saveRole(UserRole.DONOR.name)
                        onRoleSelected(UserRole.DONOR)
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---- Courier centered below ----
            RoleCard(
                role = UserRole.COURIER,
                icon = Icons.Rounded.LocalShipping,
                modifier = Modifier.width(160.dp),
                onClick = {
                    viewModel.saveRole(UserRole.COURIER.name)
                    onRoleSelected(UserRole.COURIER)
                }
            )

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun RoleCard(
    role: UserRole,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.height(150.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = role.label,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = role.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            if (!enabled) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Coming soon",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
