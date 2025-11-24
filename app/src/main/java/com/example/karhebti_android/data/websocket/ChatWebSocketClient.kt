package com.example.karhebti_android.data.websocket

import android.util.Log
import com.example.karhebti_android.data.api.ChatMessage
import com.example.karhebti_android.data.api.NotificationResponse
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

class ChatWebSocketClient(
    private val token: String,
    private val onMessageReceived: (ChatMessage) -> Unit,
    private val onNotificationReceived: (NotificationResponse) -> Unit,
    private val onUserTyping: (String, String) -> Unit,
    private val onUserStatus: (String, Boolean) -> Unit,
    private val onConnectionChanged: (Boolean) -> Unit
) {
    private var socket: Socket? = null
    private val gson = Gson()

    // New property to keep track of pending conversation joins
    private val pendingConversationJoins = mutableListOf<String>()

    companion object {
        private const val TAG = "ChatWebSocketClient"
        private const val SERVER_URL = "http://192.168.1.190:3000"
        private const val NAMESPACE = "/chat"
    }

    fun connect() {
        try {
            Log.d(TAG, "Attempting to connect to Socket.IO server")
            Log.d(TAG, "Server: $SERVER_URL")
            Log.d(TAG, "Namespace: $NAMESPACE")
            Log.d(TAG, "Token (first 20 chars): ${token.take(20)}...")

            // CRITICAL: Extract user ID from JWT token
            var userId: String? = null
            try {
                val parts = token.split(".")
                if (parts.size >= 2) {
                    val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
                    val jsonObject = JSONObject(payload)
                    userId = jsonObject.optString("sub")
                    Log.d(TAG, "✅ Extracted userId from token: $userId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to extract userId from token: ${e.message}")
            }

            // Configure Socket.IO options
            val opts = IO.Options().apply {
                // Authentication
                extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
                reconnection = true
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                reconnectionAttempts = Integer.MAX_VALUE
                timeout = 10000
                transports = arrayOf("websocket", "polling")
            }

            // Create socket instance
            socket = IO.socket("$SERVER_URL$NAMESPACE", opts)

            // Register event listeners
            socket?.on(Socket.EVENT_CONNECT, onConnect)
            socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socket?.on("new_message", onNewMessage)
            socket?.on("notification", onNotification)
            socket?.on("user_typing", onUserTypingEvent)
            socket?.on("user_online", onUserOnline)
            socket?.on("user_offline", onUserOffline)
            socket?.on("joined_conversation", onJoinedConversation)

            // DEBUG: Listen to ALL events (optional wildcard listener)
            socket?.on("*") { args ->
                Log.d(TAG, "🔔 Wildcard event received with ${args.size} args")
                args.forEachIndexed { index, arg ->
                    Log.d(TAG, "Arg $index: $arg")
                }
            }

            // Initiate connection
            socket?.connect()
            Log.d(TAG, "Socket.IO connect() called")

        } catch (e: URISyntaxException) {
            Log.e(TAG, "❌ Invalid Socket.IO URI: ${e.message}", e)
            onConnectionChanged(false)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating Socket.IO connection: ${e.message}", e)
            e.printStackTrace()
            onConnectionChanged(false)
        }
    }

    private val onConnect = Emitter.Listener {
        Log.d(TAG, "✅ Socket.IO Connected successfully!")
        Log.d(TAG, "Connected to: $SERVER_URL$NAMESPACE")
        Log.d(TAG, "Socket ID: ${socket?.id()}")

        // Auto-join any pending conversation rooms after connection
        pendingConversationJoins.forEach { conversationId ->
            Log.d(TAG, "Auto-joining pending conversation: $conversationId")
            joinConversation(conversationId)
        }
        pendingConversationJoins.clear()

        onConnectionChanged(true)
    }

    private val onDisconnect = Emitter.Listener { args ->
        val reason = if (args.isNotEmpty()) args[0].toString() else "unknown"
        Log.d(TAG, "Socket.IO Disconnected: $reason")
        onConnectionChanged(false)
    }

    private val onConnectError = Emitter.Listener { args ->
        val error = if (args.isNotEmpty()) args[0] else "Unknown error"
        Log.e(TAG, "❌ Socket.IO connection error: $error")
        if (error is Exception) {
            error.printStackTrace()
        }
        onConnectionChanged(false)
    }

    private val onNewMessage = Emitter.Listener { args ->
        try {
            if (args.isNotEmpty()) {
                val data = args[0]
                Log.d(TAG, "📨 ========================================")
                Log.d(TAG, "📨 NEW MESSAGE EVENT RECEIVED")
                Log.d(TAG, "📨 Data type: ${data.javaClass.simpleName}")
                Log.d(TAG, "📨 Raw data: $data")

                val jsonString = when (data) {
                    is JSONObject -> data.toString()
                    is String -> data
                    else -> data.toString()
                }

                Log.d(TAG, "📨 JSON string: $jsonString")

                val message = gson.fromJson(jsonString, ChatMessage::class.java)
                Log.d(TAG, "📨 ✅ Parsed message successfully:")
                Log.d(TAG, "📨   - ID: ${message.id}")
                Log.d(TAG, "📨   - ConversationID: ${message.conversationId}")
                Log.d(TAG, "📨   - SenderID: ${message.senderId}")
                Log.d(TAG, "📨   - Content: ${message.content}")
                Log.d(TAG, "📨   - CreatedAt: ${message.createdAt}")
                Log.d(TAG, "📨 ========================================")

                // Call the callback to update UI
                onMessageReceived(message)
            } else {
                Log.e(TAG, "❌ new_message event received but args is empty")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ========================================")
            Log.e(TAG, "❌ Error parsing new_message: ${e.message}", e)
            Log.e(TAG, "❌ Exception type: ${e.javaClass.simpleName}")
            if (args.isNotEmpty()) {
                Log.e(TAG, "❌ Raw data was: ${args[0]}")
            }
            Log.e(TAG, "❌ ========================================")
        }
    }

    private val onNotification = Emitter.Listener { args ->
        try {
            if (args.isNotEmpty()) {
                val data = args[0]
                Log.d(TAG, "📨 Notification event received: $data")

                val jsonString = when (data) {
                    is JSONObject -> data.toString()
                    is String -> data
                    else -> data.toString()
                }

                val notification = gson.fromJson(jsonString, NotificationResponse::class.java)
                Log.d(TAG, "✅ Parsed notification")
                onNotificationReceived(notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing notification: ${e.message}", e)
        }
    }

    private val onUserTypingEvent = Emitter.Listener { args ->
        try {
            if (args.isNotEmpty()) {
                val data = args[0] as? JSONObject
                val userId = data?.optString("userId", "") ?: ""
                val conversationId = data?.optString("conversationId", "") ?: ""
                Log.d(TAG, "✅ User typing: $userId in conversation $conversationId")
                onUserTyping(userId, conversationId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing user_typing: ${e.message}", e)
        }
    }

    private val onUserOnline = Emitter.Listener { args ->
        try {
            if (args.isNotEmpty()) {
                val data = args[0] as? JSONObject
                val userId = data?.optString("userId", "") ?: ""
                Log.d(TAG, "✅ User online: $userId")
                onUserStatus(userId, true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing user_online: ${e.message}", e)
        }
    }

    private val onUserOffline = Emitter.Listener { args ->
        try {
            if (args.isNotEmpty()) {
                val data = args[0] as? JSONObject
                val userId = data?.optString("userId", "") ?: ""
                Log.d(TAG, "✅ User offline: $userId")
                onUserStatus(userId, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing user_offline: ${e.message}", e)
        }
    }

    private val onJoinedConversation = Emitter.Listener { args ->
        try {
            if (args.isNotEmpty()) {
                val data = args[0]
                Log.d(TAG, "✅ Joined conversation successfully: $data")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing joined_conversation: ${e.message}", e)
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting Socket.IO...")
        socket?.off()
        socket?.disconnect()
        socket = null
    }

    fun joinConversation(conversationId: String) {
        try {
            val data = JSONObject().apply {
                put("conversationId", conversationId)
            }
            socket?.emit("join_conversation", data)
            Log.d(TAG, "Sent join_conversation: $conversationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending join_conversation: ${e.message}", e)
        }
    }

    fun leaveConversation(conversationId: String) {
        try {
            val data = JSONObject().apply {
                put("conversationId", conversationId)
            }
            socket?.emit("leave_conversation", data)
            Log.d(TAG, "Sent leave_conversation: $conversationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending leave_conversation: ${e.message}", e)
        }
    }

    fun sendChatMessage(conversationId: String, content: String) {
        try {
            val data = JSONObject().apply {
                put("conversationId", conversationId)
                put("content", content)
            }
            socket?.emit("send_message", data)
            Log.d(TAG, "Sent send_message: $data")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending send_message: ${e.message}", e)
        }
    }

    fun sendTypingIndicator(conversationId: String) {
        try {
            val data = JSONObject().apply {
                put("conversationId", conversationId)
            }
            socket?.emit("typing", data)
            Log.d(TAG, "Sent typing indicator for: $conversationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending typing indicator: ${e.message}", e)
        }
    }

    fun isConnected(): Boolean {
        val connected = socket?.connected() ?: false
        Log.d(TAG, "isConnected() = $connected, socket exists = ${socket != null}")
        return connected
    }
}
