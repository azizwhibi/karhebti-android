package com.example.karhebti_android.data.repository

import android.util.Log
import com.example.karhebti_android.data.api.*
import com.example.karhebti_android.data.websocket.ChatWebSocketClient

class MarketplaceRepository(
    private val apiService: KarhebtiApiService,
    private val token: String
) {
    companion object {
        private const val TAG = "MarketplaceRepository"
    }

    // WebSocket client instance
    private var webSocketClient: ChatWebSocketClient? = null

    // ==================== CARS ====================

    suspend fun getAvailableCars(): Resource<List<MarketplaceCarResponse>> {
        return try {
            val response = apiService.getAvailableCars()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error fetching available cars: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching available cars", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun listCarForSale(carId: String, price: Double, description: String?): Resource<MarketplaceCarResponse> {
        return try {
            val request = ListCarForSaleRequest(price, description)
            val response = apiService.listCarForSale(carId, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error listing car for sale: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception listing car for sale", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun unlistCar(carId: String): Resource<MarketplaceCarResponse> {
        return try {
            val response = apiService.unlistCar(carId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error unlisting car: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception unlisting car", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    // ==================== SWIPES ====================

    suspend fun createSwipe(carId: String, direction: String): Resource<SwipeResponse> {
        return try {
            val request = CreateSwipeRequest(carId, direction)
            val response = apiService.createSwipe(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error creating swipe: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating swipe", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun acceptSwipe(swipeId: String): Resource<SwipeStatusResponse> {
        return try {
            val response = apiService.acceptSwipe(swipeId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error accepting swipe: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception accepting swipe", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun declineSwipe(swipeId: String): Resource<SwipeStatusResponse> {
        return try {
            val response = apiService.declineSwipe(swipeId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error declining swipe: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception declining swipe", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMySwipes(): Resource<MySwipesResponse> {
        return try {
            val response = apiService.getMySwipes()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error fetching my swipes: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching my swipes", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getPendingSwipes(): Resource<List<SwipeResponse>> {
        return try {
            val response = apiService.getPendingSwipes()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error fetching pending swipes: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching pending swipes", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    // ==================== CONVERSATIONS ====================

    suspend fun getConversations(): Resource<List<ConversationResponse>> {
        return try {
            val response = apiService.getConversations()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error fetching conversations: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching conversations", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getConversation(conversationId: String): Resource<ConversationResponse> {
        return try {
            val response = apiService.getConversation(conversationId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error fetching conversation: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching conversation", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMessages(conversationId: String): Resource<List<ChatMessage>> {
        return try {
            val response = apiService.getMessages(conversationId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error fetching messages: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching messages", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun sendMessage(conversationId: String, content: String): Resource<ChatMessage> {
        return try {
            val request = SendMessageRequest(content)
            val response = apiService.sendMessage(conversationId, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error sending message: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending message", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun markConversationAsRead(conversationId: String): Resource<MessageResponse> {
        return try {
            val response = apiService.markConversationAsRead(conversationId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error marking conversation as read: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception marking conversation as read", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    // ==================== NOTIFICATIONS ====================

    suspend fun getNotifications(): Resource<List<NotificationResponse>> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error fetching notifications: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching notifications", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getUnreadNotificationCount(): Resource<UnreadCountResponse> {
        return try {
            val response = apiService.getUnreadNotificationCount()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error fetching unread count: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching unread count", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Resource<NotificationResponse> {
        return try {
            val response = apiService.markNotificationAsRead(notificationId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error marking notification as read: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception marking notification as read", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun markAllNotificationsAsRead(): Resource<MessageResponse> {
        return try {
            val response = apiService.markAllNotificationsAsRead()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error marking all notifications as read: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception marking all notifications as read", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    // ==================== WEBSOCKET ====================

    fun initWebSocket(
        onMessageReceived: (ChatMessage) -> Unit,
        onNotificationReceived: (NotificationResponse) -> Unit,
        onUserTyping: (String, String) -> Unit,
        onUserStatus: (String, Boolean) -> Unit,
        onConnectionChanged: (Boolean) -> Unit
    ) {
        webSocketClient = ChatWebSocketClient(
            token = token,
            onMessageReceived = onMessageReceived,
            onNotificationReceived = onNotificationReceived,
            onUserTyping = onUserTyping,
            onUserStatus = onUserStatus,
            onConnectionChanged = onConnectionChanged
        )
        webSocketClient?.connect()
    }

    fun disconnectWebSocket() {
        webSocketClient?.disconnect()
        webSocketClient = null
    }

    fun joinConversation(conversationId: String) {
        webSocketClient?.joinConversation(conversationId)
    }

    fun leaveConversation(conversationId: String) {
        webSocketClient?.leaveConversation(conversationId)
    }

    fun sendChatMessage(conversationId: String, content: String) {
        webSocketClient?.sendChatMessage(conversationId, content)
    }

    fun sendTypingIndicator(conversationId: String) {
        webSocketClient?.sendTypingIndicator(conversationId)
    }

    fun isWebSocketConnected(): Boolean {
        return webSocketClient?.isConnected() ?: false
    }
}
