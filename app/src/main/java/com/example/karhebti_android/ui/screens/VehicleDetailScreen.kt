package com.example.karhebti_android.ui.screens

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.karhebti_android.data.api.CarResponse
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.*
import com.example.karhebti_android.viewmodel.CarViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: String,
    onBackClick: () -> Unit,
    onEditClick: (CarResponse) -> Unit = {}
) {
    val context = LocalContext.current
    val carViewModel: CarViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    val carsState by carViewModel.carsState.observeAsState()
    val deleteCarState by carViewModel.deleteCarState.observeAsState()
    val updateCarState by carViewModel.updateCarState.observeAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Load cars if not already loaded
    LaunchedEffect(Unit) {
        if (carsState == null) {
            carViewModel.getMyCars()
        }
    }

    // Handle delete result - Navigate back IMMEDIATELY on success
    LaunchedEffect(deleteCarState) {
        if (deleteCarState is Resource.Success) {
            // Navigate back immediately when delete succeeds
            onBackClick()
        }
    }

    // Handle update result
    LaunchedEffect(updateCarState) {
        when (updateCarState) {
            is Resource.Success -> {
                showEditDialog = false
                carViewModel.getMyCars() // Refresh the car data
            }
            else -> {}
        }
    }

    // Find the specific car
    val car = remember(carsState, vehicleId) {
        (carsState as? Resource.Success)?.data?.find { it.id == vehicleId }
    }

    // If currently deleting, show loading overlay instead of normal UI
    if (deleteCarState is Resource.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = AlertRed)
                Text(
                    "Suppression en cours...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
        return
    }

    // If delete was successful, don't render anything (navigation is happening)
    if (deleteCarState is Resource.Success) {
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du véhicule") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    if (car != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, "Modifier", tint = Color.White)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Supprimer", tint = AlertRed)
                        }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
        ) {
            when {
                carsState is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DeepPurple)
                    }
                }
                car == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = AlertRed
                            )
                            Text("Véhicule non trouvé", style = MaterialTheme.typography.titleLarge)
                            Button(onClick = onBackClick) {
                                Text("Retour")
                            }
                        }
                    }
                }
                else -> {
                    VehicleDetailContent(car = car)
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && car != null) {
        AlertDialog(
            onDismissRequest = {
                if (deleteCarState !is Resource.Loading) {
                    showDeleteDialog = false
                }
            },
            title = { Text("Supprimer le véhicule ?") },
            text = {
                Text("Êtes-vous sûr de vouloir supprimer ${car.marque} ${car.modele} ? Cette action est irréversible.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        carViewModel.deleteCar(car.id)
                        showDeleteDialog = false
                    },
                    enabled = deleteCarState !is Resource.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    if (deleteCarState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Oui, supprimer", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = deleteCarState !is Resource.Loading
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    // Edit vehicle dialog
    if (showEditDialog && car != null) {
        EditVehicleDialogInDetail(
            car = car,
            onDismiss = { showEditDialog = false },
            carViewModel = carViewModel,
            updateCarState = updateCarState
        )
    }
}

@Composable
fun VehicleDetailContent(car: CarResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vehicle Image
        if (car.imageUrl != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(car.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Image du véhicule",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary.copy(alpha = 0.5f)
                        )
                        Text(
                            "Aucune image",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Vehicle Title Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepPurple),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${car.marque} ${car.modele}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Année ${car.annee}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Status Card (if available)
        car.statut?.let { status ->
            StatusCard(status = status)
        }

        // Basic Information
        DetailSection(title = "Informations de base") {
            DetailRow(
                icon = Icons.Default.DriveEta,
                label = "Marque",
                value = car.marque,
                color = DeepPurple
            )
            DetailRow(
                icon = Icons.Default.DirectionsCar,
                label = "Modèle",
                value = car.modele,
                color = DeepPurple
            )
            DetailRow(
                icon = Icons.Default.CalendarToday,
                label = "Année",
                value = car.annee.toString(),
                color = AccentBlue
            )
            DetailRow(
                icon = Icons.Default.CreditCard,
                label = "Immatriculation",
                value = car.immatriculation,
                color = AccentOrange
            )
            DetailRow(
                icon = Icons.Default.LocalGasStation,
                label = "Type de carburant",
                value = car.typeCarburant,
                color = AccentGreen
            )
        }

        // Mileage and Maintenance
        DetailSection(title = "Entretien") {
            DetailRow(
                icon = Icons.Default.Speed,
                label = "Kilométrage",
                value = if (car.kilometrage != null) "${car.kilometrage} km" else "Non défini",
                color = AccentBlue
            )

            car.prochainEntretien?.let { date ->
                DetailRow(
                    icon = Icons.Default.Build,
                    label = "Prochain entretien",
                    value = date,
                    color = AccentOrange
                )
            }

            car.joursProchainEntretien?.let { days ->
                DetailRow(
                    icon = Icons.Default.Schedule,
                    label = "Jours restants",
                    value = "$days jours",
                    color = when {
                        days <= 7 -> AlertRed
                        days <= 30 -> AccentOrange
                        else -> AccentGreen
                    }
                )
            }
        }
    }
}

@Composable
fun StatusCard(status: String) {
    val (statusText, statusColor, statusIcon) = when (status.uppercase()) {
        "BON" -> Triple("Bon état", AccentGreen, Icons.Default.CheckCircle)
        "ATTENTION" -> Triple("Attention nécessaire", AccentOrange, Icons.Default.Warning)
        "URGENT" -> Triple("Entretien urgent", AlertRed, Icons.Default.Error)
        else -> Triple(status, TextSecondary, Icons.Default.Info)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, statusColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Statut du véhicule",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPurple
            )
            HorizontalDivider()
            content()
        }
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVehicleDialogInDetail(
    car: CarResponse,
    onDismiss: () -> Unit,
    carViewModel: CarViewModel,
    updateCarState: Resource<CarResponse>?
) {
    var marque by remember { mutableStateOf(car.marque) }
    var modele by remember { mutableStateOf(car.modele) }
    var annee by remember { mutableStateOf(car.annee.toString()) }
    var immatriculation by remember { mutableStateOf(car.immatriculation) }
    var typeCarburant by remember { mutableStateOf(car.typeCarburant) }
    var expanded by remember { mutableStateOf(false) }

    val carburants = listOf("Essence", "Diesel", "Électrique", "Hybride", "GPL")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le véhicule") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = marque,
                    onValueChange = { marque = it },
                    label = { Text("Marque") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = modele,
                    onValueChange = { modele = it },
                    label = { Text("Modèle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = annee,
                    onValueChange = { if (it.length <= 4) annee = it.filter { c -> c.isDigit() } },
                    label = { Text("Année") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = immatriculation,
                    onValueChange = { },
                    label = { Text("Immatriculation") },
                    singleLine = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = typeCarburant,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type de carburant") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        carburants.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    typeCarburant = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                when (updateCarState) {
                    is Resource.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    is Resource.Error -> {
                        Text(
                            text = updateCarState.message ?: "Erreur",
                            color = AlertRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = annee.toIntOrNull()
                    if (marque.isNotBlank() && modele.isNotBlank() && year != null && typeCarburant.isNotBlank()) {
                        carViewModel.updateCar(
                            id = car.id,
                            marque = marque,
                            modele = modele,
                            annee = year,
                            typeCarburant = typeCarburant
                        )
                    }
                },
                enabled = updateCarState !is Resource.Loading &&
                         marque.isNotBlank() && modele.isNotBlank() &&
                         annee.toIntOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = DeepPurple)
            ) {
                if (updateCarState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sauvegarder")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = updateCarState !is Resource.Loading
            ) {
                Text("Annuler")
            }
        }
    )
}
