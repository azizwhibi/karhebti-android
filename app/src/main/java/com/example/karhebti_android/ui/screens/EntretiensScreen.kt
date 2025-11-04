package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.karhebti_android.data.model.*
import com.example.karhebti_android.ui.theme.*
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntretiensScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("À venir", "Historique")

    // Sample data
    val upcomingEntretiens = remember {
        listOf(
            Entretien(
                id = "1",
                vehicleId = "1",
                vehicleName = "Renault Clio",
                type = MaintenanceType.VIDANGE,
                title = "Vidange complète",
                description = "Vidange + Filtre à huile + Filtre à air",
                date = Date(),
                location = "Garage Central",
                garageName = "Garage Central",
                price = 120.0,
                status = EntretienStatus.A_VENIR,
                urgency = UrgencyLevel.URGENT,
                tasks = listOf("Vidange", "Filtre à huile", "Filtre à air"),
                daysUntil = 5
            ),
            Entretien(
                id = "2",
                vehicleId = "2",
                vehicleName = "Peugeot 308",
                type = MaintenanceType.REVISION,
                title = "Révision complète",
                description = "Révision annuelle",
                date = Date(),
                location = "Auto Service Pro",
                garageName = "Auto Service Pro",
                price = 280.0,
                status = EntretienStatus.A_VENIR,
                urgency = UrgencyLevel.ATTENTION,
                tasks = listOf("Révision", "Freins", "Pneus"),
                daysUntil = 15
            )
        )
    }

    val historicEntretiens = remember {
        listOf(
            Entretien(
                id = "3",
                vehicleId = "1",
                vehicleName = "Renault Clio",
                type = MaintenanceType.CONTROLE_TECHNIQUE,
                title = "Contrôle technique",
                description = "Contrôle technique périodique",
                date = Date(),
                location = "CT Auto",
                garageName = "CT Auto",
                price = 65.0,
                status = EntretienStatus.TERMINE,
                urgency = UrgencyLevel.NORMAL,
                tasks = listOf("Contrôle technique")
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entretiens") },
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
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = DeepPurple
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        selectedContentColor = DeepPurple,
                        unselectedContentColor = TextSecondary
                    )
                }
            }

            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val entretiens = if (selectedTab == 0) upcomingEntretiens else historicEntretiens
                items(entretiens) { entretien ->
                    EntretienCard(entretien = entretien)
                }
            }
        }
    }
}

@Composable
fun EntretienCard(entretien: Entretien) {
    val urgencyColor = when (entretien.urgency) {
        UrgencyLevel.NORMAL -> AccentGreen
        UrgencyLevel.ATTENTION -> AccentYellow
        UrgencyLevel.URGENT -> AlertRed
    }

    val maintenanceIcon = when (entretien.type) {
        MaintenanceType.VIDANGE -> Icons.Default.Opacity
        MaintenanceType.REVISION -> Icons.Default.Build
        MaintenanceType.PNEUS -> Icons.Default.Circle
        MaintenanceType.FREINS -> Icons.Default.Warning
        MaintenanceType.CONTROLE_TECHNIQUE -> Icons.Default.VerifiedUser
        MaintenanceType.BATTERIE -> Icons.Default.BatteryChargingFull
        else -> Icons.Default.Build
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (entretien.urgency == UrgencyLevel.URGENT) {
                    Modifier.border(2.dp, AlertRed, RoundedCornerShape(16.dp))
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = maintenanceIcon,
                        contentDescription = null,
                        tint = DeepPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = entretien.vehicleName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = entretien.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                }

                if (entretien.urgency != UrgencyLevel.NORMAL) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = urgencyColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (entretien.urgency == UrgencyLevel.URGENT) "Urgent" else "Attention",
                            style = MaterialTheme.typography.labelMedium,
                            color = urgencyColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Tasks
            if (entretien.tasks.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = LightPurple.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Travaux prévus:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        entretien.tasks.forEach { task ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = task,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Location and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                        text = entretien.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (entretien.daysUntil != null) {
                    Text(
                        text = "Dans ${entretien.daysUntil} jours",
                        style = MaterialTheme.typography.bodySmall,
                        color = urgencyColor
                    )
                }
            }

            HorizontalDivider()

            // Price and Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entretien.price}€",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepPurple
                )

                OutlinedButton(
                    onClick = { /* Show details */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DeepPurple
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Text("Détails")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EntretiensScreenPreview() {
    KarhebtiandroidTheme {
        EntretiensScreen()
    }
}
