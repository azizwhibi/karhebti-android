package com.example.karhebti_android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.karhebti_android.ui.theme.RedSOS
import com.example.karhebti_android.viewmodel.BreakdownViewModel
import com.example.karhebti_android.viewmodel.BreakdownViewModelFactory
import com.example.karhebti_android.viewmodel.BreakdownUiState
import kotlinx.coroutines.launch

/**
 * Detailed view for garage owners to accept or reject an SOS request
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakdownDetailScreen(
    breakdownId: String,
    onBackClick: () -> Unit = {},
    onAccepted: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val api = remember { RetrofitClient.breakdownsApiService }
    val repo = remember { BreakdownsRepository(api) }
    val factory = remember { BreakdownViewModelFactory(repo) }
    val viewModel: BreakdownViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    var breakdown by remember { mutableStateOf<BreakdownResponse?>(null) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    // Fetch breakdown details
    LaunchedEffect(breakdownId) {
        viewModel.fetchBreakdownById(breakdownId.toInt())
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
            is BreakdownUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    if (showAcceptDialog) {
        AcceptConfirmationDialog(
            breakdown = breakdown,
            onDismiss = { showAcceptDialog = false },
            onConfirm = {
                showAcceptDialog = false
                scope.launch {
                    viewModel.updateBreakdownStatus(breakdownId.toInt(), "ACCEPTED")
                    snackbarHostState.showSnackbar("Demande acceptée ✓")
                    onAccepted()
                }
            }
        )
    }

    if (showRejectDialog) {
        RejectConfirmationDialog(
            onDismiss = { showRejectDialog = false },
            onConfirm = {
                showRejectDialog = false
                scope.launch {
                    viewModel.updateBreakdownStatus(breakdownId.toInt(), "REFUSED")
                    snackbarHostState.showSnackbar("Demande refusée")
                    onBackClick()
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails SOS") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState is BreakdownUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                breakdown != null -> {
                    BreakdownDetailContent(
                        breakdown = breakdown!!,
                        onAccept = { showAcceptDialog = true },
                        onReject = { showRejectDialog = true }
                    )
                }

                else -> {
                    Text(
                        "Chargement...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun BreakdownDetailContent(
    breakdown: BreakdownResponse,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = RedSOS.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = RedSOS,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "🚨 Nouvelle demande SOS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Assistance ${breakdown.type} demandée",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Map Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            OpenStreetMapView(
                latitude = breakdown.latitude ?: 0.0,
                longitude = breakdown.longitude ?: 0.0,
                zoom = 15.0,
                markerTitle = "Position du client"
            )
        }

        // Location Info
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "📍 Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Latitude: ${breakdown.latitude}")
                Text("Longitude: ${breakdown.longitude}")
                // TODO: Add distance calculation
                Text(
                    "Distance: Calcul en cours...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Problem Details
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "📋 Détails du problème",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    Text("Type: ", fontWeight = FontWeight.Medium)
                    Text(breakdown.type)
                }

                if (!breakdown.description.isNullOrBlank()) {
                    Row {
                        Text("Description: ", fontWeight = FontWeight.Medium)
                        Text(breakdown.description)
                    }
                }

                Row {
                    Text("ID: ", fontWeight = FontWeight.Medium)
                    Text(breakdown.id ?: "N/A")
                }

                Row {
                    Text("Statut: ", fontWeight = FontWeight.Medium)
                    Text(breakdown.status)
                }
            }
        }

        // Client Info (if available)
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "👤 Client",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("User ID: ${breakdown.userId ?: "N/A"}")
                // TODO: Add phone number if available
            }
        }

        // Action Buttons
        if (breakdown.status == "PENDING") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "✅ Accepter",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RedSOS
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "❌ Refuser",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    "Cette demande a déjà été ${if (breakdown.status == "ACCEPTED") "acceptée" else "traitée"}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun AcceptConfirmationDialog(
    breakdown: BreakdownResponse?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text("Accepter cette demande SOS?") },
        text = {
            Column {
                Text("Vous vous engagez à:")
                Spacer(Modifier.height(8.dp))
                Text("• Vous rendre sur place")
                Text("• Arriver dans 15-20 minutes")
                Text("• Apporter le matériel nécessaire")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Le client sera notifié immédiatement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun RejectConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Cancel,
                contentDescription = null,
                tint = RedSOS,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text("Refuser cette demande?") },
        text = {
            Text("Le client sera notifié et la demande sera annulée. Cette action est irréversible.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedSOS
                )
            ) {
                Text("Refuser")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
