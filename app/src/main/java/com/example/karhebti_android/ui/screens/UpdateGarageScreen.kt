package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.GarageResponse
import com.example.karhebti_android.data.api.OsmLocationSuggestion
import com.example.karhebti_android.data.api.ServiceResponse
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.DeepPurple
import com.example.karhebti_android.viewmodel.GarageViewModel
import com.example.karhebti_android.viewmodel.OsmViewModel
import com.example.karhebti_android.viewmodel.ServiceViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

data class ServiceFormState(
    val id: String? = null,
    val type: String,
    var coutMoyen: String = "",
    var dureeEstimee: String = "",
    val isExisting: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateGarageScreen(
    garageId: String,
    onBackClick: () -> Unit = {},
    onGarageUpdated: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val garageViewModel: GarageViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val serviceViewModel: ServiceViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val osmViewModel: OsmViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    // ✅ NOUVEAU: ViewModel pour les réservations
    val reservationViewModel: com.example.karhebti_android.viewmodel.ReservationViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    // ✅ NOUVEAU: ViewModel pour les repair bays
    val repairBayViewModel: com.example.karhebti_android.viewmodel.RepairBayViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    val garagesState by garageViewModel.garagesState.observeAsState()
    val updateGarageState by garageViewModel.updateGarageState.observeAsState()
    val servicesState by serviceViewModel.servicesState.observeAsState()
    val updateServiceState by serviceViewModel.updateServiceState.observeAsState()
    val createServiceState by serviceViewModel.createServiceState.observeAsState()
    val deleteServiceState by serviceViewModel.deleteServiceState.observeAsState()
    val searchResults by osmViewModel.searchResults.observeAsState()
    val selectedLocation by osmViewModel.selectedLocation.observeAsState()
    // ✅ NOUVEAU: États pour réservations et repair bays
    val reservationsState by reservationViewModel.reservationsState.observeAsState()
    val repairBaysState by repairBayViewModel.repairBaysState.observeAsState()

    var nom by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var noteUtilisateur by remember { mutableStateOf("0.0") }
    var heureOuverture by remember { mutableStateOf("08:00") }
    var heureFermeture by remember { mutableStateOf("18:00") }

    // ✅ NOUVEAU: Nombre de créneaux de réparation
    var numberOfBays by remember { mutableStateOf(1) }
    var originalNumberOfBays by remember { mutableStateOf(1) }

    // ✅ NOUVEAU: État pour les erreurs de validation des baies
    var bayReductionError by remember { mutableStateOf<String?>(null) }

    // ✅ NOUVEAU: États pour la sélection des baies à supprimer
    var showBaySelectionDialog by remember { mutableStateOf(false) }
    var selectedBaysToDelete by remember { mutableStateOf<Set<String>>(emptySet()) }
    var targetNumberOfBays by remember { mutableStateOf(1) }

    // Coordonnées GPS
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // Map and search states
    var showMapDialog by remember { mutableStateOf(false) }
    var showAddressSuggestions by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var selectedServices by remember { mutableStateOf(listOf<ServiceFormState>()) }

    val allServiceTypes = listOf(
        "vidange", "contrôle technique", "réparation pneu", "changement pneu",
        "freinage", "batterie", "climatisation", "échappement",
        "révision complète", "diagnostic électronique", "carrosserie", "peinture", "pare-brise",
        "suspension", "embrayage", "transmission", "injection", "refroidissement", "démarrage",
        "lavage auto", "équilibrage roues", "parallélisme", "système électrique",
        "filtre à air", "filtre à huile", "plaquettes de frein"
    )
    var expanded by remember { mutableStateOf(false) }
    var showMinServicesError by remember { mutableStateOf(false) }

    // Refresh garages and services on screen load
    LaunchedEffect(garageId) {
        garageViewModel.getGarages()
        serviceViewModel.getServicesByGarage(garageId)
        reservationViewModel.getReservations() // ✅ Charger les réservations
        repairBayViewModel.getRepairBaysByGarage(garageId) // ✅ Charger les repair bays
    }

    // Populate form when data is loaded
    LaunchedEffect(garagesState, servicesState) {
        val garage = when (garagesState) {
            is Resource.Success -> {
                (garagesState as Resource.Success<List<GarageResponse>>).data?.find { it.id == garageId }
            }
            else -> null
        }
        val services = when (servicesState) {
            is Resource.Success -> {
                (servicesState as Resource.Success<List<ServiceResponse>>).data ?: emptyList()
            }
            else -> emptyList()
        }
        garage?.let {
            nom = it.nom
            adresse = it.adresse
            telephone = it.telephone
            noteUtilisateur = it.noteUtilisateur.toString()
            heureOuverture = it.heureOuverture ?: "08:00"
            heureFermeture = it.heureFermeture ?: "18:00"
            latitude = it.latitude
            longitude = it.longitude
            numberOfBays = it.numberOfBays ?: 1
            originalNumberOfBays = it.numberOfBays ?: 1
            isLoading = false
        }

        selectedServices = services.map { service ->
            ServiceFormState(
                id = service.id,
                type = service.type,
                coutMoyen = service.coutMoyen.toString(),
                dureeEstimee = service.dureeEstimee.toString(),
                isExisting = true
            )
        }

        if (garagesState is Resource.Error || servicesState is Resource.Error) {
            isLoading = false
        }
    }

    // Handle selected location from OSM search
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let { location ->
            adresse = location.displayName
            latitude = location.latitude
            longitude = location.longitude
            showAddressSuggestions = false
        }
    }

    // Refresh after successful update
    // ✅ Nouveau LaunchedEffect pour la navigation
    LaunchedEffect(updateGarageState) {
        if (updateGarageState is Resource.Success) {
            // Attendre un peu pour que les services soient mis à jour
            delay(500)
            onGarageUpdated() // ← Navigation après succès
        }
    }

    val canSubmit = nom.isNotBlank() &&
            adresse.isNotBlank() &&
            telephone.isNotBlank() &&
            heureOuverture.matches(Regex("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\$")) &&
            heureFermeture.matches(Regex("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]\$")) &&
            heureOuverture < heureFermeture &&
            selectedServices.isNotEmpty() &&
            selectedServices.all {
                it.coutMoyen.toDoubleOrNull() != null && it.dureeEstimee.toIntOrNull() != null
            }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier le garage") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DeepPurple)
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 28.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Informations du garage",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepPurple,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))

                        // Nom
                        OutlinedTextField(
                            value = nom,
                            onValueChange = { nom = it },
                            label = { Text("Nom du garage") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))

                        // Adresse avec recherche et carte
                        Column {
                            OutlinedTextField(
                                value = adresse,
                                onValueChange = { query ->
                                    adresse = query
                                    showAddressSuggestions = query.length >= 3

                                    searchJob?.cancel()

                                    if (query.length >= 3) {
                                        searchJob = coroutineScope.launch {
                                            delay(500)
                                            osmViewModel.searchAddress(query)
                                        }
                                    } else {
                                        osmViewModel.clearSearch()
                                    }
                                },
                                label = { Text("Adresse") },
                                placeholder = { Text("Rechercher ou choisir sur la carte...") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Localisation",
                                        tint = DeepPurple
                                    )
                                },
                                trailingIcon = {
                                    Row {
                                        IconButton(onClick = { showMapDialog = true }) {
                                            Icon(
                                                Icons.Default.Map,
                                                contentDescription = "Choisir sur la carte",
                                                tint = DeepPurple
                                            )
                                        }
                                        if (adresse.isNotEmpty()) {
                                            IconButton(onClick = {
                                                adresse = ""
                                                latitude = null
                                                longitude = null
                                                showAddressSuggestions = false
                                                osmViewModel.clearSearch()
                                                osmViewModel.clearSelection()
                                            }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            // Address suggestions
                            if (showAddressSuggestions && searchResults is Resource.Success) {
                                val suggestions = (searchResults as Resource.Success<List<OsmLocationSuggestion>>).data

                                if (!suggestions.isNullOrEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 250.dp)
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            suggestions.forEach { suggestion ->
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            osmViewModel.selectLocation(suggestion)
                                                        },
                                                    color = Color.Transparent
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.LocationOn,
                                                            contentDescription = null,
                                                            tint = DeepPurple.copy(alpha = 0.6f),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(Modifier.width(12.dp))
                                                        Text(
                                                            text = suggestion.displayName,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color.Black
                                                        )
                                                    }
                                                }
                                                if (suggestion != suggestions.last()) {
                                                    HorizontalDivider()
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (searchResults is Resource.Loading) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    color = DeepPurple
                                )
                            }

                            // Show coordinates
                            if (latitude != null && longitude != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = DeepPurple.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = DeepPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Position: ${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DeepPurple,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        // Téléphone
                        OutlinedTextField(
                            value = telephone,
                            onValueChange = { telephone = it },
                            label = { Text("Téléphone") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))

                        // Heures
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = heureOuverture,
                                onValueChange = { heureOuverture = it },
                                label = { Text("Ouverture") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = heureFermeture,
                                onValueChange = { heureFermeture = it },
                                label = { Text("Fermeture") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        // Note
                        OutlinedTextField(
                            value = noteUtilisateur,
                            onValueChange = { noteUtilisateur = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Note (0 à 5)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        Spacer(Modifier.height(24.dp))

                        // ✅ NOUVEAU: Section Nombre de baies de réparation
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = DeepPurple.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Build,
                                        contentDescription = null,
                                        tint = DeepPurple,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Nombre d'emplacements simultanés",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Decrease button
                                    IconButton(
                                        onClick = {
                                            if (numberOfBays > 1) {
                                                targetNumberOfBays = numberOfBays - 1
                                                showBaySelectionDialog = true
                                                bayReductionError = null
                                            }
                                        },
                                        enabled = numberOfBays > 1
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Diminuer",
                                            tint = if (numberOfBays > 1) DeepPurple else Color.Gray
                                        )
                                    }

                                    // Number display
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = DeepPurple.copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = numberOfBays.toString(),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepPurple,
                                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                                        )
                                    }

                                    // Increase button
                                    IconButton(
                                        onClick = {
                                            if (numberOfBays < 10) {
                                                numberOfBays++
                                                bayReductionError = null
                                            }
                                        },
                                        enabled = numberOfBays < 10
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Augmenter",
                                            tint = if (numberOfBays < 10) DeepPurple else Color.Gray
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Maximum: 10 créneaux",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                // ✅ Afficher l'erreur de réduction de baies
                                if (bayReductionError != null) {
                                    Spacer(Modifier.height(12.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = bayReductionError!!,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Services section
                        Text(
                            "Services",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepPurple,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = if (selectedServices.isEmpty()) "Ajouter un service"
                                else "${selectedServices.size} service(s)",
                                onValueChange = {},
                                label = { Text("Services") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                allServiceTypes.filter { type ->
                                    selectedServices.none { it.type == type }
                                }.forEach { serviceType ->
                                    DropdownMenuItem(
                                        text = { Text(serviceType) },
                                        onClick = {
                                            selectedServices = selectedServices + ServiceFormState(type = serviceType)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        // Services list
                        selectedServices.forEach { serviceForm ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (serviceForm.isExisting)
                                        DeepPurple.copy(alpha = 0.08f)
                                    else Color.Green.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(serviceForm.type, color = DeepPurple, fontWeight = FontWeight.Bold)
                                        Row {
                                            Text(
                                                if (serviceForm.isExisting) "Existant" else "Nouveau",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (serviceForm.isExisting) DeepPurple else Color.Green,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    if (selectedServices.size > 1) {
                                                        if (serviceForm.isExisting && serviceForm.id != null) {
                                                            serviceViewModel.deleteService(serviceForm.id, garageId)
                                                        }
                                                        selectedServices = selectedServices.filter { it.type != serviceForm.type }
                                                        showMinServicesError = false
                                                    } else {
                                                        showMinServicesError = true
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Supprimer",
                                                    tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = serviceForm.coutMoyen,
                                        onValueChange = { value ->
                                            selectedServices = selectedServices.map {
                                                if (it.type == serviceForm.type)
                                                    it.copy(coutMoyen = value.filter { c -> c.isDigit() || c == '.' })
                                                else it
                                            }
                                        },
                                        label = { Text("Coût moyen (DH)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = serviceForm.dureeEstimee,
                                        onValueChange = { value ->
                                            selectedServices = selectedServices.map {
                                                if (it.type == serviceForm.type)
                                                    it.copy(dureeEstimee = value.filter { c -> c.isDigit() })
                                                else it
                                            }
                                        },
                                        label = { Text("Durée (min)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                        }

                        if (showMinServicesError) {
                            Text(
                                "Chaque garage doit contenir au moins un service.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(6.dp))
                        }

                        Spacer(Modifier.height(16.dp))

                        // Error/Success messages
                        val errorMessage = when {
                            updateGarageState is Resource.Error -> (updateGarageState as? Resource.Error)?.message
                            updateServiceState is Resource.Error -> (updateServiceState as? Resource.Error)?.message
                            createServiceState is Resource.Error -> (createServiceState as? Resource.Error)?.message
                            deleteServiceState is Resource.Error -> (deleteServiceState as? Resource.Error)?.message
                            else -> null
                        }

                        if (errorMessage != null) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(6.dp))
                        }

                        if (updateGarageState is Resource.Success) {
                            Text("Modifications enregistrées!", color = Color.Green)
                            Spacer(Modifier.height(6.dp))
                        }

                        // Submit button
                        Button(
                            onClick = {
                                val note = noteUtilisateur.toDoubleOrNull() ?: 0.0

                                // ✅ NOUVEAU: Gérer la création/suppression des baies de réparation
                                coroutineScope.launch {
                                    // Mettre à jour le garage
                                    garageViewModel.updateGarage(
                                        garageId = garageId,
                                        nom = nom,
                                        adresse = adresse,
                                        telephone = telephone,
                                        noteUtilisateur = note,
                                        heureOuverture = heureOuverture,
                                        heureFermeture = heureFermeture,
                                        latitude = latitude,
                                        longitude = longitude,
                                        numberOfBays = numberOfBays
                                    )

                                    // ✅ Supprimer les baies sélectionnées
                                    if (selectedBaysToDelete.isNotEmpty()) {
                                        selectedBaysToDelete.forEach { bayId ->
                                            repairBayViewModel.deleteRepairBay(bayId)
                                        }
                                        selectedBaysToDelete = emptySet()
                                    }

                                    // ✅ Créer les nouvelles baies si nécessaire
                                    if (numberOfBays > originalNumberOfBays) {
                                        // Créer les baies manquantes
                                        for (bayNumber in (originalNumberOfBays + 1)..numberOfBays) {
                                            repairBayViewModel.createRepairBay(
                                                garageId = garageId,
                                                bayNumber = bayNumber,
                                                name = "Créneau $bayNumber",
                                                heureOuverture = heureOuverture,
                                                heureFermeture = heureFermeture,
                                                isActive = true
                                            )
                                        }
                                    }

                                    // Mettre à jour les services
                                    selectedServices.forEach { serviceForm ->
                                        val cout = serviceForm.coutMoyen.toDoubleOrNull()
                                        val duree = serviceForm.dureeEstimee.toIntOrNull()
                                        if (cout != null && duree != null) {
                                            if (serviceForm.isExisting && serviceForm.id != null) {
                                                serviceViewModel.updateService(
                                                    serviceId = serviceForm.id,
                                                    type = serviceForm.type,
                                                    garageId = garageId,
                                                    coutMoyen = cout,
                                                    dureeEstimee = duree
                                                )
                                            } else {
                                                serviceViewModel.createService(
                                                    type = serviceForm.type,
                                                    coutMoyen = cout,
                                                    dureeEstimee = duree,
                                                    garageId = garageId
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPurple),
                            enabled = (updateGarageState !is Resource.Loading) && canSubmit
                        ) {
                            if (updateGarageState is Resource.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Enregistrer", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Annuler")
                        }
                    }
                }
            }
        }
    }

    // ✅ NOUVEAU: Dialogue de sélection des baies à supprimer
    if (showBaySelectionDialog) {
        BaySelectionDialog(
            repairBaysState = repairBaysState,
            reservationsState = reservationsState,
            currentNumberOfBays = numberOfBays,
            targetNumberOfBays = targetNumberOfBays,
            selectedBaysToDelete = selectedBaysToDelete,
            onBaySelectionChange = { bayId, isSelected ->
                selectedBaysToDelete = if (isSelected) {
                    selectedBaysToDelete + bayId
                } else {
                    selectedBaysToDelete - bayId
                }
            },
            onConfirm = {
                if (selectedBaysToDelete.size == (numberOfBays - targetNumberOfBays)) {
                    numberOfBays = targetNumberOfBays
                    showBaySelectionDialog = false
                    bayReductionError = null
                } else {
                    bayReductionError = "Veuillez sélectionner exactement ${numberOfBays - targetNumberOfBays} baie(s) à supprimer"
                }
            },
            onDismiss = {
                showBaySelectionDialog = false
                selectedBaysToDelete = emptySet()
            }
        )
    }

    // Map Dialog
    if (showMapDialog) {
        MapPickerDialogUpdate(
            initialLat = latitude ?: 36.79952, // tunis default
            initialLon = longitude ?: 10.17849,
            onLocationSelected = { lat, lon ->
                latitude = lat
                longitude = lon
                showMapDialog = false

                coroutineScope.launch {
                    osmViewModel.reverseGeocode(lat, lon)
                    delay(1000)
                    val result = osmViewModel.reverseGeocodeResult.value
                    if (result is Resource.Success) {
                        adresse = result.data?.displayName ?: adresse
                    }
                }
            },
            onDismiss = { showMapDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerDialogUpdate(
    initialLat: Double,
    initialLon: Double,
    onLocationSelected: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLat by remember { mutableStateOf(initialLat) }
    var selectedLon by remember { mutableStateOf(initialLon) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Modifier la position",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Appuyez sur la carte pour déplacer le marqueur",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                AndroidView(
                    factory = { ctx ->
                        Configuration.getInstance().userAgentValue = ctx.packageName

                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(GeoPoint(initialLat, initialLon))

                            val newMarker = Marker(this).apply {
                                position = GeoPoint(initialLat, initialLon)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Position du garage"
                            }
                            overlays.add(newMarker)
                            marker = newMarker

                            overlays.add(object : org.osmdroid.views.overlay.Overlay() {
                                override fun onSingleTapConfirmed(
                                    e: android.view.MotionEvent,
                                    mapView: MapView
                                ): Boolean {
                                    val projection = mapView.projection
                                    val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint

                                    selectedLat = geoPoint.latitude
                                    selectedLon = geoPoint.longitude

                                    marker?.position = geoPoint
                                    mapView.invalidate()

                                    return true
                                }
                            })

                            mapView = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "📍 ${String.format(Locale.US, "%.5f", selectedLat)}, ${String.format(Locale.US, "%.5f", selectedLon)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DeepPurple,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLocationSelected(selectedLat, selectedLon) },
                colors = ButtonDefaults.buttonColors(containerColor = DeepPurple)
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

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }
}

// ✅ NOUVEAU: Dialogue de sélection des baies à supprimer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaySelectionDialog(
    repairBaysState: Resource<List<com.example.karhebti_android.data.api.RepairBayResponse>>?,
    reservationsState: Resource<List<com.example.karhebti_android.data.api.ReservationResponse>>?,
    currentNumberOfBays: Int,
    targetNumberOfBays: Int,
    selectedBaysToDelete: Set<String>,
    onBaySelectionChange: (String, Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val repairBays = (repairBaysState as? Resource.Success)?.data ?: emptyList()
    val reservations = (reservationsState as? Resource.Success)?.data ?: emptyList()
    val now = java.util.Date()

    val baysToDelete = currentNumberOfBays - targetNumberOfBays

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "Sélectionnez les baies à supprimer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Veuillez sélectionner $baysToDelete baie(s) à supprimer",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                repairBays.sortedBy { it.bayNumber }.forEach { bay ->
                    // Vérifier s'il y a des réservations actives pour cette baie
                    val activeReservations = reservations.filter { reservation ->
                        val bayId = reservation.getRepairBayId()
                        val reservationDate = reservation.date

                        bayId == bay.id &&
                        reservationDate.after(now) &&
                        reservation.status !in listOf("annulée", "terminée", "cancelled", "completed")
                    }

                    val hasActiveReservations = activeReservations.isNotEmpty()
                    val isSelected = selectedBaysToDelete.contains(bay.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                hasActiveReservations -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                isSelected -> DeepPurple.copy(alpha = 0.2f)
                                else -> Color.White
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) DeepPurple else Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !hasActiveReservations) {
                                    onBaySelectionChange(bay.id, !isSelected)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (!hasActiveReservations) {
                                        onBaySelectionChange(bay.id, checked)
                                    }
                                },
                                enabled = !hasActiveReservations,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DeepPurple,
                                    uncheckedColor = if (hasActiveReservations) Color.Gray else DeepPurple
                                )
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Build,
                                        contentDescription = null,
                                        tint = if (hasActiveReservations) MaterialTheme.colorScheme.error else DeepPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = bay.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasActiveReservations) Color.Gray else Color.Black
                                    )
                                }

                                if (hasActiveReservations) {
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "${activeReservations.size} réservation(s) active(s)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "Impossible de supprimer cette baie",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Résumé de la sélection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedBaysToDelete.size == baysToDelete) {
                            Color.Green.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (selectedBaysToDelete.size == baysToDelete) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (selectedBaysToDelete.size == baysToDelete) Color.Green else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${selectedBaysToDelete.size} / $baysToDelete baie(s) sélectionnée(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedBaysToDelete.size == baysToDelete) Color.Green else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedBaysToDelete.size == baysToDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepPurple,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = DeepPurple)
            }
        }
    )
}

