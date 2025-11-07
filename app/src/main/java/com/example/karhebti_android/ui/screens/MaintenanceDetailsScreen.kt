package com.example.karhebti_android.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.MaintenanceResponse
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.*
import com.example.karhebti_android.viewmodel.MaintenanceViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceDetailsScreen(
    maintenanceId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val maintenanceViewModel: MaintenanceViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val carViewModel: com.example.karhebti_android.viewmodel.CarViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val garageViewModel: com.example.karhebti_android.viewmodel.GarageViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    val maintenanceState by maintenanceViewModel.maintenanceState.observeAsState()
    val updateMaintenanceState by maintenanceViewModel.updateMaintenanceState.observeAsState()
    val carsState by carViewModel.carsState.observeAsState()
    val garagesState by garageViewModel.garagesState.observeAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var newDate by remember { mutableStateOf<Date?>(null) }

    LaunchedEffect(maintenanceId) {
        maintenanceViewModel.getMaintenanceById(maintenanceId)
        carViewModel.getMyCars()
        garageViewModel.getGarages()
    }

    // Fallback: if fetching by ID fails, try to find in the list
    LaunchedEffect(maintenanceState) {
        if (maintenanceState is Resource.Error) {
            // Try to get from the existing list as fallback
            android.util.Log.d("MaintenanceDetails", "Failed to fetch by ID, trying list fallback")
            maintenanceViewModel.getMaintenances()
        }
    }

    // Extract maintenance from list if individual fetch failed
    val maintenanceFromList = remember(maintenanceViewModel.maintenancesState.observeAsState().value) {
        val listState = maintenanceViewModel.maintenancesState.value
        if (listState is Resource.Success) {
            listState.data?.find { it.id == maintenanceId }
        } else null
    }

    LaunchedEffect(updateMaintenanceState) {
        if (updateMaintenanceState is Resource.Success) {
            // Refresh data after update
            maintenanceViewModel.getMaintenanceById(maintenanceId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de l'entretien") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    (maintenanceState as? Resource.Success)?.data?.let {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Edit, "Modifier la date", tint = Color.White)
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
            when (val state = maintenanceState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DeepPurple)
                    }
                }
                is Resource.Success -> {
                    val maintenance = state.data
                    if (maintenance != null) {
                        MaintenanceDetailsContent(
                            maintenance = maintenance,
                            cars = (carsState as? Resource.Success)?.data ?: emptyList(),
                            garages = (garagesState as? Resource.Success)?.data ?: emptyList()
                        )
                    } else {
                        Text("Détails non disponibles", modifier = Modifier.align(Alignment.Center))
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Error, null, tint = AlertRed, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Erreur de chargement",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.message ?: "Une erreur est survenue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                maintenanceViewModel.getMaintenanceById(maintenanceId)
                                carViewModel.getMyCars()
                                garageViewModel.getGarages()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPurple)
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Réessayer")
                        }
                    }
                }
                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DeepPurple)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        val maintenance = (maintenanceState as? Resource.Success)?.data
        maintenance?.date?.let { calendar.time = it }

        DatePickerDialog(
            LocalContext.current,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                newDate = calendar.time
                showDatePicker = false

                newDate?.let { date ->
                    val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
                    val dateString = isoDateFormat.format(date)
                    maintenance?.let {
                        maintenanceViewModel.updateMaintenanceDate(it.id, dateString)
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDatePicker = false }
            show()
        }
    }
}

@Composable
fun MaintenanceDetailsContent(
    maintenance: MaintenanceResponse,
    cars: List<com.example.karhebti_android.data.api.CarResponse> = emptyList(),
    garages: List<com.example.karhebti_android.data.api.GarageResponse> = emptyList()
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE)

    // Check if maintenance is not confirmed - use yellow border
    val isUnconfirmed = maintenance.status != "confirmed"

    // Look up car and garage from their IDs
    val car = maintenance.voiture?.let { carId ->
        cars.find { it.id == carId }
    }
    val garage = maintenance.garage?.let { garageId ->
        garages.find { it.id == garageId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isUnconfirmed) {
                        Modifier.border(3.dp, AccentYellow, RoundedCornerShape(16.dp))
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow(label = "Type d'entretien", value = maintenance.type.replaceFirstChar { it.uppercase() })
                car?.let {
                    DetailRow(label = "Véhicule", value = "${it.marque} ${it.modele}")
                }
                garage?.let {
                    DetailRow(label = "Garage", value = it.nom)
                }
                DetailRow(label = "Date", value = dateFormat.format(maintenance.date))
                DetailRow(label = "Coût", value = "${maintenance.cout} DT", isHighlight = true)
                DetailRow(label = "Statut", value = (maintenance.status ?: "pending").replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = if (isHighlight) DeepPurple else TextPrimary
        )
    }
}
