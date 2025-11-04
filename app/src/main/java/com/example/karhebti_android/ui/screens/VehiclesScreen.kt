package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.karhebti_android.data.model.Vehicle
import com.example.karhebti_android.data.model.VehicleStatus
import com.example.karhebti_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    onBackClick: () -> Unit = {},
    onVehicleClick: (String) -> Unit = {}
) {
    // Sample data
    val vehicles = remember {
        listOf(
            Vehicle(
                id = "1",
                name = "Renault Clio",
                brand = "Renault",
                model = "Clio V",
                year = 2020,
                plateNumber = "AB-123-CD",
                mileage = 45000,
                status = VehicleStatus.BON,
                nextMaintenance = "Révision",
                nextMaintenanceDays = 15
            ),
            Vehicle(
                id = "2",
                name = "Peugeot 308",
                brand = "Peugeot",
                model = "308",
                year = 2019,
                plateNumber = "EF-456-GH",
                mileage = 78000,
                status = VehicleStatus.URGENT,
                nextMaintenance = "Vidange",
                nextMaintenanceDays = 5
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes véhicules") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add vehicle */ },
                containerColor = DeepPurple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Ajouter véhicule")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(vehicles) { vehicle ->
                VehicleCard(
                    vehicle = vehicle,
                    onClick = { onVehicleClick(vehicle.id) }
                )
            }
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit
) {
    val statusColor = when (vehicle.status) {
        VehicleStatus.BON -> AccentGreen
        VehicleStatus.ATTENTION -> AccentYellow
        VehicleStatus.URGENT -> AlertRed
    }

    val statusLabel = when (vehicle.status) {
        VehicleStatus.BON -> "Bon"
        VehicleStatus.ATTENTION -> "Attention"
        VehicleStatus.URGENT -> "Urgent"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (vehicle.status == VehicleStatus.BON) {
                    Modifier.border(2.dp, DeepPurple, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            ),
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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "${vehicle.year} • ${vehicle.plateNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Mileage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${vehicle.mileage} km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Next Maintenance
            if (vehicle.nextMaintenance != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = LightPurple.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Prochain entretien",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = vehicle.nextMaintenance,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (vehicle.nextMaintenanceDays!! < 10) AlertRed.copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${vehicle.nextMaintenanceDays} jours",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (vehicle.nextMaintenanceDays < 10) AlertRed else AccentGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconTextButton(
                    icon = Icons.Default.Info,
                    label = "Détails",
                    onClick = onClick
                )
                IconTextButton(
                    icon = Icons.Default.Build,
                    label = "Entretien",
                    onClick = { /* Navigate to maintenance */ }
                )
                IconTextButton(
                    icon = Icons.Default.Article,
                    label = "Docs",
                    onClick = { /* Navigate to documents */ }
                )
            }
        }
    }
}

@Composable
fun IconTextButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = DeepPurple,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = DeepPurple
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VehiclesScreenPreview() {
    KarhebtiandroidTheme {
        VehiclesScreen()
    }
}
