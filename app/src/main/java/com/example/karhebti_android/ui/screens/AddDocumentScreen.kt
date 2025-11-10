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
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.viewmodel.CarViewModel
import com.example.karhebti_android.viewmodel.DocumentViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val documentViewModel: DocumentViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )
    val carViewModel: CarViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

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
    val createDocumentState by documentViewModel.createDocumentState.observeAsState()
    val documentTypes = listOf("Assurance", "Carte Grise", "Contrôle Technique", "Autre")

    // Fetch cars on screen launch
    LaunchedEffect(Unit) {
        carViewModel.getMyCars()
    }

    // Observe creation state
    LaunchedEffect(createDocumentState) {
        when (val state = createDocumentState) {
            is Resource.Success -> {
                isLoading = false
                Toast.makeText(context, "Document ajouté avec succès", Toast.LENGTH_SHORT).show()
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

    // Dummy file path, replace with actual file picker logic
    val filePath = "/path/to/dummy/file.pdf"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un Document") },
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

            // Document Type Dropdown
            ExposedDropdownMenuBox(expanded = typeMenuExpanded, onExpandedChange = { if (!isLoading) typeMenuExpanded = !typeMenuExpanded }) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type de document") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    enabled = !isLoading
                )
                ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    documentTypes.forEach {
                        type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                typeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Car Selection Dropdown
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
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    enabled = !isLoading
                )
                ExposedDropdownMenu(expanded = carMenuExpanded, onDismissRequest = { carMenuExpanded = false }) {
                    if (carsState is Resource.Success) {
                        (carsState as Resource.Success).data?.forEach { car ->
                            DropdownMenuItem(
                                text = { Text("${car.marque} ${car.modele}") },
                                onClick = {
                                    selectedCarId = car.id
                                    carMenuExpanded = false
                                }
                            )
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
                    val request = CreateDocumentRequest(
                        type = selectedType.lowercase().replace(" ", "_"),
                        dateEmission = dateEmission,
                        dateExpiration = dateExpiration,
                        fichier = filePath,
                        voiture = selectedCarId!!
                    )
                    documentViewModel.createDocument(request)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedType.isNotBlank() && dateEmission.isNotBlank() && dateExpiration.isNotBlank() && selectedCarId != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Enregistrer")
                }
            }
        }
    }
}
