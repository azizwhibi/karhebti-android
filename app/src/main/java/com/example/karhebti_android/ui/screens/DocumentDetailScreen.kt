package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.viewmodel.DocumentViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val context = LocalContext.current
    val documentViewModel: DocumentViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    val documentDetailState by documentViewModel.documentDetailState.observeAsState()

    LaunchedEffect(documentId) {
        documentViewModel.getDocumentById(documentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du Document") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") }
                },
                actions = {
                    IconButton(onClick = { onEditClick(documentId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier le document")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val resource = documentDetailState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is Resource.Success -> {
                    val document = resource.data
                    if (document != null) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = document.type, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Expire le: ${document.dateExpiration}")
                        }
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Document non trouvé") }
                    }
                }
                is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(resource.message ?: "Erreur de chargement") }
                null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Document non trouvé") }
            }
        }
    }
}

