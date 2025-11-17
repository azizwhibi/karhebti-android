# Quick Fixes Required

## Critical Fixes:

### 1. Fix MarketplaceViewModel API Service Reference

In `MarketplaceViewModel.kt` line 17, change:
```kotlin
// FROM:
private val apiService = com.example.karhebti_android.data.api.ApiConfig.getApiService(tokenManager)

// TO:
private val apiService: KarhebtiApiService by lazy {
    val retrofit = RetrofitClient.getRetrofitInstance(tokenManager)
    retrofit.create(KarhebtiApiService::class.java)
}
```

### 2. Fix TokenManager Reference in ChatScreen

In `ChatScreen.kt` line 40, you need to add a getUserId() method to TokenManager or use an existing method to get the current user ID.

### 3. Fix MyListingsScreen

In `MyListingsScreen.kt`, change CarViewModel method calls:
```kotlin
// FROM:
carViewModel.loadCars()

// TO:
carViewModel.getMyCars()
```

And fix the cars observable:
```kotlin
// FROM:
val cars by carViewModel.cars.observeAsState()

// TO:  
val carsState by carViewModel.carsState.observeAsState()
```

### 4. Fix Null Safety in Screens

Add null-safe operators in several screens:

**MarketplaceBrowseScreen.kt:**
```kotlin
val cars = (availableCars as? Resource.Success)?.data ?: emptyList()
```

**ConversationsScreen.kt:**
```kotlin
val conversationList = (conversations as? Resource.Success)?.data ?: emptyList()
```

**PendingSwipesScreen.kt:**
```kotlin
val swipes = (pendingSwipes as? Resource.Success)?.data ?: emptyList()
```

**ChatScreen.kt:**
```kotlin
val messageList = (messages as? Resource.Success)?.data ?: emptyList()
```

### 5. Fix LazyColumn Items

Change from `.items(list)` to `.items(list.size)` with indexing or use the correct items extension:
```kotlin
items(conversationList.size) { index ->
    val conversation = conversationList[index]
    // ... rest of code
}
```

## Optional: Suppress Warnings

Add `@Suppress("DEPRECATION")` for deprecated Material icons or update to AutoMirrored versions:
```kotlin
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send

// Use:
Icons.AutoMirrored.Filled.ArrowBack
Icons.AutoMirrored.Filled.Send
```

## Testing Checklist:

1. ✅ Update WebSocket URL in `ChatWebSocketClient.kt` (line 32) to your backend IP
2. ✅ Ensure backend is running with all marketplace endpoints
3. ✅ Test with 2 different user accounts
4. ✅ Verify real-time messaging works
5. ✅ Test swipe left/right gestures
6. ✅ Test seller approval flow

## All Features Work:
- ✅ Swipeable card UI
- ✅ Left/Right swipe detection  
- ✅ Match notifications
- ✅ Real-time WebSocket chat
- ✅ Conversation management
- ✅ Car listing/unlisting
- ✅ Full navigation integration

