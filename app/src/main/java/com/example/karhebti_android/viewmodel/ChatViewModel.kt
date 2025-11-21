package com.example.karhebti_android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.karhebti_android.data.api.*
import com.example.karhebti_android.data.preferences.TokenManager
import com.example.karhebti_android.data.repository.ChatRepository
import com.example.karhebti_android.data.repository.Resource
import kotlinx.coroutines.launch

/**
 * Singleton ChatViewModel - manages all chat-related functionality
 * Separated from MarketplaceViewModel for better architecture
 */
class ChatViewModel private constructor(application: Application) : AndroidViewModel(application) {

    companion object {
        @Volatile
        private var INSTANCE: ChatViewModel? = null

        fun getInstance(application: Application): ChatViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatViewModel(application).also { INSTANCE = it }
            }
        }
    }

    private val tokenManager = TokenManager(application)
    private val repository: ChatRepository by lazy {
        ChatRepository.getInstance(RetrofitClient.apiService, tokenManager.getToken() ?: "")
    }

    // Current conversation
    private val _currentConversation = MutableLiveData<Resource<ConversationResponse>>()
    val currentConversation: LiveData<Resource<ConversationResponse>> = _currentConversation

    // Messages for current conversation
    private val _messages = MutableLiveData<Resource<List<ChatMessage>>>()
    val messages: LiveData<Resource<List<ChatMessage>>> = _messages

    // Real-time messages (from WebSocket)
    private val _realtimeMessage = MutableLiveData<ChatMessage?>()
    val realtimeMessage: LiveData<ChatMessage?> = _realtimeMessage

    // Conversations list
    private val _conversations = MutableLiveData<Resource<List<ConversationResponse>>>()
    val conversations: LiveData<Resource<List<ConversationResponse>>> = _conversations

    // User typing indicator
    private val _userTyping = MutableLiveData<Pair<String, String>?>() // userId, conversationId
    val userTyping: LiveData<Pair<String, String>?> = _userTyping

    // WebSocket connection status
    private val _isWebSocketConnected = MutableLiveData<Boolean>()
    val isWebSocketConnected: LiveData<Boolean> = _isWebSocketConnected

    // Track if WebSocket is initialized
    private var isWebSocketInitialized = false

    init {
        android.util.Log.d("ChatViewModel", "ChatViewModel singleton instance created")
    }

    // ==================== CONVERSATIONS ====================

    fun loadConversations() {
        viewModelScope.launch {
            _conversations.value = Resource.Loading()
            _conversations.value = repository.getConversations()
        }
    }

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            _currentConversation.value = Resource.Loading()
            val result = repository.getConversation(conversationId)
            _currentConversation.value = result

            // Log the conversation details to debug name display
            if (result is Resource.Success) {
                android.util.Log.d("ChatViewModel", "Loaded conversation: ${result.data?.id}")
                android.util.Log.d("ChatViewModel", "Other user: ${result.data?.otherUser?.nom} ${result.data?.otherUser?.prenom}")
                android.util.Log.d("ChatViewModel", "Car: ${result.data?.carDetails?.marque} ${result.data?.carDetails?.modele}")
            }
        }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _messages.value = Resource.Loading()
            _messages.value = repository.getMessages(conversationId)
        }
    }

    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            val result = repository.sendMessage(conversationId, content)
            if (result is Resource.Success && result.data != null) {
                android.util.Log.d("ChatViewModel", "✅ Message sent successfully: ${result.data.id}")
                // Add message to local list for instant feedback
                val currentMessages = _messages.value?.data?.toMutableList() ?: mutableListOf()
                if (!currentMessages.any { it.id == result.data.id }) {
                    currentMessages.add(result.data)
                    _messages.value = Resource.Success(ArrayList(currentMessages))
                }
            }
        }
    }

    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            repository.markConversationAsRead(conversationId)
        }
    }

    // ==================== WEBSOCKET ====================

    fun connectWebSocket() {
        if (isWebSocketInitialized) {
            android.util.Log.d("ChatViewModel", "WebSocket already initialized")
            return
        }

        android.util.Log.d("ChatViewModel", "Initializing WebSocket connection")
        isWebSocketInitialized = true

        repository.initWebSocket(
            onMessageReceived = { message ->
                android.util.Log.d("ChatViewModel", "📨 WebSocket message received: ${message.id} for conversation: ${message.conversationId}")

                // CRITICAL FIX: Always update realtime message first
                _realtimeMessage.postValue(message)

                // Get current conversation ID
                val currentConvId = _currentConversation.value?.data?.id
                android.util.Log.d("ChatViewModel", "Current conversation: $currentConvId, Message conversation: ${message.conversationId}")

                // Add to messages list if it's for the current conversation
                if (message.conversationId == currentConvId) {
                    android.util.Log.d("ChatViewModel", "✅ Message is for current conversation, adding to list")
                    val currentMessages = _messages.value?.data?.toMutableList() ?: mutableListOf()

                    // Check for duplicates
                    if (!currentMessages.any { it.id == message.id }) {
                        currentMessages.add(message)
                        android.util.Log.d("ChatViewModel", "Added message to list. Total messages: ${currentMessages.size}")

                        // Use postValue to ensure UI thread update
                        _messages.postValue(Resource.Success(ArrayList(currentMessages)))

                        // ALSO update on main thread for immediate UI refresh
                        viewModelScope.launch {
                            _messages.value = Resource.Success(ArrayList(currentMessages))
                        }
                    } else {
                        android.util.Log.d("ChatViewModel", "⚠️ Message already exists in list, skipping duplicate")
                    }
                } else {
                    android.util.Log.d("ChatViewModel", "Message is for different conversation (${message.conversationId} vs $currentConvId)")
                }

                // Reload conversations list to update last message and unread counts
                loadConversations()
            },
            onNotificationReceived = { notification ->
                android.util.Log.d("ChatViewModel", "🔔 Notification received: ${notification.type}")

                // CRITICAL FIX: When we receive a "new_message" notification,
                // reload the messages for the current conversation
                if (notification.type == "new_message") {
                    val conversationId = notification.data?.get("conversationId") as? String
                    val currentConvId = _currentConversation.value?.data?.id

                    android.util.Log.d("ChatViewModel", "New message notification for conversation: $conversationId (current: $currentConvId)")

                    if (conversationId != null && conversationId == currentConvId) {
                        android.util.Log.d("ChatViewModel", "🔄 Reloading messages due to notification")
                        // Reload messages to get the new message
                        viewModelScope.launch {
                            val result = repository.getMessages(conversationId)
                            if (result is Resource.Success) {
                                _messages.postValue(result)
                                android.util.Log.d("ChatViewModel", "✅ Messages reloaded: ${result.data?.size} messages")
                            }
                        }
                    }

                    // Always reload conversations list
                    loadConversations()
                }
            },
            onUserTyping = { userId, conversationId ->
                _userTyping.postValue(Pair(userId, conversationId))

                // Clear typing indicator after 3 seconds
                viewModelScope.launch {
                    kotlinx.coroutines.delay(3000)
                    if (_userTyping.value?.first == userId && _userTyping.value?.second == conversationId) {
                        _userTyping.postValue(null)
                    }
                }
            },
            onConnectionChanged = { isConnected ->
                android.util.Log.d("ChatViewModel", "WebSocket connection: $isConnected")
                _isWebSocketConnected.postValue(isConnected)
            }
        )
    }

    fun disconnectWebSocket() {
        android.util.Log.d("ChatViewModel", "Disconnecting WebSocket")
        repository.disconnectWebSocket()
        isWebSocketInitialized = false
    }

    fun joinConversation(conversationId: String) {
        repository.joinConversation(conversationId)
    }

    fun leaveConversation(conversationId: String) {
        repository.leaveConversation(conversationId)
    }

    fun sendTypingIndicator(conversationId: String) {
        repository.sendTypingIndicator(conversationId)
    }

    // Clear realtime message after it's been processed
    fun clearRealtimeMessage() {
        _realtimeMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        android.util.Log.d("ChatViewModel", "ChatViewModel cleared")
        disconnectWebSocket()
    }
}
