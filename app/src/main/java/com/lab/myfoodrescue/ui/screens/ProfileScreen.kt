package com.lab.myfoodrescue.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.myfoodrescue.viewmodel.AuthViewModel

// ============================================================
//  VIEW — Profile screen with account info + Log out button
// ============================================================

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onLoggedOut: () -> Unit,
    onRoleChanged: (UserRole) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(24.dp))

            //Avatar with initials
            val name = if (state.isGuest) {
                "Guest"
            } else {
                state.profile?.username
                    ?: com.google.firebase.auth.FirebaseAuth.getInstance()
                        .currentUser?.displayName.orEmpty()
            }
            val initials = name.takeIf { it.isNotBlank() }
                ?.split(" ")
                ?.mapNotNull { it.firstOrNull() }
                ?.take(2)
                ?.joinToString("")
                ?.uppercase()
                ?: "?"

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = name.ifBlank { "Guest" },
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(24.dp))

            //Info card
            androidx.compose.material3.Card(
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow(
                        icon = Icons.Rounded.Email,
                        label = if (state.isGuest) "Browsing as guest"
                        else state.profile?.email
                            ?: com.google.firebase.auth.FirebaseAuth.getInstance()
                                .currentUser?.email.orEmpty()
                    )
                    Spacer(Modifier.height(12.dp))
                    InfoRow(
                        icon = Icons.Rounded.Phone,
                        label = state.profile?.phone?.takeIf { it.isNotBlank() } ?: "—"
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InfoRow(
                            icon = Icons.Rounded.Work,
                            label = RoleSession.currentRole?.label ?: "—"
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { showRoleDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Change role",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            //Log out
            Button(
                onClick = { showLogoutDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Log Out", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("You will need to log in again to use the app.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                    onLoggedOut()
                }) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change role dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Change role") },
            text = {
                Column {
                    Text(
                        text = if (state.isGuest)
                            "Guests can switch roles for this session only."
                        else
                            "Pick a role — it will be saved to your account.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    UserRole.values().forEach { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showRoleDialog = false
                                    RoleSession.currentRole = role
                                    viewModel.saveRole(role.name)
                                    onRoleChanged(role)
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = roleIcon(role),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.size(12.dp))
                            Text(
                                text = role.label,
                                fontSize = 15.sp,
                                fontWeight = if (RoleSession.currentRole == role)
                                    FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.weight(1f))
                            if (RoleSession.currentRole == role) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Current role",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun roleIcon(role: UserRole) = when (role) {
    UserRole.RECIPIENT -> Icons.Rounded.ShoppingBasket
    UserRole.DONOR -> Icons.Rounded.VolunteerActivism
    UserRole.COURIER -> Icons.Rounded.LocalShipping
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}