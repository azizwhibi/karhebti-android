# Complete SOS Notification Flow - Implementation Guide

## 🎯 Overview

This document explains the complete end-to-end flow for SOS requests from normal users to pro garages, including FCM notifications, polling, and status updates.

## 📱 Complete Flow Timeline

```
0:00  ✅ User sends SOS request (CARBURANT)
0:01  ✅ Backend creates breakdown (status: PENDING)
0:02  ✅ Backend finds nearby garage owners
0:03  ✅ Backend sends FCM notification to garage
0:04  🔔 Garage owner's phone receives notification
0:05  👆 Garage owner taps notification
0:06  👀 Garage owner sees SOS details screen
0:07  ✅ Garage owner clicks "Accepter"
0:08  ✅ Backend updates status to ACCEPTED
0:10  🔄 User app polls and detects status change
0:11  🚀 User app auto-navigates to tracking screen
0:12  ✅ Both parties connected!
```

## 🔧 What Was Implemented

### 1. **Authenticated API for Breakdowns** ✅
- **File**: `ApiConfig.kt`
- **Added**: `breakdownsApiService` property to `RetrofitClient`
- **Purpose**: Ensures all breakdown requests include JWT token for authentication
- **Key Change**: SOS requests now automatically include `Authorization: Bearer <token>` header

### 2. **Enhanced Notification Service** ✅
- **File**: `KarhebtiMessagingService.kt`
- **Added Features**:
  - Multiple notification channels (SOS, Messages, Documents)
  - SOS-specific notifications with high priority and vibration
  - Status update notifications
  - Different styling and actions per notification type
  
**Notification Types**:
```kotlin
- "new_breakdown" / "sos_request" → Red urgent notification
- "breakdown_status_update" → Green success notification  
- "new_message" → Standard message notification
```

### 3. **Breakdowns List Screen for Pro Garages** ✅
- **File**: `BreakdownsListScreen.kt`
- **Features**:
  - Shows all PENDING SOS requests
  - Auto-refreshes every 10 seconds
  - Manual refresh button
  - Click to view details
  - Visual indicators (status badges, icons)

### 4. **Breakdown Detail Screen** ✅
- **File**: `BreakdownDetailScreen.kt`
- **Features**:
  - Full SOS request details
  - Interactive map showing client location
  - Accept/Reject buttons with confirmation dialogs
  - Updates breakdown status via API

### 5. **Enhanced SOS Status Screen with Polling** ✅
- **File**: `SOSStatusScreen.kt`
- **Features**:
  - Polls backend every 5 seconds for status changes
  - Auto-detects when status changes from PENDING → ACCEPTED
  - Auto-navigates to tracking screen when accepted
  - Visual feedback for each status

### 6. **Repository & ViewModel Updates** ✅
- **Files**: `BreakdownsRepository.kt`, `BreakdownViewModel.kt`
- **Added Methods**:
  - `fetchBreakdownById()` - Get single breakdown
  - `updateBreakdownStatus()` - Change status (ACCEPTED, REFUSED, etc.)
  - `getAllBreakdowns()` - List all with optional filters

## 🔄 Complete User Flow

### **For Normal Users (Send SOS)**

1. **Open SOS Screen**
   ```
   Settings → "Déclarer une panne (SOS)"
   ```

2. **Fill Form**
   - Select type: CARBURANT, PNEU, BATTERIE, etc.
   - Add description (optional)
   - Add photo (optional)
   - Location captured automatically

3. **Send Request**
   - Click "Envoyer la demande SOS"
   - Confirm in dialog
   - Request sent with JWT token

4. **Status Screen Shown**
   - Shows PENDING status
   - Polls every 5 seconds
   - Displays: "Recherche d'un garage..."

5. **Auto-Navigation When Accepted**
   - When garage accepts, status → ACCEPTED
   - Screen shows: "🎉 Garage trouvé!"
   - Auto-redirects to tracking screen

### **For Pro Garages (Receive & Accept SOS)**

