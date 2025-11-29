# Quick Implementation Guide for Remaining Screens

## 🚗 VehiclesScreen - Complete Example

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    onBackClick: () -> Unit = {},
    onVehicleClick: (String) -> Unit = {},
    carViewModel: CarViewModel = viewModel()
) {
    // Load vehicles on screen launch
    LaunchedEffect(Unit) {
        carViewModel.getMyCars()
    }
    
    // Observe state
    val carsState by carViewModel.carsState.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show errors
    LaunchedEffect(carsState) {
        if (carsState is Resource.Error) {
            snackbarHostState.showSnackbar(
                message = (carsState as Resource.Error).message ?: "Erreur",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes véhicules") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Show add vehicle dialog */ },
                containerColor = DeepPurple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Ajouter véhicule")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (carsState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DeepPurple)
                }
            }
            is Resource.Success -> {
                val cars = (carsState as Resource.Success).data ?: emptyList()
                
                if (cars.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextSecondary
                            )
                            Text(
                                text = "Aucun véhicule",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextSecondary
                            )
                            Text(
                                text = "Ajoutez votre premier véhicule",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SoftWhite)
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cars) { car ->
                            CarCard(
                                car = car,
                                onClick = { onVehicleClick(car.id) },
                                onDelete = { carViewModel.deleteCar(car.id) }
                            )
                        }
                    }
                }
            }
            is Resource.Error -> {
                // Error state with retry
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AlertRed
                        )
                        Text(
                            text = "Erreur de chargement",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Button(onClick = { carViewModel.getMyCars() }) {
                            Text("Réessayer")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun CarCard(
    car: CarResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
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
                        text = "${car.annee} • ${car.immatriculation}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = AlertRed
                    )
                }
            }
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = LightPurple.copy(alpha = 0.3f)
            ) {
                Text(
                    text = car.typeCarburant,
                    style = MaterialTheme.typography.bodySmall,
                    color = DeepPurple,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le véhicule") },
            text = { Text("Êtes-vous sûr de vouloir supprimer ce véhicule ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Supprimer", color = AlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
```

## 🔧 EntretiensScreen - Complete Example

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntretiensScreen(
    onBackClick: () -> Unit = {},
    maintenanceViewModel: MaintenanceViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("À venir", "Historique")
    
    // Load maintenances on screen launch
    LaunchedEffect(Unit) {
        maintenanceViewModel.getMaintenances()
    }
    
    // Observe state
    val maintenancesState by maintenanceViewModel.maintenancesState.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entretiens") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            when (maintenancesState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DeepPurple)
                    }
                }
                is Resource.Success -> {
                    val allMaintenances = (maintenancesState as Resource.Success).data ?: emptyList()
                    
                    // Filter based on tab (you can add date logic)
                    val displayMaintenances = allMaintenances
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(displayMaintenances) { maintenance ->
                            MaintenanceCard(
                                maintenance = maintenance,
                                onDelete = { maintenanceViewModel.deleteMaintenance(maintenance.id) }
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    ErrorView(
                        message = (maintenancesState as Resource.Error).message,
                        onRetry = { maintenanceViewModel.getMaintenances() }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun MaintenanceCard(
    maintenance: MaintenanceResponse,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = maintenance.type.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    maintenance.garage?.let {
                        Text(
                            text = it.nom,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
                
                Text(
                    text = "${maintenance.cout}€",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepPurple
                )
            }
            
            // Vehicle info
            maintenance.voiture?.let { car ->
                Text(
                    text = "${car.marque} ${car.modele}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
```

## 🏢 GaragesScreen - Complete Example

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaragesScreen(
    onBackClick: () -> Unit = {},
    garageViewModel: GarageViewModel = viewModel()
) {
    var selectedFilter by remember { mutableStateOf("Tous") }
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("Tous", "Révision", "Pneus", "CT")
    
    // Load garages on screen launch
    LaunchedEffect(Unit) {
        garageViewModel.getGarages()
    }
    
    // Observe state
    val garagesState by garageViewModel.garagesState.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Garages") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Rechercher un garage...", color = InputPlaceholder) },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Rechercher", tint = TextSecondary)
                },
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

            // Content
            when (garagesState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DeepPurple)
                    }
                }
                is Resource.Success -> {
                    val allGarages = (garagesState as Resource.Success).data ?: emptyList()
                    
                    // Filter by search query
                    val filteredGarages = if (searchQuery.isEmpty()) {
                        allGarages
                    } else {
                        allGarages.filter { 
                            it.nom.contains(searchQuery, ignoreCase = true) ||
                            it.adresse.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredGarages) { garage ->
                            GarageCard(garage = garage)
                        }
                    }
                }
                is Resource.Error -> {
                    ErrorView(
                        message = (garagesState as Resource.Error).message,
                        onRetry = { garageViewModel.getGarages() }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun GarageCard(garage: GarageResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = garage.nom,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            
            Text(
                text = garage.adresse,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            // Rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = garage.noteUtilisateur.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
            
            // Services
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                garage.typeService.forEach { service ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightPurple
                    ) {
                        Text(
                            text = service,
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepPurple,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
```

## 📄 DocumentsScreen - Complete Example

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    onBackClick: () -> Unit = {},
    documentViewModel: DocumentViewModel = viewModel()
) {
    var selectedFilter by remember { mutableStateOf("Tous") }
    val filters = listOf("Tous", "Administratif", "Entretien")
    
    // Load documents
    LaunchedEffect(Unit) {
        documentViewModel.getDocuments()
    }
    
    val documentsState by documentViewModel.documentsState.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documents") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Upload document */ }) {
                        Icon(Icons.Default.Add, "Ajouter", tint = Color.White)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
        ) {
            when (documentsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DeepPurple)
                    }
                }
                is Resource.Success -> {
                    val documents = (documentsState as Resource.Success).data ?: emptyList()
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(documents) { document ->
                            DocumentCard(
                                document = document,
                                onDelete = { documentViewModel.deleteDocument(document.id) }
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    ErrorView(
                        message = (documentsState as Resource.Error).message,
                        onRetry = { documentViewModel.getDocuments() }
                    )
                }
                else -> {}
            }
        }
    }
}
```

## 🔄 Common Error View Component

```kotlin
@Composable
fun ErrorView(
    message: String?,
    onRetry: () -> Unit
) {
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
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AlertRed
            )
            Text(
                text = message ?: "Une erreur est survenue",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = DeepPurple)
            ) {
                Text("Réessayer")
            }
        }
    }
}
```

## 🎯 Next Steps

1. **Copy these implementations** into your respective screen files
2. **Add necessary imports** (viewModel, observeAsState, etc.)
3. **Test each screen** with your backend running
4. **Add dialogs** for creating new items (vehicles, maintenances, etc.)
5. **Implement file upload** for documents using MultipartBody

## 📝 Required Dependencies

Make sure you have these in your `build.gradle.kts`:

```kotlin
// LiveData for Compose
implementation("androidx.compose.runtime:runtime-livedata:1.5.4")

// Lifecycle ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
```

## 🔥 Pro Tips

1. **Always observe state** in your composables
2. **Show loading indicators** during API calls
3. **Handle empty states** gracefully
4. **Add pull-to-refresh** for better UX
5. **Cache data locally** for offline mode (future enhancement)

---

**Implementation Priority:**
1. ✅ Authentication (DONE)
2. 🔄 Vehicles Screen (Copy code above)
3. 🔄 Entretiens Screen (Copy code above)
4. 🔄 Garages Screen (Copy code above)
5. 🔄 Documents Screen (Copy code above)

