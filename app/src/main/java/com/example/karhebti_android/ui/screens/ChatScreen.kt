package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.karhebti_android.data.api.ChatMessage
import com.example.karhebti_android.data.preferences.TokenManager
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val currentUserId = tokenManager.getUserId()

    // Use singleton ChatViewModel
    val viewModel = remember { ChatViewModel.getInstance(context.applicationContext as android.app.Application) }

    val conversation by viewModel.currentConversation.observeAsState()
    val messages by viewModel.messages.observeAsState()
    val realtimeMessage by viewModel.realtimeMessage.observeAsState()
    val userTyping by viewModel.userTyping.observeAsState()
    val isConnected by viewModel.isWebSocketConnected.observeAsState(false)

    var messageText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Track message count for auto-scroll detection
    var lastMessageCount by remember { mutableStateOf(0) }

    // Load conversation and messages on first launch
    LaunchedEffect(conversationId) {
        android.util.Log.d("ChatScreen", "Loading conversation: $conversationId")
        viewModel.loadConversation(conversationId)
        viewModel.loadMessages(conversationId)
        viewModel.connectWebSocket()
        viewModel.joinConversation(conversationId)
        viewModel.markConversationAsRead(conversationId)
    }

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages) {
        val messageList = (messages as? Resource.Success)?.data
        val newCount = messageList?.size ?: 0

        if (newCount > 0) {
            if (lastMessageCount == 0) {
                // Initial load - scroll to bottom
                lastMessageCount = newCount
                android.util.Log.d("ChatScreen", "📨 Initial load: $newCount messages")
                scope.launch {
                    listState.scrollToItem(newCount - 1)
                }
            } else if (newCount > lastMessageCount) {
                // New messages detected - animate scroll
                android.util.Log.d("ChatScreen", "🆕 New messages! $lastMessageCount → $newCount")
                lastMessageCount = newCount
                scope.launch {
                    listState.animateScrollToItem(newCount - 1)
                }
            }
        }
    }

    // Handle real-time message via WebSocket - IMPROVED
    LaunchedEffect(realtimeMessage) {
        realtimeMessage?.let { newMessage ->
            android.util.Log.d("ChatScreen", "⚡ Real-time message received: ${newMessage.id} for conversation: ${newMessage.conversationId}")
            android.util.Log.d("ChatScreen", "Current conversation: $conversationId")

            if (newMessage.conversationId == conversationId) {
                android.util.Log.d("ChatScreen", "✅ Message is for current conversation, handling...")

                // Wait a bit for ViewModel to add the message to the list
                kotlinx.coroutines.delay(50)

                // Force scroll to bottom
                val messageList = (messages as? Resource.Success)?.data
                if (!messageList.isNullOrEmpty()) {
                    android.util.Log.d("ChatScreen", "Scrolling to message ${messageList.size}")
                    scope.launch {
                        listState.animateScrollToItem(messageList.size - 1)
                    }
                } else {
                    android.util.Log.w("ChatScreen", "⚠️ Message list is empty or null after receiving real-time message")
                }
            } else {
                android.util.Log.d("ChatScreen", "Message is for different conversation: ${newMessage.conversationId}")
            }

            // Don't clear immediately - let it stay for a bit to ensure UI processes it
            kotlinx.coroutines.delay(500)
            viewModel.clearRealtimeMessage()
        }
    }

    // Handle typing indicator
    LaunchedEffect(messageText) {
        if (messageText.isNotEmpty() && !isTyping) {
            isTyping = true
            viewModel.sendTypingIndicator(conversationId)
        } else if (messageText.isEmpty() && isTyping) {
            isTyping = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.leaveConversation(conversationId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = (conversation as? Resource.Success)?.data?.getOtherUser(currentUserId ?: "")?.let {
                                "${it.nom} ${it.prenom}"
                            } ?: "Chat",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (userTyping?.second == conversationId) {
                            Text(
                                text = "typing...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (isConnected) {
                            Text(
                                text = "online",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Car info card
            (conversation as? Resource.Success)?.data?.car?.let { car ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${car.marque} ${car.modele}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Year: ${car.annee} • ${car.kilometrage ?: "N/A"} km",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        car.price?.let {
                            Text(
                                "$${it}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Messages list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (messages) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is Resource.Success -> {
                        @Suppress("UNCHECKED_CAST")
                        val messageList = (messages as Resource.Success<List<ChatMessage>>).data ?: emptyList()
                        if (messageList.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No messages yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Start the conversation!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(messageList.size) { index ->
                                    val message = messageList[index]
                                    MessageBubble(
                                        message = message,
                                        isCurrentUser = message.senderId == currentUserId
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
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Error loading messages",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {}
                }
            }

            // Message input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(conversationId, messageText.trim())
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatMessageTime(message.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

fun formatMessageTime(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(date)
    }
}
