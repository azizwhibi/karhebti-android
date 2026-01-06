# 💻 Exemples de Code - Intégration BreakdownViewModel

## 📱 1. BreakdownSOSScreen - Envoi du SOS

```kotlin
@Composable
fun BreakdownSOSScreen(
    onSOSSuccess: (breakdownId: String, type: String, lat: Double, lon: Double) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // 🔧 Setup ViewModel
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://172.18.1.246:3000/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor(context))
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = retrofit.create(BreakdownsApi::class.java)
    val repo = remember { BreakdownsRepository(api) }
    val factory = remember { BreakdownViewModelFactory(repo) }
    val viewModel: BreakdownViewModel = viewModel(factory = factory)
    
    // 📊 États
    val uiState by viewModel.uiState.collectAsState()
    var selectedType by remember { mutableStateOf("PNEU") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // 📍 Location
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    // 🎯 Gérer les réponses du ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BreakdownUiState.Success -> {
                val breakdown = state.data as BreakdownResponse
                Log.d("SOSScreen", "✅ SOS créé: ${breakdown.id}")
                
                // Navigation vers l'écran de statut
                onSOSSuccess(
                    breakdown.id,
                    breakdown.type,
                    latitude ?: 0.0,
                    longitude ?: 0.0
                )
                
                // Reset pour prochain SOS
                viewModel.resetState()
            }
            
            is BreakdownUiState.Error -> {
                Log.e("SOSScreen", "❌ Erreur: ${state.message}")
                // Afficher snackbar ou dialog
            }
            
            else -> {}
        }
    }
    
    // 📱 UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚨 SOS") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type de panne
            Text("Type de panne", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("PNEU", "BATTERIE", "ACCIDENT").forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type) }
                    )
                }
            }
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            // Position
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("📍 Position GPS", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    
                    if (latitude != null && longitude != null) {
                        Text("✅ Position détectée")
                        Text("Lat: ${latitude?.format(4)}", style = MaterialTheme.typography.bodySmall)
                        Text("Lon: ${longitude?.format(4)}", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("⚠️ Position non détectée")
                        Button(onClick = {
                            // Demander permission et obtenir position
                            try {
                                val location = locationManager.getLastKnownLocation(
                                    LocationManager.GPS_PROVIDER
                                )
                                latitude = location?.latitude
                                longitude = location?.longitude
                            } catch (e: SecurityException) {
                                Log.e("SOSScreen", "Permission manquante")
                            }
                        }) {
                            Text("Détecter position")
                        }
                    }
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            // Bouton d'envoi
            Button(
                onClick = {
                    if (latitude != null && longitude != null) {
                        showConfirmDialog = true
                    } else {
                        // Afficher erreur
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is BreakdownUiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)
                )
            ) {
                if (uiState is BreakdownUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text("📤 Envoyer SOS")
                }
            }
        }
    }
    
    // Dialog de confirmation
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Confirmer la demande SOS") },
            text = {
                Column {
                    Text("Vous êtes sur le point d'envoyer une demande d'assistance.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Type: $selectedType", fontWeight = FontWeight.Medium)
                    if (description.isNotBlank()) {
                        Text("• Description: $description")
                    }
                    Text("• Position: ${latitude?.format(4)}, ${longitude?.format(4)}")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Un technicien sera notifié et se dirigera vers votre position.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        
                        // 🚨 ENVOYER LE SOS
                        val request = CreateBreakdownRequest(
                            type = selectedType,
                            description = description.takeIf { it.isNotBlank() },
                            latitude = latitude!!,
                            longitude = longitude!!
                        )
                        
                        Log.d("SOSScreen", "📤 Envoi SOS: $request")
                        viewModel.declareBreakdown(request)
                    }
                ) {
                    Text("Confirmer", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

// Extension pour formater les coordonnées
fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
```

---

## 🔄 2. SOSStatusScreen - Polling & Attente

