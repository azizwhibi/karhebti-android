@file:Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")

package com.example.karhebti_android.ui.screens

// Écran de déclaration de panne (SOS) - Version améliorée
// Suit le flux complet : Vérification GPS → Carte interactive → Confirmation → Envoi → Statut

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.karhebti_android.ui.theme.RedSOS
import com.example.karhebti_android.viewmodel.BreakdownViewModel
import com.example.karhebti_android.viewmodel.BreakdownViewModelFactory
import com.example.karhebti_android.repository.BreakdownsRepository
import com.example.karhebti_android.network.BreakdownsApi
import com.example.karhebti_android.utils.LocationSettingsHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import android.os.Looper
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.ui.components.OpenStreetMapView
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.example.karhebti_android.data.api.AuthInterceptor
import com.example.karhebti_android.data.preferences.TokenManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.content.Context
import android.util.Base64
import org.json.JSONObject
import com.example.karhebti_android.viewmodel.BreakdownUiState


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
    
    // États du flux SOS
    var currentStep by remember { mutableStateOf(SOSStep.CHECKING_PERMISSION) }
    // Use rememberSaveable so the state is preserved across process death and analyzer recognizes usage
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    // Add a log statement to confirm dialog state changes
    Log.d("BreakdownSOSScreen", "showConfirmDialog state changed: $showConfirmDialog")

    // Setup ViewModel avec AuthInterceptor
    val retrofit = remember {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val api = remember { retrofit.create(BreakdownsApi::class.java) }
    val repo = remember { BreakdownsRepository(api) }
    val factory = remember { BreakdownViewModelFactory(repo) }
    val viewModel: BreakdownViewModel = viewModel(factory = factory)
    
    // Fix collectAsState issue by ensuring the function is called within a @Composable context
    val uiState by viewModel.uiState.collectAsState(initial = BreakdownUiState.Idle)

     var lastRequestJson by remember { mutableStateOf<String?>(null) }
     var lastError by remember { mutableStateOf<String?>(null) }
     val topCoroutineScope = rememberCoroutineScope()

    // États du formulaire
    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var showTypeMenu by remember { mutableStateOf(false) }
    val types = listOf("PNEU", "BATTERIE", "MOTEUR", "CARBURANT", "REMORQUAGE", "AUTRE")
    
    // États de localisation
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationError by remember { mutableStateOf<String?>(null) }

    // Image picker
    var photoUri by remember { mutableStateOf<String?>(null) }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> photoUri = uri?.toString() }
    )
    
    // Vérification permission GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                // Permission accordée, vérifier si GPS est activé
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
    
    // Launcher pour les paramètres de localisation
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
    
    // Vérification initiale au lancement
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
    
    // Gérer le succès de l'envoi
    LaunchedEffect(uiState) {
        if (uiState is com.example.karhebti_android.viewmodel.BreakdownUiState.Success) {
            val response = (uiState as com.example.karhebti_android.viewmodel.BreakdownUiState.Success).data as com.example.karhebti_android.data.BreakdownResponse
            onSOSSuccess(response.id, type, latitude ?: 0.0, longitude ?: 0.0)
        }
    }



    // Dialogue de confirmation
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                // Add explicit logging to track state changes
                Log.d("BreakdownSOSScreen", "Dialog dismissed, updating showConfirmDialog to false")
                showConfirmDialog = false
            },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = RedSOS,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Confirmer l'envoi du SOS", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Vous êtes sur le point d'envoyer une demande d'assistance.")
            },
            confirmButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
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
                        message = "L'accès à la localisation est nécessaire pour utiliser le service SOS. Veuillez accorder la permission dans les paramètres de l'application.",
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
                        message = "Veuillez activer le GPS pour utiliser le service SOS. Cela nous permet de localiser votre position exacte.",
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
                        message = locationError ?: "Impossible d'obtenir votre position. Vérifiez que le GPS est activé et réessayez.",
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
                    // Formulaire principal avec carte
                    // compute token info for debug (try encrypted storage first like AuthInterceptor)
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
                          onTypeMenuChange = { showTypeMenu = it },
                          onTypeChange = { type = it },
                          onDescriptionChange = { description = it },
                          onPhotoClick = { pickImageLauncher.launch("image/*") },
                          onSendClick = {
                              if (type.isNotBlank() && latitude != null && longitude != null /* now allow without userId */) {
                                  // Added explicit logging to track state changes
                                  Log.d("BreakdownSOSScreen", "showConfirmDialog updated to true")
                                  // Ensure the state change is explicitly tied to UI rendering
                                  showConfirmDialog = true
                                  if (showConfirmDialog) {
                                      Log.d("BreakdownSOSScreen", "Dialog should now be visible")
                                  }
                              }
                          },
                          sendEnabled = type.isNotBlank() && latitude != null && longitude != null,
                          userId = TokenManager.getInstance(context).getUser()?.id, // kept for UI info only
                          tokenPresent = !currentToken.isNullOrBlank(),
                          tokenMasked = tokenMasked,
                          lastRequestJson = lastRequestJson,
                          lastError = lastError
                      )
                  }

                SOSStep.DISPLAYING_BREAKDOWNS -> {
                    // Handle displaying breakdowns
                }

                SOSStep.ERROR -> {
                    // Handle generic error
                }

                // Add an 'else' branch to ensure the 'when' expression is exhaustive
                else -> {
                    // Handle unexpected cases
                    ErrorStep(
                        icon = Icons.Default.Warning,
                        title = "Étape inconnue",
                        message = "Une erreur inattendue s'est produite. Veuillez réessayer.",
                        actionLabel = "Retour",
                        onAction = onBackClick,
                        onCancel = onBackClick
                    )
                }
             }

            // Afficher le loader pendant l'envoi
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
    
    // Gestion des erreurs
    LaunchedEffect(uiState) {
        when (uiState) {
            is com.example.karhebti_android.viewmodel.BreakdownUiState.Error -> {
                val msg = (uiState as com.example.karhebti_android.viewmodel.BreakdownUiState.Error).message
                lastError = msg
                snackbarHostState.showSnackbar("Erreur : $msg")
            }
            else -> {}
        }
    }
}

