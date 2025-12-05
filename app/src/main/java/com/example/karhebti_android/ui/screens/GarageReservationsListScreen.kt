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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.viewmodel.ReservationViewModel
import com.example.karhebti_android.viewmodel.GarageViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import com.example.karhebti_android.data.api.ReservationResponse
import com.example.karhebti_android.data.api.GarageResponse
import com.example.karhebti_android.ui.theme.AccentYellow
import com.example.karhebti_android.ui.theme.AlertRed
import com.example.karhebti_android.ui.theme.DeepPurple
import com.example.karhebti_android.ui.theme.LightGrey
import com.example.karhebti_android.ui.theme.LightPurple
import com.example.karhebti_android.ui.theme.MediumGrey
import com.example.karhebti_android.ui.theme.StatusGood
import com.example.karhebti_android.ui.theme.TextPrimary
import com.example.karhebti_android.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageReservationsListScreen(
    garageId: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: ReservationViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val garageViewModel: GarageViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    val reservationsState by viewModel.reservationsState.observeAsState()
    val updateStatusState by viewModel.updateStatusState.observeAsState()
    val garagesState by garageViewModel.garagesState.observeAsState()

    var statusFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortAscending by remember { mutableStateOf(true) }

    // Get current garage info
    val currentGarage = remember(garagesState) {
        (garagesState as? Resource.Success<List<GarageResponse>>)?.data?.find { it.id == garageId }
    }

    val numberOfBays = currentGarage?.numberOfBays ?: 1


    // Get all reservations for this garage
    val allGarageReservations = remember(reservationsState, garageId) {
        when (val state = reservationsState) {
            is Resource.Success -> {
                (state.data ?: emptyList()).filter { it.getGarageId() == garageId }
            }
            else -> emptyList()
        }
    }

    // Filter reservations for this specific garage
    val filteredReservations = remember(reservationsState, statusFilter, garageId, searchQuery, sortAscending) {
        when (val state = reservationsState) {
            is Resource.Success -> {
                var filtered = (state.data ?: emptyList())
                    .filter { it.getGarageId() == garageId }
                    .filter { statusFilter == null || it.status == statusFilter }

                // Apply search filter
                if (searchQuery.isNotBlank()) {
                    filtered = filtered.filter { reservation ->
                        val userName = reservation.getUserName().orEmpty()
                        val userEmail = reservation.getUserEmail().orEmpty()
                        val services = reservation.services?.joinToString(" ") ?: ""

                        userName.contains(searchQuery, ignoreCase = true) ||
                                userEmail.contains(searchQuery, ignoreCase = true) ||
                                services.contains(searchQuery, ignoreCase = true)
                    }
                }

                // Apply sorting
                if (sortAscending) {
                    filtered.sortedBy { it.date }
                } else {
                    filtered.sortedByDescending { it.date }
                }
            }
            else -> emptyList()
        }
    }

    // Load this garage's reservations initially
    LaunchedEffect(garageId) {
        viewModel.getReservations()
        garageViewModel.getGarages()
    }

    // When updateStatusState changes, reload reservation list
    LaunchedEffect(updateStatusState) {
        if (updateStatusState is Resource.Success) {
            viewModel.getReservations()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Réservations du garage",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { sortAscending = !sortAscending }) {
                        Icon(
                            imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = if (sortAscending) "Tri chronologique" else "Tri anti-chronologique",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Garage Info Banner with Number of Bays
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DeepPurple.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentGarage?.nom ?: "Chargement...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Créneaux de réparation",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DeepPurple
                    ) {
                        Text(
                            text = "$numberOfBays",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Rechercher un client ou service...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, "Rechercher", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Effacer", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            // Status Filter Chips
            Text(
                "Filtrer par statut:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("Tous", "en_attente", "confirmé", "annulé")) { filter ->
                    val count = when (filter) {
                        "en_attente" -> filteredReservations.count { it.status == "en_attente" }
                        "confirmé" -> filteredReservations.count { it.status == "confirmé" }
                        "annulé" -> filteredReservations.count { it.status == "annulé" }
                        else -> filteredReservations.size
                    }

                    FilterChip(
                        selected = (filter == "Tous" && statusFilter == null) || statusFilter == filter,
                        onClick = {
                            statusFilter = when (filter) {
                                "Tous" -> null
                                else -> filter
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    when (filter) {
                                        "en_attente" -> "En attente"
                                        "confirmé" -> "Confirmé"
                                        "annulé" -> "Annulé"
                                        else -> filter
                                    }
                                )
                                if (filter != "Tous" && count > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        color = DeepPurple,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
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

            // Update Status State Indicator
            when (updateStatusState) {
                is Resource.Loading -> {
                    LinearProgressIndicator(
                        color = DeepPurple,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is Resource.Error -> {
                    Text(
                        text = (updateStatusState as Resource.Error).message ?: "Erreur lors de la mise à jour",
                        color = AlertRed,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                else -> {}
            }

            // Main Content
            when (val state = reservationsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = DeepPurple)
                            Text(
                                "Chargement des réservations...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    ErrorGarageReservationsState(
                        message = state.message ?: "Erreur de chargement",
                        onRetry = { viewModel.getReservations() }
                    )
                }
                is Resource.Success -> {
                    if (filteredReservations.isEmpty()) {
                        EmptyGarageReservationsState(
                            hasFilter = statusFilter != null || searchQuery.isNotBlank()
                        )
                    } else {
                        // Statistics Cards
                        if (statusFilter == null) {
                            val pendingCount = filteredReservations.count { it.status == "en_attente" }
                            val confirmedCount = filteredReservations.count { it.status == "confirmé" }
                            val cancelledCount = filteredReservations.count { it.status == "annulé" }

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    StatCard(
                                        title = "Total",
                                        count = filteredReservations.size,
                                        color = DeepPurple
                                    )
                                }
                                item {
                                    StatCard(
                                        title = "En attente",
                                        count = pendingCount,
                                        color = AccentYellow
                                    )
                                }
                                item {
                                    StatCard(
                                        title = "Confirmées",
                                        count = confirmedCount,
                                        color = StatusGood
                                    )
                                }
                                item {
                                    StatCard(
                                        title = "Annulées",
                                        count = cancelledCount,
                                        color = AlertRed
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredReservations, key = { it.id }) { reservation ->
                                ModernGarageReservationCard(
                                    reservation = reservation,
                                    allReservations = allGarageReservations,
                                    numberOfBays = numberOfBays,
                                    onStatusUpdate = { newStatus ->
                                        viewModel.updateReservationStatus(reservation.id, newStatus)
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ModernGarageReservationCard(
    reservation: ReservationResponse,
    allReservations: List<ReservationResponse>,
    numberOfBays: Int,
    onStatusUpdate: (String) -> Unit
) {
    // Calculate how many confirmed reservations exist for the same date and time slot
    val conflictingReservations = remember(allReservations, reservation) {
        allReservations.filter { otherReservation ->
            otherReservation.id != reservation.id &&
            otherReservation.status == "confirmé" &&
            isSameDateAndTime(reservation, otherReservation)
        }
    }

    val occupiedBays = conflictingReservations.size
    val availableBays = numberOfBays - occupiedBays
    val canConfirm = availableBays > 0


    // Show dialog state
    var showConfirmDialog by remember { mutableStateOf(false) }

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
            // Header with Client info and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reservation.getUserName() ?: "Client",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    reservation.getUserEmail()?.let { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (reservation.status) {
                        "confirmé" -> StatusGood.copy(alpha = 0.2f)
                        "annulé" -> AlertRed.copy(alpha = 0.2f)
                        else -> AccentYellow.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = reservation.status.replace("_", " ").replaceFirstChar { it.uppercase() },
                        color = when (reservation.status) {
                            "confirmé" -> StatusGood
                            "annulé" -> AlertRed
                            else -> AccentYellow
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = LightGrey)

            // Date and Time Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = formatReservationDate(reservation.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Heure",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "${reservation.heureDebut} - ${reservation.heureFin}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bay Availability Warning/Info (only for pending reservations)
            if (reservation.status == "en_attente") {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        !canConfirm -> AlertRed.copy(alpha = 0.15f)
                        availableBays <= 1 -> AccentYellow.copy(alpha = 0.15f)
                        else -> StatusGood.copy(alpha = 0.15f)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = when {
                                    !canConfirm -> Icons.Default.Warning
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = when {
                                    !canConfirm -> AlertRed
                                    availableBays <= 1 -> AccentYellow
                                    else -> StatusGood
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (canConfirm) {
                                        "Créneaux disponibles: $availableBays / $numberOfBays"
                                    } else {
                                        "Aucun créneau disponible"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        !canConfirm -> AlertRed
                                        availableBays <= 1 -> AccentYellow
                                        else -> StatusGood
                                    }
                                )
                                if (occupiedBays > 0) {
                                    Text(
                                        text = "$occupiedBays réservation(s) déjà confirmée(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Services Section
            if (!reservation.services.isNullOrEmpty()) {
                Column {
                    Text(
                        text = "Services",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(reservation.services) { service ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DeepPurple.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = service,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeepPurple,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Commentaires
            reservation.commentaires?.let { comment ->
                if (comment.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightPurple.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "💬 $comment",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // Action buttons for pending reservations only
            if (reservation.status == "en_attente") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onStatusUpdate("annulé") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Refuser")
                    }
                    Button(
                        onClick = {
                            if (canConfirm) {
                                showConfirmDialog = true
                            }
                        },
                        enabled = canConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusGood,
                            disabledContainerColor = MediumGrey
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (canConfirm) "Confirmer" else "Complet")
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    "Confirmer la réservation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Voulez-vous confirmer cette réservation ?")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        "Client: ${reservation.getUserName() ?: "Non spécifié"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Date: ${formatReservationDate(reservation.date)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Heure: ${reservation.heureDebut} - ${reservation.heureFin}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (availableBays <= 2) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentYellow.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AccentYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Il restera ${availableBays - 1} créneau(x) disponible(s) après confirmation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStatusUpdate("confirmé")
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGood)
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Annuler", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// Helper function to check if two reservations have the same date and overlapping time
private fun isSameDateAndTime(reservation1: ReservationResponse, reservation2: ReservationResponse): Boolean {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date1 = dateFormat.format(reservation1.date)
    val date2 = dateFormat.format(reservation2.date)

    if (date1 != date2) return false

    // Check if time slots overlap
    val start1 = reservation1.heureDebut
    val end1 = reservation1.heureFin
    val start2 = reservation2.heureDebut
    val end2 = reservation2.heureFin

    // Times overlap if: start1 < end2 AND start2 < end1
    return start1 < end2 && start2 < end1
}

@Composable
fun StatCard(title: String, count: Int, color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptyGarageReservationsState(hasFilter: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Aucune réservation",
                modifier = Modifier.size(64.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
            Text(
                "Aucune réservation trouvée",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                if (hasFilter) {
                    "Aucune réservation ne correspond à vos critères de recherche"
                } else {
                    "Aucune réservation pour ce garage pour le moment"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorGarageReservationsState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Erreur",
                modifier = Modifier.size(64.dp),
                tint = AlertRed
            )
            Text(
                "Erreur de chargement",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = DeepPurple)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Réessayer")
            }
        }
    }
}

// Helper function for date formatting
private fun formatReservationDate(date: Date): String {
    val formatter = SimpleDateFormat("EEE d MMM yyyy", Locale.FRENCH)
    return formatter.format(date)
}
