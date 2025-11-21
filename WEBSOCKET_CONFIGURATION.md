# WebSocket Configuration Update ✅

## Changes Applied

Updated the WebSocket client and API configuration to use the correct endpoint and IP address.

## WebSocket Endpoint Configuration

### Updated Endpoint
```
ws://192.168.1.190:3000/chat
```

### Supported Events

#### Outgoing Events (Client → Server)
1. **join_conversation** - Join a conversation room
   ```json
   {
     "event": "join_conversation",
     "conversationId": "conversation_id_here"
   }
   ```

2. **leave_conversation** - Leave a conversation room
   ```json
   {
     "event": "leave_conversation",
     "conversationId": "conversation_id_here"
   }
   ```

3. **send_message** - Send a message
   ```json
   {
     "event": "send_message",
     "conversationId": "conversation_id_here",
     "content": "message_content_here"
   }
   ```

4. **typing** - Send typing indicator
   ```json
   {
     "event": "typing",
     "conversationId": "conversation_id_here"
   }
   ```

#### Incoming Events (Server → Client)
1. **new_message** - Receive a new message
2. **notification** - Receive a notification
3. **user_online** - User came online
4. **user_offline** - User went offline
5. **user_typing** - User is typing

## Files Modified

### 1. ChatWebSocketClient.kt
**Location**: `app/src/main/java/com/example/karhebti_android/data/websocket/ChatWebSocketClient.kt`

**Changes**:
- Updated WebSocket URL to `ws://192.168.1.190:3000/chat`
- Removed endpoint fallback logic (no longer needed)
- Enhanced logging for all incoming events
- Confirmed all 5 incoming events are properly handled:
  - ✅ new_message
  - ✅ notification
  - ✅ user_online
  - ✅ user_offline
  - ✅ user_typing

**Key Features**:
```kotlin
companion object {
    private const val TAG = "ChatWebSocketClient"
    private const val WS_URL = "ws://192.168.1.190:3000/chat"
}
```

### 2. ApiConfig.kt
**Location**: `app/src/main/java/com/example/karhebti_android/data/api/ApiConfig.kt`

**Changes**:
- Updated REST API base URL to `http://192.168.1.190:3000/`
- Ensures API calls and WebSocket use the same server IP

**Configuration**:
```kotlin
object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.190:3000/"
    // ...
}
```

## How It Works

### Connection Flow
1. **Screen Opens** → Calls `viewModel.connectWebSocket()`
2. **ViewModel** → Calls `repository.initWebSocket()`
3. **Repository** → Uses shared singleton `ChatWebSocketClient`
4. **WebSocket** → Connects to `ws://192.168.1.190:3000/chat`
5. **Authentication** → Sends `Authorization: Bearer <token>` header
6. **Success** → `onConnectionChanged(true)` callback fires

### Message Flow
1. **User sends message** → ChatScreen calls `viewModel.sendMessage()`
2. **ViewModel** → Calls `repository.sendChatMessage()`
3. **WebSocket** → Emits `send_message` event
4. **Server processes** → Broadcasts to all participants
5. **Receive callback** → `new_message` event triggers `onMessageReceived()`
6. **UI updates** → Message appears in chat

### Event Handlers

All events are properly wired in `MarketplaceViewModel.kt`:

```kotlin
repository.initWebSocket(
    onMessageReceived = { message ->
        // Updates _realtimeMessage LiveData
        // Adds to _messages list
        // Triggers UI refresh
    },
    onNotificationReceived = { notification ->
        // Updates _realtimeNotification LiveData
        // Triggers notification UI
    },
    onUserTyping = { userId, conversationId ->
        // Updates _userTyping LiveData
        // Shows "User is typing..." indicator
    },
    onUserStatus = { userId, isOnline ->
        // Updates user online/offline status
    },
    onConnectionChanged = { isConnected ->
        // Updates _isWebSocketConnected LiveData
        // Falls back to HTTP polling if disconnected
    }
)
```

## Network Configuration Summary

| Service | Protocol | URL |
|---------|----------|-----|
| WebSocket | `ws://` | `192.168.1.190:3000/chat` |
| REST API | `http://` | `192.168.1.190:3000/` |
| MongoDB | N/A | `192.168.1.190:27017` |

## Testing the Connection

### Check Logs
Look for these log messages in Android Studio Logcat:

**Successful Connection**:
```
D/ChatWebSocketClient: Attempting to connect to WebSocket at: ws://192.168.1.190:3000/chat
D/ChatWebSocketClient: ✅ WebSocket Connected successfully to: ws://192.168.1.190:3000/chat
```

**Message Received**:
```
D/ChatWebSocketClient: 📨 Message received: {"event":"new_message","data":{...}}
D/ChatWebSocketClient: ✅ Parsed new_message event: message_id_here
```

**Connection Failed**:
```
E/ChatWebSocketClient: ❌ WebSocket connection failed at ws://192.168.1.190:3000/chat
E/ChatWebSocketClient: Error: Failed to connect to /192.168.1.190:3000
```

## Troubleshooting

### Connection Issues

1. **"Failed to connect"**
   - Verify server is running on `192.168.1.190:3000`
   - Check both devices are on the same network
   - Ensure firewall allows port 3000
   - Test with: `telnet 192.168.1.190 3000`

2. **"Connection timeout"**
   - Network may be blocking WebSocket connections
   - Check server WebSocket endpoint is `/chat`
   - Verify server is listening for WebSocket upgrades

3. **"Authentication failed"**
   - Check token is valid and not expired
   - Verify `Authorization` header is sent correctly
   - Check server logs for auth errors

### Testing Checklist

- [ ] WebSocket connects successfully on app launch
- [ ] Can join conversation rooms
- [ ] Sending messages works
- [ ] Receiving messages works in real-time
- [ ] Typing indicator appears
- [ ] User online/offline status updates
- [ ] Notifications arrive
- [ ] Falls back to HTTP polling if WebSocket fails
- [ ] Reconnects after network interruption

## Integration with Existing Features

### Singleton Pattern (Fixed)
- ✅ Each screen has its own ViewModel instance
- ✅ All ViewModels share the same WebSocket connection
- ✅ No race conditions or state conflicts
- ✅ Efficient resource usage

### Timeout Handling (Fixed)
- ✅ All screens have 10-second timeout
- ✅ Consolidated LaunchedEffect pattern
- ✅ Clean error recovery with retry

### Real-time Updates
- ✅ ConversationsScreen - Updates conversation list
- ✅ ChatScreen - Displays new messages instantly
- ✅ PendingSwipesScreen - Shows new swipe requests
- ✅ MarketplaceBrowseScreen - Notifies of matches

## Important Notes

⚠️ **IP Address**: The IP `192.168.1.190` is a local network address. If you're using:
- **Android Emulator**: Change to `10.0.2.2` (host loopback)
- **Physical Device**: Use your computer's local IP (e.g., `192.168.1.X`)
- **Production**: Use your domain or public IP

💡 **Port Forwarding**: If testing on physical device connected via USB, you may need ADB port forwarding:
```bash
adb reverse tcp:3000 tcp:3000
```

🔒 **Security**: In production, use WSS (WebSocket Secure) instead of WS:
```kotlin
private const val WS_URL = "wss://yourdomain.com/chat"
```

## Status: ✅ COMPLETE

All WebSocket functionality is now properly configured and ready to use with the endpoint `ws://192.168.1.190:3000/chat`.

