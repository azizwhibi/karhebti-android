# Network Connectivity Troubleshooting Guide

## Problem
Error: `Unable to resolve host "karhebti-backend.onrender.com": No address associated with hostname`

This error means your Android device/emulator **cannot connect to the internet** or **cannot resolve DNS** for the backend server.

## Solutions (Try in Order)

### 1. Check Backend Server Status
- Open your browser and visit: https://karhebti-backend.onrender.com
- **If it doesn't load**: The Render.com free tier server is "sleeping"
  - Wait 30-60 seconds for it to wake up
  - Refresh the page
  - Try again in the app

### 2. Restart Your Emulator/Device

#### For Android Emulator:
```
1. Close the emulator completely
2. In Android Studio, go to: Tools → Device Manager
3. Click the ▶️ button to restart your emulator
4. Wait for it to fully boot
5. Try the app again
```

#### For Physical Device:
```
1. Turn WiFi off and on again
2. Or restart your device
3. Ensure you have active internet connection
4. Try the app again
```

### 3. Check Computer's Internet Connection
If using an emulator, the emulator uses your computer's internet:
- Make sure your computer has working internet
- Try opening a website in your browser
- Check if firewall/antivirus is blocking connections

### 4. Try a Different Network
- Switch from WiFi to mobile data (or vice versa)
- Connect to a different WiFi network
- Some networks block certain domains

### 5. Clear App Data and Cache
```
Settings → Apps → Karhebti → Storage → Clear Cache → Clear Data
```

### 6. Check DNS Settings (Advanced)

#### On Physical Device:
```
1. Go to Settings → WiFi
2. Long press your connected network
3. Select "Modify Network"
4. Advanced Options → IP Settings → Static
5. DNS 1: 8.8.8.8 (Google DNS)
6. DNS 2: 8.8.4.4
7. Save and reconnect
```

#### On Emulator:
```
1. In emulator, go to Settings → Network & Internet
2. WiFi → Your network → Advanced
3. Set DNS to 8.8.8.8
```

### 7. Create New Emulator (If Current One Fails)
```
1. Android Studio → Tools → Device Manager
2. Click "Create Device"
3. Choose a phone model (e.g., Pixel 5)
4. Choose system image (e.g., Android 13)
5. Finish and start the new emulator
```

### 8. Verify App Permissions
The app needs these permissions (already configured):
- ✅ INTERNET
- ✅ ACCESS_NETWORK_STATE

These are in `AndroidManifest.xml` - no action needed.

### 9. Test Network Connectivity in Emulator
In the emulator's terminal or using ADB:
```bash
adb shell ping google.com
```
If this fails, the emulator has no internet.

### 10. Backend URL Configuration
The app is configured to use:
```
https://karhebti-backend.onrender.com
```

If you need to change this (e.g., for local development):
- Edit: `app/src/main/java/com/example/karhebti_android/data/api/ApiConfig.kt`
- Change the `BASE_URL` constant

## Recent Code Changes Made

✅ **Updated network security configuration** to explicitly trust the Render.com domain
✅ **Increased connection timeouts** from 30s to 60s
✅ **Enabled automatic retry** on connection failures
✅ **Added proper DNS handling** with system DNS resolver

## Still Not Working?

### Check Logcat for Specific Errors
Look for these tags:
- `okhttp.OkHttpClient`
- `AuthRepository`
- `RetrofitClient`

### Common Error Patterns:
1. **"Unable to resolve host"** → DNS/Network issue (use this guide)
2. **"Connection refused"** → Backend is down or URL is wrong
3. **"Timeout"** → Backend is very slow (wait for Render to wake up)
4. **"401 Unauthorized"** → Authentication issue (different problem)
5. **"404 Not Found"** → API endpoint doesn't exist (backend issue)

## Contact Support
If none of these work, provide:
1. Full error logs from Logcat
2. Your network type (WiFi/Mobile/Emulator)
3. Android version
4. Whether browser can access the backend URL

---
**Last Updated**: December 14, 2025

