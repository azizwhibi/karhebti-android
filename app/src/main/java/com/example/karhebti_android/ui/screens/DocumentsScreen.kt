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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.karhebti_android.data.model.Document
import com.example.karhebti_android.data.model.DocumentType
import com.example.karhebti_android.ui.theme.*
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("Tous") }
    val filters = listOf("Tous", "Administratif", "Entretien")

    // Sample data
    val documents = remember {
        listOf(
            Document(
                id = "1",
                vehicleId = "1",
                vehicleName = "Renault Clio",
                name = "Assurance Auto 2024",
                type = DocumentType.ADMINISTRATIF,
                subtype = "Assurance",
                uploadDate = Date(),
                expiryDate = Date(),
                fileUrl = "https://example.com/doc1.pdf",
                fileSize = 2048576,
                mimeType = "application/pdf"
            ),
            Document(
                id = "2",
                vehicleId = "1",
                vehicleName = "Renault Clio",
                name = "Carte grise",
                type = DocumentType.ADMINISTRATIF,
                subtype = "Carte grise",
                uploadDate = Date(),
                fileUrl = "https://example.com/doc2.pdf",
                fileSize = 1024000,
                mimeType = "application/pdf"
            ),
            Document(
                id = "3",
                vehicleId = "1",
                vehicleName = "Renault Clio",
                name = "Facture vidange",
                type = DocumentType.ENTRETIEN,
                subtype = "Facture",
                uploadDate = Date(),
                fileUrl = "https://example.com/doc3.pdf",
                fileSize = 512000,
                mimeType = "application/pdf"
            )
        )
    }

    val filteredDocuments = if (selectedFilter == "Tous") {
        documents
    } else {
        documents.filter {
            it.type.name == selectedFilter.uppercase()
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
                    IconButton(onClick = { /* Upload document */ }) {
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
                onClick = { /* Upload document */ },
                containerColor = DeepPurple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.CloudUpload, "Téléverser")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDocuments) { document ->
                    DocumentCard(document = document)
                }
            }
        }
    }
}

@Composable
fun DocumentCard(document: Document) {
    val icon = when (document.type) {
        DocumentType.ADMINISTRATIF -> Icons.Default.Article
        DocumentType.ENTRETIEN -> Icons.Default.Build
        DocumentType.FACTURE -> Icons.Default.Receipt
        DocumentType.AUTRE -> Icons.Default.Article
    }

    val typeColor = when (document.type) {
        DocumentType.ADMINISTRATIF -> DeepPurple
        DocumentType.ENTRETIEN -> AccentGreen
        DocumentType.FACTURE -> AccentYellow
        DocumentType.AUTRE -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Document Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = document.subtype,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = document.vehicleName,
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepPurple
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = formatFileSize(document.fileSize),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Download Button
            IconButton(
                onClick = { /* Download document */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DeepPurple.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Télécharger",
                    tint = DeepPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

@Preview(showBackground = true)
@Composable
fun DocumentsScreenPreview() {
    KarhebtiandroidTheme {
        DocumentsScreen()
    }
}
