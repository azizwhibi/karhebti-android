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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.DocumentResponse
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.*
import com.example.karhebti_android.viewmodel.DocumentViewModel
import com.example.karhebti_android.viewmodel.CarViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

// Backend-Integrated DocumentsScreen
// All document data from API, Upload/Delete operations call backend
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val documentViewModel: DocumentViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val carViewModel: CarViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    // Observe states
    val documentsState by documentViewModel.documentsState.observeAsState()
    val createDocumentState by documentViewModel.createDocumentState.observeAsState()
    val carsState by carViewModel.carsState.observeAsState()

    var selectedFilter by remember { mutableStateOf("Tous") }
    val filters = listOf("Tous", "assurance", "carte grise", "contrôle technique")
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<DocumentResponse?>(null) }

    // Load data on screen start
    LaunchedEffect(Unit) {
        documentViewModel.getDocuments()
        carViewModel.getMyCars()
    }

    // Handle create result
    LaunchedEffect(createDocumentState) {
        when (createDocumentState) {
            is Resource.Success -> {
                showAddDialog = false
            }
            else -> {}
        }
    }

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
                    IconButton(onClick = { documentViewModel.getDocuments() }) {
                        Icon(Icons.Default.Refresh, "Actualiser", tint = Color.White)
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Ajouter", tint = Color.White)
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
                onClick = { showAddDialog = true },
                containerColor = DeepPurple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Téléverser")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
        ) {
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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

            // Documents List
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = documentsState) {
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
                                Text("Chargement des documents...", color = TextSecondary)
                            }
                        }
                    }
                    is Resource.Success -> {
                        val allDocuments = state.data ?: emptyList()
                        val filteredDocuments = if (selectedFilter == "Tous") {
                            allDocuments
                        } else {
                            allDocuments.filter { it.type.lowercase() == selectedFilter.lowercase() }
                        }

                        if (filteredDocuments.isEmpty()) {
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
                                        imageVector = Icons.Default.Article,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = TextSecondary.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        "Aucun document",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "Ajoutez vos documents de véhicule",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    Button(
                                        onClick = { showAddDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepPurple)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Ajouter un document")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredDocuments, key = { it.id }) { document ->
                                    DocumentCardBackendIntegrated(
                                        document = document,
                                        onDelete = { showDeleteDialog = document }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
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
                                    state.message ?: "Une erreur est survenue",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Button(
                                    onClick = { documentViewModel.getDocuments() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepPurple)
                                ) {
                                    Icon(Icons.Default.Refresh, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Réessayer")
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    // Add document dialog
    if (showAddDialog) {
        AddDocumentDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { type, dateEmission, dateExpiration, fichier, voitureId ->
                documentViewModel.createDocument(type, dateEmission, dateExpiration, fichier, voitureId)
            },
            createState = createDocumentState,
            carsState = carsState
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { document ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Supprimer le document ?") },
            text = { Text("Voulez-vous vraiment supprimer ${document.type} ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        documentViewModel.deleteDocument(document.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AlertRed)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun DocumentCardBackendIntegrated(
    document: DocumentResponse,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    // Check expiry status
    val isExpired = document.dateExpiration.before(Date())
    val daysUntilExpiry = ((document.dateExpiration.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
    val expiryColor = when {
        isExpired -> AlertRed
        daysUntilExpiry <= 30 -> AccentYellow
        else -> AccentGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .background(DeepPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = null,
                    tint = DeepPurple,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.type,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                document.voiture?.let { car ->
                    Text(
                        text = "${car.marque} ${car.modele}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = expiryColor
                    )
                    Text(
                        text = "Expire: ${dateFormat.format(document.dateExpiration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = expiryColor
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "Menu", tint = TextSecondary)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Télécharger") },
                        onClick = {
                            showMenu = false
                            // Open document URL
                        },
                        leadingIcon = { Icon(Icons.Default.Download, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Supprimer", color = AlertRed) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = AlertRed) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String) -> Unit,
    createState: Resource<DocumentResponse>?,
    carsState: Resource<*>?
) {
    var type by remember { mutableStateOf("assurance") }
    var dateEmission by remember { mutableStateOf("") }
    var dateExpiration by remember { mutableStateOf("") }
    var fichier by remember { mutableStateOf("") }
    var selectedCarId by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf(false) }

    val types = listOf("assurance", "carte grise", "contrôle technique")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau document") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        types.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    type = item
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dateEmission,
                    onValueChange = { dateEmission = it },
                    label = { Text("Date d'émission (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateExpiration,
                    onValueChange = { dateExpiration = it },
                    label = { Text("Date d'expiration (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fichier,
                    onValueChange = { fichier = it },
                    label = { Text("URL du fichier") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = selectedCarId,
                    onValueChange = { selectedCarId = it },
                    label = { Text("ID Voiture") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                when (createState) {
                    is Resource.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    is Resource.Error -> {
                        Text(
                            text = createState.message ?: "Erreur",
                            color = AlertRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (type.isNotBlank() && dateEmission.isNotBlank() &&
                        dateExpiration.isNotBlank() && fichier.isNotBlank() && selectedCarId.isNotBlank()) {
                        onAdd(type, dateEmission, dateExpiration, fichier, selectedCarId)
                    }
                },
                enabled = createState !is Resource.Loading &&
                         type.isNotBlank() && dateEmission.isNotBlank() &&
                         dateExpiration.isNotBlank() && fichier.isNotBlank() && selectedCarId.isNotBlank()
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = createState !is Resource.Loading
            ) {
                Text("Annuler")
            }
        }
    )
}
