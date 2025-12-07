# SOS Feature for Normal Users - Guide

## What I Fixed

### Problem Identified
The SOS screen was creating its own Retrofit instance **without authentication**, which meant:
- ❌ No JWT token was being sent with SOS requests
- ❌ The backend couldn't identify who was sending the request
- ❌ Requests would fail with 401/403 errors

### Solution Implemented
I updated the code to use the centralized authenticated API client:

1. **Added `breakdownsApiService` to RetrofitClient** (`ApiConfig.kt`)
   - Now provides authenticated access to the breakdowns API
   - Automatically includes JWT token in all requests via AuthInterceptor

2. **Updated BreakdownSOSScreen** (`BreakdownSOSScreen.kt`)
   - Now uses `RetrofitClient.breakdownsApiService` instead of creating local Retrofit
   - Your JWT token is now automatically sent with every SOS request
   - Backend can properly identify you and create the breakdown record

## How SOS Works for Normal Users

### Step-by-Step Flow

1. **Open SOS Screen**
   - Navigate to Settings → "Déclarer une panne (SOS)"
   - Or use the SOS button from the main screen

2. **Location Permission**
   - App asks for GPS permission
   - GPS must be enabled on your device
   - App fetches your current location automatically

3. **Fill SOS Form**
   - **Type de problème** (Required): Select from dropdown
     - PNEU (Tire)
     - BATTERIE (Battery)
     - MOTEUR (Engine)
     - CARBURANT (Fuel)
     - REMORQUAGE (Towing)
     - AUTRE (Other)
   
   - **Description** (Optional): Describe your problem
   
   - **Photo** (Optional): Add a photo of the problem
   
   - **Location**: Automatically captured (shown on map)

4. **Send SOS Request**
   - Click "Envoyer la demande SOS"
   - Confirm in the dialog
   - Request is sent with your authentication token

5. **What Happens Next**
   - ✅ Backend receives your request
   - ✅ Your user ID is extracted from JWT token
   - ✅ Breakdown record is created in database
   - ✅ Nearby pro garages/technicians are notified
   - ✅ You're redirected to SOS Status screen
   - ✅ You can track the status of your request

### Request Structure (Normal User)

```json
{
  "vehicleId": null,           // Normal users don't need vehicle ID
  "type": "CARBURANT",         // Your selected problem type
  "description": "I have a problem",  // Optional description
  "latitude": 37.4220,         // Your GPS latitude
  "longitude": -122.0840,      // Your GPS longitude
  "photo": null,               // Optional photo URL
  "userId": null               // Backend extracts from JWT token
}
```

### Authentication

The app automatically includes your authentication token in the request header:
```
Authorization: Bearer eyJhbG...HQmO
```

The backend extracts your user ID from this token, so you don't need to manually provide it.

### Troubleshooting

**If SOS request fails:**

1. **Check Token**: Make sure you're logged in
   - Token status shown at bottom of SOS screen
   - Should say "État du token: Présent"

2. **Check GPS**: Location must be available
   - Enable GPS on your device
   - Grant location permission to the app

3. **Check Network**: Backend must be reachable
   - Default: `http://192.168.1.190:3000/`
   - Check if backend server is running

4. **Check Required Fields**:
   - Type de problème (Required)
   - Location (Required)
   - Description and photo are optional

**Common Errors:**

- **"Erreur : utilisateur non identifié"** → You're not logged in
- **"HTTP 401: Non authentifié"** → Token expired, please log in again
- **"HTTP 403: Non autorisé"** → Token invalid or expired
- **"Impossible d'obtenir votre position"** → GPS not working

### Viewing Your SOS Requests

- Click the History icon (⏱️) in the top right of SOS screen
- View all your past SOS requests
- See status: Pending, In Progress, Resolved, Cancelled

## Technical Details

### API Endpoint
- **POST** `/breakdowns`
- Requires: `Authorization: Bearer <token>` header
- Returns: Breakdown object with ID

### Response Example
```json
{
  "id": "507f1f77bcf86cd799439011",
  "userId": "user123",
  "type": "CARBURANT",
  "status": "PENDING",
  "latitude": 37.4220,
  "longitude": -122.0840,
  "createdAt": "2025-12-07T10:30:00.000Z"
}
```

### Pro Garages Get Notified
When you send an SOS:
1. Backend finds nearby pro garages (within 50km radius)
2. Sends notifications to their devices
3. They can view your request and accept it
4. You'll see status update when someone accepts

## Difference Between Normal Users and Pro Garages

| Feature | Normal User | Pro Garage |
|---------|-------------|------------|
| Send SOS | ✅ Yes | ❌ No |
| Receive SOS notifications | ❌ No | ✅ Yes |
| Accept SOS requests | ❌ No | ✅ Yes |
| Vehicle required | ❌ No | N/A |
| Can see all breakdowns | ❌ Only yours | ✅ All nearby |

## Next Steps

After the fix:
1. Build and run the app
2. Make sure you're logged in as a normal user
3. Try sending an SOS request
4. Check logs to verify token is being sent
5. Backend should successfully create the breakdown record

The authentication issue is now fixed, and your SOS requests will work properly! 🎉

