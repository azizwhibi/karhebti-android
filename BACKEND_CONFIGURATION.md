# Backend Configuration Guide

## 🔧 Switching Between Production and Local Development

The app is now configured to support both:
- **Production**: `https://karhebti-backend.onrender.com` (default)
- **Local Development**: `http://10.0.2.2:3000`

### How to Switch to Local Development (Port 3000)

1. **Open `ApiConfig.kt`**
   - Location: `app/src/main/java/com/example/karhebti_android/data/api/ApiConfig.kt`
   - Find line 17: `private const val USE_LOCAL_SERVER = false`
   - Change it to: `private const val USE_LOCAL_SERVER = true`

2. **Open `ImageUrlHelper.kt`**
   - Location: `app/src/main/java/com/example/karhebti_android/util/ImageUrlHelper.kt`
   - Find line 8: `private const val USE_LOCAL_SERVER = false`
   - Change it to: `private const val USE_LOCAL_SERVER = true`

3. **Rebuild the app** (Clean & Rebuild)

### Important Notes

#### For Android Emulator
- Uses `http://10.0.2.2:3000`
- `10.0.2.2` is the special address that emulator uses to access host machine's `localhost`

#### For Physical Device
If testing on a physical device, you need to:
1. Find your computer's local IP address:
   ```bash
   # On Windows
   ipconfig
   # Look for "IPv4 Address" (e.g., 192.168.1.100)
   ```

2. Update both files to use your IP:
   ```kotlin
   private const val LOCAL_URL = "http://192.168.1.100:3000/"
   ```

3. Make sure your device and computer are on the same WiFi network

### Current Configuration

```
✅ Production URL: https://karhebti-backend.onrender.com/
✅ Local URL: http://10.0.2.2:3000/ (for emulator)
✅ Currently using: PRODUCTION (USE_LOCAL_SERVER = false)
```

### Quick Switch Reference

| Environment | USE_LOCAL_SERVER | Backend URL |
|------------|------------------|-------------|
| Production (Render.com) | `false` | `https://karhebti-backend.onrender.com/` |
| Local Development | `true` | `http://10.0.2.2:3000/` |

### Starting Your Local Backend

Before switching to local development, make sure your backend is running:

```bash
# Navigate to your backend directory
cd path/to/backend

# Start the server on port 3000
npm start
# or
node server.js
```

Verify it's running by opening `http://localhost:3000` in your browser.

### Troubleshooting

**Issue**: App can't connect after switching to local server
- ✅ Check that backend is running on port 3000
- ✅ Verify you're using Android Emulator (not physical device)
- ✅ Try restarting the emulator
- ✅ Check if your firewall is blocking port 3000

**Issue**: Physical device can't connect
- ✅ Use your computer's IP instead of `10.0.2.2`
- ✅ Ensure both are on the same WiFi network
- ✅ Check firewall settings

---
**Last Updated**: December 14, 2025

