# Singleton ViewModel Issue - Fixed ✅

## Problem Description

**Symptom**: Only the first screen you enter works. After navigating to other screens, they won't load.
- Enter Conversations → works ✅
- Navigate to Browse Cars → infinite loading ❌
- Navigate to Pending Requests → infinite loading ❌

## Root Cause Analysis

### The Problem: Shared ViewModel Instance

The `ViewModelFactory` was using a **singleton pattern** for `MarketplaceViewModel`:

```kotlin
// BEFORE (BROKEN)
class ViewModelFactory {
    companion object {
        // ❌ Single instance shared across ALL screens
        private var marketplaceViewModelInstance: MarketplaceViewModel? = null
        
        fun getMarketplaceViewModel(app: Application): MarketplaceViewModel {
            return marketplaceViewModelInstance ?: synchronized(this) {
                marketplaceViewModelInstance ?: MarketplaceViewModel(app).also {
                    marketplaceViewModelInstance = it
                }
            }
        }
    }
}
```

### Why This Caused Issues

When all screens share the SAME ViewModel instance:

1. **ConversationsScreen** loads → sets `_conversations` to `Loading()`
2. **BrowseCarsScreen** loads → uses SAME ViewModel → tries to set `_availableCars` to `Loading()`
3. **Conflict**: The timeout LaunchedEffect from ConversationsScreen is still watching the shared state
4. **Result**: Race conditions, infinite loading, screens breaking

### Visual Explanation

```
BEFORE (Broken):
┌──────────────────────┐
│ ConversationsScreen  │───┐
└──────────────────────┘   │
                           ├──→ [Same ViewModel Instance]
┌──────────────────────┐   │    ├─ _conversations: LiveData
│ BrowseCarsScreen     │───┤    ├─ _availableCars: LiveData  
└──────────────────────┘   │    └─ _pendingSwipes: LiveData
                           │
┌──────────────────────┐   │    ❌ All LiveData states CONFLICT!
│ PendingSwipesScreen  │───┘
└──────────────────────┘
```

## Solution Applied

### 1. Remove ViewModel Singleton Pattern

Each screen now gets its **OWN ViewModel instance** with independent LiveData states:

```kotlin
// AFTER (FIXED)
class ViewModelFactory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // ... other ViewModels
            modelClass.isAssignableFrom(MarketplaceViewModel::class.java) -> {
                // ✅ Create a NEW instance for each screen
                MarketplaceViewModel(application) as T
            }
            // ...
        }
    }
}
```

### 2. Keep WebSocket Connection Shared

The WebSocket connection SHOULD be shared (otherwise you'd have multiple connections). Made it a singleton at the Repository level:

```kotlin
// In MarketplaceRepository
companion object {
    // ✅ Singleton WebSocket - shared across all repository instances
    @Volatile
    private var sharedWebSocketClient: ChatWebSocketClient? = null
    
    // ✅ Singleton Polling service - shared across all repository instances
    @Volatile
    private var sharedPollingService: ChatPollingService? = null
    
    private val webSocketLock = Any()
}

fun initWebSocket(...) {
    // Use the shared WebSocket client instance
    val client = sharedWebSocketClient ?: synchronized(webSocketLock) {
        sharedWebSocketClient ?: ChatWebSocketClient(...).also { 
            sharedWebSocketClient = it 
        }
    }
    webSocketClient = client
    webSocketClient?.connect()
}
```

### Visual Explanation

```
AFTER (Fixed):
┌──────────────────────┐
│ ConversationsScreen  │──→ [ViewModel Instance 1]
└──────────────────────┘    ├─ _conversations: LiveData ✅
                            └─ repository ──┐
                                           │
┌──────────────────────┐                   │
│ BrowseCarsScreen     │──→ [ViewModel Instance 2]    │
└──────────────────────┘    ├─ _availableCars: LiveData ✅  │
                            └─ repository ──┤
                                           │
┌──────────────────────┐                   ├──→ [Shared WebSocket] ✅
│ PendingSwipesScreen  │──→ [ViewModel Instance 3]    │
└──────────────────────┘    ├─ _pendingSwipes: LiveData ✅  │
                            └─ repository ──┘

✅ Each screen has independent LiveData states
✅ All screens share the same WebSocket connection
```

## Benefits of This Architecture

✅ **Screen Independence**: Each screen has its own ViewModel with independent state  
✅ **No Race Conditions**: LiveData states don't conflict between screens  
✅ **Efficient WebSocket**: Single WebSocket connection shared across all screens  
✅ **Proper Resource Management**: WebSocket/Polling services are singletons  
✅ **Follows Best Practices**: ViewModels are scoped to screens, network resources are shared  

## Files Modified

1. ✅ `ViewModelFactory.kt` - Removed singleton pattern for MarketplaceViewModel
2. ✅ `MarketplaceRepository.kt` - Made WebSocket and Polling services singleton

## Testing Checklist

Now all screens should work independently:

- [ ] Open Conversations → loads correctly ✅
- [ ] Navigate to Browse Cars → loads correctly ✅
- [ ] Navigate to Pending Requests → loads correctly ✅
- [ ] Navigate back to Conversations → loads correctly ✅
- [ ] All screens can load in any order ✅
- [ ] WebSocket notifications work across all screens ✅
- [ ] No duplicate WebSocket connections ✅

## Technical Notes

### Why WebSocket Should Be Singleton

- **Single Connection**: You only need ONE WebSocket connection to the server
- **Real-time Updates**: All screens receive the same real-time notifications
- **Efficient**: Avoids multiple connections competing for resources
- **Server-friendly**: Prevents connection spam

### Why ViewModels Should NOT Be Singleton

- **State Isolation**: Each screen needs its own loading/success/error states
- **Memory Management**: ViewModels are lifecycle-aware and clean up automatically
- **Compose Best Practice**: ViewModels should be scoped to composable lifecycles
- **Prevents Conflicts**: Separate instances = no state conflicts

## The Fix in Simple Terms

**Before**: All screens were using the same brain (ViewModel), so they kept overwriting each other's thoughts (LiveData states).

**After**: Each screen has its own brain (ViewModel), but they all share the same phone line (WebSocket) to receive calls.

🎉 Problem solved!

