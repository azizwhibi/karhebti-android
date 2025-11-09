package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.viewmodel.GarageViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.DeepPurple
import com.example.karhebti_android.ui.theme.SoftWhite

enum class GarageService(val display: String) {
    REVISION("Révision"),
    VIDANGE("Vidange")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGarageScreen(
    onBackClick: () -> Unit = {},
    onGarageAdded: () -> Unit = {}
) {
    val context = LocalContext.current
    val garageViewModel: GarageViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val createGarageState by garageViewModel.createGarageState.observeAsState()

    var nom by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var noteUtilisateur by remember { mutableStateOf("0.0") }

    val allServices = GarageService.values().toList()
    var expanded by remember { mutableStateOf(false) }
    var selectedServices by remember { mutableStateOf(listOf<GarageService>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un garage") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom du garage") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adresse,
                        onValueChange = { adresse = it },
                        label = { Text("Adresse") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = telephone,
                        onValueChange = { telephone = it },
                        label = { Text("Téléphone") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))

                    // Multi-select dropdown for services
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = if (selectedServices.isEmpty()) "" else selectedServices.joinToString { it.display },
                            onValueChange = {},
                            label = { Text("Types de service") },
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
                            allServices.forEach { service ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                service.display,
                                                fontWeight = if (selectedServices.contains(service)) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (selectedServices.contains(service)) {
                                                Spacer(Modifier.width(8.dp))
                                                Icon(Icons.Default.Check, "Sélectionné", tint = DeepPurple)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedServices = if (selectedServices.contains(service)) {
                                            selectedServices - service
                                        } else {
                                            selectedServices + service
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = noteUtilisateur,
                        onValueChange = { noteUtilisateur = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Note utilisateur (0 à 5)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(24.dp))

                    if (createGarageState is Resource.Error) {
                        Text(
                            text = (createGarageState as? Resource.Error)?.message ?: "Erreur",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(6.dp))
                    }

                    Button(
                        onClick = {
                            val note = noteUtilisateur.toDoubleOrNull() ?: 0.0
                            garageViewModel.createGarage(
                                nom,
                                adresse,
                                selectedServices.map { it.display },
                                telephone,
                                note
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepPurple),
                        enabled = (createGarageState !is Resource.Loading)
                    ) {
                        if (createGarageState is Resource.Loading) {
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

    if (createGarageState is Resource.Success) {
        LaunchedEffect(Unit) { onGarageAdded() }
    }
}