```kotlin
@Composable
fun SOSStatusScreen(
    breakdownId: String,
    type: String,
    latitude: Double,
    longitude: Double,
    onNavigateToTracking: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // 🔧 Setup ViewModel
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://172.18.1.246:3000/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor(context))
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = retrofit.create(BreakdownsApi::class.java)
    val repo = remember { BreakdownsRepository(api) }
    val factory = remember { BreakdownViewModelFactory(repo) }
    val viewModel: BreakdownViewModel = viewModel(factory = factory)
    
    // 📊 États
    val uiState by viewModel.uiState.collectAsState()
    var currentBreakdown by remember { mutableStateOf<BreakdownResponse?>(null) }
    var currentStatus by remember { mutableStateOf("PENDING") }
    var hasNavigated by remember { mutableStateOf(false) }
    
    // 🔄 Démarrer le polling au lancement
    LaunchedEffect(breakdownId) {
        Log.d("SOSStatus", "🔄 Démarrage du polling pour breakdown $breakdownId")
        viewModel.startPollingBreakdown(
            breakdownId = breakdownId.toIntOrNull() ?: 0,
            intervalMs = 5000L // Poll every 5 seconds
        )
    }
    
    // 🎯 Gérer les changements de statut
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BreakdownUiState.Success -> {
                val data = state.data
                if (data is BreakdownResponse) {
                    currentBreakdown = data
                    currentStatus = data.status
                    Log.d("SOSStatus", "📊 Status: ${data.status}")
                }
            }
            
            is BreakdownUiState.StatusChanged -> {
                val newStatus = state.breakdown.status
                val prevStatus = state.previousStatus
                
                Log.d("SOSStatus", "🔄 Changement détecté: $prevStatus → $newStatus")
                
                currentBreakdown = state.breakdown
                currentStatus = newStatus
                
                // 🎉 Navigation automatique quand accepté
                if (prevStatus == "PENDING" && newStatus == "ACCEPTED" && !hasNavigated) {
                    Log.d("SOSStatus", "✅ ACCEPTED! Navigation vers tracking...")
                    hasNavigated = true
                    
                    // Petit délai pour l'animation
                    delay(1000)
                    
                    // Arrêter le polling
                    viewModel.stopPolling()
                    
                    // Naviguer
                    onNavigateToTracking(breakdownId)
                }
            }
            
            is BreakdownUiState.Error -> {
                Log.e("SOSStatus", "❌ Erreur: ${state.message}")
            }
            
            else -> {}
        }
    }
    
    // 🧹 Cleanup: arrêter le polling à la sortie
    DisposableEffect(Unit) {
        onDispose {
            Log.d("SOSStatus", "🧹 Arrêt du polling")
            viewModel.stopPolling()
        }
    }
    
    // 🎨 Animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // 📱 UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚨 Demande SOS") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopPolling()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (currentStatus) {
                "PENDING" -> {
                    // En attente d'un garage
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Icône animée
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scale),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .rotate(rotation),
                                tint = Color(0xFFE53935)
                            )
                        }
                        
                        Text(
                            "Recherche d'un garage à proximité...",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        CircularProgressIndicator(color = Color(0xFFE53935))
                        
                        // Infos de la demande
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📋 Détails de la demande", style = MaterialTheme.typography.titleMedium)
                                Divider()
                                Text("Type: $type")
                                Text("Position: ${latitude.format(4)}, ${longitude.format(4)}")
                                if (currentBreakdown?.description != null) {
                                    Text("Description: ${currentBreakdown?.description}")
                                }
                                Text("ID: $breakdownId", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        
                        Text(
                            "⏱️ Temps d'attente moyen: 2-5 minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                "ACCEPTED" -> {
                    // Garage trouvé!
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        
                        Text(
                            "🎉 Garage trouvé!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF4CAF50)
                        )
                        
                        Text(
                            "Redirection vers le suivi...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                }
                
                "REFUSED" -> {
                    // Refusé
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = Color(0xFFF44336)
                        )
                        
                        Text(
                            "❌ Demande refusée",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        
                        Text(
                            "Aucun garage disponible dans votre zone.",
                            textAlign = TextAlign.Center
                        )
                        
                        Button(onClick = onBack) {
                            Text("Retour")
                        }
                    }
                }
            }
        }
    }
}
```

---

## 🏢 3. BreakdownDetailScreen - Garage Owner

