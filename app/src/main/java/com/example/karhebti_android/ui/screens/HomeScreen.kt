package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
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
    // Get ViewModels
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

    // Collect counters from StateFlow
    val carCount by carViewModel.carCount.collectAsState()
    val maintenanceCount by maintenanceViewModel.maintenanceCount.collectAsState()
    val garageCount by garageViewModel.garageCount.collectAsState()
    val documentCount by documentViewModel.documentCount.collectAsState()

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
    var alertsTitle by remember { mutableStateOf("Alertes importantes") }
    var revisionTitle by remember { mutableStateOf("Révision à prévoir") }
    var revisionDescription by remember { mutableStateOf("Vidange + Filtre à huile") }
    var revisionDeadline by remember { mutableStateOf("Dans 15 jours ou 500 km") }
    var urgentText by remember { mutableStateOf("URGENT") }
    var scheduleText by remember { mutableStateOf("Planifier") }
    var fuelLowText by remember { mutableStateOf("Niveau carburant bas") }
    var fuelDetail by remember { mutableStateOf("15% restant · Autonomie 45 km") }
    var quickActionsTitle by remember { mutableStateOf("Actions rapides") }
    var vehiclesLabel by remember { mutableStateOf("Véhicules") }
    var maintenanceLabel by remember { mutableStateOf("Entretien") }
    var documentsLabel by remember { mutableStateOf("Documents") }
    var garagesLabel by remember { mutableStateOf("Garages") }
    var overviewTitle by remember { mutableStateOf("Aperçu") }
    var settingsDescription by remember { mutableStateOf("Paramètres") }

    // Update translations when language changes
    LaunchedEffect(currentLanguage) {
        coroutineScope.launch {
            welcomeText = translationManager.translate("hello", "Bonjour", currentLanguage)
            alertsTitle = translationManager.translate("important_alerts", "Alertes importantes", currentLanguage)
            revisionTitle = translationManager.translate("revision_needed", "Révision à prévoir", currentLanguage)
            revisionDescription = translationManager.translate("oil_change_filter", "Vidange + Filtre à huile", currentLanguage)
            revisionDeadline = translationManager.translate("in_15_days", "Dans 15 jours ou 500 km", currentLanguage)
            urgentText = translationManager.translate("urgent", "URGENT", currentLanguage)
            scheduleText = translationManager.translate("schedule", "Planifier", currentLanguage)
            fuelLowText = translationManager.translate("low_fuel", "Niveau carburant bas", currentLanguage)
            fuelDetail = translationManager.translate("fuel_detail", "15% restant · Autonomie 45 km", currentLanguage)
            quickActionsTitle = translationManager.translate("quick_actions", "Actions rapides", currentLanguage)
            vehiclesLabel = translationManager.translate("vehicles", "Véhicules", currentLanguage)
            maintenanceLabel = translationManager.translate("maintenance", "Entretien", currentLanguage)
            documentsLabel = translationManager.translate("documents", "Documents", currentLanguage)
            garagesLabel = translationManager.translate("garages", "Garages", currentLanguage)
            overviewTitle = translationManager.translate("overview", "Aperçu", currentLanguage)
            settingsDescription = translationManager.translate("settings", "Paramètres", currentLanguage)
        }
    }

    // Load data on first composition
    LaunchedEffect(Unit) {
        carViewModel.getMyCars()
        maintenanceViewModel.getMaintenances()
        garageViewModel.getGarages()
        documentViewModel.getDocuments()
    }

    // Get current user data for personalized greeting
    val currentUser = authViewModel.getCurrentUser()
    val userFirstName = currentUser?.prenom ?: "Utilisateur"
    val userInitials = if (currentUser != null) {
        "${currentUser.prenom.firstOrNull()?.uppercaseChar() ?: ""}${currentUser.nom.firstOrNull()?.uppercaseChar() ?: ""}"
    } else {
        "U"
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
                    // Profile Avatar
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

                    // App Name
                    Text(
                        text = "Karhebti",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    // Settings Icon
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
            // Personalized Welcome Message
            Text(
                text = "$welcomeText, $userFirstName 👋",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            // Alertes importantes Section
            Text(
                text = alertsTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            // Révision à prévoir Card
            AlertCard(
                title = revisionTitle,
                description = revisionDescription,
                deadline = revisionDeadline,
                urgency = urgentText,
                urgencyColor = AlertRed,
                scheduleButtonText = scheduleText,
                onActionClick = { /* Handle planifier */ }
            )

            // Niveau carburant bas Card
            FuelAlertCard(
                fuelLowTitle = fuelLowText,
                fuelDetail = fuelDetail
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions
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

            // Overview Chips - Now with dynamic counters
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
                    color = LightPurple,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Marketplace Section
            Text(
                text = "🚗 Car Marketplace",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            // Marketplace Quick Actions
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
        }
    }
}

@Composable
fun AlertCard(
    title: String,
    description: String,
    deadline: String,
    urgency: String,
    urgencyColor: Color,
    scheduleButtonText: String = "Planifier",
    onActionClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = urgencyColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = urgencyColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = urgency,
                        style = MaterialTheme.typography.labelSmall,
                        color = urgencyColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = deadline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = onActionClick) {
                    Text(scheduleButtonText, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun FuelAlertCard(
    fuelLowTitle: String = "Niveau carburant bas",
    fuelDetail: String = "15% restant · Autonomie 45 km"
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AccentYellow.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fuelLowTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = fuelDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    KarhebtiandroidTheme {
        HomeScreen()
    }
}
