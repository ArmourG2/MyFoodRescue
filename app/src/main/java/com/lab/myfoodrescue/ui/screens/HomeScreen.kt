package com.lab.myfoodrescue.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab.myfoodrescue.R
import com.lab.myfoodrescue.data.repository.DonorStore
import com.lab.myfoodrescue.data.repository.toSurplusPost
import com.lab.myfoodrescue.ui.theme.FlashGreen


// surplus posts

data class SurplusPost(
    val name: String,
    val expiryDate: String,
    val distanceKm: String,
    val foodType: String,
    val quantity: String,
    val location: String,
    val donorName: String,
    val pickupWindow: String,
    val pickupPoint: String,
    val description: String,
    val photoRes: Int? = null,
    val id: String = ""        // courier pickup code, e.g. "FR20260045"
)

// Food types used by the filter chips and search matching
val FOOD_TYPES = listOf("Vegetables", "Fruits", "Dairy", "Bakery", "Prepared Meals")

// The full recipient feed: built-in posts + live donor listings
fun allPosts(): List<SurplusPost> =
    SURPLUS_POSTS + DonorStore.listings.map { it.toSurplusPost() }


val SURPLUS_POSTS = listOf(
    SurplusPost(
        "Fresh Vegetables", "Expires: 10 Oct 2026", "2.4 km", "Vegetables", "7 kg",
        "Bangsar, KL", "Aisyah Binti Rahman", "Mon–Fri, 4 PM – 7 PM",
        "Jalan Bangkung, Bangsar, 59100 Kuala Lumpur",
        "Assorted mixed vegetables rescued from a neighbourhood grocer in Bangsar. Leafy greens, carrots, broccoli and more — perfect for home cooking.",
        R.drawable.fresh_veg
    ),
    SurplusPost(
        "Chicken Rice", "Expires: 9 Oct 2026", "3.1 km", "Prepared Meals", "12 packs",
        "Ampang, KL", "Lim Wei Jie", "Daily, 11 AM – 2 PM",
        "Jalan Ampang, 50450 Kuala Lumpur",
        "Classic Malaysian chicken rice rescued from a food court in Ampang. Steamed chicken with fragrant rice and chilli sauce.",
        R.drawable.chicken_rice
    ),
    SurplusPost(
        "Butter Croissants", "Expires: 9 Oct 2026", "1.2 km", "Bakery", "20 pcs",
        "Bangsar, KL", "Nurul Hidayah", "Daily, 5 PM – 8 PM",
        "Jalan Bangsar Utama 3, 59000 Kuala Lumpur",
        "Freshly baked butter croissants rescued from a bakery in Bangsar. Best enjoyed warm — pairs well with morning coffee.",
        R.drawable.butter_crois
    ),
    SurplusPost(
        "Fresh Apples", "Expires: 12 Oct 2026", "0.8 km", "Fruits", "15 kg",
        "Pudu, KL", "Tan Weng Hong", "Weekends, 10 AM – 2 PM",
        "Jalan Pudu, 55100 Kuala Lumpur",
        "Crisp red apples rescued from a fruit stall in Pudu. Gently bruised but perfectly edible — great for juicing or snacking.",
        R.drawable.apple
    ),
    SurplusPost(
        "Steamed Buns", "Expires: 10 Oct 2026", "2.9 km", "Bakery", "30 pcs",
        "Setiawangsa, KL", "Fatimah Binti Zulkifli", "Daily, 7 AM – 10 AM",
        "Jalan Setiawangsa 11, 50490 Kuala Lumpur",
        "Soft steamed buns rescued from a kopitiam in Setiawangsa. Assorted fillings of kaya, red bean and char siu.",
        R.drawable.steam_bun
    ),
    SurplusPost(
        "Sushi Platter", "Expires: 9 Oct 2026", "4.5 km", "Prepared Meals", "8 boxes",
        "Gombak, KL", "Chong Mei Ling", "Daily, 9 PM – 10 PM",
        "Jalan Gombak, 53000 Kuala Lumpur",
        "Assorted maki and nigiri rescued from a sushi outlet in Gombak. Same-day batches kept chilled — consume immediately.",
        R.drawable.sushi_platter
    ),
    SurplusPost(
        "Banana Bread", "Expires: 13 Oct 2026", "1.7 km", "Bakery", "10 loaves",
        "Cheras, KL", "Hafiz Bin Roslan", "Mon–Wed, 3 PM – 6 PM",
        "Jalan Cheras, 56100 Kuala Lumpur",
        "Homemade banana bread rescued from a café in Cheras. Moist and fragrant — still fresh for another two days.",
        R.drawable.banana_bread
    ),
    SurplusPost(
        "Vegetable Curry", "Expires: 11 Oct 2026", "3.6 km", "Prepared Meals", "6 packs",
        "Klang Lama, KL", "Leong Kar Yee", "Mon–Fri, 12 PM – 3 PM",
        "Jalan Klang Lama, 58000 Kuala Lumpur",
        "Rich Malaysian vegetable curry rescued from a mamak restaurant in Klang Lama. Mild spice — with potatoes, long beans and tofu.",
        R.drawable.veg_curry
    ),
    SurplusPost(
        "Milk", "Expires: 15 Oct 2026", "2.2 km", "Dairy", "25 cartons",
        "Damansara, KL", "Siti Binti Ahmad", "Daily, 8 AM – 11 AM",
        "Jalan Damansara, 60000 Kuala Lumpur",
        "Full-cream milk cartons rescued from a minimart in Damansara. Kept refrigerated at all times — expires soon.",
        R.drawable.milk
    ),
    SurplusPost(
        "Wholemeal Bread", "Expires: 11 Oct 2026", "1.9 km", "Bakery", "18 loaves",
        "Ipoh Road, KL", "Kwok Wai Hong", "Daily, 6 AM – 9 AM",
        "Jalan Ipoh, 51200 Kuala Lumpur",
        "Wholemeal loaves rescued from a bakery in Ipoh. Soft crumb, no preservatives — ideal for sandwiches.",
        R.drawable.sour_bread
    ),
    SurplusPost(
        "Roast Chicken", "Expires: 9 Oct 2026", "5.2 km", "Prepared Meals", "14 packs",
        "Genting Klang", "Ravindran A/L Kumar", "Daily, 4 PM – 7 PM",
        "Jalan Genting Klang, 53300 Kuala Lumpur",
        "Roast chicken packs rescued from a hawker stall in Genting Klang. Fully cooked — reheat before serving.",
        R.drawable.roasted_chick
    ),
    SurplusPost(
        "Fruit Yogurt Cups", "Expires: 14 Oct 2026", "0.6 km", "Dairy", "40 cups",
        "SS2, PJ", "Farah Nadia Binti Osman", "Weekends, 9 AM – 12 PM",
        "Jalan SS2/64, 47300 Petaling Jaya",
        "Chilled fruit yogurt cups rescued from a supermarket in SS2. Assorted strawberry and mango flavours.",
        R.drawable.yogurt
    ),
    SurplusPost(
        "Fried Noodles", "Expires: 10 Oct 2026", "2.8 km", "Prepared Meals", "20 packs",
        "Pahang, KL", "Arif Bin Ismail", "Daily, 10 AM – 1 PM",
        "Jalan Pahang, 53000 Kuala Lumpur",
        "Fried noodles rescued from a mamak restaurant in Pahang. Wok-fried with bean sprouts and egg.",
        R.drawable.noodle
    )
)

