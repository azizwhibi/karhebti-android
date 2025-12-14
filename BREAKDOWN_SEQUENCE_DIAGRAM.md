# 🔄 Diagramme de Séquence - Flux SOS Complet

## Vue d'ensemble temporelle (0:00 - 0:12)

```
User App          BreakdownViewModel          Backend          FCM          Garage App
   │                      │                      │              │               │
   │                      │                      │              │               │
0:00 [Envoyer SOS]        │                      │              │               │
   │──────────────────────>│                      │              │               │
   │  declareBreakdown()  │                      │              │               │
   │                      │                      │              │               │
   │                  [Loading]                  │              │               │
   │                      │                      │              │               │
0:01                      │──POST /breakdowns──>│              │               │
   │                      │                      │              │               │
   │                      │                  [Create]           │               │
   │                      │                  status:PENDING     │               │
   │                      │                      │              │               │
0:02                      │                      │ [Find nearby │               │
   │                      │                      │  garages]    │               │
   │                      │                      │              │               │
0:03                      │                      │──Send FCM──>│               │
   │                      │                      │              │               │
   │                      │<────Success──────────│              │               │
   │                      │  {id, status:PENDING}│              │               │
   │                      │                      │              │               │
   │<─────Success─────────│                      │              │               │
   │  BreakdownResponse   │                      │              │               │
   │                      │                      │              │               │
   │  [Navigate to        │                      │              │               │
   │   SOSStatusScreen]   │                      │              │               │
   │                      │                      │              │               │
   │──Start Polling──────>│                      │              │               │
   │  startPollingBreakdown()                    │              │               │
   │                      │                      │              │               │
   │                   [Poll Loop]               │              │               │
   │                      │                      │              │               │
0:04                      │──GET /breakdowns/id─>│              │──Notification──>│
   │                      │                      │              │   arrives       │
   │                      │<─status:PENDING──────│              │                 │
   │<─Success(PENDING)────│                      │              │                 │
   │                      │                      │              │                 │
0:05                      │──GET /breakdowns/id─>│              │  [User taps]    │
   │                      │                      │              │                 │
   │                      │<─status:PENDING──────│              │                 │
   │<─Success(PENDING)────│                      │              │                 │
   │                      │                      │              │                 │
0:06                      │                      │              │     [App opens] │
   │                      │                      │              │                 │
   │                      │                      │              │      [Load SOS] │
   │                      │                      │<───GET /breakdowns/id──────────│
   │                      │                      │                                │
   │                      │                      │────Breakdown details──────────>│
   │                      │                      │    {type, location, desc}      │
   │                      │                      │                                │
0:07                      │                      │                  [User clicks  │
   │                      │                      │                   "Accepter"]  │
   │                      │                      │                                │
   │                      │                      │<───PATCH /breakdowns/id────────│
   │                      │                      │    {status: "ACCEPTED"}        │
   │                      │                      │                                │
0:08                      │                      │──[Update status]               │
   │                      │                      │   PENDING → ACCEPTED           │
   │                      │                      │                                │
   │                      │                      │────Success────────────────────>│
   │                      │                      │    {status: "ACCEPTED"}        │
   │                      │                      │                                │
   │                      │                      │                    [Navigate to│
   │                      │                      │                     Tracking]  │
   │                      │                      │                                │
0:10                      │──GET /breakdowns/id─>│                                │
   │                      │                      │                                │
   │                      │<─status:ACCEPTED─────│                                │
   │                      │                      │                                │
   │<──StatusChanged──────│                      │                                │
   │  {breakdown, prev:"PENDING"}                │                                │
   │                      │                      │                                │
   │  [Detect ACCEPTED!]  │                      │                                │
   │                      │                      │                                │
0:11 [Auto-navigate]      │                      │                                │
   │  to TrackingScreen   │                      │                                │
   │                      │                      │                                │
   │──stopPolling()──────>│                      │                                │
   │                      │                      │                                │
   │                  [Stopped]                  │                                │
   │                      │                      │                                │
0:12 🎉 CONNECTED!        │                      │                    🎉 CONNECTED!│
   │                      │                      │                                │
   │  [Show Map]          │                      │                    [Show Map]  │
   │  [User position]     │                      │                [Garage position]│
   │  [ETA: 15 min]       │                      │                 [Route to user]│
   │                      │                      │                                │
```

## États du ViewModel pendant le flux

```
Time    User App State              Garage App State
────────────────────────────────────────────────────────────
0:00    Idle                        -
0:01    Loading                     -
0:02    Success(breakdown)          -
        status: PENDING
0:03    [Navigate to Status]        -
        Polling started
0:04    Success(PENDING)            Notification received
0:05    Success(PENDING)            App opened
0:06    Success(PENDING)            Loading breakdown
0:07    Success(PENDING)            Success(breakdown)
0:08    Success(PENDING)            Loading (updating)
                                    Success(ACCEPTED)
0:10    StatusChanged!              -
        (PENDING → ACCEPTED)
0:11    [Navigate to Tracking]      [Navigate to Tracking]
        Polling stopped
0:12    Success(ACCEPTED)           Success(ACCEPTED)
```

## Flux de données détaillé

### Phase 1: Création du SOS (0:00 - 0:03)

