package com.example.karhebti_android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.BreakdownResponse
import com.example.karhebti_android.data.api.RetrofitClient
import com.example.karhebti_android.repository.BreakdownsRepository
import com.example.karhebti_android.ui.components.OpenStreetMapView
import com.example.karhebti_android.ui.theme.*
import com.example.karhebti_android.viewmodel.BreakdownViewModel
import com.example.karhebti_android.viewmodel.BreakdownViewModelFactory
import com.example.karhebti_android.viewmodel.BreakdownUiState
import com.example.karhebti_android.utils.DistanceUtils
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Wrapper composable pour charger les données du breakdown avant d'afficher le tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakdownTrackingScreenWrapper(
    breakdownId: String,
    userRole: String = "user",
    onBackClick: () -> Unit = {}
) {
    val api = remember { RetrofitClient.breakdownsApiService }
    val repo = remember { BreakdownsRepository(api) }
    val factory = remember { BreakdownViewModelFactory(repo) }
    val viewModel: BreakdownViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    var breakdown by remember { mutableStateOf<BreakdownResponse?>(null) }

    // Fetch breakdown and poll for updates
    LaunchedEffect(breakdownId) {
        while (true) {
            viewModel.fetchBreakdownById(breakdownId)  // ✅ String directement
            delay(10000) // Poll every 10 seconds
        }
    }

    // Handle state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BreakdownUiState.Success -> {
                val data = state.data
                if (data is BreakdownResponse) {
                    breakdown = data
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suivi SOS") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState is BreakdownUiState.Loading && breakdown == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                breakdown != null -> {
                    BreakdownTrackingScreen(
                        breakdown = breakdown!!,
                        userRole = userRole
                    )
                }
                uiState is BreakdownUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Erreur de chargement",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            (uiState as BreakdownUiState.Error).message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Écran de suivi en temps réel d'une demande SOS acceptée
 */
@Composable
fun BreakdownTrackingScreen(
    breakdown: BreakdownResponse,
    userRole: String = "user",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Position réelle du garage assigné
    val garageLatitude = breakdown.assignedToDetails?.latitude
    val garageLongitude = breakdown.assignedToDetails?.longitude

    // Log pour débogage
    LaunchedEffect(breakdown.assignedTo, garageLatitude, garageLongitude) {
        if (breakdown.assignedTo != null) {
            if (garageLatitude != null && garageLongitude != null) {
                android.util.Log.d("BreakdownTracking", "Client: ${breakdown.latitude}, ${breakdown.longitude}")
                android.util.Log.d("BreakdownTracking", "Garage réel: $garageLatitude, $garageLongitude")
            } else {
                android.util.Log.w("BreakdownTracking", "Position du garage non disponible pour assignedTo=${breakdown.assignedTo}")
            }
        }
    }

    // Calculer la distance si les deux positions sont disponibles
    val distance = remember(breakdown.latitude, breakdown.longitude, garageLatitude, garageLongitude) {
        val clientLat = breakdown.latitude
        val clientLon = breakdown.longitude
        val garageLat = garageLatitude
        val garageLon = garageLongitude

        if (clientLat != null && clientLon != null && garageLat != null && garageLon != null) {
            DistanceUtils.calculateDistance(clientLat, clientLon, garageLat, garageLon)
        } else null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Badge de statut
        StatusCard(status = breakdown.status)

        Spacer(modifier = Modifier.height(24.dp))

        // Distance Card - Afficher si la distance est calculable
        if (distance != null && (breakdown.status == "ACCEPTED" || breakdown.status == "IN_PROGRESS")) {
            DistanceCard(
                distance = distance,
                status = breakdown.status
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Carte de localisation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (breakdown.latitude != null && breakdown.longitude != null) {
                    OpenStreetMapView(
                        latitude = breakdown.latitude,
                        longitude = breakdown.longitude,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Position en cours...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Informations sur le breakdown
        BreakdownInfoCard(breakdown = breakdown)

        Spacer(modifier = Modifier.height(16.dp))

        // Timeline de progression
        TimelineCard(status = breakdown.status)

        Spacer(modifier = Modifier.height(24.dp))

        // Boutons d'action
        if (breakdown.status == "ACCEPTED" || breakdown.status == "IN_PROGRESS") {
            // Déterminer le texte et l'action selon le rôle
            val isGarageOwner = userRole == "propGarage"
            val buttonText = if (isGarageOwner) "Appeler le client" else "Appeler l'assistant"
            val buttonSubtext = if (isGarageOwner) "Contacter pour plus d'informations" else "Contacter le garage pour assistance"
            
            // Bouton pour appeler
            Button(
                onClick = {
                    // TODO: Récupérer le vrai numéro selon le rôle
                    // - Si garagiste: numéro du client
                    // - Si client: numéro du garage
                    val phoneNumber = "tel:+216" // Numéro fictif si non disponible
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        buttonText,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        buttonSubtext,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(status: String) {
    val (color, icon, text) = when (status) {
        "PENDING" -> Triple(RedSOS, Icons.Default.Warning, "En attente...")
        "ACCEPTED" -> Triple(BlueInfo, Icons.Default.CheckCircle, "Accepté ✓")
        "IN_PROGRESS" -> Triple(OrangeInProgress, Icons.Default.Build, "En cours")
        "COMPLETED" -> Triple(GreenResolved, Icons.Default.Done, "Terminé ✓")
        "REFUSED" -> Triple(Color.Gray, Icons.Default.Close, "Refusé")
        "CANCELLED" -> Triple(Color.Gray, Icons.Default.Cancel, "Annulé")
        else -> Triple(Color.Gray, Icons.Default.Info, status)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Carte affichant la distance réelle entre le garage et le client
 * et le temps estimé d'arrivée de l'assistant
 */
@Composable
private fun DistanceCard(
    distance: Double,
    status: String
) {
    val formattedDistance = DistanceUtils.formatDistance(distance)
    val eta = DistanceUtils.estimateETA(distance)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BlueInfo.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Titre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = BlueInfo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (status == "ACCEPTED") "L'assistant est en route" else "Assistant en cours d'intervention",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BlueInfo
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Distance et temps côte à côte
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Distance
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = BlueInfo,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Distance",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        formattedDistance,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlueInfo
                    )
                }

                // Divider vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(70.dp)
                        .background(Color.LightGray)
                )

                // Temps estimé
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = if (status == "IN_PROGRESS") OrangeInProgress else BlueInfo,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Arrivée estimée",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        eta,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (status == "IN_PROGRESS") OrangeInProgress else BlueInfo
                    )
                }
            }

            // Message selon le statut
            if (status == "ACCEPTED") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BlueInfo.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = null,
                        tint = BlueInfo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "L'assistant se dirige vers votre position",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BlueInfo,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (status == "IN_PROGRESS") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OrangeInProgress.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = OrangeInProgress,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "L'assistant est sur place et travaille sur votre véhicule",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrangeInProgress,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownInfoCard(breakdown: BreakdownResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Détails de la demande",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(
                icon = Icons.Default.Build,
                label = "Type",
                value = breakdown.type
            )

            if (breakdown.description.isNullOrEmpty().not()) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    icon = Icons.Default.Info,
                    label = "Description",
                    value = breakdown.description
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(
                icon = Icons.Default.DateRange,
                label = "Créé le",
                value = breakdown.createdAt?.take(10) ?: "N/A"
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TimelineCard(status: String) {
    val steps = listOf(
        "PENDING" to "En attente",
        "ACCEPTED" to "Accepté",
        "IN_PROGRESS" to "En cours",
        "COMPLETED" to "Terminé"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Progression",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, (stepStatus, stepLabel) ->
                    val isActive = isStepActive(status, stepStatus)
                    val isPassed = isStepPassed(status, stepStatus)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    when {
                                        isPassed -> GreenResolved
                                        isActive -> BlueInfo
                                        else -> Color.LightGray
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPassed) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    (index + 1).toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            stepLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isActive || isPassed) MaterialTheme.colorScheme.primary else Color.Gray,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

private fun isStepActive(currentStatus: String, stepStatus: String): Boolean {
    return currentStatus == stepStatus
}

private fun isStepPassed(currentStatus: String, stepStatus: String): Boolean {
    val statusOrder = listOf("PENDING", "ACCEPTED", "IN_PROGRESS", "COMPLETED")
    val currentIndex = statusOrder.indexOf(currentStatus)
    val stepIndex = statusOrder.indexOf(stepStatus)
    return currentIndex > stepIndex
}

@Composable
fun StatusBadge(type: String, status: String) {
    val color = statusColor(status)
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(type.take(1), color = Color.White, fontWeight = FontWeight.Bold)
    }
}

fun statusColor(status: String): Color = when (status) {
    "PENDING" -> RedSOS
    "ACCEPTED" -> BlueInfo
    "IN_PROGRESS" -> OrangeInProgress
    "COMPLETED" -> GreenResolved
    else -> Color.Gray
}

