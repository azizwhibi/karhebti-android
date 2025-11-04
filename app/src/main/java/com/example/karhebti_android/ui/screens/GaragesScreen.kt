package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.karhebti_android.data.model.Garage
import com.example.karhebti_android.data.model.GarageService
import com.example.karhebti_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaragesScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("Tous") }
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("Tous", "Révision", "Pneus", "CT")

    // Sample data
    val garages = remember {
        listOf(
            Garage(
                id = "1",
                name = "Garage Central",
                address = "15 Rue de la République, 75001 Paris",
                latitude = 48.8566,
                longitude = 2.3522,
                distance = 2.5,
                rating = 4.5f,
                reviewCount = 127,
                phoneNumber = "+33 1 23 45 67 89",
                isOpen = true,
                openUntil = "18h00",
                services = listOf(
                    GarageService.REVISION,
                    GarageService.VIDANGE,
                    GarageService.FREINS
                )
            ),
            Garage(
                id = "2",
                name = "Auto Service Pro",
                address = "28 Avenue des Champs, 75008 Paris",
                latitude = 48.8698,
                longitude = 2.3078,
                distance = 4.2,
                rating = 4.8f,
                reviewCount = 245,
                phoneNumber = "+33 1 98 76 54 32",
                isOpen = true,
                openUntil = "19h00",
                services = listOf(
                    GarageService.REVISION,
                    GarageService.PNEUS,
                    GarageService.CONTROLE_TECHNIQUE
                )
            ),
            Garage(
                id = "3",
                name = "CT Express",
                address = "42 Boulevard Saint-Michel, 75006 Paris",
                latitude = 48.8534,
                longitude = 2.3416,
                distance = 1.8,
                rating = 4.2f,
                reviewCount = 89,
                phoneNumber = "+33 1 45 67 89 01",
                isOpen = false,
                openUntil = null,
                services = listOf(
                    GarageService.CONTROLE_TECHNIQUE
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Garages") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepPurple,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Rechercher un garage...", color = InputPlaceholder) },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Rechercher", tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Effacer", tint = TextSecondary)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = InputBorder,
                    focusedBorderColor = InputBorderFocused,
                    unfocusedTextColor = InputText,
                    focusedTextColor = InputText,
                    cursorColor = DeepPurple
                ),
                singleLine = true
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepPurple,
                            selectedLabelColor = Color.White,
                            containerColor = LightPurple,
                            labelColor = DeepPurple
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Garages List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(garages) { garage ->
                    GarageCard(garage = garage)
                }
            }
        }
    }
}

@Composable
fun GarageCard(garage: Garage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with name and distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = garage.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )

                    // Rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${garage.rating}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "(${garage.reviewCount} avis)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Distance Badge
                Surface(
                    shape = CircleShape,
                    color = LightGrey.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${garage.distance} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Address
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = garage.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Open Status
            if (garage.isOpen && garage.openUntil != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Ouvert jusqu'à ${garage.openUntil}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AlertRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Fermé",
                        style = MaterialTheme.typography.bodySmall,
                        color = AlertRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Services
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(garage.services) { service ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightPurple
                    ) {
                        Text(
                            text = getServiceLabel(service),
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepPurple,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider()

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call Button
                OutlinedButton(
                    onClick = { /* Call garage */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DeepPurple
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Appeler")
                }

                // Reserve Button
                Button(
                    onClick = { /* Reserve */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepPurple
                    )
                ) {
                    Text("Réserver")
                }
            }
        }
    }
}

fun getServiceLabel(service: GarageService): String {
    return when (service) {
        GarageService.REVISION -> "Révision"
        GarageService.VIDANGE -> "Vidange"
        GarageService.PNEUS -> "Pneus"
        GarageService.FREINS -> "Freins"
        GarageService.CONTROLE_TECHNIQUE -> "CT"
        GarageService.BATTERIE -> "Batterie"
        GarageService.CLIMATISATION -> "Climatisation"
        GarageService.CARROSSERIE -> "Carrosserie"
    }
}

@Preview(showBackground = true)
@Composable
fun GaragesScreenPreview() {
    KarhebtiandroidTheme {
        GaragesScreen()
    }
}