```kotlin
// User clicks "Envoyer SOS"
viewModel.declareBreakdown(CreateBreakdownRequest(
    type = "PNEU",
    description = "Pneu crevé",
    latitude = 36.8065,
    longitude = 10.1815
))

// ViewModel
_uiState.value = Loading

// Repository
repo.createBreakdown(request).collect { result ->
    // Success
    _uiState.value = Success(BreakdownResponse(
        id = "6756e8f8abc123",
        status = "PENDING",
        type = "PNEU",
        latitude = 36.8065,
        longitude = 10.1815
    ))
}

// Backend
✅ POST /api/breakdowns 201
✅ Breakdown created: 6756e8f8abc123
✅ Status: PENDING
🔍 Looking for nearby garages...
📍 Found 1 verified garage owners
📤 Sending FCM notification...
✅ Notification sent!
```

### Phase 2: Polling (0:03 - 0:10)

```kotlin
// Auto-start polling after SOS creation
LaunchedEffect(breakdownId) {
    viewModel.startPollingBreakdown(breakdownId.toInt())
}

// ViewModel starts loop
pollingJob = viewModelScope.launch {
    while (true) {
        fetchBreakdownById(breakdownId)
        delay(5000) // 5 seconds
    }
}

// Every 5 seconds:
// 0:04 → GET /breakdowns/123 → status: PENDING
// 0:05 → GET /breakdowns/123 → status: PENDING
// 0:10 → GET /breakdowns/123 → status: ACCEPTED ✅
```

### Phase 3: Détection du changement (0:10)

```kotlin
// ViewModel detects change
fun fetchBreakdownById(id: Int) {
    // ...
    if (lastKnownStatus != null && 
        lastKnownStatus != breakdown.status) {
        
        Log.d("BreakdownVM", 
            "🔄 Changement: $lastKnownStatus → ${breakdown.status}")
        
        val previous = lastKnownStatus!!
        lastKnownStatus = breakdown.status
        
        _uiState.value = StatusChanged(breakdown, previous)
    }
}

// Screen handles change
LaunchedEffect(uiState) {
    when (val state = uiState) {
        is StatusChanged -> {
            if (state.previousStatus == "PENDING" && 
                state.breakdown.status == "ACCEPTED") {
                // 🎉 Navigation automatique!
                onNavigateToTracking(breakdownId)
            }
        }
    }
}
```

### Phase 4: Garage Owner accepte (0:07 - 0:08)

```kotlin
// Garage owner clicks "Accepter"
Button(onClick = {
    viewModel.updateBreakdownStatus(
        id = breakdownId,
        status = "ACCEPTED"
    )
}) {
    Text("✅ Accepter")
}

// ViewModel
fun updateBreakdownStatus(id: Int, status: String) {
    _uiState.value = Loading
    
    viewModelScope.launch {
        repo.updateBreakdownStatus(id, status).collect { result ->
            _uiState.value = result.fold(
                onSuccess = { breakdown ->
                    Log.d("BreakdownVM", "✅ Statut: ${breakdown.status}")
                    Success(breakdown)
                },
                onFailure = { Error(it.message) }
            )
        }
    }
}

// Backend
✅ PATCH /api/breakdowns/123 200
✅ Status updated: ACCEPTED
✅ assignedTo: prop.garage@example.com
```

### Phase 5: Navigation automatique (0:11)

```kotlin
// User's app
LaunchedEffect(uiState) {
    when (val state = uiState) {
        is StatusChanged -> {
            if (state.breakdown.status == "ACCEPTED") {
                // Stop polling
                viewModel.stopPolling()
                
                // Navigate to tracking
                navController.navigate(
                    "tracking/${state.breakdown.id}"
                )
            }
        }
    }
}

// Garage owner's app
LaunchedEffect(uiState) {
    when (val state = uiState) {
        is Success -> {
            val breakdown = state.data as BreakdownResponse
            if (breakdown.status == "ACCEPTED") {
                // Navigate to tracking
                navController.navigate(
                    "tracking/${breakdown.id}"
                )
            }
        }
    }
}
```

## Résumé des interactions

| Temps | Action | Actor | ViewModel Method | Backend Endpoint |
|-------|--------|-------|------------------|------------------|
| 0:00 | Envoyer SOS | User | `declareBreakdown()` | POST /breakdowns |
| 0:03 | Démarrer polling | User App | `startPollingBreakdown()` | - |
| 0:04 | Poll #1 | User App | `fetchBreakdownById()` | GET /breakdowns/:id |
| 0:05 | Poll #2 | User App | `fetchBreakdownById()` | GET /breakdowns/:id |
| 0:06 | Voir détails | Garage | `fetchBreakdownById()` | GET /breakdowns/:id |
| 0:07 | Accepter | Garage | `updateBreakdownStatus()` | PATCH /breakdowns/:id |
| 0:10 | Poll #3 (change!) | User App | `fetchBreakdownById()` | GET /breakdowns/:id |
| 0:11 | Auto-navigation | Both | `stopPolling()` | - |
| 0:12 | Tracking actif | Both | - | - |

## Points clés du design

### 1. Polling optimisé
- ✅ Intervalle de 5 secondes (configurable)
- ✅ Arrêt automatique lors de la navigation
- ✅ Nettoyage dans `onCleared()`

### 2. Détection de changement
- ✅ Comparaison avec `lastKnownStatus`
- ✅ État spécial `StatusChanged`
- ✅ Logs détaillés pour debug

### 3. Navigation automatique
- ✅ Pas besoin d'action utilisateur
- ✅ Transition fluide vers tracking
- ✅ Arrêt du polling avant navigation

### 4. Gestion d'erreurs
- ✅ Messages personnalisés par code erreur
- ✅ Retry possible sur erreur réseau
- ✅ Logs pour traçabilité

### 5. Synchronisation
- ✅ Les deux apps voient le même statut
- ✅ Backend est la source de vérité
- ✅ Pas de conflit possible

---

**Dernière mise à jour:** 14 décembre 2025

