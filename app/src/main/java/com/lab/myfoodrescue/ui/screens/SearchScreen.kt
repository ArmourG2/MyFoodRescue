package com.lab.myfoodrescue.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab.myfoodrescue.data.repository.DonorStore
import com.lab.myfoodrescue.data.repository.PostStore
import com.lab.myfoodrescue.ui.theme.FlashGreen

// ============================================================
//  VIEW — Search: top bar ("Search" centered, NO profile
//  picture in the top right), rounded search field with filter
//  icon on the right, and the surplus food listing below.
//  User can search by food NAME and by FOOD TYPE.
// ============================================================

// Active filter state (food type chips + expired date range)
data class SearchFilter(
    val foodType: String? = null,
    val expiryFrom: String = "",   // e.g. 10/10/2026
    val expiryTo: String = ""      // e.g. 15/10/2026
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    filter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    onOpenFilter: () -> Unit,
    reservedMap: Map<String, Boolean>,
    onPostClick: (SurplusPost) -> Unit,
    onBackToHome: () -> Unit
) {
    // Matches the food NAME or the FOOD TYPE + applied filters
    // (live, Firestore-synced posts and donor listings)
    val results = remember(searchText, filter, DonorStore.listings.size, PostStore.posts.size) {
        allPosts().filter { post ->
            val query = searchText.trim()
            val matchesSearch = query.isEmpty() ||
                post.name.contains(query, ignoreCase = true) ||
                post.foodType.contains(query, ignoreCase = true)
            val matchesType = filter.foodType == null ||
                post.foodType.equals(filter.foodType, ignoreCase = true)
            val matchesDateRange = expiryInRange(post.expiryDate, filter.expiryFrom, filter.expiryTo)
            matchesSearch && matchesType && matchesDateRange
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                title = {
                    Text(
                        text = "Search",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ---- Search bar: rounded field + filter icon on the right ----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SearchBarField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    hint = "Search...",
                    modifier = Modifier.weight(1f)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                        .clickable { onOpenFilter() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ---- Results count ----
            Text(
                text = "${results.size} results",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (results.isEmpty()) {
                EmptySearchState()
            } else {
                // ---- Surplus food listing posts (same cards as Home) ----
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 16.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results) { post ->
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
}

@Composable
private fun SearchBarField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .defaultMinSize(minHeight = 46.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(23.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(8.dp))
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(FlashGreen),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun EmptySearchState() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No results found",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Try a different food name or type",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
//  VIEW — Filter panel: back + Reset, Food Type chips (like the
//  mock buttons) and the Expired Date Range with two LIGHT
//  fields (left and right, NOT black) like the mock.
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    filter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                title = {
                    Text(
                        text = "Filter",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    TextButton(onClick = { onFilterChange(SearchFilter()) }) {
                        Text(
                            text = "Reset",
                            color = FlashGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ---- Food Type chip buttons ----
            Text(
                text = "Food Type",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FOOD_TYPES.forEach { type ->
                    FilterChipItem(
                        label = type,
                        selected = filter.foodType == type,
                        onClick = {
                            onFilterChange(
                                if (filter.foodType == type) filter.copy(foodType = null)
                                else filter.copy(foodType = type)
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---- Expired Date Range: left + right fields, light style ----
            Text(
                text = "Expired Date Range",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LightDateField(
                    value = filter.expiryFrom,
                    hint = "dd/mm/yyyy",
                    onValueChange = { onFilterChange(filter.copy(expiryFrom = it)) },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "to",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LightDateField(
                    value = filter.expiryTo,
                    hint = "dd/mm/yyyy",
                    onValueChange = { onFilterChange(filter.copy(expiryTo = it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "e.g. 10/10/2026 – 15/10/2026",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Chip button for a food type (selected = filled green, unselected = white + border)
@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) FlashGreen else MaterialTheme.colorScheme.surface,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

// Light (NOT black) date range field — white surface, light border, rounded
@Composable
private fun LightDateField(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box {
            if (value.isEmpty()) {
                Text(
                    text = hint,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(FlashGreen),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Matches the hardcoded "Expires: dd MMM yyyy" against a dd/MM/yyyy range.
// Blank/invalid range inputs are treated as "no bound".
private fun expiryInRange(expiryDate: String, from: String, to: String): Boolean {
    val expiry = parseDate(expiryDate.removePrefix("Expires: ").trim()) ?: return true
    val fromD = parseDate(from)
    val toD = parseDate(to)
    if (fromD != null && expiry < fromD) return false
    if (toD != null && expiry > toD) return false
    return true
}

// Parses "dd/MM/yyyy" or "dd MMM yyyy" into a comparable Int (yyyyMMdd)
private fun parseDate(input: String): Int? {
    val clean = input.trim()
    return when {
        Regex("^\\d{2}/\\d{2}/\\d{4}$").matches(clean) -> {
            val (dd, mm, yyyy) = clean.split("/")
            (yyyy + mm + dd).toInt()
        }
        Regex("^\\d{1,2} [A-Za-z]{3} \\d{4}$").matches(clean) -> {
            val parts = clean.split(" ")
            val month = months[parts[1].lowercase()] ?: return null
            (parts[2] + month + parts[0].padStart(2, '0')).toInt()
        }
        else -> null
    }
}

private val months = mapOf(
    "jan" to "01", "feb" to "02", "mar" to "03", "apr" to "04",
    "may" to "05", "jun" to "06", "jul" to "07", "aug" to "08",
    "sep" to "09", "oct" to "10", "nov" to "11", "dec" to "12"
)