package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.GarageResponse
import com.example.karhebti_android.data.preferences.TokenManager
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.*

import com.example.karhebti_android.viewmodel.GarageViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaragesScreen(
    onBackClick: () -> Unit = {},
    onAddGarageClick: () -> Unit = {},
    onGarageClick: (garageId: String) -> Unit = {} // Add navigation callback
) {
    val context = LocalContext.current
    val garageViewModel: GarageViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val currentUser = TokenManager.getInstance(context).getUser()
    val isPropGarage = currentUser?.role == "propGarage"

    val garagesState by garageViewModel.garagesState.observeAsState()
    val recommendationsState by garageViewModel.recommendationsState.observeAsState()

    var selectedFilter by remember { mutableStateOf("Tous") }
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("Tous", "Révision", "Pneus", "CT")
    var showRecommendations by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        garageViewModel.getGarages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Garages") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { garageViewModel.getGarages() }) {
                        Icon(Icons.Default.Refresh, "Actualiser", tint = Color.White)
                    }
                    IconButton(onClick = {
                        showRecommendations = !showRecommendations
                        if (showRecommendations) {
                            garageViewModel.getRecommendations()
                        }
                    }) {
                        Icon(
                            imageVector = if (showRecommendations) Icons.Default.List else Icons.Default.Stars,
                            contentDescription = if (showRecommendations) "Tous" else "Recommandations",
                            tint = Color.White
                        )
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
            if (isPropGarage) {
                FloatingActionButton(
                    onClick = onAddGarageClick,
                    shape = CircleShape,
                    containerColor = DeepPurple,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter un garage")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
        ) {
            if (!showRecommendations) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Rechercher un garage...", color = InputPlaceholder) },
                    leadingIcon = { Icon(Icons.Default.Search, "Rechercher", tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Effacer", tint = TextSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = InputBorder,
                        focusedBorderColor = InputBorderFocused,
                        unfocusedTextColor = InputText,
                        focusedTextColor = InputText,
                        cursorColor = DeepPurple
                    ),
                    singleLine = true
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepPurple,
                                selectedLabelColor = Color.White,
                                containerColor = LightPurple,
                                labelColor = DeepPurple
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                if (showRecommendations) {
                    // TODO: add recommendation UI if desired
                } else {
                    when (val state = garagesState) {
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
                                    Text("Chargement des garages...", color = TextSecondary)
                                }
                            }
                        }
                        is Resource.Success -> {
                            val allGarages = state.data ?: emptyList()
                            val filteredGarages = allGarages.filter { garage ->
                                val matchesSearch = searchQuery.isEmpty() ||
                                        garage.nom.contains(searchQuery, ignoreCase = true) ||
                                        garage.adresse.contains(searchQuery, ignoreCase = true)
                                val matchesFilter = selectedFilter == "Tous" ||
                                        garage.typeService.any { it.contains(selectedFilter, ignoreCase = true) }
                                matchesSearch && matchesFilter
                            }

                            if (filteredGarages.isEmpty()) {
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
                                            imageVector = Icons.Default.Store,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = TextSecondary.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            "Aucun garage trouvé",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = TextPrimary
                                        )
                                        Text(
                                            "Essayez de modifier vos filtres",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(filteredGarages, key = { it.id }) { garage ->
                                        GarageCardBackendIntegrated(
                                            garage = garage,
                                            onClick = { onGarageClick(garage.id) }
                                        )
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            ErrorStateView(
                                message = state.message ?: "Erreur",
                                onRetry = { garageViewModel.getGarages() }
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun GarageCardBackendIntegrated(garage: GarageResponse, onClick: () -> Unit = {}) {
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = garage.nom,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = garage.adresse,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentGreen.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AccentYellow
                        )
                        Text(
                            text = "%.1f".format(garage.noteUtilisateur),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }
                }
            }

            HorizontalDivider()

            if (garage.typeService.isNotEmpty()) {
                Text(
                    text = "Services:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(garage.typeService) { service ->
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* Call phone */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Appeler")
                }
                OutlinedButton(
                    onClick = { /* Open map */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Itinéraire")
                }
            }
        }
    }
}

@Composable
fun ErrorStateView(message: String, onRetry: () -> Unit) {
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
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
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
