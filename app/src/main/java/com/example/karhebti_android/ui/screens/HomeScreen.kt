package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.ui.theme.*
import com.example.karhebti_android.viewmodel.AuthViewModel
import com.example.karhebti_android.viewmodel.CarViewModel
import com.example.karhebti_android.viewmodel.DocumentViewModel
import com.example.karhebti_android.viewmodel.GarageViewModel
import com.example.karhebti_android.viewmodel.MaintenanceViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import com.example.karhebti_android.data.repository.TranslationManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import com.example.karhebti_android.data.repository.Resource
import androidx.compose.runtime.livedata.observeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onVehiclesClick: () -> Unit = {},
    onEntretiensClick: () -> Unit = {},
    onDocumentsClick: () -> Unit = {},
    onGaragesClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMarketplaceClick: () -> Unit = {},
    onMyListingsClick: () -> Unit = {},
    onConversationsClick: () -> Unit = {},
    onPendingSwipesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    val carViewModel: CarViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val maintenanceViewModel: MaintenanceViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val garageViewModel: GarageViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val documentViewModel: DocumentViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    // Add BreakdownViewModel for SOS requests
    val retrofitLocal = remember {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(com.example.karhebti_android.data.api.AuthInterceptor(context))
            .addInterceptor(logging)
            .build()
        retrofit2.Retrofit.Builder()
            .baseUrl(com.example.karhebti_android.data.api.ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
    }
    val apiLocal = retrofitLocal.create(com.example.karhebti_android.network.BreakdownsApi::class.java)
    val repoLocal = com.example.karhebti_android.repository.BreakdownsRepository(apiLocal)
    val factoryLocal = com.example.karhebti_android.viewmodel.BreakdownViewModelFactory(repoLocal)
    val breakdownViewModel: com.example.karhebti_android.viewmodel.BreakdownViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(factory = factoryLocal)

    val carCount by carViewModel.carCount.collectAsState()
    val maintenanceCount by maintenanceViewModel.maintenanceCount.collectAsState()
    val documentCount by documentViewModel.documentCount.collectAsState()

    // Observer le LiveData pour les garages
    val garagesState by garageViewModel.garagesState.observeAsState()

    // Calculer le nombre de garages depuis garagesState
    val garageCount = when (val state = garagesState) {
        is Resource.Success -> state.data?.size ?: 0
        else -> 0
    }

    // Translation manager setup
    val db = com.example.karhebti_android.data.database.AppDatabase.getInstance(context.applicationContext)
    val translationRepository = com.example.karhebti_android.data.repository.TranslationRepository(
        apiService = com.example.karhebti_android.data.api.RetrofitClient.apiService,
        translationDao = db.translationDao(),
        languageCacheDao = db.languageCacheDao(),
        languageListCacheDao = db.languageListCacheDao()
    )
    val translationManager = remember { TranslationManager.getInstance(translationRepository, context) }
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by translationManager.currentLanguage.collectAsState()

    // Translated UI strings
    var welcomeText by remember { mutableStateOf("Bonjour") }
    var quickActionsTitle by remember { mutableStateOf("Actions rapides") }
    var vehiclesLabel by remember { mutableStateOf("Véhicules") }
    var maintenanceLabel by remember { mutableStateOf("Entretien") }
    var documentsLabel by remember { mutableStateOf("Documents") }
    var garagesLabel by remember { mutableStateOf("Garages") }
    var overviewTitle by remember { mutableStateOf("Aperçu") }
    var settingsDescription by remember { mutableStateOf("Paramètres") }

    LaunchedEffect(currentLanguage) {
        coroutineScope.launch {
            welcomeText = translationManager.translate("hello", "Bonjour", currentLanguage)
            quickActionsTitle = translationManager.translate("quick_actions", "Actions rapides", currentLanguage)
            vehiclesLabel = translationManager.translate("vehicles", "Véhicules", currentLanguage)
            maintenanceLabel = translationManager.translate("maintenance", "Entretien", currentLanguage)
            documentsLabel = translationManager.translate("documents", "Documents", currentLanguage)
            garagesLabel = translationManager.translate("garages", "Garages", currentLanguage)
            overviewTitle = translationManager.translate("overview", "Aperçu", currentLanguage)
            settingsDescription = translationManager.translate("settings", "Paramètres", currentLanguage)
        }
    }

    LaunchedEffect(Unit) {
        carViewModel.getMyCars()
        maintenanceViewModel.getMaintenances()
        garageViewModel.getGarages()
        documentViewModel.getDocuments()
    }

    val currentUser = authViewModel.getCurrentUser()
    val userFirstName = currentUser?.prenom ?: "Utilisateur"
    val userInitials = if (currentUser != null) {
        "${currentUser.prenom.firstOrNull()?.uppercaseChar() ?: ""}${currentUser.nom.firstOrNull()?.uppercaseChar() ?: ""}"
    } else {
        "U"
    }

    // Get current user role
    val userRole = currentUser?.role ?: ""
    val isGarageOwner = userRole == "propGarage"

    // Load breakdown requests for garage owners
    LaunchedEffect(isGarageOwner) {
        if (isGarageOwner) {
            android.util.Log.d("HomeScreen", "Loading SOS requests for garage owner")
            android.util.Log.d("HomeScreen", "Current user: ${currentUser?.email}, Role: $userRole")
            android.util.Log.d("HomeScreen", "Token available: ${com.example.karhebti_android.data.preferences.TokenManager.getInstance(context).getToken() != null}")
            breakdownViewModel.fetchAllBreakdowns(status = "pending")
        }
    }

    // Breakdown UI state for garage owners
    val breakdownUiState by breakdownViewModel.uiState.collectAsState()
    val pendingSOSRequests = remember(breakdownUiState) {
        if (breakdownUiState is com.example.karhebti_android.viewmodel.BreakdownUiState.Success) {
            val data = (breakdownUiState as com.example.karhebti_android.viewmodel.BreakdownUiState.Success).data
            if (data is List<*>) {
                data.filterIsInstance<com.example.karhebti_android.data.BreakdownResponse>()
                    .filter { it.status == "pending" || it.assignedTo == null }
            } else emptyList()
        } else emptyList()
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
                            .clickable { onSettingsClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Text(
                        text = "Karhebti",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = settingsDescription,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "$welcomeText, $userFirstName 👋",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = quickActionsTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.DriveEta,
                    label = vehiclesLabel,
                    onClick = onVehiclesClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Build,
                    label = maintenanceLabel,
                    onClick = onEntretiensClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Article,
                    label = documentsLabel,
                    onClick = onDocumentsClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Store,
                    label = garagesLabel,
                    onClick = onGaragesClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = overviewTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewChip(
                    count = carCount.toString(),
                    label = vehiclesLabel,
                    color = DeepPurple,
                    modifier = Modifier.weight(1f)
                )
                OverviewChip(
                    count = maintenanceCount.toString(),
                    label = maintenanceLabel,
                    color = AccentGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewChip(
                    count = documentCount.toString(),
                    label = documentsLabel,
                    color = AccentYellow,
                    modifier = Modifier.weight(1f)
                )
                OverviewChip(
                    count = garageCount.toString(),
                    label = garagesLabel,
                    color = AccentBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "🚗 Car Marketplace",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Explore,
                    label = "Browse Cars",
                    onClick = onMarketplaceClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Sell,
                    label = "My Listings",
                    onClick = onMyListingsClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Chat,
                    label = "Conversations",
                    onClick = onConversationsClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Notifications,
                    label = "Requests",
                    onClick = onPendingSwipesClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // SOS Requests Section for Garage Owners
            if (isGarageOwner) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "🆘 Demandes SOS",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                when (breakdownUiState) {
                    is com.example.karhebti_android.viewmodel.BreakdownUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                    is com.example.karhebti_android.viewmodel.BreakdownUiState.Error -> {
                        Text(
                            text = "Erreur de chargement: ${(breakdownUiState as com.example.karhebti_android.viewmodel.BreakdownUiState.Error).message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlertRed
                        )
                    }
                    else -> {
                        if (pendingSOSRequests.isNotEmpty()) {
                            pendingSOSRequests.forEach { request ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🆘 Demande SOS",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = AlertRed.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = request.status.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AlertRed,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Type: ${request.type.ifEmpty { "Non spécifié" }}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }

                                        request.description?.let { desc ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Description,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = desc,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }

                                        if (request.latitude != null && request.longitude != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Position: ${request.latitude}, ${request.longitude}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }

                                        request.createdAt?.let { date ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Reçu: $date",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    // TODO: Implement accept SOS request
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Accepter la demande SOS ${request.id}",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = AccentGreen
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Accepter",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Accepter")
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    // TODO: Implement view details
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Voir détails de ${request.id}",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Visibility,
                                                    contentDescription = "Détails",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Détails")
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Aucune demande SOS en attente",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OverviewChip(
    count: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}
