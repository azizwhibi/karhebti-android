# URGENT: App Crash After Login - Debugging Guide

## 🔴 Current Issue
App crashes immediately after successful login when trying to navigate to HomeScreen.

## 🔧 What I Just Fixed

### 1. Added Comprehensive Logging ✅
I've added detailed logging throughout the login flow to pinpoint exactly where the crash occurs:

**LoginScreen.kt** - Now logs:
- Auth state changes
- Navigation attempts
- Any errors during navigation

**AuthViewModel.kt** - Now logs:
- Login start
- API call results
- Token saving
- Any exceptions

**MainActivity.kt** - Now logs:
- App initialization
- Any startup errors

### 2. Added Error Handling ✅
- Wrapped navigation in try-catch
- Added error handling to token saving
- Protected against null pointers

## 📊 How to See What's Causing the Crash

### Step 1: Open Logcat
1. In Android Studio, click on "Logcat" tab at the bottom
2. Clear the log: Click the 🗑️ icon
3. Run your app

### Step 2: Filter for Errors
In the search box, type: `tag:LoginScreen|tag:AuthViewModel|tag:MainActivity`

### Step 3: Try to Login
1. Enter email and password
2. Click login
3. **Watch the Logcat output**

You should see logs like:
```
LoginScreen: Auth State Changed: Loading
AuthViewModel: Starting login for: test@test.com
AuthViewModel: Login result: Success(...)
AuthViewModel: Login successful, saving token...
AuthViewModel: Token and user saved successfully
LoginScreen: Login Success - User: test@test.com
LoginScreen: Navigation triggered successfully
```

**If you see a crash**, the last log message before the crash will tell us exactly where it's failing.

## 🎯 Most Likely Causes

### Cause 1: Backend Not Running
**Symptom**: Error message about network connection
**Solution**: 
```bash
# In your backend directory:
cd path/to/nestjs-backend
npm run start:dev
```
Then try again.

### Cause 2: Wrong API Response Format
**Symptom**: Crash happens at "saving token"
**Check**: Your backend is returning the correct response format:
```json
{
  "access_token": "jwt-token-here",
  "user": {
    "_id": "user-id",
    "email": "test@test.com",
    "nom": "Test",
    "prenom": "User",
    "role": "user",
    "telephone": "1234567890"
  }
}
```

### Cause 3: Navigation/HomeScreen Issue
**Symptom**: Crash happens at "Navigation triggered"
**Check**: The logs will show if it gets past navigation

### Cause 4: Missing Permissions
**Already Fixed** - network_security_config.xml is in place

## 🚨 Emergency Test Mode

If the app keeps crashing, try this **temporary bypass** to see if HomeScreen itself has issues:

### Option A: Test Without Backend
Modify `LoginScreen.kt` temporarily:

```kotlin
Button(
    onClick = {
        // TEMPORARY - Skip validation and backend
        onLoginSuccess()
    },
    // ... rest of button
)
```

If this works, the problem is with the API call or token saving.
If this STILL crashes, the problem is with HomeScreen or navigation.

### Option B: Add a Simple Test Screen
Replace HomeScreen in NavGraph temporarily with a simple test:

```kotlin
composable(Screen.Home.route) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Login Successful!", style = MaterialTheme.typography.headlineLarge)
    }
}
```

If this works, the issue is specifically in HomeScreen.

## 📝 What to Check in Logcat

Look for these specific error patterns:

### Pattern 1: NullPointerException
```
FATAL EXCEPTION: main
java.lang.NullPointerException: ...
```
**Meaning**: Something expected a value but got null
**Location**: The stack trace will show the exact line

### Pattern 2: ClassCastException
```
java.lang.ClassCastException: cannot be cast to ...
```
**Meaning**: Type mismatch in data
**Fix**: Check API response format

### Pattern 3: NetworkOnMainThreadException
```
android.os.NetworkOnMainThreadException
```
**Meaning**: Network call on UI thread (shouldn't happen with our coroutines)
**Fix**: Already handled, but check if it appears

### Pattern 4: Navigation Error
```
IllegalArgumentException: navigation destination ... is not found
```
**Meaning**: NavGraph route mismatch
**Fix**: Verify all route names match

## 🔍 Step-by-Step Debugging

### Test 1: Check Backend Connection
Run this test first:
1. Open the app
2. Try to login
3. Look for: `AuthViewModel: Starting login for: ...`
4. Then look for: `AuthViewModel: Login result: ...`

**If you see "Login result: Error"** → Backend issue
**If you see "Login result: Success"** → Go to Test 2

### Test 2: Check Token Saving
After seeing Success, look for:
```
AuthViewModel: Login successful, saving token...
AuthViewModel: Token and user saved successfully
```

**If you see an error here** → TokenManager issue
**If you see "saved successfully"** → Go to Test 3

### Test 3: Check Navigation
Look for:
```
LoginScreen: Navigation triggered successfully
```

**If you DON'T see this** → Navigation issue
**If you DO see this but app crashes** → HomeScreen issue

## 🛠️ Quick Fixes to Try

### Fix 1: Clean Install
```bash
# Uninstall the app completely
adb uninstall com.example.karhebti_android

# Then rebuild and install
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

### Fix 2: Clear App Data
In the emulator:
1. Settings → Apps → Karhebti
2. Storage → Clear Data
3. Try again

### Fix 3: Verify Backend URL
Check `ApiConfig.kt`:
```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/"
```

For emulator, this MUST be `10.0.2.2`, NOT `localhost`

### Fix 4: Test Backend Directly
In your browser or Postman:
```
POST http://localhost:3000/auth/login
Body: {
  "email": "test@test.com",
  "motDePasse": "password123"
}
```

Should return the auth response with token and user.

## 📞 Next Steps

**After running the app, send me:**
1. The last 20 lines from Logcat (especially any red ERROR lines)
2. Where exactly the crash happens (based on the logs)
3. Any error messages you see

**The logs will show:**
- ✅ If backend is reachable
- ✅ If login succeeds
- ✅ If token saves
- ✅ If navigation triggers
- ✅ Where exactly it crashes

## 🎯 Expected Success Logs

When everything works, you should see this sequence:
```
MainActivity: onCreate started
MainActivity: setContent completed successfully
LoginScreen: Auth state is null or initial
[User enters credentials and clicks login]
LoginScreen: Auth State Changed: Loading
AuthViewModel: Starting login for: test@test.com
[API call happens]
AuthViewModel: Login result: Success(...)
AuthViewModel: Login successful, saving token...
AuthViewModel: Token and user saved successfully
LoginScreen: Auth State Changed: Success
LoginScreen: Login Success - User: test@test.com
LoginScreen: Navigation triggered successfully
[HomeScreen should appear]
```

If you see all these logs, the app should work!

---

**Run the app now and check Logcat immediately!**
The logs will tell us exactly what's happening.