1. **Receive FCM Notification**
   ```
   🔔 Notification appears:
   Title: "🚨 Nouvelle demande SOS"
   Body: "Assistance CARBURANT demandée"
   Sound + Vibration
   ```

2. **Tap Notification**
   - Opens app
   - Shows Breakdowns List Screen
   - Or navigates directly to detail screen

3. **View SOS Details**
   - See problem type, description
   - View client location on map
   - See distance (if calculated)

4. **Accept Request**
   - Click "✅ Accepter"
   - Confirm in dialog
   - API call updates status to ACCEPTED

5. **Client Notified**
   - Backend sends notification to client
   - Client app detects status change via polling
   - Client navigates to tracking

## 📡 Backend Requirements

### **POST /breakdowns**
Create new breakdown (SOS request)

**Headers**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Body**:
```json
{
  "vehicleId": null,
  "type": "CARBURANT",
  "description": "I have a problem",
  "latitude": 37.4220,
  "longitude": -122.0840,
  "photo": null
}
```

**Response**:
```json
{
  "id": "6756e8f8...",
  "userId": "user123",
  "type": "CARBURANT",
  "status": "PENDING",
  "latitude": 37.4220,
  "longitude": -122.0840,
  "createdAt": "2025-12-07T10:30:00.000Z"
}
```

**Backend Actions**:
1. Extract `userId` from JWT token
2. Create breakdown record
3. Find nearby garage owners (within 50km)
4. Send FCM notifications to their devices

### **GET /breakdowns/:id**
Get single breakdown by ID

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Response**:
```json
{
  "id": "6756e8f8...",
  "userId": "user123",
  "type": "CARBURANT",
  "status": "PENDING",
  "latitude": 37.4220,
  "longitude": -122.0840,
  "description": "I have a problem",
  "createdAt": "2025-12-07T10:30:00.000Z"
}
```

### **PATCH /breakdowns/:id**
Update breakdown status

**Headers**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Body**:
```json
{
  "status": "ACCEPTED"
}
```

**Response**:
```json
{
  "id": "6756e8f8...",
  "status": "ACCEPTED",
  "updatedAt": "2025-12-07T10:35:00.000Z"
}
```

**Backend Actions**:
1. Update breakdown status
2. Send FCM notification to client (status update)

### **GET /breakdowns?status=PENDING**
List all breakdowns with optional filters

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Query Parameters**:
- `status` (optional): Filter by status
- `userId` (optional): Filter by user

**Response**:
```json
{
  "breakdowns": [
    {
      "id": "6756e8f8...",
      "userId": "user123",
      "type": "CARBURANT",
      "status": "PENDING",
      "latitude": 37.4220,
      "longitude": -122.0840
    }
  ]
}
```

## 🔔 FCM Notification Payload

### **For Garage Owners (New SOS)**
```json
{
  "notification": {
    "title": "🚨 Nouvelle demande SOS",
    "body": "Assistance CARBURANT demandée"
  },
  "data": {
    "type": "new_breakdown",
    "breakdownId": "6756e8f8...",
    "problemType": "CARBURANT",
    "latitude": "37.4220",
    "longitude": "-122.0840"
  },
  "to": "<garage_fcm_token>"
}
```

### **For Clients (Status Update)**
```json
{
  "notification": {
    "title": "✅ Garage trouvé!",
    "body": "Un garage a accepté votre demande"
  },
  "data": {
    "type": "breakdown_status_update",
    "breakdownId": "6756e8f8...",
    "status": "ACCEPTED"
  },
  "to": "<client_fcm_token>"
}
```

## 🔍 Polling Mechanism

The user's app polls for status changes every 5 seconds:

```kotlin
LaunchedEffect(breakdownId) {
    if (breakdownId != null) {
        while (true) {
            viewModel.fetchBreakdownById(breakdownId.toInt())
            delay(5000) // Poll every 5 seconds
        }
    }
}
```

**When status changes**:
```kotlin
if (currentStatus == "PENDING" && newStatus == "ACCEPTED") {
    onNavigateToTracking(breakdownId)
}
```

