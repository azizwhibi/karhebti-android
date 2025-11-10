package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.*
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.viewmodel.DocumentViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val documentViewModel: DocumentViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    val echeancesState by documentViewModel.echeancesState.observeAsState()

    var showAddEcheanceDialog by remember { mutableStateOf(false) }
    var echeanceToEdit by remember { mutableStateOf<EcheanceResponse?>(null) }

    LaunchedEffect(documentId) {
        documentViewModel.getEcheancesForDocument(documentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Échéances du Document") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") }
                },
                actions = {
                    IconButton(onClick = { documentViewModel.getEcheancesForDocument(documentId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddEcheanceDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une échéance")
            }
        }
    ) { paddingValues ->
        
        if (showAddEcheanceDialog) {
            ManageEcheanceDialog(
                documentId = documentId,
                onDismiss = { showAddEcheanceDialog = false },
                onConfirm = { request: CreateEcheanceRequest ->
                    documentViewModel.createEcheance(request)
                    showAddEcheanceDialog = false
                }
            )
        }

        echeanceToEdit?.let { echeance ->
            ManageEcheanceDialog(
                echeance = echeance,
                documentId = documentId,
                onDismiss = { echeanceToEdit = null },
                onConfirm = { request ->
                    documentViewModel.updateEcheance(echeance.id, request, documentId)
                    echeanceToEdit = null
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val resource = echeancesState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is Resource.Success -> {
                    val echeances = resource.data
                    if (echeances.isNullOrEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Aucune échéance pour ce document.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(echeances, key = { it.id }) { echeance ->
                                EcheanceCard(
                                    echeance = echeance,
                                    onDelete = { documentViewModel.deleteEcheance(echeance.id, documentId) },
                                    onEdit = { echeanceToEdit = echeance }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(resource.message ?: "Erreur de chargement") }
                null -> {}
            }
        }
    }
}


@Composable
private fun EcheanceCard(
    echeance: EcheanceResponse, 
    onDelete: () -> Unit, 
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(echeance.description, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text("Le ${SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(echeance.dateEcheance)}", style = MaterialTheme.typography.bodyMedium)
            }
            AssistChip(
                onClick = {},
                label = { Text(if (echeance.estTerminee) "Terminée" else "En cours") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (echeance.estTerminee) Color.Green.copy(alpha = 0.2f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                    labelColor = if (echeance.estTerminee) Color.Green else MaterialTheme.colorScheme.tertiary
                )
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Modifier") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = {
                        onEdit()
                        showMenu = false
                    })
                    DropdownMenuItem(text = { Text("Supprimer") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = {
                        onDelete()
                        showMenu = false
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageEcheanceDialog(
    echeance: EcheanceResponse? = null,
    documentId: String,
    onDismiss: () -> Unit,
    onConfirm: (UpdateEcheanceRequest) -> Unit
) {
    var description by remember { mutableStateOf(echeance?.description ?: "") }
    val initialDate = remember { echeance?.dateEcheance?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) } ?: "" }
    var date by remember { mutableStateOf(initialDate) }
    var isTerminee by remember { mutableStateOf(echeance?.estTerminee ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (echeance == null) "Ajouter une Échéance" else "Modifier l'Échéance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (AAAA-MM-JJ)") },
                     modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Terminée")
                    Spacer(Modifier.weight(1f))
                    Switch(checked = isTerminee, onCheckedChange = { isTerminee = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val request = UpdateEcheanceRequest(
                    description = description.takeIf { it != echeance?.description },
                    dateEcheance = date.takeIf { it != initialDate },
                    estTerminee = isTerminee.takeIf { it != echeance?.estTerminee }
                )
                onConfirm(request)
            }) {
                Text(if (echeance == null) "Ajouter" else "Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

// Overload for creating a new Echeance
@Composable
private fun ManageEcheanceDialog(
    documentId: String,
    onDismiss: () -> Unit,
    onConfirm: (CreateEcheanceRequest) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var isTerminee by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une Échéance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (AAAA-MM-JJ)") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Terminée")
                    Spacer(Modifier.weight(1f))
                    Switch(checked = isTerminee, onCheckedChange = { isTerminee = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(CreateEcheanceRequest(documentId, date, description, isTerminee))
            }) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
