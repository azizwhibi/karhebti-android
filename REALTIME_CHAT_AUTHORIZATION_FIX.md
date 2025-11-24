# Real-Time Chat Authorization Fix

## Problem Identified

The backend was rejecting WebSocket `join_conversation` requests with:
```
🎯 Event 'error' received with 1 args
🎯 Arg[0]: {"message":"Unauthorized access to conversation"}
```

This meant:
- ✅ WebSocket connected successfully
- ✅ HTTP messages sent successfully
- ❌ **Backend rejected room join requests**
- ❌ **No real-time messages received**

## Root Cause

The backend's Socket.IO authentication requires **user identity verification** for each `join_conversation` event, not just at connection time. The token sent in `extraHeaders` during connection wasn't being used to authenticate individual room join requests.

## Solution Implemented

### 1. Extract User ID from JWT Token
Added code to extract the user ID (`sub` claim) from the JWT token when the WebSocket client is initialized:

```kotlin
init {
    try {
        val parts = token.split(".")
        if (parts.size == 3) {
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val jsonObject = JSONObject(payload)
            userId = jsonObject.optString("sub", "")
            Log.d(TAG, "✅ Extracted userId from token: $userId")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to extract userId from token: ${e.message}")
    }
}
```

### 2. Include User ID in Join Requests
Modified `joinConversation()` to include the `userId` field:

```kotlin
val data = JSONObject().apply {
    put("conversationId", conversationId)
    put("userId", userId)  // ← NEW: Include user ID
}
socket?.emit("join_conversation", data)
```

### 3. Added DisposableEffect for Proper Room Management
In `ChatScreen.kt`, added a `DisposableEffect` that:
- Runs **every time** you enter the chat screen (not just when conversation ID changes)
- Ensures WebSocket is connected
- Rejoins the conversation room
- Properly leaves the room when navigating away

## Files Modified

1. **ChatWebSocketClient.kt**
   - Added `userId` property
   - Added `init` block to extract user ID from token
   - Modified `joinConversation()` to include `userId`

2. **ChatScreen.kt**
   - Added `DisposableEffect(Unit)` for proper lifecycle management
   - Ensures room is rejoined when returning from marketplace screens

## Testing Instructions

### Test 1: Basic Real-Time Messaging
1. **Rebuild and install the app**
2. Open a chat conversation
3. Send a message from **device/account B**
4. ✅ **Message should appear instantly on device A**

### Test 2: Navigate to Marketplace and Back
1. Open a chat conversation
2. Navigate to **any marketplace screen** (vehicles, marketplace hub, etc.)
3. Navigate **back** to the same chat conversation
4. Send a message from device/account B
5. ✅ **Message should still appear instantly**

### Test 3: Multiple Conversations
1. Open conversation A
2. Navigate to conversation B
3. Send message in conversation A from other device
4. Navigate back to conversation A
5. ✅ **New message should be visible**

## Expected Log Output

When you enter a chat screen, you should see:
```
ChatWebSocketClient: ✅ Extracted userId from token: 6911ec39538b2b0a9072268f
ChatWebSocketClient: ✅ Sent join_conversation: 69246949385ce54038c9a624
```

**You should NO LONGER see:**
```
🎯 Event 'error' received with 1 args
🎯 Arg[0]: {"message":"Unauthorized access to conversation"}
```

When a message arrives, you should see:
```
📨 NEW MESSAGE EVENT RECEIVED
📨 ✅ Parsed message successfully
```

## What If It Still Doesn't Work?

If you still get "Unauthorized access" errors, the backend might require additional fields. Check with the backend team about what the `join_conversation` event handler expects.

Possible backend requirements:
- Different field name (e.g., `user_id` instead of `userId`)
- Token passed in the event data
- Different authentication mechanism

## Summary

The fix adds user identity verification to WebSocket room join requests, allowing the backend to properly authorize access to conversations. Combined with proper lifecycle management via `DisposableEffect`, this ensures real-time messaging works correctly even when navigating between screens.

