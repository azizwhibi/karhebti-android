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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.CarResponse
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.*
import com.example.karhebti_android.viewmodel.CarViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory

// Backend-Integrated VehiclesScreen
// All data fetched from API, all actions call backend endpoints
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    onBackClick: () -> Unit = {},
    onVehicleClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val carViewModel: CarViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    // Observe cars state from backend
    val carsState by carViewModel.carsState.observeAsState()
    val createCarState by carViewModel.createCarState.observeAsState()
    val deleteCarState by carViewModel.deleteCarState.observeAsState()

    // UI State
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<CarResponse?>(null) }
    var refreshing by remember { mutableStateOf(false) }

    // Load cars on screen start
    LaunchedEffect(Unit) {
        carViewModel.getMyCars()
    }

    // Handle create car result
    LaunchedEffect(createCarState) {
        when (createCarState) {
            is Resource.Success -> {
                showAddDialog = false
            }
            is Resource.Error -> {
                // Error shown in dialog
            }
            else -> {}
        }
    }

    // Handle delete result
    LaunchedEffect(deleteCarState) {
        when (deleteCarState) {
            is Resource.Success -> {
                showDeleteDialog = null
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mes véhicules")
                        if (carsState is Resource.Success) {
                            Text(
                                text = "${(carsState as Resource.Success).data?.size ?: 0} véhicule(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    // Refresh button
                    IconButton(
                        onClick = {
                            refreshing = true
                            carViewModel.getMyCars()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, "Actualiser", tint = Color.White)
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
                onClick = { showAddDialog = true },
                containerColor = DeepPurple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Ajouter véhicule")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
        ) {
            when (val state = carsState) {
                is Resource.Loading -> {
                    // Loading indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = DeepPurple)
                            Text("Chargement des véhicules...", color = TextSecondary)
                        }
                    }
                }
                is Resource.Success -> {
                    val cars = state.data ?: emptyList()

                    if (refreshing) {
                        refreshing = false
                    }

                    if (cars.isEmpty()) {
                        // Empty state
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
                                    imageVector = Icons.Default.DriveEta,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = TextSecondary.copy(alpha = 0.5f)
                                )
                                Text(
                                    "Aucun véhicule",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    "Ajoutez votre premier véhicule pour commencer",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DeepPurple
                                    )
                                ) {
                                    Icon(Icons.Default.Add, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Ajouter un véhicule")
                                }
                            }
                        }
                    } else {
                        // Cars list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(cars, key = { it.id }) { car ->
                                VehicleCardBackendIntegrated(
                                    car = car,
                                    onClick = { onVehicleClick(car.id) },
                                    onDelete = { showDeleteDialog = car }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    // Error state
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
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = AlertRed
                            )
                            Text(
                                "Erreur de chargement",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                state.message ?: "Une erreur est survenue",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Button(
                                onClick = { carViewModel.getMyCars() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DeepPurple
                                )
                            ) {
                                Icon(Icons.Default.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Réessayer")
                            }
                        }
                    }
                }
                else -> {
                    // Initial state
                }
            }
        }
    }

    // Add vehicle dialog
    if (showAddDialog) {
        AddVehicleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { marque, modele, annee, immatriculation, typeCarburant ->
                carViewModel.createCar(marque, modele, annee, immatriculation, typeCarburant)
            },
            createState = createCarState
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { car ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Supprimer le véhicule ?") },
            text = { Text("Voulez-vous vraiment supprimer ${car.marque} ${car.modele} ?") },
            confirmButton = {
                TextButton(
                    onClick = { carViewModel.deleteCar(car.id) },
                    colors = ButtonDefaults.textButtonColors(contentColor = AlertRed)
                ) {
                    if (deleteCarState is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Supprimer")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun VehicleCardBackendIntegrated(
    car: CarResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                        text = "${car.marque} ${car.modele}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Année ${car.annee}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = TextSecondary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Voir détails") },
                            onClick = {
                                showMenu = false
                                onClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Info, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Supprimer", color = AlertRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = AlertRed) }
                        )
                    }
                }
            }

            HorizontalDivider()

            // Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.CreditCard,
                    label = car.immatriculation,
                    color = DeepPurple
                )
                InfoChip(
                    icon = Icons.Default.LocalGasStation,
                    label = car.typeCarburant,
                    color = AccentGreen
                )
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, String, String) -> Unit,
    createState: Resource<CarResponse>?
) {
    var marque by remember { mutableStateOf("") }
    var modele by remember { mutableStateOf("") }
    var annee by remember { mutableStateOf("") }
    var immatriculation by remember { mutableStateOf("") }
    var typeCarburant by remember { mutableStateOf("Essence") }
    var expanded by remember { mutableStateOf(false) }

    val carburants = listOf("Essence", "Diesel", "Électrique", "Hybride", "GPL")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un véhicule") },
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
                    onValueChange = { immatriculation = it.uppercase() },
                    label = { Text("Immatriculation") },
                    singleLine = true,
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

                when (createState) {
                    is Resource.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    is Resource.Error -> {
                        Text(
                            text = createState.message ?: "Erreur",
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
                    if (marque.isNotBlank() && modele.isNotBlank() && year != null &&
                        immatriculation.isNotBlank() && typeCarburant.isNotBlank()) {
                        onAdd(marque, modele, year, immatriculation, typeCarburant)
                    }
                },
                enabled = createState !is Resource.Loading &&
                         marque.isNotBlank() && modele.isNotBlank() &&
                         annee.toIntOrNull() != null && immatriculation.isNotBlank()
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = createState !is Resource.Loading
            ) {
                Text("Annuler")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun VehiclesScreenPreview() {
    KarhebtiandroidTheme {
        VehiclesScreen()
    }
}
