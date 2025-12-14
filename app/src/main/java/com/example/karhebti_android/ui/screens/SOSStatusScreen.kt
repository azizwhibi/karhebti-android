package com.example.karhebti_android.ui.screens

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.RetrofitClient
import com.example.karhebti_android.repository.BreakdownsRepository
import com.example.karhebti_android.ui.theme.RedSOS
import com.example.karhebti_android.viewmodel.BreakdownViewModel
import com.example.karhebti_android.viewmodel.BreakdownViewModelFactory
import com.example.karhebti_android.viewmodel.BreakdownUiState
import com.example.karhebti_android.data.BreakdownResponse
import kotlinx.coroutines.delay

/**
 * Écran affichant le statut d'une demande SOS avec polling automatique
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSStatusScreen(
    breakdownId: String?,
    type: String,
    latitude: Double,
    longitude: Double,
    status: String = "PENDING",
    onBackClick: () -> Unit = {},
    onNavigateToTracking: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // ViewModel for polling
    val api = remember { RetrofitClient.breakdownsApiService }
    val repo = remember { BreakdownsRepository(api) }
    val factory = remember { BreakdownViewModelFactory(repo) }
    val viewModel: BreakdownViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    var currentBreakdown by remember { mutableStateOf<BreakdownResponse?>(null) }
    var currentStatus by remember { mutableStateOf(status) }

    // Poll for status changes every 5 seconds
    LaunchedEffect(breakdownId) {
        if (breakdownId != null) {
            while (true) {
                viewModel.fetchBreakdownById(breakdownId)  // ✅ Utiliser String directement
                delay(5000) // Poll every 5 seconds
            }
        }
    }

    // Handle state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BreakdownUiState.Success -> {
                val data = state.data
                if (data is BreakdownResponse) {
                    currentBreakdown = data
                    val newStatus = data.status

                    // Auto-navigate to tracking when status changes to ACCEPTED
                    if (currentStatus == "PENDING" && newStatus == "ACCEPTED") {
                        android.util.Log.d("SOSStatus", "✅ Status changed to ACCEPTED! Navigating to tracking...")
                        onNavigateToTracking(breakdownId ?: "")
                    }

                    currentStatus = newStatus
                }
            }
            else -> {}
        }
    }

    // Animation pour le pulse du bouton SOS
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statut SOS") },
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icône animée selon le statut
            when (currentStatus) {
                "PENDING" -> {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(scale)
                            .background(RedSOS.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "En attente",
                            tint = RedSOS,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        "Recherche d'un garage...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                "ACCEPTED" -> {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(scale)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Accepté",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                "REFUSED" -> {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = "Refusé",
                        tint = RedSOS,
                        modifier = Modifier.size(120.dp)
                    )
                }
                "IN_PROGRESS" -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(120.dp),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                "COMPLETED" -> {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Terminé",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(120.dp)
                    )
                }
                else -> {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Statut inconnu",
                        tint = Color.Gray,
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Titre du statut
            Text(
                text = when (currentStatus) {
                    "PENDING" -> "Demande SOS reçue"
                    "ACCEPTED" -> "🎉 Garage trouvé!"
                    "REFUSED" -> "SOS Refusé"
                    "IN_PROGRESS" -> "Assistance en route"
                    "COMPLETED" -> "Assistance terminée"
                    else -> "Statut inconnu"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Message de statut
            Text(
                text = when (currentStatus) {
                    "PENDING" -> "Votre demande d'assistance a été enregistrée. Un technicien sera bientôt assigné."
                    "ACCEPTED" -> "Le garage a accepté votre demande ! Redirection vers le suivi..."
                    "REFUSED" -> "Le garage a refusé votre demande. Veuillez réessayer ou contacter un autre garage."
                    "IN_PROGRESS" -> "Un technicien est en route vers votre position. Veuillez rester sur place."
                    "COMPLETED" -> "L'assistance a été effectuée avec succès. Merci d'avoir utilisé notre service."
                    else -> "Veuillez patienter..."
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // Carte d'informations
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ID de la demande
                    if (breakdownId != null) {
                        InfoRow(label = "ID Demande", value = "#${breakdownId.take(8)}...")
                    }

                    // Type de panne
                    InfoRow(label = "Type de panne", value = type)

                    // Position
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                "Position GPS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Lat: ${String.format("%.4f", latitude)}, Lon: ${String.format("%.4f", longitude)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Statut
                    InfoRow(
                        label = "Statut",
                        value = when (currentStatus) {
                            "PENDING" -> "⏱️ En attente de réponse du garage..."
                            "ACCEPTED" -> "✅ Accepté"
                            "REFUSED" -> "❌ Refusé"
                            "IN_PROGRESS" -> "🚗 En cours"
                            "COMPLETED" -> "✓ Terminé"
                            else -> currentStatus
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action button
            if (currentStatus == "ACCEPTED") {
                Button(
                    onClick = { onNavigateToTracking(breakdownId ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Voir le suivi", modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            if (currentStatus == "PENDING") {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Retour à l'accueil")
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
