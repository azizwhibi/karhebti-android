@file:Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")

package com.example.karhebti_android.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import coil.compose.AsyncImage
import com.example.karhebti_android.data.preferences.TokenManager
import com.example.karhebti_android.network.BreakdownsApi
import com.example.karhebti_android.repository.BreakdownsRepository
import com.example.karhebti_android.ui.components.OpenStreetMapView
import com.example.karhebti_android.ui.components.InteractiveMapView
import com.example.karhebti_android.ui.theme.RedSOS
import com.example.karhebti_android.utils.LocationSettingsHelper
import com.example.karhebti_android.viewmodel.BreakdownViewModel
import com.example.karhebti_android.viewmodel.BreakdownViewModelFactory
import com.google.android.gms.location.*
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakdownSOSScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onSOSSuccess: (breakdownId: String?, type: String, lat: Double, lon: Double) -> Unit = { _, _, _, _ -> }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var currentStep by remember { mutableStateOf(SOSStep.CHECKING_PERMISSION) }
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    // Use the authenticated API client from RetrofitClient
    val api = remember {
        try {
            com.example.karhebti_android.data.api.RetrofitClient.breakdownsApiService
        } catch (e: Exception) {
            Log.e("BreakdownSOSScreen", "Failed to get authenticated API service: ${e.message}")
            null
        }
    }

    val repo = remember {
        if (api != null) {
            BreakdownsRepository(api)
        } else {
            // Fallback to local retrofit instance (should not happen if app is properly initialized)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://karhebti-backend-supa.onrender.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            BreakdownsRepository(retrofit.create(BreakdownsApi::class.java))
        }
    }
    val factory = remember { BreakdownViewModelFactory(repo) }
    val viewModel: BreakdownViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    var lastRequestJson by remember { mutableStateOf<String?>(null) }
    var lastError by remember { mutableStateOf<String?>(null) }
    val topCoroutineScope = rememberCoroutineScope()

    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var showTypeMenu by remember { mutableStateOf(false) }

    val types = listOf("PNEU", "BATTERIE", "MOTEUR", "CARBURANT", "REMORQUAGE", "AUTRE")

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationError by remember { mutableStateOf<String?>(null) }

    var photoUri by remember { mutableStateOf<String?>(null) }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> photoUri = uri?.toString() }
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                if (LocationSettingsHelper.isGPSEnabled(context)) {
                    currentStep = SOSStep.FETCHING_LOCATION
                    fetchLocation(
                        fusedLocationClient = fusedLocationClient,
                        onLocation = { lat, lon ->
                            latitude = lat
                            longitude = lon
                            locationError = null
                            currentStep = SOSStep.SHOWING_MAP
                        },
                        onError = { err ->
                            locationError = err
                            currentStep = SOSStep.GPS_ERROR
                        }
                    )
                } else {
                    currentStep = SOSStep.GPS_DISABLED
                }
            } else {
                currentStep = SOSStep.PERMISSION_DENIED
            }
        }
    )

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (LocationSettingsHelper.isGPSEnabled(context)) {
            currentStep = SOSStep.FETCHING_LOCATION
            fetchLocation(
                fusedLocationClient = fusedLocationClient,
                onLocation = { lat, lon ->
                    latitude = lat
                    longitude = lon
                    locationError = null
                    currentStep = SOSStep.SHOWING_MAP
                },
                onError = { err ->
                    locationError = err
                    currentStep = SOSStep.GPS_ERROR
                }
            )
        } else {
            currentStep = SOSStep.GPS_DISABLED
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            if (LocationSettingsHelper.isGPSEnabled(context)) {
                currentStep = SOSStep.FETCHING_LOCATION
                fetchLocation(
                    fusedLocationClient = fusedLocationClient,
                    onLocation = { lat, lon ->
                        latitude = lat
                        longitude = lon
                        locationError = null
                        currentStep = SOSStep.SHOWING_MAP
                    },
                    onError = { err ->
                        locationError = err
                        currentStep = SOSStep.GPS_ERROR
                    }
                )
            } else {
                currentStep = SOSStep.GPS_DISABLED
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is com.example.karhebti_android.viewmodel.BreakdownUiState.Success -> {
                try {
                    val data = state.data
                    if (data is com.example.karhebti_android.data.BreakdownResponse) {
                        Log.d("BreakdownSOSScreen", "✅ SOS sent successfully! ID: ${data.id}")
                        onSOSSuccess(
                            data.id,
                            type,
                            latitude ?: 0.0,
                            longitude ?: 0.0
                        )
                    }
                } catch (e: Exception) {
                    Log.e("BreakdownSOSScreen", "Error handling success: ${e.message}", e)
                    snackbarHostState.showSnackbar("SOS envoyé mais erreur de navigation: ${e.message}")
                }
            }
            is com.example.karhebti_android.viewmodel.BreakdownUiState.Error -> {
                val msg = state.message
                lastError = msg
                Log.e("BreakdownSOSScreen", "❌ SOS error: $msg")
                snackbarHostState.showSnackbar("Erreur: $msg")
            }
            else -> {}
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = RedSOS,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Confirmer la demande SOS") },
            text = {
                Column {
                    Text("Vous êtes sur le point d'envoyer une demande d'assistance.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Type: $type", fontWeight = FontWeight.Medium)
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

                        val tokenNow = TokenManager.getInstance(context).getToken()
                        if (tokenNow.isNullOrBlank()) {
                            topCoroutineScope.launch {
                                snackbarHostState.showSnackbar("Erreur : utilisateur non identifié.")
                            }
                            return@TextButton
                        }

                        // Vérifier que latitude et longitude sont disponibles
                        val currentLat = latitude
                        val currentLon = longitude

                        if (currentLat == null || currentLon == null) {
                            topCoroutineScope.launch {
                                snackbarHostState.showSnackbar("Erreur : position GPS non disponible.")
                            }
                            return@TextButton
                        }

                        // Vérifier que le type est sélectionné
                        if (type.isBlank()) {
                            topCoroutineScope.launch {
                                snackbarHostState.showSnackbar("Erreur : veuillez sélectionner un type de panne.")
                            }
                            return@TextButton
                        }

                        val normalizedPhoto = if (photoUri != null && (photoUri!!.startsWith("http") || photoUri!!.startsWith("/uploads"))) {
                            photoUri
                        } else {
                            null
                        }

                        try {
                            val request = com.example.karhebti_android.data.CreateBreakdownRequest(
                                vehicleId = null,
                                type = type,
                                description = description.takeIf { it.isNotBlank() },
                                latitude = currentLat,
                                longitude = currentLon,
                                photo = normalizedPhoto
                            )

                            lastRequestJson = try { Gson().toJson(request) } catch (_: Exception) { null }
                            Log.d("BreakdownSOSScreen", "Sending SOS: $lastRequestJson")
                            viewModel.declareBreakdown(request)
                        } catch (e: Exception) {
                            Log.e("BreakdownSOSScreen", "Error creating SOS request: ${e.message}", e)
                            topCoroutineScope.launch {
                                snackbarHostState.showSnackbar("Erreur lors de l'envoi: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Confirmer et envoyer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SOS - Assistance routière") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "Historique")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentStep) {
                SOSStep.CHECKING_PERMISSION -> {
                    LoadingStep(message = "Vérification des permissions...")
                }

                SOSStep.PERMISSION_DENIED -> {
                    ErrorStep(
                        icon = Icons.Default.LocationOff,
                        title = "Permission refusée",
                        message = "L'accès à la localisation est nécessaire pour utiliser le service SOS.",
                        actionLabel = "Réessayer",
                        onAction = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        onCancel = onBackClick
                    )
                }

                SOSStep.GPS_DISABLED -> {
                    ErrorStep(
                        icon = Icons.Default.GpsOff,
                        title = "GPS désactivé",
                        message = "Veuillez activer le GPS pour continuer.",
                        actionLabel = "Activer le GPS",
                        onAction = {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            locationSettingsLauncher.launch(intent)
                        },
                        onCancel = onBackClick
                    )
                }

                SOSStep.FETCHING_LOCATION -> {
                    LoadingStep(message = "Récupération de votre position...")
                }

                SOSStep.GPS_ERROR -> {
                    ErrorStep(
                        icon = Icons.Default.ErrorOutline,
                        title = "Erreur de localisation",
                        message = locationError ?: "Impossible d'obtenir votre position.",
                        actionLabel = "Réessayer",
                        onAction = {
                            currentStep = SOSStep.FETCHING_LOCATION
                            fetchLocation(
                                fusedLocationClient = fusedLocationClient,
                                onLocation = { lat, lon ->
                                    latitude = lat
                                    longitude = lon
                                    locationError = null
                                    currentStep = SOSStep.SHOWING_MAP
                                },
                                onError = { err ->
                                    locationError = err
                                    currentStep = SOSStep.GPS_ERROR
                                }
                            )
                        },
                        onCancel = onBackClick
                    )
                }

                SOSStep.SHOWING_MAP -> {
                    val currentToken = readAnyToken(context)
                    val tokenMasked = currentToken?.let { t ->
                        if (t.length <= 10) t else t.take(6) + "..." + t.takeLast(4)
                    }

                    SOSFormContent(
                        latitude = latitude,
                        longitude = longitude,
                        type = type,
                        description = description,
                        photoUri = photoUri,
                        types = types,
                        showTypeMenu = showTypeMenu,
                        onTypeChange = { type = it },
                        onTypeMenuChange = { showTypeMenu = it },
                        onDescriptionChange = { description = it },
                        onPhotoClick = { pickImageLauncher.launch("image/*") },
                        onSendClick = { showConfirmDialog = true },
                        sendEnabled = type.isNotBlank() && latitude != null && longitude != null,
                        userId = TokenManager.getInstance(context).getUser()?.id,
                        tokenPresent = !currentToken.isNullOrBlank(),
                        tokenMasked = tokenMasked,
                        lastRequestJson = lastRequestJson,
                        lastError = lastError,
                        onRefreshLocation = {
                            // Rafraîchir la position GPS
                            Log.d("BreakdownSOSScreen", "🔄 Rafraîchissement de la position GPS...")
                            currentStep = SOSStep.FETCHING_LOCATION
                            fetchLocation(
                                fusedLocationClient = fusedLocationClient,
                                onLocation = { lat, lon ->
                                    latitude = lat
                                    longitude = lon
                                    locationError = null
                                    currentStep = SOSStep.SHOWING_MAP
                                    topCoroutineScope.launch {
                                        snackbarHostState.showSnackbar("Position mise à jour ✓")
                                    }
                                },
                                onError = { err ->
                                    locationError = err
                                    currentStep = SOSStep.GPS_ERROR
                                }
                            )
                        },
                        onLocationSelected = { lat, lon ->
                            // L'utilisateur a cliqué/déplacé le marqueur sur la carte
                            Log.d("BreakdownSOSScreen", "📍 Position sélectionnée sur la carte: $lat, $lon")
                            latitude = lat
                            longitude = lon
                            topCoroutineScope.launch {
                                snackbarHostState.showSnackbar("Position mise à jour ✓")
                            }
                        }
                    )
                }
            }

            if (uiState is com.example.karhebti_android.viewmodel.BreakdownUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Envoi de la demande SOS...")
                        }
                    }
                }
            }
        }
    }
}