/**
 * Enum représentant les différentes étapes du flux SOS
 */
enum class SOSStep {
    CHECKING_PERMISSION,    // Vérification de la permission GPS
    PERMISSION_DENIED,      // Permission GPS refusée
    GPS_DISABLED,           // GPS désactivé
    FETCHING_LOCATION,      // Récupération de la position
    GPS_ERROR,              // Erreur lors de la récupération
    SHOWING_MAP,            // Affichage de la carte et du formulaire
    DISPLAYING_BREAKDOWNS,  // Affichage des pannes (historique)
    ERROR                    // Affichage d'une erreur générique
}

/**
 * Composant d'étape de chargement
 */
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

/**
 * Composant d'étape d'erreur
 */
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

/**
 * Contenu principal du formulaire SOS avec carte
 */
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
     onTypeMenuChange: (Boolean) -> Unit,
     onTypeChange: (String) -> Unit,
     onDescriptionChange: (String) -> Unit,
     onPhotoClick: () -> Unit,
     onSendClick: () -> Unit,
     sendEnabled: Boolean,
     userId: String?,
     tokenPresent: Boolean,
     tokenMasked: String?,
     lastRequestJson: String?,
     lastError: String?
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
        // Bouton SOS principal
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

        // Carte OpenStreetMap (osmdroid) - Gratuite et open source !
        if (latitude != null && longitude != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                OpenStreetMapView(
                    latitude = latitude,
                    longitude = longitude,
                    zoom = 15.0,
                    markerTitle = "Votre position"
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Lat: ${latitude.format(4)}, Lon: ${longitude.format(4)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Type de problème (fallback dropdown: OutlinedTextField + DropdownMenu)
        Box(modifier = Modifier.fillMaxWidth()) {
            // TextField visible
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
                modifier = Modifier
                    .fillMaxWidth(),
                isError = showValidation && type.isBlank()
            )

            // Transparent overlay to ensure taps are always received (fixes cases where TextField consumes clicks)
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        Log.d("BreakdownSOS", "Type field tapped - opening menu")
                        onTypeMenuChange(true)
                    }
            )

            // Use a modal AlertDialog for selection (reliable across devices/emulators)
            if (showTypeMenu) {
                AlertDialog(
                    onDismissRequest = { onTypeMenuChange(false) },
                    title = { Text("Sélectionner le type de panne") },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            types.forEach { typeOption ->
                                TextButton(
                                    onClick = {
                                        Log.d("BreakdownSOS", "Type selected from dialog: $typeOption")
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

            // Inline validation message shown below field if needed
            if (showValidation && type.isBlank()) {
                Text(
                    text = "⚠️ Veuillez sélectionner un type de panne",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
            }
         }

        Spacer(Modifier.height(16.dp))

        // Description
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

        // Photo
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

        // Bouton Envoyer avec validation locale
        Button(
            onClick = {
                if (!sendEnabled) {
                    // trigger validation messages
                    showValidation = true
                    // scroll to top so user sees validation (use coroutine scope)
                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                } else {
                    onSendClick()
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            // keep it clickable so user can ask for validation messages; visually show disabled style
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

        // Message d'erreur (affiché uniquement après tentative d'envoi)
        if (showValidation && !sendEnabled) {
            val missingFields = buildList {
                if (type.isBlank()) add("Type de panne")
                if (latitude == null || longitude == null) add("Localisation GPS")
                // DO NOT require userId here: backend extracts user from JWT
            }
            
            Text(
                text = "⚠️ Champs manquants : ${missingFields.joinToString(", ")}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // Reset validation when fields become valid
        LaunchedEffect(type, latitude, longitude) {
            if (type.isNotBlank() && latitude != null && longitude != null) showValidation = false
        }

        Spacer(Modifier.height(32.dp))

        // Debug panel (developer): show last request and last error to help diagnose 400 responses
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
                    lastError?.let { err ->
                        Text("Dernière erreur:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        Text(err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    // show token status and masked token
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

// Helper: try to read token from EncryptedSharedPreferences, fallback to TokenManager
private fun readAnyToken(context: Context): String? {
    try {
        // 1) Try EncryptedSharedPreferences (primary)
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
                Log.d("BreakdownSOS", "readAnyToken: found token in EncryptedSharedPreferences")
                return tokenEnc
            } else {
                Log.d("BreakdownSOS", "readAnyToken: no token in EncryptedSharedPreferences")
            }
        } catch (e: Exception) {
            Log.w("BreakdownSOS", "readAnyToken: encrypted prefs read failed: ${e.message}")
        }

        // 2) Try plain SharedPreferences with same name (some environments may write unencrypted)
        try {
            val plainPrefs = context.getSharedPreferences("secret_shared_prefs", Context.MODE_PRIVATE)
            val tokenPlain = plainPrefs.getString("jwt_token", null)
            if (!tokenPlain.isNullOrBlank()) {
                Log.d("BreakdownSOS", "readAnyToken: found token in plain SharedPreferences(secret_shared_prefs)")
                return tokenPlain
            }
        } catch (e: Exception) {
            Log.w("BreakdownSOS", "readAnyToken: plain prefs read failed: ${e.message}")
        }

        // 3) Fallback to TokenManager (app-level prefs)
        val tmToken = TokenManager.getInstance(context).getToken()
        if (!tmToken.isNullOrBlank()) {
            Log.d("BreakdownSOS", "readAnyToken: found token in TokenManager prefs")
            return tmToken
        }

        Log.d("BreakdownSOS", "readAnyToken: no token found in any store")
        return null
    } catch (e: Exception) {
        Log.e("BreakdownSOS", "readAnyToken: unexpected error: ${e.message}", e)
        return TokenManager.getInstance(context).getToken()
    }
}

/**
 * Fonction utilitaire pour obtenir la localisation rapidement
 */
@SuppressLint("MissingPermission")
private fun fetchLocation(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocation: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
        if (loc != null) {
            onLocation(loc.latitude, loc.longitude)
        } else {
            // Si lastLocation est null, demander une mise à jour active
            val request = LocationRequest.Builder(1000L)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdates(1)
                .build()
            fusedLocationClient.requestLocationUpdates(
                request,
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val l = result.lastLocation
                        if (l != null) {
                            onLocation(l.latitude, l.longitude)
                        } else {
                            onError("Impossible d'obtenir la position")
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                },
                Looper.getMainLooper()
            )
        }
    }.addOnFailureListener {
        onError("Erreur d'accès à la localisation")
    }
}

// Extension pour formater les doubles
private fun Double.format(digits: Int): String {
    // Clamp digits to a safe range to avoid IllegalFormatPrecisionException
    val d = digits.coerceIn(0, 8)
    return String.format(java.util.Locale.US, "%.${d}f", this)
}

// Helper to decode JWT and extract the "sub" claim without external libs (best-effort)
private fun jwtSubClaim(token: String): String? {
    try {
        var t = token
        // remove Bearer prefix if present
        if (t.startsWith("Bearer ", true)) t = t.substringAfter(" ")
        val parts = t.split('.')
        if (parts.size < 2) return null
        var payload = parts[1]
        // base64url -> base64
        payload = payload.replace('-', '+').replace('_', '/')
        // Pad base64 if necessary
        val padLen = (4 - payload.length % 4) % 4
        payload += "=".repeat(padLen)
        val decoded = Base64.decode(payload, Base64.DEFAULT)
        val json = String(decoded, Charsets.UTF_8)
        val obj = JSONObject(json)
        // common name for user id claim could be 'sub' or 'userId' depending on backend
        if (obj.has("sub")) return obj.getString("sub")
        if (obj.has("userId")) return obj.getString("userId")
        if (obj.has("id")) return obj.getString("id")
        // some tokens include a nested user object: { user: { id: '...' } }
        if (obj.has("user")) {
            try {
                val u = obj.get("user")
                if (u is JSONObject && u.has("id")) return u.getString("id")
            } catch (_: Exception) { /* ignore */ }
        }
        return null
    } catch (e: Exception) {
        Log.w("BreakdownSOS", "jwtSubClaim parse failed: ${e.message}")
        return null
    }
}
