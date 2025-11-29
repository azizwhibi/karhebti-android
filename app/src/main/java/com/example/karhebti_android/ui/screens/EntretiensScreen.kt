package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.MaintenanceResponse
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.*
import com.example.karhebti_android.viewmodel.MaintenanceViewModel
import com.example.karhebti_android.viewmodel.CarViewModel
import com.example.karhebti_android.viewmodel.GarageViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
//import androidx.compose.material3.HorizontalDivider

// Backend-Integrated EntretiensScreen
// All maintenance data from API, Create/Delete operations call backend
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntretiensScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val maintenanceViewModel: MaintenanceViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val carViewModel: CarViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val garageViewModel: GarageViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    // Observe states
    val maintenancesState by maintenanceViewModel.maintenancesState.observeAsState()
    val createMaintenanceState by maintenanceViewModel.createMaintenanceState.observeAsState()
    val carsState by carViewModel.carsState.observeAsState()
    val garagesState by garageViewModel.garagesState.observeAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("À venir", "Historique")
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<MaintenanceResponse?>(null) }

    // Load data on screen start
    LaunchedEffect(Unit) {
        maintenanceViewModel.getMaintenances()
        carViewModel.getMyCars()
        garageViewModel.getGarages()
    }

    // Handle create result
    LaunchedEffect(createMaintenanceState) {
        when (createMaintenanceState) {
            is Resource.Success -> {
                showAddDialog = false
            }
            else -> {}
        }
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
                actions = {
                    IconButton(onClick = { maintenanceViewModel.getMaintenances() }) {
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
                Icon(Icons.Default.Add, "Ajouter entretien")
            }
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
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = maintenancesState) {
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
                                Text("Chargement des entretiens...", color = TextSecondary)
                            }
                        }
                    }
                    is Resource.Success -> {
                        val allMaintenances = state.data ?: emptyList()
                        val now = Date()

                        // Filter based on tab
                        val displayedMaintenances = if (selectedTab == 0) {
                            allMaintenances.filter { it.date.after(now) || it.date == now }
                        } else {
                            allMaintenances.filter { it.date.before(now) }
                        }

                        if (displayedMaintenances.isEmpty()) {
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
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = TextSecondary.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        if (selectedTab == 0) "Aucun entretien à venir" else "Aucun historique",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary
                                    )
                                    Text(
                                        if (selectedTab == 0) "Planifiez votre prochain entretien" else "Vos entretiens passés apparaîtront ici",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(displayedMaintenances, key = { it.id }) { maintenance ->
                                    MaintenanceCardBackendIntegrated(
                                        maintenance = maintenance,
                                        onDelete = { showDeleteDialog = maintenance }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
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
                                    onClick = { maintenanceViewModel.getMaintenances() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepPurple)
                                ) {
                                    Icon(Icons.Default.Refresh, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Réessayer")
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    // Add maintenance dialog
    if (showAddDialog) {
        AddMaintenanceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { type, date, cout, garageId, voitureId ->
                maintenanceViewModel.createMaintenance(type, date, cout, garageId, voitureId)
            },
            createState = createMaintenanceState,
            carsState = carsState,
            garagesState = garagesState
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { maintenance ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Supprimer l'entretien ?") },
            text = { Text("Voulez-vous vraiment supprimer cet entretien ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        maintenanceViewModel.deleteMaintenance(maintenance.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AlertRed)
                ) {
                    Text("Supprimer")
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
fun MaintenanceCardBackendIntegrated(
    maintenance: MaintenanceResponse,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    // Determine urgency based on date
    val daysUntil = ((maintenance.date.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
    val urgencyColor = when {
        daysUntil < 0 -> TextSecondary // Past
        daysUntil <= 7 -> AlertRed // Urgent
        daysUntil <= 30 -> AccentYellow // Attention
        else -> AccentGreen // Normal
    }

    val urgencyLabel = when {
        daysUntil < 0 -> "Terminé"
        daysUntil == 0 -> "Aujourd'hui"
        daysUntil <= 7 -> "Urgent"
        daysUntil <= 30 -> "Bientôt"
        else -> "Prévu"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, urgencyColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = maintenance.type,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    maintenance.voiture?.let { car ->
                        Text(
                            text = "${car.marque} ${car.modele}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = urgencyColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = urgencyLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = urgencyColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
            }

            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = Color.LightGray.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary
                    )
                    Text(
                        text = dateFormat.format(maintenance.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "${maintenance.cout} DH",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepPurple
                )
            }

            maintenance.garage?.let { garage ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary
                    )
                    Text(
                        text = garage.nom,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, String, String) -> Unit,
    createState: Resource<MaintenanceResponse>?,
    carsState: Resource<*>?,
    garagesState: Resource<*>?
) {
    var type by remember { mutableStateOf("vidange") }
    var date by remember { mutableStateOf("") }
    var cout by remember { mutableStateOf("") }
    var selectedCarId by remember { mutableStateOf("") }
    var selectedGarageId by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf(false) }
    var expandedCar by remember { mutableStateOf(false) }
    var expandedGarage by remember { mutableStateOf(false) }

    val types = listOf("vidange", "révision", "réparation", "pneus", "freins")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel entretien") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        types.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    type = item
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    placeholder = { Text("2024-12-31") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cout,
                    onValueChange = { cout = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Coût (DH)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Car selection (simplified - you'd show actual car data)
                OutlinedTextField(
                    value = selectedCarId,
                    onValueChange = { selectedCarId = it },
                    label = { Text("ID Voiture") },
                    placeholder = { Text("Entrez l'ID de la voiture") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Garage selection (simplified)
                OutlinedTextField(
                    value = selectedGarageId,
                    onValueChange = { selectedGarageId = it },
                    label = { Text("ID Garage") },
                    placeholder = { Text("Entrez l'ID du garage") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                    val coutValue = cout.toDoubleOrNull()
                    if (type.isNotBlank() && date.isNotBlank() && coutValue != null &&
                        selectedCarId.isNotBlank() && selectedGarageId.isNotBlank()) {
                        onAdd(type, date, coutValue, selectedGarageId, selectedCarId)
                    }
                },
                enabled = createState !is Resource.Loading &&
                         type.isNotBlank() && date.isNotBlank() &&
                         cout.toDoubleOrNull() != null &&
                         selectedCarId.isNotBlank() && selectedGarageId.isNotBlank()
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
fun EntretiensScreenPreview() {
    KarhebtiandroidTheme {
        EntretiensScreen()
    }
}
