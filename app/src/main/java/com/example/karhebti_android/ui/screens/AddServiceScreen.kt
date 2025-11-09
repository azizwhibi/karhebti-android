package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.CarResponse
import com.example.karhebti_android.viewmodel.CarViewModel
import com.example.karhebti_android.viewmodel.ServiceViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.DeepPurple
import com.example.karhebti_android.ui.theme.SoftWhite

enum class ServiceType(val display: String) {
    VIDANGE("vidange"),
    CONTROLE_TECHNIQUE("contrôle technique"),
    REPARATION_PNEU("réparation pneu")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    garageId: String,  // Must be passed as param!
    onBackClick: () -> Unit = {},
    onServiceAdded: () -> Unit = {}
) {
    val context = LocalContext.current
    val serviceViewModel: ServiceViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val carViewModel: CarViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val createState by serviceViewModel.createServiceState.observeAsState()
    val carsState by carViewModel.carsState.observeAsState()

    var type by remember { mutableStateOf(ServiceType.VIDANGE) }
    var coutMoyen by remember { mutableStateOf("") }
    var dureeEstimee by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf(false) }

    var selectedCar: CarResponse? by remember { mutableStateOf(null) }
    var expandedCar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { carViewModel.getMyCars() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un service") },
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
        },
        containerColor = SoftWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 28.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    // Service type selection
                    Box {
                        OutlinedTextField(
                            value = type.display,
                            onValueChange = {},
                            label = { Text("Type de service") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedType = true },
                            shape = RoundedCornerShape(12.dp),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            ServiceType.values().forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t.display, fontWeight = if (type == t) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        type = t
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = coutMoyen,
                        onValueChange = { coutMoyen = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Coût moyen (DH)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = dureeEstimee,
                        onValueChange = { dureeEstimee = it.filter { c -> c.isDigit() } },
                        label = { Text("Durée estimée (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(20.dp))

                    Text("Sélectionnez le véhicule :", color = DeepPurple)
                    Box {
                        OutlinedTextField(
                            value = selectedCar?.let { "${it.marque} ${it.modele} (${it.immatriculation})" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Véhicule") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedCar = true },
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = expandedCar,
                            onDismissRequest = { expandedCar = false }
                        ) {
                            when (val cars = carsState) {
                                is Resource.Success -> {
                                    cars.data?.forEach { car ->
                                        DropdownMenuItem(
                                            text = { Text("${car.marque} ${car.modele} (${car.immatriculation})") },
                                            onClick = {
                                                selectedCar = car
                                                expandedCar = false
                                            }
                                        )
                                    }
                                }
                                is Resource.Loading -> {
                                    DropdownMenuItem(
                                        text = { Text("Chargement...") },
                                        onClick = { }
                                    )
                                }
                                is Resource.Error -> {
                                    DropdownMenuItem(
                                        text = { Text("Erreur de chargement") },
                                        onClick = { }
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    if (createState is Resource.Error) {
                        Text(
                            text = (createState as? Resource.Error)?.message ?: "Erreur",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            val cout = coutMoyen.toDoubleOrNull() ?: 0.0
                            val duree = dureeEstimee.toIntOrNull() ?: 0
                            val carId = selectedCar?.id?.oid ?: return@Button
                            serviceViewModel.createService(
                                type.display,
                                cout,
                                duree,
                                garageId,              // already a String when passed from NavGraph
                                carId                  // must be a String
                            )
                        },
                        enabled = (createState !is Resource.Loading && selectedCar != null),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepPurple),
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        if (createState is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
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

    if (createState is Resource.Success) {
        LaunchedEffect(Unit) { onServiceAdded() }
    }
}