## 🎨 Status States

| Status | User Sees | Garage Sees | Color |
|--------|-----------|-------------|-------|
| `PENDING` | "⏱️ En attente de réponse..." | "En attente" (can accept/reject) | Orange |
| `ACCEPTED` | "✅ Accepté" → Auto-navigate | "Accepté" | Green |
| `REFUSED` | "❌ Refusé" | "Refusé" | Red |
| `IN_PROGRESS` | "🚗 En cours" | "En cours" | Blue |
| `COMPLETED` | "✓ Terminé" | "Terminé" | Green |

## 📂 File Structure

```
app/src/main/java/com/example/karhebti_android/
├── data/
│   ├── api/
│   │   └── ApiConfig.kt (✅ Added breakdownsApiService)
│   ├── notifications/
│   │   └── KarhebtiMessagingService.kt (✅ Enhanced with SOS notifications)
│   └── CreateBreakdownRequest.kt
├── repository/
│   └── BreakdownsRepository.kt (✅ Added new methods)
├── viewmodel/
│   └── BreakdownViewModel.kt (✅ Added new methods)
├── ui/screens/
│   ├── BreakdownSOSScreen.kt (✅ Fixed authentication)
│   ├── SOSStatusScreen.kt (✅ Added polling)
│   ├── BreakdownsListScreen.kt (✅ NEW - For garages)
│   └── BreakdownDetailScreen.kt (✅ NEW - For accepting)
└── network/
    └── BreakdownsApi.kt
```

## 🧪 Testing Checklist

### **Normal User Flow**
- [ ] Login as normal user
- [ ] Open SOS screen
- [ ] Fill form and send
- [ ] See "Demande SOS reçue" screen
- [ ] Status shows PENDING
- [ ] Polling indicator appears

### **Pro Garage Flow**
- [ ] Login as pro garage
- [ ] FCM notification received when user sends SOS
- [ ] Tap notification opens app
- [ ] See SOS in list
- [ ] Tap to view details
- [ ] Accept the request
- [ ] Confirmation shown

### **Status Update**
- [ ] User's screen updates within 5 seconds
- [ ] Status changes to ACCEPTED
- [ ] Auto-navigation to tracking screen
- [ ] FCM notification sent to user

### **Backend Logs**
```
✅ POST /api/breakdowns 201 - 203ms
✅ JWT Auth Successful
✅ Breakdown created: 6756e8f8...
✅ Status: PENDING

🔍 Looking for nearby garages...
📍 Breakdown location: 37.4220, -122.0840
👥 Found 1 verified garage owner:
   - prop.garage@example.com
   
📤 Sending notification to prop.garage@example.com...
🔐 FCM Token: eYxRk7F_Sa2...
✅ Notification sent successfully!
   Response: projects/karhebti/messages/0:1234567890

💾 Notification saved to database
📊 Summary: 1 sent, 0 failed
```

## 🚀 Next Steps

1. **Build and run the app**
2. **Test with two devices** (one normal user, one pro garage)
3. **Check backend logs** for notification sending
4. **Verify FCM tokens** are registered correctly
5. **Test the complete flow** end-to-end

## 🐛 Troubleshooting

**Notification not received?**
- Check FCM token is registered in backend
- Verify notification permissions granted
- Check device is connected to internet
- Look for errors in backend logs

**Polling not working?**
- Check JWT token is valid
- Verify backend endpoint returns breakdown
- Check logs for API errors

**Auto-navigation not triggered?**
- Verify status changed from PENDING to ACCEPTED
- Check logs for status change detection
- Ensure navigation callback is provided

## ✅ Summary

All missing pieces have been implemented:

1. ✅ Authenticated API for SOS requests
2. ✅ Enhanced FCM notification service
3. ✅ Breakdowns list screen for garages
4. ✅ Breakdown detail screen with accept/reject
5. ✅ Polling mechanism for status updates
6. ✅ Auto-navigation when status changes
7. ✅ Repository and ViewModel methods

**The complete SOS notification flow is now ready to test!** 🎉

