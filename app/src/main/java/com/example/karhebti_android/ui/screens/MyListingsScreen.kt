package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.karhebti_android.data.api.CarResponse
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.viewmodel.CarViewModel
import com.example.karhebti_android.viewmodel.MarketplaceViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory

// Helper function to build full image URL
private fun getFullImageUrl(imageUrl: String?): String? {
    if (imageUrl == null) return null
    return if (imageUrl.startsWith("http")) {
        imageUrl
    } else {
        "http://10.0.2.2:3000${if (imageUrl.startsWith("/")) imageUrl else "/$imageUrl"}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    onBackClick: () -> Unit,
    carViewModel: CarViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    ),
    marketplaceViewModel: MarketplaceViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val cars by carViewModel.carsState.observeAsState()
    val listCarResult by marketplaceViewModel.listCarResult.observeAsState()

    var showListDialog by remember { mutableStateOf(false) }
    var selectedCar: CarResponse? by remember { mutableStateOf(null) }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        carViewModel.getMyCars()
    }

    // Handle list car result
    LaunchedEffect(listCarResult) {
        when (listCarResult) {
            is Resource.Success -> {
                showListDialog = false
                price = ""
                description = ""
                selectedCar = null
                carViewModel.getMyCars()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Listings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { carViewModel.getMyCars() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val carsState = cars) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    val carList = carsState.data ?: emptyList()
                    if (carList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No cars yet",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Add a car first to list it for sale",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(carList) { car ->
                                MyListingCard(
                                    car = car,
                                    onListForSale = {
                                        selectedCar = car
                                        showListDialog = true
                                    },
                                    onUnlist = {
                                        marketplaceViewModel.unlistCar(car.id)
                                    }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Error loading cars",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            carsState.message ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { carViewModel.getMyCars() }) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
                else -> {}
            }

            // List for sale dialog
            if (showListDialog && selectedCar != null) {
                selectedCar?.let { car ->
                    AlertDialog(
                        onDismissRequest = { showListDialog = false },
                        title = { Text("List Car for Sale") },
                        text = {
                            Column {
                                Text("${car.marque} ${car.modele}")
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = price,
                                    onValueChange = { price = it },
                                    label = { Text("Price ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Description (Optional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val priceValue = price.toDoubleOrNull()
                                    if (priceValue != null && priceValue > 0) {
                                        marketplaceViewModel.listCarForSale(
                                            car.id,
                                            priceValue,
                                            description.ifBlank { null }
                                        )
                                    }
                                },
                                enabled = price.toDoubleOrNull()?.let { it > 0 } == true
                            ) {
                                Text("List for Sale")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showListDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MyListingCard(
    car: CarResponse,
    onListForSale: () -> Unit,
    onUnlist: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Car image
            if (car.imageUrl != null) {
                AsyncImage(
                    model = getFullImageUrl(car.imageUrl),
                    contentDescription = "${car.marque} ${car.modele}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${car.marque} ${car.modele}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Year: ${car.annee} • ${car.immatriculation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                car.kilometrage?.let {
                    Text(
                        text = "Mileage: $it km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status badge - you would need to check if car is listed
                // For now, we'll show a button to list or unlist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onListForSale,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Sell, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("List for Sale")
                    }

                    /* Uncomment when you have a way to check if car is listed
                    OutlinedButton(
                        onClick = onUnlist,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.RemoveCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unlist")
                    }
                    */
                }
            }
        }
    }
}