// ============================================================
//  VIEW — Home: top app bar ("flashfood") + Surplus Listing grid
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    reservedMap: Map<String, Boolean> = emptyMap(),
    onPostClick: (SurplusPost) -> Unit = {}
) {
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
            val posts = remember(DonorStore.listings.size) { allPosts() }
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
                    text = "${posts.size} available",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---- 2-column grid of surplus posts ----
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 16.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                items(posts) { post ->
                    SurplusCard(
                        post = post,
                        isReserved = reservedMap[post.name] == true,
                        onClick = { onPostClick(post) }
                    )
                }
            }
        }
    }
}

// ============================================================
//  Surplus card — shared by Home and Search screens.
//  Tapping a card opens the post detail screen.
// ============================================================
@Composable
fun SurplusCard(
    post: SurplusPost,
    isReserved: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // ---- Photo area ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Color(0xFFB9C2C9))
            ) {
                if (post.photoRes != null) {
                    Image(
                        painter = painterResource(post.photoRes),
                        contentDescription = post.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Rounded.Image,
                            contentDescription = null,
                            tint = Color(0xFFE7ECEF),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // ---- Food type badge (top-left) ----
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FlashGreen,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = post.foodType,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // ---- Reserved badge (top-right) ----
                if (isReserved) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Reserved",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlashGreen
                        )
                    }
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

                // ---- Hardcoded location (below the title) ----
                Text(
                    text = post.location,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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