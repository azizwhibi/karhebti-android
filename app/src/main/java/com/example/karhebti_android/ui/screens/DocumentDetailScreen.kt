package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.viewmodel.DocumentViewModel
import com.example.karhebti_android.viewmodel.CarViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

fun fixEmulatorImageUrl(url: String?): String? {
    // Pas besoin de remplacer localhost pour Render
    return url
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val documentViewModel: DocumentViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val carViewModel: CarViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    val documentDetailState by documentViewModel.documentDetailState.observeAsState()
    val carsState by carViewModel.carsState.observeAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    LaunchedEffect(documentId) {
        android.util.Log.d("DocumentDetailScreen", "Loading document with ID: $documentId")
        documentViewModel.getDocumentById(documentId)
        carViewModel.getMyCars()
    }

    LaunchedEffect(documentDetailState) {
        when (val state = documentDetailState) {
            is Resource.Success -> android.util.Log.d("DocumentDetailScreen", "Document loaded: ${state.data?.type}")
            is Resource.Error -> android.util.Log.e("DocumentDetailScreen", "Error: ${state.message}")
            is Resource.Loading -> android.util.Log.d("DocumentDetailScreen", "Loading...")
            null -> android.util.Log.d("DocumentDetailScreen", "State is null")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du Document") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(documentId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val resource = documentDetailState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val document = resource.data
                if (document != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        val baseUrl = "https://karhebti-backend-supa.onrender.com"
                        val supabaseUrl = "https://tyhficbnlzwhovbfcflk.supabase.co/storage/v1/object/public"

                        // Build list of possible image URLs to try
                        val possibleImageUrls = mutableListOf<String>()

                        when {
                            document.fichier.isBlank() -> {
                                // No image
                            }
                            document.fichier.startsWith("http://") || document.fichier.startsWith("https://") -> {
                                // Already a full URL
                                possibleImageUrls.add(document.fichier)
                            }
                            else -> {
                                // Try multiple possible URLs
                                val filename = document.fichier.removePrefix("/uploads/documents/")
                                    .removePrefix("/uploads/")
                                    .removePrefix("/")

                                // 1. Try Supabase Storage (like car images) - MOST LIKELY TO WORK
                                possibleImageUrls.add("$supabaseUrl/documents/$filename")
                                possibleImageUrls.add("$supabaseUrl/document-images/$filename")
                                possibleImageUrls.add("$supabaseUrl/documents%20images/$filename")

                                // 2. Try direct path (what backend sends)
                                if (document.fichier.startsWith("/")) {
                                    possibleImageUrls.add("$baseUrl${document.fichier}")
                                }

                                // 3. Try API endpoint (in case static files aren't served)
                                possibleImageUrls.add("$baseUrl/api/documents/${document.id}/file")

                                // 4. Try with /uploads/documents/ prefix
                                if (!document.fichier.startsWith("/uploads/")) {
                                    possibleImageUrls.add("$baseUrl/uploads/documents/$filename")
                                }

                                // 5. Try root level
                                possibleImageUrls.add("$baseUrl/$filename")
                            }
                        }

                        // Use first URL as primary, others as fallbacks
                        val imageUrl = possibleImageUrls.firstOrNull()
                        val fixedImageUrl = fixEmulatorImageUrl(imageUrl)

                        // Debug log for image URLs
                        android.util.Log.d("DocumentDetailScreen", "🖼️ Primary Image URL: $fixedImageUrl")
                        android.util.Log.d("DocumentDetailScreen", "📄 Document fichier: ${document.fichier}")
                        android.util.Log.d("DocumentDetailScreen", "🔄 Fallback URLs available: ${possibleImageUrls.size}")
                        possibleImageUrls.forEachIndexed { index, url ->
                            android.util.Log.d("DocumentDetailScreen", "   $index: $url")
                        }

                        // Track which URL we're currently trying
                        var currentUrlIndex by remember { mutableStateOf(0) }
                        var allUrlsFailed by remember { mutableStateOf(false) }
                        val currentImageUrl = if (currentUrlIndex < possibleImageUrls.size && !allUrlsFailed) {
                            possibleImageUrls[currentUrlIndex]
                        } else {
                            null
                        }

                        // Only show image card if we have URLs to try and not all failed
                        if (possibleImageUrls.isNotEmpty() && !allUrlsFailed) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Image du document",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp)
                                )

                                if (currentImageUrl != null) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(currentImageUrl)
                                            .crossfade(true)
                                            .listener(
                                                onStart = {
                                                    android.util.Log.d("DocumentDetailScreen", "🔄 Image loading started: $currentImageUrl")
                                                },
                                                onSuccess = { _, _ ->
                                                    android.util.Log.d("DocumentDetailScreen", "✅ Image loaded successfully from: $currentImageUrl")
                                                },
                                                onError = { _, result ->
                                                    android.util.Log.e("DocumentDetailScreen", "❌ Image load error from $currentImageUrl: ${result.throwable.message}")

                                                    // Try next URL if available
                                                    if (currentUrlIndex < possibleImageUrls.size - 1) {
                                                        android.util.Log.w("DocumentDetailScreen", "🔄 Trying fallback URL ${currentUrlIndex + 1}...")
                                                        currentUrlIndex++
                                                    } else {
                                                        android.util.Log.e("DocumentDetailScreen", "❌ All ${possibleImageUrls.size} URLs failed - hiding image section")
                                                        allUrlsFailed = true
                                                    }
                                                }
                                            )
                                            .build(),
                                        contentDescription = "Image du document",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        loading = {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        "Chargement...",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (currentUrlIndex > 0) {
                                                        Text(
                                                            "Tentative ${currentUrlIndex + 1}/${possibleImageUrls.size}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        error = {
                                            // Error will trigger the flag to hide this card
                                            // This lambda is required by Coil but won't be displayed
                                            Box(modifier = Modifier.size(1.dp))
                                        }
                                    )
                                }
                            }
                        }
                    }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Type de document",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        document.type.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Date d'émission",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        dateFormat.format(document.dateEmission),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Event,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Date d'expiration",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        dateFormat.format(document.dateExpiration),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        document.voiture?.let { voitureId ->
                            (carsState as? Resource.Success)?.data?.find { it.id == voitureId }?.let { car ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                "Véhicule",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "Marque & Modèle",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                "${car.marque} ${car.modele}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "Année",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                car.annee.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "Immatriculation",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                car.immatriculation,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }

                                        car.kilometrage?.let { km ->
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    "Kilométrage",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    "$km km",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Document introuvable")
                    }
                }
            }
            is Resource.Error -> {
                val errorMessage = resource.message ?: "Une erreur est survenue"
                // Détecter si c'est une erreur liée à des données corrompues
                val isCorruptedData = errorMessage.contains("données corrompues", ignoreCase = true) ||
                                     errorMessage.contains("structure invalide", ignoreCase = true) ||
                                     errorMessage.contains("champ \"voiture\"", ignoreCase = true) ||
                                     errorMessage.contains("Erreur 500", ignoreCase = true) ||
                                     errorMessage.contains("Internal server error", ignoreCase = true)

                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Icon(
                            if (isCorruptedData) Icons.Default.WarningAmber else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isCorruptedData) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (isCorruptedData) "⚠️ Document Corrompu Détecté" else "Erreur lors du chargement",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorruptedData) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isCorruptedData) {
                            // Message d'explication pour les données corrompues
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "🔴 Problème de Base de Données",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Ce document contient des données corrompues dans la base de données MongoDB. Le serveur backend (NestJS) ne peut pas traiter ce document en raison d'une structure de données invalide.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "🔍 Cause du problème",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Le champ 'voiture' contient une structure d'objet complexe au lieu d'un simple ID de référence. MongoDB ne peut pas le traiter correctement.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "✅ Solutions disponibles",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "1. Supprimer ce document (recommandé)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "2. Contacter l'admin pour réparer la BDD",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "3. Utiliser le script cleanup_corrupted_documents.js",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Document ID: $documentId",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isCorruptedData) {
                            // Bouton de suppression pour les documents corrompus
                            var showDeleteConfirm by remember { mutableStateOf(false) }

                            Button(
                                onClick = { showDeleteConfirm = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Supprimer le document corrompu")
                            }

                            if (showDeleteConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirm = false },
                                    icon = {
                                        Icon(Icons.Default.Warning, contentDescription = null)
                                    },
                                    title = {
                                        Text("⚠️ Confirmer la suppression")
                                    },
                                    text = {
                                        Column {
                                            Text("Êtes-vous sûr de vouloir supprimer ce document corrompu ?")
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Cette action est irréversible. Le document ne pourra pas être récupéré.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                documentViewModel.deleteDocument(documentId)
                                                showDeleteConfirm = false
                                                onBackClick()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("Oui, supprimer")
                                        }
                                    },
                                    dismissButton = {
                                        OutlinedButton(onClick = { showDeleteConfirm = false }) {
                                            Text("Annuler")
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            Button(
                                onClick = {
                                    documentViewModel.getDocumentById(documentId)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Réessayer")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retour à la liste")
                        }
                    }
                }
            }
            null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
