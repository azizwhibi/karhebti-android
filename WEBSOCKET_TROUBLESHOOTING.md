# WebSocket Connection Troubleshooting Guide 🔧

## Current Error

```
❌ WebSocket connection failed
Error: IOException - unexpected end of stream on http://192.168.1.190:3000/...
```

## What This Error Means

The error "unexpected end of stream" during WebSocket handshake means:
- The server **accepted the HTTP connection** (TCP connected successfully)
- But **immediately closed it** before completing the WebSocket upgrade handshake
- This indicates the server either:
  1. Doesn't have a WebSocket endpoint at this path
  2. Is rejecting the connection due to authentication
  3. Requires different headers or protocol

## Most Likely Causes & Solutions

### ❌ Cause 1: Backend WebSocket Not Running (MOST LIKELY)

**Check if your Node.js backend has WebSocket configured:**

```javascript
// Your backend should have something like:
const WebSocket = require('ws');
const wss = new WebSocket.Server({ server: httpServer, path: '/chat' });

// OR using Socket.IO:
const io = require('socket.io')(httpServer);
```

**Solution**: 
1. Check your backend code for WebSocket configuration
2. Make sure the WebSocket server is actually running
3. Verify the path (could be `/`, `/chat`, `/socket.io`, etc.)

### ❌ Cause 2: Wrong WebSocket Path

Your backend might use a different path. Common paths:
- `ws://192.168.1.190:3000` (root)
- `ws://192.168.1.190:3000/chat`
- `ws://192.168.1.190:3000/ws`
- `ws://192.168.1.190:3000/socket.io` (if using Socket.IO)

**Solution**: I've updated the code to try the root path. If that doesn't work, we need to know the correct path from your backend.

### ❌ Cause 3: Backend Uses Socket.IO

If your backend uses Socket.IO (very common with Node.js), raw WebSocket won't work.

**Check your backend for**:
```javascript
const io = require('socket.io')(server);
```

**Solution**: If this is the case, we need to use the Socket.IO client (which I tried earlier but got polling errors).

### ❌ Cause 4: Authentication Issues

The server might be rejecting connections with invalid tokens.

**Solution**: Check the token being sent in logs:
```
Token (first 20 chars): eyJhbGciOiJIUzI1NiIs...
```

## What I Changed

### 1. Updated WebSocket URL
```kotlin
// OLD
private const val WS_URL = "ws://192.168.1.190:3000/chat"

// NEW - trying root path
private const val WS_URL = "ws://192.168.1.190:3000"
```

### 2. Added Extensive Debug Logging

Now you'll see:
- The token being sent (first 20 chars)
- Exact error details
- Response codes and headers when connection succeeds

## Next Steps - Action Required! 🚨

**I need you to check your backend code and tell me:**

### Question 1: What WebSocket library does your backend use?
- [ ] `ws` (raw WebSocket)
- [ ] `socket.io` (Socket.IO)
- [ ] Other: ___________

### Question 2: What is the WebSocket endpoint path?
Look for:
```javascript
// Example 1 - raw ws
const wss = new WebSocket.Server({ server, path: '/chat' });

// Example 2 - Socket.IO
const io = require('socket.io')(server, {
  path: '/socket.io'  // or could be '/chat'
});
```

The `path` value is what we need!

### Question 3: Does your backend require authentication for WebSocket?
Look for:
```javascript
wss.on('connection', (ws, req) => {
  const token = req.headers.authorization;
  // If there's token verification here, auth is required
});
```

## Temporary Workaround

Since WebSocket is failing, the app is **automatically falling back to HTTP Polling**, which works but is less efficient:

```
⚠️ WebSocket failed, switching to HTTP Polling
🔄 Initializing HTTP Polling service
```

**This means:**
- ✅ Chat still works
- ✅ Messages are delivered (polling every 3 seconds)
- ❌ Higher latency
- ❌ More battery usage
- ❌ More server load

## Quick Backend Tests

### Test 1: Check if WebSocket Server is Running

From your computer (where the backend runs), try:
```bash
# Linux/Mac
wscat -c ws://localhost:3000

# Or using curl
curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" http://localhost:3000
```

### Test 2: Check with the IP
```bash
wscat -c ws://192.168.1.190:3000
```

If these commands work, the server is running. If they fail with the same error, the WebSocket server isn't configured.

## Backend Examples

### If You're Using `ws` Library:
```javascript
const WebSocket = require('ws');
const wss = new WebSocket.Server({ 
  server: httpServer,
  path: '/chat'  // This is the path we need!
});

wss.on('connection', (ws, req) => {
  console.log('Client connected');
  
  ws.on('message', (message) => {
    console.log('Received:', message);
  });
});
```

### If You're Using Socket.IO:
```javascript
const io = require('socket.io')(httpServer, {
  path: '/socket.io',  // Usually this
  cors: {
    origin: "*"
  }
});

io.on('connection', (socket) => {
  console.log('Client connected');
});
```

## Current Status

✅ HTTP Polling is working (chat functional but slower)  
❌ WebSocket connection failing (need backend info)  
⏳ Waiting for backend configuration details  

## What to Do Now

1. **Check your backend code** for WebSocket configuration
2. **Tell me which library you're using** (`ws` or `socket.io`)
3. **Tell me the path** (e.g., `/`, `/chat`, `/socket.io`)
4. **Or share your backend WebSocket setup code** and I'll configure the Android client correctly

Once I know these details, I can configure the Android client to connect properly! 🚀

