package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.karhebti_android.navigation.Screen
import com.example.karhebti_android.viewmodel.GarageViewModel
import com.example.karhebti_android.viewmodel.ServiceViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageDetailsScreen(
    garageId: String,
    onBackClick: () -> Unit,
    userRole: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    val garageViewModel: GarageViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val serviceViewModel: ServiceViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val garagesState by garageViewModel.garagesState.observeAsState()
    val servicesState by serviceViewModel.servicesState.observeAsState()

    // Refresh both garage and services when garageId changes
    LaunchedEffect(garageId) {
        garageViewModel.getGarages()
        serviceViewModel.getServicesByGarage(garageId)
    }

    val garage = garagesState?.data?.find { it.id == garageId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails Garage") },
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
        floatingActionButton = {
            if (userRole == "propGarage") {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddService.createRoute(garageId)) },
                    containerColor = DeepPurple,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Ajouter Service")
                }
            }
        },
        containerColor = SoftWhite
    ) { paddingValues ->
        if (garage == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepPurple)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SoftWhite)
                    .padding(paddingValues)
            ) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        // Name and rating
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                garage.nom,
                                style = MaterialTheme.typography.headlineMedium,
                                color = DeepPurple,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, null, tint = AccentYellow)
                                Text("%.1f".format(garage.noteUtilisateur), color = AccentYellow, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        // Address and contacts
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(garage.adresse, color = TextSecondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, null, tint = DeepPurple, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(garage.telephone, color = TextSecondary)
                        }
                        Spacer(Modifier.height(18.dp))

                        // Services
                        Text("Services proposés :", color = DeepPurple, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        when (val servicesResource = servicesState) {
                            is Resource.Success -> {
                                // Only show services with matching garage.id
                                val services = servicesResource.data.orEmpty()
                                    .filter { it.garage?.oid == garageId }
                                if (services.isEmpty()) {
                                    Text("Aucun service renseigné", color = TextSecondary)
                                } else {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(services) { service ->
                                            Surface(
                                                color = DeepPurple.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(
                                                    text = "${service.type} (${service.coutMoyen} DH, ${service.dureeEstimee} min)",
                                                    color = DeepPurple,
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            is Resource.Loading -> {
                                Text("Chargement...", color = TextSecondary)
                            }
                            is Resource.Error -> {
                                Text("Erreur de chargement des services", color = MaterialTheme.colorScheme.error)
                            }
                            else -> {}
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