```kotlin
@Composable
fun BreakdownDetailScreen(
    breakdownId: Int,
    onAccepted: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // 🔧 Setup ViewModel
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://172.18.1.246:3000/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor(context))
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = retrofit.create(BreakdownsApi::class.java)
    val repo = remember { BreakdownsRepository(api) }
    val factory = remember { BreakdownViewModelFactory(repo) }
    val viewModel: BreakdownViewModel = viewModel(factory = factory)
    
    // 📊 États
    val uiState by viewModel.uiState.collectAsState()
    var breakdown by remember { mutableStateOf<BreakdownResponse?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // 🔄 Charger les détails au lancement
    LaunchedEffect(breakdownId) {
        Log.d("BreakdownDetail", "📥 Chargement breakdown #$breakdownId")
        viewModel.fetchBreakdownById(breakdownId)
    }
    
    // 🎯 Gérer les réponses
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BreakdownUiState.Success -> {
                val data = state.data
                if (data is BreakdownResponse) {
                    breakdown = data
                    Log.d("BreakdownDetail", "✅ Breakdown chargé: ${data.status}")
                    
                    // Si déjà accepté, naviguer au tracking
                    if (data.status == "ACCEPTED") {
                        delay(500)
                        onAccepted()
                    }
                }
            }
            
            is BreakdownUiState.Error -> {
                Log.e("BreakdownDetail", "❌ Erreur: ${state.message}")
            }
            
            else -> {}
        }
    }
    
    // 📱 UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚨 Demande SOS") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
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
            when {
                uiState is BreakdownUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                breakdown != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Type de panne
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE53935).copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        breakdown!!.type,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color(0xFFE53935)
                                    )
                                }
                                
                                if (breakdown!!.description != null) {
                                    Text(
                                        breakdown!!.description!!,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        
                        // Position
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📍 Position", style = MaterialTheme.typography.titleMedium)
                                Divider()
                                
                                Text("Latitude: ${breakdown!!.latitude}")
                                Text("Longitude: ${breakdown!!.longitude}")
                                
                                // TODO: Afficher la carte
                                Button(
                                    onClick = {
                                        // Ouvrir dans Google Maps
                                        val uri = "geo:${breakdown!!.latitude},${breakdown!!.longitude}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.LocationOn, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Voir sur la carte")
                                }
                            }
                        }
                        
                        // Infos client
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("👤 Client", style = MaterialTheme.typography.titleMedium)
                                Divider()
                                
                                Text("ID: ${breakdown!!.userId ?: "N/A"}")
                                
                                // TODO: Afficher nom et téléphone du client
                                Button(
                                    onClick = {
                                        // Appeler le client
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Phone, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Appeler le client")
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Boutons d'action
                        if (breakdown!!.status == "PENDING") {
                            Button(
                                onClick = { showConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(Modifier.width(8.dp))
                                Text("✅ Accepter")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    viewModel.updateBreakdownStatus(breakdownId, "REFUSED")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF44336)
                                )
                            ) {
                                Icon(Icons.Default.Cancel, null)
                                Spacer(Modifier.width(8.dp))
                                Text("❌ Refuser")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialog de confirmation
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Accepter cette demande SOS?") },
            text = {
                Column {
                    Text("Vous vous engagez à:")
                    Spacer(Modifier.height(8.dp))
                    Text("• Vous rendre sur place")
                    Text("• Arriver dans 15-20 min")
                    Text("• Apporter le matériel nécessaire")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Le client sera notifié de votre acceptation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        
                        // ✅ ACCEPTER LA DEMANDE
                        Log.d("BreakdownDetail", "✅ Acceptation de la demande #$breakdownId")
                        viewModel.updateBreakdownStatus(breakdownId, "ACCEPTED")
                    }
                ) {
                    Text("Confirmer", color = Color(0xFF4CAF50))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
```

---

## 📚 Utilisation dans NavGraph

```kotlin
// Dans NavGraph.kt

// Route pour l'envoi du SOS
composable(Screen.SOS.route) {
    BreakdownSOSScreen(
        onSOSSuccess = { breakdownId, type, lat, lon ->
            navController.navigate(
                "sos_status/$breakdownId/$type/$lat/$lon"
            ) {
                popUpTo(Screen.SOS.route) { inclusive = true }
            }
        },
        onBack = { navController.popBackStack() }
    )
}

// Route pour le statut (avec polling)
composable(
    route = "sos_status/{breakdownId}/{type}/{lat}/{lon}",
    arguments = listOf(
        navArgument("breakdownId") { type = NavType.StringType },
        navArgument("type") { type = NavType.StringType },
        navArgument("lat") { type = NavType.FloatType },
        navArgument("lon") { type = NavType.FloatType }
    )
) { backStackEntry ->
    val breakdownId = backStackEntry.arguments?.getString("breakdownId") ?: ""
    val type = backStackEntry.arguments?.getString("type") ?: ""
    val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
    val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 0.0
    
    SOSStatusScreen(
        breakdownId = breakdownId,
        type = type,
        latitude = lat,
        longitude = lon,
        onNavigateToTracking = { id ->
            navController.navigate("breakdown_tracking/$id") {
                popUpTo("sos_status/$breakdownId/$type/$lat/$lon") { inclusive = true }
            }
        },
        onBack = { navController.popBackStack() }
    )
}

// Route pour le tracking
composable(
    route = "breakdown_tracking/{breakdownId}",
    arguments = listOf(
        navArgument("breakdownId") { type = NavType.StringType }
    )
) { backStackEntry ->
    val breakdownId = backStackEntry.arguments?.getString("breakdownId") ?: ""
    
    BreakdownTrackingScreen(
        breakdownId = breakdownId,
        onBack = {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    )
}

// Route pour les détails (garage owner)
composable(
    route = "breakdown_detail/{breakdownId}",
    arguments = listOf(
        navArgument("breakdownId") { type = NavType.IntType }
    )
) { backStackEntry ->
    val breakdownId = backStackEntry.arguments?.getInt("breakdownId") ?: 0
    
    BreakdownDetailScreen(
        breakdownId = breakdownId,
        onAccepted = {
            navController.navigate("breakdown_tracking/$breakdownId") {
                popUpTo("breakdown_detail/$breakdownId") { inclusive = true }
            }
        },
        onBack = { navController.popBackStack() }
    )
}
```

---

**Dernière mise à jour:** 14 décembre 2025

