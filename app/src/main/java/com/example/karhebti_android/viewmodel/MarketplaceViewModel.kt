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
                val currentMessages = _messages.value?.data?.toMutableList() ?: mutableListOf()
                currentMessages.add(result.data)
                _messages.value = Resource.Success(currentMessages)
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
        repository.initWebSocket(
            onMessageReceived = { message ->
                _realtimeMessage.postValue(message)
                // Add to messages list
                val currentMessages = _messages.value?.data?.toMutableList() ?: mutableListOf()
                currentMessages.add(message)
                _messages.postValue(Resource.Success(currentMessages))
            },
            onNotificationReceived = { notification ->
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
                _isWebSocketConnected.postValue(isConnected)
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

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
}
