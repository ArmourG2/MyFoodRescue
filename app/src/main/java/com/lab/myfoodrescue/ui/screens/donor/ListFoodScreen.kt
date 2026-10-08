package com.lab.myfoodrescue.ui.screens.donor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab.myfoodrescue.R
import com.lab.myfoodrescue.data.repository.DonorListing
import com.lab.myfoodrescue.data.repository.DonorStore
import com.lab.myfoodrescue.ui.screens.FOOD_TYPES
import com.lab.myfoodrescue.ui.theme.FlashGreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================
//  VIEW — List Food form (donor FAB). The donor fills in the
//  food details and posts the listing; it appears at the top
//  of the Donate tab and in the recipient feed.
// ============================================================

private val PRESET_PHOTOS = listOf(
    R.drawable.fresh_veg, R.drawable.chicken_rice, R.drawable.butter_crois,
    R.drawable.apple, R.drawable.milk, R.drawable.veg_curry,
    R.drawable.steam_bun, R.drawable.yogurt, R.drawable.banana, R.drawable.beras, R.drawable.burger,
    R.drawable.lekor, R.drawable.meggi_kari, R.drawable.roti_canai, R.drawable.sardine,
    R.drawable.tepung
)

@Composable
fun ListFoodScreen(
    onBack: () -> Unit,
    onPosted: () -> Unit
) {
    var photoRes by remember { mutableStateOf<Int?>(null) }
    var showPhotoPicker by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var foodType by remember { mutableStateOf("") }
    var expireDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(8.dp))

            // ---- Header: back arrow + title ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "List Food",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(20.dp))

            // ---- Photo (optional) ----
            Surface(
                onClick = { showPhotoPicker = true },
                enabled = true,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
            ) {
                val selectedPhoto = photoRes
                if (selectedPhoto != null) {
                    Image(
                        painter = painterResource(selectedPhoto),
                        contentDescription = "Selected food photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "+ Add Photos (optional)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Food Name ----
            FormField(
                label = "Food Name",
                required = true,
                value = name,
                onValueChange = { name = it },
                placeholder = "e.g. Fresh Vegetables",
                error = errors["name"]
            )

            Spacer(Modifier.height(12.dp))

            // ---- Food Type (chips) ----
            Text(
                text = "Food Type *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            errors["foodType"]?.let {
                Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                FOOD_TYPES.forEach { type ->
                    FilterChip(
                        selected = foodType == type,
                        onClick = {
                            foodType = type
                            errors = errors - "foodType"
                        },
                        label = { Text(type, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Expire Date ----
            FormField(
                label = "Expire Date",
                required = true,
                value = expireDate,
                onValueChange = {},
                placeholder = "Select date",
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Rounded.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { showDatePicker = true },
                error = errors["expireDate"]
            )

            Spacer(Modifier.height(12.dp))

            // ---- Weight ----
            FormField(
                label = "Weight",
                required = true,
                value = weight,
                onValueChange = { weight = it },
                placeholder = "e.g. 5 kg (approx.)",
                error = errors["weight"]
            )

            Spacer(Modifier.height(12.dp))

            // ---- Location ----
            FormField(
                label = "Location",
                required = true,
                value = location,
                onValueChange = { location = it },
                placeholder = "e.g. Bangsar, KL",
                error = errors["location"]
            )

            Spacer(Modifier.height(12.dp))

            // ---- About this food ----
            Text(
                text = "About This Food (optional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = {
                    Text(
                        "Additional details about the food...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // ---- Post Listing ----
            Button(
                onClick = {
                    val newErrors = validate(name, foodType, expireDate, weight, location)
                    if (newErrors.isEmpty()) {
                        // Range is generated automatically: a random 0.1 km – 7.0 km
                        val randomRange = String.format(Locale.US, "%.1f", (1..70).random() / 10.0)
                        DonorStore.add(
                            DonorListing(
                                id = DonorStore.nextId(),
                                name = name.trim(),
                                foodType = foodType,
                                weight = weight.trim(),
                                expiryDate = "Exp: $expireDate",
                                location = location.trim(),
                                distanceKm = "$randomRange km",
                                description = description.trim(),
                                photoRes = photoRes
                            )
                        )
                        onPosted()
                    } else {
                        errors = newErrors
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FlashGreenDark,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = "Post Listing", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ---- Date picker ----
    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        expireDate = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                            .format(Date(millis))
                        errors = errors - "expireDate"
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ---- Photo picker (preset images for the prototype) ----
    if (showPhotoPicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPhotoPicker = false },
            title = { Text("Choose a photo") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    PRESET_PHOTOS.forEach { res ->
                        Image(
                            painter = painterResource(res),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    photoRes = res
                                    showPhotoPicker = false
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    photoRes = null
                    showPhotoPicker = false
                }) { Text("No photo") }
            }
        )
    }
}

private fun validate(
    name: String, foodType: String, expireDate: String,
    weight: String, location: String
): Map<String, String> {
    val errors = mutableMapOf<String, String>()
    if (name.isBlank()) errors["name"] = "Food name is required."
    if (foodType.isBlank()) errors["foodType"] = "Select a food type."
    if (expireDate.isBlank()) errors["expireDate"] = "Select a date."
    if (weight.isBlank()) errors["weight"] = "Weight is required."
    if (location.isBlank()) errors["location"] = "Location is required."
    return errors
}

@Composable
private fun FormField(
    label: String,
    required: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    error: String? = null
) {
    Column {
        Text(
            text = if (required) "$label *" else label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            enabled = onClick == null,
            placeholder = {
                Text(
                    placeholder,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = error != null,
            supportingText = error?.let {
                { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            colors = fieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable { onClick() }
                    } else {
                        Modifier
                    }
                )
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
)
