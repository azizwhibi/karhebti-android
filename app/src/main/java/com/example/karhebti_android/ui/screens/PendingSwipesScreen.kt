package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.api.SwipeResponse
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.viewmodel.MarketplaceViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingSwipesScreen(
    onBackClick: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: MarketplaceViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val pendingSwipes by viewModel.pendingSwipes.observeAsState()
    val swipeResponseResult by viewModel.swipeResponseResult.observeAsState()
    val realtimeNotification by viewModel.realtimeNotification.observeAsState()

    var showChatDialog by remember { mutableStateOf(false) }
    var chatConversationId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadPendingSwipes()
        viewModel.connectWebSocket()
    }

    // Handle swipe response result
    LaunchedEffect(swipeResponseResult) {
        when (swipeResponseResult) {
            is Resource.Success -> {
                val result = (swipeResponseResult as Resource.Success).data
                if (result?.status == "accepted" && result.conversationId != null) {
                    chatConversationId = result.conversationId
                    showChatDialog = true
                }
                // Reload pending swipes
                viewModel.loadPendingSwipes()
            }
            else -> {}
        }
    }

    // Handle new swipe notifications
    LaunchedEffect(realtimeNotification) {
        realtimeNotification?.let {
            if (it.type == "swipe_right") {
                viewModel.loadPendingSwipes()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnectWebSocket()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pending Requests") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPendingSwipes() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (pendingSwipes) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    val swipes = (pendingSwipes as Resource.Success).data ?: emptyList()
                    if (swipes.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No pending requests",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "You'll see requests here when buyers are interested in your cars",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(swipes) { swipe ->
                                PendingSwipeCard(
                                    swipe = swipe,
                                    onAccept = { viewModel.acceptSwipe(swipe.id) },
                                    onDecline = { viewModel.declineSwipe(swipe.id) }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Error loading requests",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.loadPendingSwipes() }) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
                else -> {}
            }

            // Chat dialog
            if (showChatDialog && chatConversationId != null) {
                AlertDialog(
                    onDismissRequest = { showChatDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = { Text("Request Accepted!") },
                    text = { Text("You can now chat with the buyer about your car.") },
                    confirmButton = {
                        Button(onClick = {
                            showChatDialog = false
                            chatConversationId?.let { onNavigateToChat(it) }
                        }) {
                            Text("Start Chat")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showChatDialog = false }) {
                            Text("Later")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PendingSwipeCard(
    swipe: SwipeResponse,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Someone is interested!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Buyer ID: ${swipe.buyerId.take(8.coerceAtMost(swipe.buyerId.length))}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatSwipeTime(swipe.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isProcessing = true
                        onDecline()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline")
                }

                Button(
                    onClick = {
                        isProcessing = true
                        onAccept()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
            }
        }
    }
}

fun formatSwipeTime(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}
