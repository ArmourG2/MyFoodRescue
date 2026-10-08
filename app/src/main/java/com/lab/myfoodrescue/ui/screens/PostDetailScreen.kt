package com.lab.myfoodrescue.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab.myfoodrescue.ui.theme.FlashGreen
import com.lab.myfoodrescue.ui.theme.FlashGreenContainer
import com.lab.myfoodrescue.ui.theme.FlashGreenDark

// ============================================================
//  Courier mode — when non-null, the detail screen swaps the
//  recipient "Reserve" button for side-by-side courier actions.
// ============================================================
data class CourierDetailState(
    val isPickedUp: Boolean = false,
    val onPickUp: () -> Unit = {},
    val onDelivered: () -> Unit = {}
)

// ============================================================
//  VIEW — Post detail (opened when a surplus post card is tapped)
//  Design per mock: photo + back arrow (NO heart icon), title +
//  "Fresh" badge, "quantity | food type", donor/pickup details
//  (hardcoded Malaysia), "About This Food", location map mock,
//  Reserve button. Tapping Reserve changes the text to "Reserved".
// ============================================================

@Composable
fun PostDetailScreen(
    post: SurplusPost,
    isReserved: Boolean,
    onReserve: () -> Unit,
    onBack: () -> Unit,
    courier: CourierDetailState? = null
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                if (courier == null) {
                    // ---- Recipient mode: single Reserve button ----
                    Button(
                        onClick = onReserve,
                        enabled = !isReserved,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FlashGreen,
                            contentColor = Color.White,
                            disabledContainerColor = FlashGreenContainer,
                            disabledContentColor = FlashGreenDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (isReserved) "Reserved" else "Reserve",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // ---- Courier mode: side-by-side actions ----
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = courier.onPickUp,
                            enabled = !courier.isPickedUp,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FlashGreen,
                                contentColor = Color.White,
                                disabledContainerColor = FlashGreenContainer,
                                disabledContentColor = FlashGreenDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Text(
                                text = if (courier.isPickedUp) "Picked Up ✓" else "Pick Up",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = courier.onDelivered,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FlashGreenDark,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Text(
                                text = "Mark as Delivered",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Photo area with back arrow (top-left only) ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
            ) {
                if (post.photoRes != null) {
                    Image(
                        painter = painterResource(post.photoRes),
                        contentDescription = post.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFB9C2C9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Image,
                            contentDescription = null,
                            tint = Color(0xFFE4E9EC),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Back arrow (top-left) — love/heart icon intentionally omitted
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x99FFFFFF))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF22332B),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Status badge (top-right of photo): "Reserved" for recipients,
                // "Picked Up" for the courier flow.
                if (isReserved || courier?.isPickedUp == true) {
                    ReservedBadge(
                        text = if (courier?.isPickedUp == true) "Picked Up" else "Reserved",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(12.dp)
                    )
                }
            }

            // ---- Details ----
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.size(10.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = FlashGreenContainer) {
                        Text(
                            text = "Fresh",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FlashGreenDark
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // e.g. "7 kg | Vegetables"
                Text(
                    text = "${post.quantity} | ${post.foodType}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(Modifier.height(12.dp))

                DetailRow(icon = Icons.Rounded.Person, text = post.donorName)
                Spacer(Modifier.height(10.dp))
                DetailRow(icon = Icons.Rounded.Schedule, text = "Pickup Window: ${post.pickupWindow}")
                Spacer(Modifier.height(10.dp))
                DetailRow(icon = Icons.Rounded.LocationOn, text = post.pickupPoint)

                Spacer(Modifier.height(20.dp))

                // ---- About This Food ----
                Text(
                    text = "About This Food",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = post.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// Hardcoded map mock — stylized roads + pin + Malaysia address chip
@Composable
private fun MapMock(pickupPoint: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE4EDDE))
    ) {
        // Fake roads to suggest a map
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(5.dp)
                .offset(y = 44.dp)
                .background(Color(0xFFCDD9C4))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(5.dp)
                .fillMaxHeight()
                .offset(x = 58.dp)
                .background(Color(0xFFCDD9C4))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(4.dp)
                .offset(y = (-52).dp)
                .background(Color(0xFFCDD9C4))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(4.dp)
                .fillMaxHeight()
                .offset(x = (-84).dp)
                .background(Color(0xFFCDD9C4))
        )

        // Pin at the hardcoded pickup point
        Icon(
            imageVector = Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = FlashGreen,
            modifier = Modifier
                .align(Alignment.Center)
                .size(44.dp)
        )

        // Address chip (hardcoded Malaysia)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        ) {
            Text(
                text = pickupPoint,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ReservedBadge(modifier: Modifier = Modifier, text: String = "Reserved") {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = modifier) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = FlashGreen
        )
    }
}