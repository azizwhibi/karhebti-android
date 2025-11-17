package com.example.karhebti_android.data.websocket

import android.util.Log
import com.example.karhebti_android.data.api.ChatMessage
import com.example.karhebti_android.data.api.NotificationResponse
import com.google.gson.Gson
import okhttp3.*
import java.util.concurrent.TimeUnit

class ChatWebSocketClient(
    private val token: String,
    private val onMessageReceived: (ChatMessage) -> Unit,
    private val onNotificationReceived: (NotificationResponse) -> Unit,
    private val onUserTyping: (String, String) -> Unit, // userId, conversationId
    private val onUserStatus: (String, Boolean) -> Unit, // userId, isOnline
    private val onConnectionChanged: (Boolean) -> Unit
) {
    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "ChatWebSocketClient"
        // Update this to match your backend WebSocket URL
        private const val WS_URL = "ws://10.0.2.2:3000" // For Android emulator
        // For physical device: "ws://YOUR_IP:3000"
    }

    fun connect() {
        val request = Request.Builder()
            .url(WS_URL)
            .addHeader("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Connected")
                onConnectionChanged(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                try {
                    val jsonObject = gson.fromJson(text, com.google.gson.JsonObject::class.java)
                    val event = jsonObject.get("event")?.asString ?: ""

                    when (event) {
                        "new_message" -> {
                            val data = jsonObject.getAsJsonObject("data")
                            val message = gson.fromJson(data, ChatMessage::class.java)
                            onMessageReceived(message)
                        }
                        "notification" -> {
                            val data = jsonObject.getAsJsonObject("data")
                            val notification = gson.fromJson(data, NotificationResponse::class.java)
                            onNotificationReceived(notification)
                        }
                        "user_typing" -> {
                            val userId = jsonObject.get("userId")?.asString ?: ""
                            val conversationId = jsonObject.get("conversationId")?.asString ?: ""
                            onUserTyping(userId, conversationId)
                        }
                        "user_online" -> {
                            val userId = jsonObject.get("userId")?.asString ?: ""
                            onUserStatus(userId, true)
                        }
                        "user_offline" -> {
                            val userId = jsonObject.get("userId")?.asString ?: ""
                            onUserStatus(userId, false)
                        }
                        "joined_conversation" -> {
                            Log.d(TAG, "Joined conversation successfully")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closing: $code / $reason")
                onConnectionChanged(false)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closed: $code / $reason")
                onConnectionChanged(false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket Error: ${t.message}")
                onConnectionChanged(false)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }

    fun joinConversation(conversationId: String) {
        val message = mapOf(
            "event" to "join_conversation",
            "conversationId" to conversationId
        )
        sendMessage(message)
    }

    fun leaveConversation(conversationId: String) {
        val message = mapOf(
            "event" to "leave_conversation",
            "conversationId" to conversationId
        )
        sendMessage(message)
    }

    fun sendChatMessage(conversationId: String, content: String) {
        val message = mapOf(
            "event" to "send_message",
            "conversationId" to conversationId,
            "content" to content
        )
        sendMessage(message)
    }

    fun sendTypingIndicator(conversationId: String) {
        val message = mapOf(
            "event" to "typing",
            "conversationId" to conversationId
        )
        sendMessage(message)
    }

    private fun sendMessage(message: Map<String, String>) {
        try {
            val json = gson.toJson(message)
            webSocket?.send(json)
            Log.d(TAG, "Sent message: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}")
        }
    }

    fun isConnected(): Boolean {
        return webSocket != null
    }
}
