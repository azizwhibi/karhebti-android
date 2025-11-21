# Real-Time Chat Debugging Guide

## ⚠️ CRITICAL ISSUE FOUND: WebSocket Connection Failure

### Current Problem
```
ChatWebSocketClient: WebSocket Error: unexpected end of stream on http://10.0.2.2:3000/...
MarketplaceViewModel: WebSocket connection status: false
```

**Messages are being sent via HTTP (working) but NOT receiving real-time updates because WebSocket is disconnected.**

## Root Cause
The WebSocket is failing to connect because:
1. **Wrong endpoint path** - The backend WebSocket server might not be at the root path
2. **Backend WebSocket server might not be running** or configured differently

## Changes Made
1. ✅ Made MarketplaceViewModel a singleton in ViewModelFactory
2. ✅ Added WebSocket initialization flag to prevent multiple connections
3. ✅ Added comprehensive logging throughout the message flow
4. ✅ Fixed SwipeResponse to include buyer and car details
5. ✅ Added kilometrage field to car creation
6. ✅ **NEW: Enhanced WebSocket client with better error logging**
7. ✅ **NEW: Added multiple endpoint fallback support**

## IMMEDIATE ACTION REQUIRED

### Option 1: Find the Correct WebSocket Endpoint (RECOMMENDED)

**You need to check your backend code to find the WebSocket endpoint.**

Common WebSocket endpoints:
- `ws://10.0.2.2:3000/ws`
- `ws://10.0.2.2:3000/socket.io`
- `ws://10.0.2.2:3000/chat`
- `ws://10.0.2.2:3000` (root)

**Steps:**
1. Look at your backend server code (Node.js, Express, etc.)
2. Find where WebSocket or Socket.IO is initialized
3. Check the path used (e.g., `io.path('/ws')` or `app.ws('/chat')`)
4. Update `ChatWebSocketClient.kt` with the correct endpoint

### Option 2: Use the Enhanced Client (Try All Endpoints)

I've updated `ChatWebSocketClient.kt` to automatically try multiple common endpoints:
1. `/ws`
2. `/socket.io`
3. `/chat`
4. Root path

**Test this now:**
1. Rebuild the app
2. Open the chat screen
3. Check logcat for: `"Attempting to connect to WebSocket at: ws://..."`
4. Look for either:
   - ✅ `"WebSocket Connected successfully to: ..."`
   - ❌ `"WebSocket connection failed at ..."`

### Option 3: Disable WebSocket and Use HTTP Polling (Temporary Workaround)

If you can't fix the WebSocket immediately, you can implement polling:

**I can help you implement HTTP polling where the app checks for new messages every few seconds.**

## How to Check Your Backend

### If using Node.js + Express + Socket.IO:
```javascript
// Look for something like this in your backend:
const io = require('socket.io')(server, {
  path: '/socket.io'  // <-- This is the path!
});
```

### If using Node.js + ws library:
```javascript
// Look for:
const wss = new WebSocket.Server({ 
  server, 
  path: '/ws'  // <-- This is the path!
});
```

### If using plain WebSocket upgrade:
```javascript
// Look for:
server.on('upgrade', (request, socket, head) => {
  // Usually at root path or /ws
});
```

## Testing Steps

### Step 1: Check Backend WebSocket Server
```bash
# In your backend directory, check if WebSocket server is running
# Look for logs like:
# "WebSocket server listening on port 3000"
# "Socket.IO server started"
```

### Step 2: Test WebSocket Connection Manually
You can test the WebSocket endpoint using a browser console or tool:
```javascript
// In Chrome DevTools console:
const ws = new WebSocket('ws://localhost:3000/ws');
ws.onopen = () => console.log('Connected!');
ws.onerror = (e) => console.log('Error:', e);
```

### Step 3: Check Logcat After My Updates
Run the app and filter by `ChatWebSocketClient`:
```
Expected logs:
- "Attempting to connect to WebSocket at: ws://..."
- Either "✅ WebSocket Connected successfully" or "❌ WebSocket connection failed"
```

## Quick Fix Options

### Fix #1: Update WebSocket Endpoint Manually
If you know the correct endpoint, edit `ChatWebSocketClient.kt` line 31:
```kotlin
private val WS_ENDPOINTS = listOf(
    "ws://10.0.2.2:3000/YOUR_CORRECT_PATH"  // Update this!
)
```

### Fix #2: Backend Might Need CORS/Auth Configuration
Check if your backend WebSocket accepts the Authorization header:
```javascript
// Backend might need:
io.use((socket, next) => {
  const token = socket.handshake.auth.token;
  // Verify token...
  next();
});
```

## Expected Behavior After Fix

Once WebSocket connects successfully:
```
✅ ChatWebSocketClient: WebSocket Connected successfully to: ws://10.0.2.2:3000/ws
✅ MarketplaceViewModel: WebSocket connection status: true
✅ ChatWebSocketClient: Sent message: {"event":"join_conversation","conversationId":"..."}
✅ ChatWebSocketClient: ✅ Joined conversation successfully

// When message received:
✅ ChatWebSocketClient: 📨 Message received: {"event":"new_message","data":{...}}
✅ ChatWebSocketClient: ✅ Parsed new_message event: 691ba972f63518946568a152
✅ MarketplaceViewModel: WebSocket received message: 691ba972f63518946568a152
✅ ChatScreen: Messages changed, count: 24
```

## Next Steps

1. **MOST IMPORTANT**: Find your backend WebSocket endpoint path
2. Test the app with the updated WebSocket client
3. Share the logcat output filtering by `ChatWebSocketClient`
4. If still failing, we may need to:
   - Check backend WebSocket implementation
   - Implement HTTP polling as fallback
   - Use Socket.IO client library instead of raw WebSocket

## Alternative Solution: HTTP Polling

If WebSocket cannot be fixed, I can implement a polling mechanism:
- Poll `/conversations/{id}/messages` every 3-5 seconds
- Compare message counts and update UI
- Less efficient but will work immediately

Let me know which option you prefer!