enum class SOSStep {
    CHECKING_PERMISSION,
    PERMISSION_DENIED,
    GPS_DISABLED,
    FETCHING_LOCATION,
    GPS_ERROR,
    SHOWING_MAP
}

@Composable
fun LoadingStep(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                strokeWidth = 6.dp
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorStep(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = RedSOS,
                modifier = Modifier.size(80.dp)
            )

            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel, modifier = Modifier.padding(vertical = 4.dp))
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Annuler")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSFormContent(
    latitude: Double?,
    longitude: Double?,
    type: String,
    description: String,
    photoUri: String?,
    types: List<String>,
    showTypeMenu: Boolean,
    onTypeChange: (String) -> Unit,
    onTypeMenuChange: (Boolean) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPhotoClick: () -> Unit,
    onSendClick: () -> Unit,
    sendEnabled: Boolean,
    userId: String?,
    tokenPresent: Boolean,
    tokenMasked: String?,
    lastRequestJson: String?,
    lastError: String?,
    onRefreshLocation: () -> Unit = {},  // ← Pour le bouton rafraîchir
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> }  // ← Pour la carte interactive
) {
    val scrollState = rememberScrollState()
    var showValidation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(RedSOS, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "SOS",
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        if (latitude != null && longitude != null) {
            // Instruction pour l'utilisateur
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Cliquez sur la carte ou déplacez le marqueur pour ajuster votre position",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                InteractiveMapView(
                    latitude = latitude,
                    longitude = longitude,
                    zoom = 15.0,
                    onLocationSelected = onLocationSelected
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = type,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type de problème *") },
                placeholder = { Text("Choisir...") },
                trailingIcon = {
                    IconButton(onClick = { onTypeMenuChange(!showTypeMenu) }) {
                        Icon(
                            imageVector = if (showTypeMenu) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = showValidation && type.isBlank()
            )

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onTypeMenuChange(true) }
            )

            if (showTypeMenu) {
                AlertDialog(
                    onDismissRequest = { onTypeMenuChange(false) },
                    title = { Text("Sélectionner le type de panne") },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            types.forEach { typeOption ->
                                TextButton(
                                    onClick = {
                                        onTypeChange(typeOption)
                                        onTypeMenuChange(false)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(typeOption, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { onTypeMenuChange(false) }) { Text("Fermer") }
                    }
                )
            }

            if (showValidation && type.isBlank()) {
                Text(
                    text = "⚠️ Veuillez sélectionner un type de panne",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (optionnel)") },
            placeholder = { Text("Décrivez le problème...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            minLines = 2
        )

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onPhotoClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (photoUri == null) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ajouter une photo (optionnel)")
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Photo sélectionnée",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Photo sélectionnée", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (!sendEnabled) {
                    showValidation = true
                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                } else {
                    onSendClick()
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sendEnabled) RedSOS else Color.Gray,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Envoyer la demande SOS", modifier = Modifier.padding(vertical = 4.dp))
        }

        if (showValidation && !sendEnabled) {
            val missingFields = buildList {
                if (type.isBlank()) add("Type de panne")
                if (latitude == null || longitude == null) add("Localisation GPS")
            }

            Text(
                text = "⚠️ Champs manquants : ${missingFields.joinToString(", ")}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        LaunchedEffect(type, latitude, longitude) {
            if (type.isNotBlank() && latitude != null && longitude != null) showValidation = false
        }

        Spacer(Modifier.height(32.dp))

        if (lastRequestJson != null || lastError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    lastRequestJson?.let { req ->
                        Text("Dernière requête:", style = MaterialTheme.typography.labelSmall)
                        Text(req, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                    }

                    Text("État du token:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        if (tokenPresent) "Présent" else "Absent",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (tokenPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    tokenMasked?.let {
                        Text("Token masqué:", style = MaterialTheme.typography.labelSmall)
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun readAnyToken(context: Context): String? {
    return try {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "secret_shared_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val tokenEnc = encryptedPrefs.getString("jwt_token", null)
            if (!tokenEnc.isNullOrBlank()) {
                return tokenEnc
            }
        } catch (e: Exception) {
            Log.w("BreakdownSOS", "Encrypted prefs read failed: ${e.message}")
        }

        TokenManager.getInstance(context).getToken()
    } catch (e: Exception) {
        Log.e("BreakdownSOS", "Error reading token: ${e.message}", e)
        null
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocation: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    // D'abord, essayer d'obtenir la dernière position connue (plus rapide)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                // On a une position récente, l'utiliser
                Log.d("BreakdownSOSScreen", "✅ Position obtenue (lastLocation): ${location.latitude}, ${location.longitude}")
                onLocation(location.latitude, location.longitude)
            } else {
                // Pas de position récente, demander une mise à jour
                Log.d("BreakdownSOSScreen", "⚠️ Pas de lastLocation, demande de mise à jour GPS...")
                requestCurrentLocation(fusedLocationClient, onLocation, onError)
            }
        }
        .addOnFailureListener { exception ->
            Log.e("BreakdownSOSScreen", "❌ Erreur lastLocation: ${exception.message}")
            // En cas d'échec, essayer quand même de demander une mise à jour
            requestCurrentLocation(fusedLocationClient, onLocation, onError)
        }
}

@SuppressLint("MissingPermission")
private fun requestCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocation: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
        .setMinUpdateIntervalMillis(500)
        .setMaxUpdates(1)
        .build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                Log.d("BreakdownSOSScreen", "✅ Position obtenue (nouvelle): ${location.latitude}, ${location.longitude}")
                onLocation(location.latitude, location.longitude)
            } ?: run {
                Log.e("BreakdownSOSScreen", "❌ Aucune position dans le résultat")
                onError("Position introuvable. Veuillez activer le GPS et réessayer.")
            }
        }
    }

    try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    } catch (e: SecurityException) {
        Log.e("BreakdownSOSScreen", "❌ Erreur permission: ${e.message}")
        onError("Permission de localisation refusée")
    } catch (e: Exception) {
        Log.e("BreakdownSOSScreen", "❌ Erreur requestLocationUpdates: ${e.message}")
        onError("Erreur lors de la récupération de la position: ${e.message}")
    }
}

private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}
