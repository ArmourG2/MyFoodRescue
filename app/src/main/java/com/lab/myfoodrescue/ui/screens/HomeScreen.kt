package com.lab.myfoodrescue.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import com.lab.myfoodrescue.R

// ============================================================
//  VIEW — Home: top app bar ("flashfood") + Surplus Listing grid
//  All posts are HARDCODED samples — nothing is stored in Firebase.
// ============================================================

data class SurplusPost(
    val name: String,
    val expiryDate: String,   // hardcoded date string
    val distanceKm: String,
    val photoRes: Int? = null // set to R.drawable.<photo> when real photos exist
)

// 13 hardcoded surplus posts (sample data only)
private val SAMPLE_SURPLUS_POSTS = listOf(
    SurplusPost("Fresh Vegetables", "Expires: 10 Oct 2026", "2.4 km", photoRes = R.drawable.fresh_veg),
    SurplusPost("Chicken Rice", "Expires: 9 Oct 2026", "3.1 km", photoRes = R.drawable.chicken_rice),
    SurplusPost("Butter Croissants", "Expires: 9 Oct 2026", "1.2 km", R.drawable.butter_crois),
    SurplusPost("Fresh Apples", "Expires: 12 Oct 2026", "0.8 km", R.drawable.apple),
    SurplusPost("Steamed Buns", "Expires: 10 Oct 2026", "2.9 km", R.drawable.steam_bun),
    SurplusPost("Sushi Platter", "Expires: 9 Oct 2026", "4.5 km", R.drawable.sushi_platter),
    SurplusPost("Banana Bread", "Expires: 13 Oct 2026", "1.7 km", R.drawable.banana_bread),
    SurplusPost("Vegetable Curry", "Expires: 11 Oct 2026", "3.6 km", R.drawable.veg_curry),
    SurplusPost("Milk", "Expires: 15 Oct 2026", "2.2 km", R.drawable.milk),
    SurplusPost("Wholemeal Bread", "Expires: 11 Oct 2026", "1.9 km", R.drawable.sour_bread),
    SurplusPost("Roast Chicken", "Expires: 9 Oct 2026", "5.2 km", R.drawable.roasted_chick),
    SurplusPost("Fruit Yogurt Cups", "Expires: 14 Oct 2026", "0.6 km", R.drawable.yogurt),
    SurplusPost("Fried Noodles", "Expires: 10 Oct 2026", "2.8 km", R.drawable.noodle)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = "flashfood",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ---- Section header ----
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Surplus Listing",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${SAMPLE_SURPLUS_POSTS.size} available",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---- 2-column grid of surplus posts ----
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 16.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                items(SAMPLE_SURPLUS_POSTS) { post ->
                    SurplusCard(post)
                }
            }
        }
    }
}

@Composable
private fun SurplusCard(post: SurplusPost) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // ---- Photo area (hardcoded placeholder) ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Color(0xFFB9C2C9)),
                contentAlignment = Alignment.Center
            ) {
                // To use a real photo later: photoRes?.let { Image(painterResource(it), ...) }
                if (post.photoRes != null) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(post.photoRes),
                        contentDescription = post.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PlaceholderPhoto()
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                // ---- Food name ----
                Text(
                    text = post.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )

                Spacer(Modifier.height(2.dp))

                // ---- Expiry date (hardcoded) ----
                Text(
                    text = post.expiryDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2574C)
                )

                Spacer(Modifier.height(6.dp))

                // ---- Distance ----
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.size(2.dp))
                    Text(
                        text = post.distanceKm,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Hardcoded placeholder "photo" — grey image tile like the design mock
@Composable
private fun PlaceholderPhoto() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF9AA7B0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = null,
                tint = Color(0xFFE7ECEF),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}