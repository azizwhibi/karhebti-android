package com.example.karhebti_android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.CreateDocumentRequest
import com.example.karhebti_android.data.api.UpdateDocumentRequest
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.viewmodel.CarViewModel
import com.example.karhebti_android.viewmodel.DocumentViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentScreen(
    onBackClick: () -> Unit,
    documentId: String? = null
) {
    val context = LocalContext.current
    val documentViewModel: DocumentViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val carViewModel: CarViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    val isEditMode = documentId != null

    // Document fields state
    var selectedType by remember { mutableStateOf("") }
    var dateEmission by remember { mutableStateOf("") }
    var dateExpiration by remember { mutableStateOf("") }
    var selectedCarId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Dropdown states
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var carMenuExpanded by remember { mutableStateOf(false) }

    // Data states
    val carsState by carViewModel.carsState.observeAsState()
    val documentTypes = listOf("Assurance", "Carte Grise", "Contrôle Technique", "Autre")

    // State for fetching document details in edit mode
    val documentDetailState by documentViewModel.documentDetailState.observeAsState()

    // Fetch cars and document details on screen launch
    LaunchedEffect(key1 = documentId) {
        carViewModel.getMyCars()
        if (isEditMode) {
            documentViewModel.getDocumentById(documentId!!)
        }
    }

    // Pre-fill form in edit mode
    LaunchedEffect(documentDetailState) {
        if (isEditMode) {
            (documentDetailState as? Resource.Success)?.data?.let { doc ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedType = doc.type.replace("_", " ").replaceFirstChar { it.titlecase(Locale.getDefault()) }
                dateEmission = sdf.format(doc.dateEmission)
                dateExpiration = sdf.format(doc.dateExpiration)
                selectedCarId = doc.voiture
            }
        }
    }

    // Observe creation/update state
    val createDocumentState by documentViewModel.createDocumentState.observeAsState()
    val updateDocumentState by documentViewModel.updateDocumentState.observeAsState()

    LaunchedEffect(createDocumentState, updateDocumentState) {
        val state = if (isEditMode) updateDocumentState else createDocumentState
        when (state) {
            is Resource.Success -> {
                isLoading = false
                val message = if (isEditMode) "Document modifié avec succès" else "Document ajouté avec succès"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                onBackClick()
            }
            is Resource.Error -> {
                isLoading = false
                Toast.makeText(context, "Erreur: ${state.message}", Toast.LENGTH_LONG).show()
            }
            is Resource.Loading -> {
                isLoading = true
            }
            null -> {}
        }
    }

    val filePath = "/path/to/dummy/file.pdf" // Replace with actual file picker logic

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Modifier le Document" else "Ajouter un Document") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form fields...
            ExposedDropdownMenuBox(expanded = typeMenuExpanded, onExpandedChange = { if (!isLoading) typeMenuExpanded = !typeMenuExpanded }) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type de document") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    enabled = !isLoading
                )
                ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    documentTypes.forEach { type ->
                        DropdownMenuItem(text = { Text(type) }, onClick = { selectedType = type; typeMenuExpanded = false })
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = carMenuExpanded, onExpandedChange = { if (!isLoading) carMenuExpanded = !carMenuExpanded }) {
                val selectedCarText = when (val state = carsState) {
                    is Resource.Success -> state.data?.find { it.id == selectedCarId }?.let { "${it.marque} ${it.modele}" } ?: ""
                    else -> "Chargement..."
                }
                OutlinedTextField(
                    value = selectedCarText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Véhicule") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = carMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    enabled = !isLoading && !isEditMode // Disable car change in edit mode for simplicity
                )
                if (!isEditMode) {
                    ExposedDropdownMenu(expanded = carMenuExpanded, onDismissRequest = { carMenuExpanded = false }) {
                        (carsState as? Resource.Success)?.data?.forEach { car ->
                            DropdownMenuItem(text = { Text("${car.marque} ${car.modele}") }, onClick = { selectedCarId = car.id; carMenuExpanded = false })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = dateEmission,
                onValueChange = { dateEmission = it },
                label = { Text("Date d\'émission (AAAA-MM-JJ)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = dateExpiration,
                onValueChange = { dateExpiration = it },
                label = { Text("Date d\'expiration (AAAA-MM-JJ)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isEditMode) {
                        val request = UpdateDocumentRequest(
                            type = selectedType.lowercase(),
                            dateEmission = dateEmission,
                            dateExpiration = dateExpiration
                        )
                        documentViewModel.updateDocument(documentId!!, request)
                    } else {
                        val request = CreateDocumentRequest(
                            type = selectedType.lowercase(),
                            dateEmission = dateEmission,
                            dateExpiration = dateExpiration,
                            fichier = filePath,
                            voiture = selectedCarId!!
                        )
                        documentViewModel.createDocument(request)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedType.isNotBlank() && dateEmission.isNotBlank() && dateExpiration.isNotBlank() && (isEditMode || selectedCarId != null) && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isEditMode) "Enregistrer les modifications" else "Enregistrer")
                }
            }
        }
    }
}
