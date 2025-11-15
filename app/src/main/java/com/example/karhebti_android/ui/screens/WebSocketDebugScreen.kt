package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.karhebti_android.data.preferences.TokenManager
import com.example.karhebti_android.data.websocket.WebSocketService
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebSocketDebugScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = TokenManager.getInstance(context)

    var isConnected by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var connectionLog by remember { mutableStateOf<List<String>>(emptyList()) }

    val webSocketService = remember {
        WebSocketService.getInstance("http://your-backend-url")
    }

    fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        connectionLog = connectionLog + "[$timestamp] $message"
    }

    LaunchedEffect(Unit) {
        val listener = object : WebSocketService.NotificationListener {
            override fun onNotificationReceived(notification: Map<String, Any>) {
                notifications = notifications + notification
                addLog("📬 Notification reçue: ${notification["titre"] ?: "Sans titre"}")
            }

            override fun onConnectionChanged(isConnectedState: Boolean) {
                isConnected = isConnectedState
                if (isConnectedState) {
                    addLog("✅ Connecté au serveur WebSocket")
                } else {
                    addLog("❌ Déconnecté du serveur WebSocket")
                }
            }
        }
        webSocketService.addListener(listener)
        val token = tokenManager.getToken()
        if (token != null) {
            webSocketService.connect(token)
            addLog("🔄 Tentative de connexion...")
        } else {
            addLog("❌ Token non trouvé")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WebSocket Debug") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
            // Connection Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isConnected) "Connecté" else "Déconnecté",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val token = tokenManager.getToken()
                        if (token != null) {
                            webSocketService.connect(token)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isConnected
                ) {
                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Connecter")
                }

                Button(
                    onClick = { webSocketService.disconnect() },
                    modifier = Modifier.weight(1f),
                    enabled = isConnected
                ) {
                    Icon(Icons.Default.CloudOff, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Déconnecter")
                }
            }

            // Notifications Count
            Text(
                text = "Notifications reçues: ${notifications.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Tabs
            var selectedTab by remember { mutableStateOf(0) }
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Logs") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Notifications (${notifications.size})") }
                )
            }

            // Content
            when (selectedTab) {
                0 -> {
                    // Logs
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                color = Color(0xFF263238),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(connectionLog) { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
                1 -> {
                    // Notifications
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notification ->
                            NotificationDebugCard(notification)
                        }
                    }
                }
            }

            // Clear Button
            Button(
                onClick = {
                    notifications = emptyList()
                    connectionLog = emptyList()
                    addLog("🗑️ Données effacées")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Effacer tout")
            }
        }
    }
}

@Composable
fun NotificationDebugCard(notification: Map<String, Any>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = notification["titre"]?.toString() ?: "Sans titre",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = notification["message"]?.toString() ?: "Pas de message",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Type: ${notification["type"]?.toString() ?: "Unknown"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
