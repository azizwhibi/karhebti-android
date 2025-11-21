package com.example.karhebti_android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.karhebti_android.data.api.*
import com.example.karhebti_android.data.preferences.TokenManager
import com.example.karhebti_android.data.repository.MarketplaceRepository
import com.example.karhebti_android.data.repository.Resource
import kotlinx.coroutines.launch

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val repository: MarketplaceRepository by lazy {
        MarketplaceRepository(RetrofitClient.apiService, tokenManager.getToken() ?: "")
    }

    // Flag to track WebSocket connection status
    private var isWebSocketInitialized = false

    // Available cars for browsing
    private val _availableCars = MutableLiveData<Resource<List<MarketplaceCarResponse>>>()
    val availableCars: LiveData<Resource<List<MarketplaceCarResponse>>> = _availableCars

    // Swipe result
    private val _swipeResult = MutableLiveData<Resource<SwipeResponse>>()
    val swipeResult: LiveData<Resource<SwipeResponse>> = _swipeResult

    // My swipes
    private val _mySwipes = MutableLiveData<Resource<MySwipesResponse>>()
    val mySwipes: LiveData<Resource<MySwipesResponse>> = _mySwipes

    // Pending swipes (received from buyers)
    private val _pendingSwipes = MutableLiveData<Resource<List<SwipeResponse>>>()
    val pendingSwipes: LiveData<Resource<List<SwipeResponse>>> = _pendingSwipes

    // Swipe response result
    private val _swipeResponseResult = MutableLiveData<Resource<SwipeStatusResponse>>()
    val swipeResponseResult: LiveData<Resource<SwipeStatusResponse>> = _swipeResponseResult

    // Conversations
    private val _conversations = MutableLiveData<Resource<List<ConversationResponse>>>()
    val conversations: LiveData<Resource<List<ConversationResponse>>> = _conversations

    // Current conversation
    private val _currentConversation = MutableLiveData<Resource<ConversationResponse>>()
    val currentConversation: LiveData<Resource<ConversationResponse>> = _currentConversation

    // Messages
    private val _messages = MutableLiveData<Resource<List<ChatMessage>>>()
    val messages: LiveData<Resource<List<ChatMessage>>> = _messages

    // Real-time messages (from WebSocket)
    private val _realtimeMessage = MutableLiveData<ChatMessage>()
    val realtimeMessage: LiveData<ChatMessage> = _realtimeMessage

    // Keep track of the last message ID to force UI updates
    private var lastMessageId: String = ""

    // Notifications
    private val _notifications = MutableLiveData<Resource<List<NotificationResponse>>>()
    val notifications: LiveData<Resource<List<NotificationResponse>>> = _notifications

    // Real-time notification
    private val _realtimeNotification = MutableLiveData<NotificationResponse>()
    val realtimeNotification: LiveData<NotificationResponse> = _realtimeNotification

    // Unread count
    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> = _unreadCount

    // User typing indicator
    private val _userTyping = MutableLiveData<Pair<String, String>>() // userId, conversationId
    val userTyping: LiveData<Pair<String, String>> = _userTyping

    // WebSocket connection status
    private val _isWebSocketConnected = MutableLiveData<Boolean>()
    val isWebSocketConnected: LiveData<Boolean> = _isWebSocketConnected

    // Car listing result
    private val _listCarResult = MutableLiveData<Resource<MarketplaceCarResponse>>()
    val listCarResult: LiveData<Resource<MarketplaceCarResponse>> = _listCarResult

    // ==================== CARS ====================

    fun loadAvailableCars() {
        viewModelScope.launch {
            _availableCars.value = Resource.Loading()
            _availableCars.value = repository.getAvailableCars()
        }
    }

    fun listCarForSale(carId: String, price: Double, description: String?) {
        viewModelScope.launch {
            _listCarResult.value = Resource.Loading()
            _listCarResult.value = repository.listCarForSale(carId, price, description)
        }
    }

    fun unlistCar(carId: String) {
        viewModelScope.launch {
            _listCarResult.value = Resource.Loading()
            _listCarResult.value = repository.unlistCar(carId)
        }
    }

    // ==================== SWIPES ====================

    fun swipeLeft(carId: String) {
        viewModelScope.launch {
            _swipeResult.value = Resource.Loading()
            _swipeResult.value = repository.createSwipe(carId, "left")
        }
    }

    fun swipeRight(carId: String) {
        viewModelScope.launch {
            _swipeResult.value = Resource.Loading()
            _swipeResult.value = repository.createSwipe(carId, "right")
        }
    }

    fun acceptSwipe(swipeId: String) {
        viewModelScope.launch {
            _swipeResponseResult.value = Resource.Loading()
            _swipeResponseResult.value = repository.acceptSwipe(swipeId)
        }
    }

    fun declineSwipe(swipeId: String) {
        viewModelScope.launch {
            _swipeResponseResult.value = Resource.Loading()
            _swipeResponseResult.value = repository.declineSwipe(swipeId)
        }
    }

    fun loadMySwipes() {
        viewModelScope.launch {
            _mySwipes.value = Resource.Loading()
            _mySwipes.value = repository.getMySwipes()
        }
    }

    fun loadPendingSwipes() {
        viewModelScope.launch {
            _pendingSwipes.value = Resource.Loading()
            _pendingSwipes.value = repository.getPendingSwipes()
        }
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
            _currentConversation.value = repository.getConversation(conversationId)
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
            // Immediately add the message to the local list for instant feedback
            if (result is Resource.Success && result.data != null) {
                android.util.Log.d("MarketplaceViewModel", "✅ Message sent successfully: ${result.data.id}")
                val currentMessages = _messages.value?.data?.toMutableList() ?: mutableListOf()

                // Check if message already exists to avoid duplicates
                if (!currentMessages.any { it.id == result.data.id }) {
                    currentMessages.add(result.data)
                    android.util.Log.d("MarketplaceViewModel", "Adding sent message to list, new count: ${currentMessages.size}")

                    // CRITICAL: Create new list instance to force LiveData update
                    val newList = ArrayList(currentMessages)
                    _messages.value = Resource.Success(newList)

                    // Also trigger with postValue for safety
                    _messages.postValue(Resource.Success(newList))
                } else {
                    android.util.Log.d("MarketplaceViewModel", "Message already exists in list, skipping")
                }
            } else {
                android.util.Log.e("MarketplaceViewModel", "❌ Failed to send message")
            }
        }
    }

    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            repository.markConversationAsRead(conversationId)
        }
    }

    // ==================== NOTIFICATIONS ====================

    fun loadNotifications() {
        viewModelScope.launch {
            _notifications.value = Resource.Loading()
            _notifications.value = repository.getNotifications()
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            val result = repository.getUnreadNotificationCount()
            if (result is Resource.Success) {
                _unreadCount.value = result.data?.count ?: 0
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
            loadNotifications()
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            loadNotifications()
        }
    }

    // ==================== WEBSOCKET ====================

    fun connectWebSocket() {
        if (isWebSocketInitialized) {
            android.util.Log.d("MarketplaceViewModel", "WebSocket already initialized, skipping")
            return
        }

        android.util.Log.d("MarketplaceViewModel", "Initializing WebSocket for the first time")
        isWebSocketInitialized = true

        repository.initWebSocket(
            onMessageReceived = { message ->
                android.util.Log.d("MarketplaceViewModel", "WebSocket received message: ${message.id} for conversation: ${message.conversationId}, content: ${message.content}")

                // ALWAYS update realtime message to trigger UI refresh
                lastMessageId = message.id
                _realtimeMessage.postValue(message)

                // Add message to list and force UI update
                val currentConvId = _currentConversation.value?.data?.id
                android.util.Log.d("MarketplaceViewModel", "Current conversation ID: $currentConvId, Message conversation: ${message.conversationId}")

                // Add message to list if it matches current conversation
                if (message.conversationId == currentConvId) {
                    android.util.Log.d("MarketplaceViewModel", "Adding message to list")
                    val currentMessages = _messages.value?.data?.toMutableList() ?: mutableListOf()

                    // Check if message already exists to avoid duplicates
                    if (!currentMessages.any { it.id == message.id }) {
                        currentMessages.add(message)
                        android.util.Log.d("MarketplaceViewModel", "Message added, new count: ${currentMessages.size}")
                        // CRITICAL: Create new list instance to force LiveData update
                        val newList = ArrayList(currentMessages)
                        _messages.postValue(Resource.Success(newList))

                        // ALSO force a value change by posting null then the new value
                        viewModelScope.launch {
                            _messages.value = Resource.Success(newList)
                        }
                    } else {
                        android.util.Log.d("MarketplaceViewModel", "Message already exists, skipping")
                    }
                } else if (currentConvId != null) {
                    // Message for different conversation, reload conversation list
                    android.util.Log.d("MarketplaceViewModel", "Message for different conversation, reloading conversations")
                    loadConversations()
                }
            },
            onNotificationReceived = { notification ->
                android.util.Log.d("MarketplaceViewModel", "Received notification: ${notification.type}")
                _realtimeNotification.postValue(notification)
                loadUnreadCount()
            },
            onUserTyping = { userId, conversationId ->
                _userTyping.postValue(Pair(userId, conversationId))
            },
            onUserStatus = { _, _ ->
                // Handle user online/offline status if needed
            },
            onConnectionChanged = { isConnected ->
                android.util.Log.d("MarketplaceViewModel", "WebSocket connection status: $isConnected")
                _isWebSocketConnected.postValue(isConnected)

                // DISABLED: HTTP Polling fallback - WebSocket is stable enough
                // If we need polling, we'll add it as an explicit user action
                if (!isConnected) {
                    android.util.Log.w("MarketplaceViewModel", "⚠️ WebSocket disconnected - Relying on reconnection")
                    // Note: WebSocket will auto-reconnect, no need for polling
                }
            }
        )
    }

    fun disconnectWebSocket() {
        repository.disconnectWebSocket()
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

    // ==================== HTTP POLLING (WebSocket Fallback) ====================

    private fun enableHttpPolling() {
        android.util.Log.d("MarketplaceViewModel", "🔄 Enabling HTTP Polling for real-time updates")

        repository.initPolling { message ->
            android.util.Log.d("MarketplaceViewModel", "Polling received message: ${message.id}")
            _realtimeMessage.postValue(message)

            val currentConvId = _currentConversation.value?.data?.id
            if (message.conversationId == currentConvId) {
                val currentMessages = _messages.value?.data?.toMutableList() ?: mutableListOf()
                if (!currentMessages.any { it.id == message.id }) {
                    currentMessages.add(message)
                    _messages.postValue(Resource.Success(currentMessages))
                }
            }
        }

        // Start observing polling messages
        viewModelScope.launch {
            repository.getPollingMessages()?.collect { message ->
                message?.let {
                    android.util.Log.d("MarketplaceViewModel", "📨 Polling detected new message: ${it.id}")
                    _realtimeMessage.postValue(it)

                    val currentConvId = _currentConversation.value?.data?.id
                    if (it.conversationId == currentConvId) {
                        val currentMessages = _messages.value?.data?.toMutableList() ?: mutableListOf()
                        if (!currentMessages.any { msg -> msg.id == it.id }) {
                            currentMessages.add(it)
                            android.util.Log.d("MarketplaceViewModel", "✅ Message added via polling, new count: ${currentMessages.size}")
                            _messages.postValue(Resource.Success(currentMessages))
                        }
                    }

                    // Reload conversations to update unread counts
                    loadConversations()
                }
            }
        }
    }

    fun startPollingForConversation(conversationId: String) {
        android.util.Log.d("MarketplaceViewModel", "🔄 Starting polling for conversation: $conversationId")
        repository.startPollingConversation(conversationId)
    }

    fun stopPollingForConversation() {
        android.util.Log.d("MarketplaceViewModel", "⏹️ Stopping polling")
        repository.stopPollingConversation()
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnectWebSocket()
        repository.cleanupPolling()
    }
}
