# 🚨 BreakdownViewModel - Flux SOS Complet

## 📋 Vue d'ensemble

Le `BreakdownViewModel` gère le flux complet du système SOS, de l'envoi de la demande par l'utilisateur jusqu'à la connexion avec le garage acceptant l'intervention.

## ⏱️ Scénario temporel (12 secondes)

```
0:00  👤 User sends SOS                    → declareBreakdown()
0:01  ⚙️  Backend creates breakdown         (status: PENDING)
0:02  🔍 Backend finds garage owners
0:03  📤 Backend sends FCM notification
0:04  📱 Garage owner receives notification
0:05  👆 Garage owner taps notification
0:06  📋 Garage owner sees SOS details
0:07  ✅ Garage owner clicks "Accepter"
0:08  ⚙️  Backend updates status            → ACCEPTED
0:10  🔄 User app polls & detects change   → startPollingBreakdown()
0:11  🗺️  User app navigates to tracking
0:12  ✅ Both parties connected!
```

## 📦 États UI (BreakdownUiState)

```kotlin
sealed class BreakdownUiState {
    object Idle                    // État initial
    object Loading                 // Chargement en cours
    data class Success(data: Any)  // Opération réussie
    data class Error(message: String)  // Erreur
    data class StatusChanged(      // Changement de statut détecté
        breakdown: BreakdownResponse,
        previousStatus: String
    )
}
```

## 🔧 Fonctions principales

### 1️⃣ declareBreakdown() - Envoi du SOS

**Quand:** L'utilisateur appuie sur "Envoyer" dans l'écran SOS

**Paramètres:**
```kotlin
CreateBreakdownRequest(
    vehicleId: String?,
    type: String,              // "PNEU", "BATTERIE", "ACCIDENT"
    description: String?,
    latitude: Double,
    longitude: Double,
    photo: String?,
    userId: String?
)
```

**Retour:**
- ✅ `Success(BreakdownResponse)` - SOS créé avec ID et statut PENDING
- ❌ `Error(message)` - Erreur avec message personnalisé

**Exemple d'utilisation:**
```kotlin
val request = CreateBreakdownRequest(
    type = "PNEU",
    description = "Pneu crevé sur autoroute",
    latitude = 36.8065,
    longitude = 10.1815
)
viewModel.declareBreakdown(request)
```

**Logs attendus:**
```
✅ SOS créé: 6756e8f8..., status: PENDING
```

---

### 2️⃣ startPollingBreakdown() - Surveillance du statut

**Quand:** Immédiatement après la création du SOS réussie

**Paramètres:**
```kotlin
breakdownId: Int        // ID de la panne à surveiller
intervalMs: Long = 5000 // Intervalle de polling (défaut: 5s)
```

**Comportement:**
- 🔄 Appelle `fetchBreakdownById()` toutes les 5 secondes
- 🔍 Détecte automatiquement les changements de statut
- 📢 Émet `StatusChanged` quand le statut change

**Exemple d'utilisation:**
```kotlin
// Après succès du SOS
LaunchedEffect(breakdownId) {
    viewModel.startPollingBreakdown(breakdownId.toInt())
}
```

**Logs attendus:**
```
🔄 Démarrage du polling pour breakdown #123 (interval: 5000ms)
🔄 Changement de statut détecté: PENDING → ACCEPTED
```

---

### 3️⃣ fetchBreakdownById() - Récupération d'une panne

**Quand:** Appelée automatiquement par le polling

**Paramètres:**
```kotlin
id: Int  // ID de la panne
```

**Retour:**
- ✅ `Success(BreakdownResponse)` - Pas de changement
- 🔄 `StatusChanged(breakdown, previousStatus)` - Statut changé !

**Détection de changement:**
```kotlin
if (lastKnownStatus != null && lastKnownStatus != breakdown.status) {
    emit(StatusChanged(breakdown, lastKnownStatus))
}
```

---

### 4️⃣ updateBreakdownStatus() - Mise à jour du statut

**Quand:** Le garage owner accepte/refuse/complète l'intervention

**Paramètres:**
```kotlin
id: Int         // ID de la panne
status: String  // Nouveau statut
```

**Statuts possibles:**
- `"PENDING"` - En attente
- `"ACCEPTED"` - Accepté par un garage
- `"REFUSED"` - Refusé
- `"IN_PROGRESS"` - En cours d'intervention
- `"COMPLETED"` - Complété

**Exemple d'utilisation (Garage Owner):**
```kotlin
// Garage owner accepte la demande
viewModel.updateBreakdownStatus(
    id = breakdownId,
    status = "ACCEPTED"
)
```

**Logs attendus:**
```
🔄 Mise à jour statut breakdown #123 → ACCEPTED
✅ Statut mis à jour: ACCEPTED
```

---

### 5️⃣ stopPolling() - Arrêt du polling

**Quand:** 
- Navigation vers l'écran de tracking
- Fermeture de l'écran
- Destruction du ViewModel

**Exemple d'utilisation:**
```kotlin
DisposableEffect(Unit) {
    onDispose {
        viewModel.stopPolling()
    }
}
```

**Logs attendus:**
```
⏹️ Polling arrêté
```

---

### 6️⃣ resetState() - Réinitialisation

**Quand:** Avant de créer un nouveau SOS

**Exemple d'utilisation:**
```kotlin
viewModel.resetState()
```

---

## 📱 Intégration dans les écrans

### BreakdownSOSScreen.kt - Envoi du SOS

```kotlin
@Composable
fun BreakdownSOSScreen(
    onSOSSuccess: (breakdownId: String, type: String, lat: Double, lon: Double) -> Unit
) {
    val viewModel: BreakdownViewModel = viewModel(factory = BreakdownViewModelFactory(repo))
    val uiState by viewModel.uiState.collectAsState()

    // 1️⃣ Écouter les états UI
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BreakdownUiState.Success -> {
                val breakdown = state.data as BreakdownResponse
                // Navigation vers SOSStatusScreen
                onSOSSuccess(breakdown.id, breakdown.type, lat, lon)
            }
            is BreakdownUiState.Error -> {
                // Afficher l'erreur
                showError(state.message)
            }
            else -> {}
        }
    }

    // 2️⃣ Envoyer le SOS
    Button(onClick = {
        val request = CreateBreakdownRequest(
            type = selectedType,
            description = description,
            latitude = currentLat,
            longitude = currentLon
        )
        viewModel.declareBreakdown(request)
    }) {
        Text("Envoyer SOS")
    }
}
```

---

### SOSStatusScreen.kt - Attente & Polling

```kotlin
@Composable
fun SOSStatusScreen(
    breakdownId: String,
    onNavigateToTracking: (String) -> Unit
) {
    val viewModel: BreakdownViewModel = viewModel(factory = BreakdownViewModelFactory(repo))
    val uiState by viewModel.uiState.collectAsState()
    var currentStatus by remember { mutableStateOf("PENDING") }

    // 1️⃣ Démarrer le polling
    LaunchedEffect(breakdownId) {
        viewModel.startPollingBreakdown(breakdownId.toInt())
    }

    // 2️⃣ Écouter les changements de statut
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BreakdownUiState.StatusChanged -> {
                val newStatus = state.breakdown.status
                if (state.previousStatus == "PENDING" && newStatus == "ACCEPTED") {
                    // 🎉 Garage accepté ! Navigation automatique
                    onNavigateToTracking(breakdownId)
                }
                currentStatus = newStatus
            }
            is BreakdownUiState.Success -> {
                val breakdown = state.data as BreakdownResponse
                currentStatus = breakdown.status
            }
            else -> {}
        }
    }

    // 3️⃣ Arrêter le polling lors de la sortie
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPolling()
        }
    }

    // UI - Animation de recherche
    Column {
        when (currentStatus) {
            "PENDING" -> {
                CircularProgressIndicator()
                Text("Recherche d'un garage à proximité...")
            }
            "ACCEPTED" -> {
                Icon(Icons.Default.CheckCircle, tint = Color.Green)
                Text("Garage trouvé ! Redirection...")
            }
        }
    }
}
```

---

### BreakdownDetailScreen.kt - Garage Owner

```kotlin
@Composable
fun BreakdownDetailScreen(
    breakdownId: Int,
    onAccepted: () -> Unit
) {
    val viewModel: BreakdownViewModel = viewModel(factory = BreakdownViewModelFactory(repo))
    val uiState by viewModel.uiState.collectAsState()
    var breakdown by remember { mutableStateOf<BreakdownResponse?>(null) }

    // 1️⃣ Charger les détails
    LaunchedEffect(breakdownId) {
        viewModel.fetchBreakdownById(breakdownId)
    }

    // 2️⃣ Écouter les mises à jour
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BreakdownUiState.Success -> {
                breakdown = state.data as BreakdownResponse
                if (breakdown?.status == "ACCEPTED") {
                    // Navigation vers tracking
                    onAccepted()
                }
            }
            else -> {}
        }
    }

    // 3️⃣ Bouton d'acceptation
    Button(onClick = {
        viewModel.updateBreakdownStatus(breakdownId, "ACCEPTED")
    }) {
        Text("✅ Accepter")
    }
}
```

---

## 🔄 Flux de données complet

```
┌─────────────────────────────────────────────────────────────┐
│                    User's Phone (Client)                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  1️⃣ BreakdownSOSScreen                                       │
│     └─> declareBreakdown(request)                           │
│         ├─> Loading                                          │
│         └─> Success(breakdown) ✅                            │
│             └─> Navigate to SOSStatusScreen                  │
│                                                               │
│  2️⃣ SOSStatusScreen                                          │
│     └─> startPollingBreakdown(id)                           │
│         └─> 🔄 Poll every 5s                                │
│             ├─> Success(status: PENDING) ⏳                  │
│             ├─> Success(status: PENDING) ⏳                  │
│             └─> StatusChanged(PENDING → ACCEPTED) 🎉        │
│                 └─> Navigate to TrackingScreen              │
│                                                               │
│  3️⃣ BreakdownTrackingScreen                                 │
│     └─> stopPolling()                                        │
│     └─> Show map with both positions                        │
│                                                               │
└─────────────────────────────────────────────────────────────┘

                            ⬇️ FCM Notification ⬇️

┌─────────────────────────────────────────────────────────────┐
│                 Garage Owner's Phone                         │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  📱 Notification received                                     │
│     └─> Tap to open                                          │
│                                                               │
│  4️⃣ BreakdownDetailScreen                                    │
│     └─> fetchBreakdownById(id)                              │
│     └─> Show details (type, location, description)          │
│     └─> Button "Accepter" clicked                           │
│         └─> updateBreakdownStatus(id, "ACCEPTED")           │
│             └─> Success ✅                                   │
│                 └─> Navigate to TrackingScreen              │
│                                                               │
└─────────────────────────────────────────────────────────────┘

                            ⬆️ Backend updates ⬆️

┌─────────────────────────────────────────────────────────────┐
│                      Backend (Node.js)                       │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  POST /breakdowns                                            │
│  ├─> Create breakdown (status: PENDING)                     │
│  ├─> Find nearby garage owners                              │
│  ├─> Send FCM notifications                                 │
│  └─> Return breakdown response                              │
│                                                               │
│  PATCH /breakdowns/:id                                       │
│  ├─> Update status to ACCEPTED                              │
│  └─> Return updated breakdown                               │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 🧪 Tests et Logs

### Logs côté User

```
BreakdownVM: ✅ SOS créé: 6756e8f8abc123, status: PENDING
BreakdownVM: 🔄 Démarrage du polling pour breakdown #123 (interval: 5000ms)
BreakdownVM: 📋 Récupéré breakdown, status: PENDING
BreakdownVM: 📋 Récupéré breakdown, status: PENDING
BreakdownVM: 🔄 Changement de statut détecté: PENDING → ACCEPTED
SOSStatus: ✅ Status changed to ACCEPTED! Navigating to tracking...
BreakdownVM: ⏹️ Polling arrêté
```

### Logs côté Garage Owner

```
BreakdownVM: 📋 Récupéré breakdown #123
BreakdownVM: 🔄 Mise à jour statut breakdown #123 → ACCEPTED
BreakdownVM: ✅ Statut mis à jour: ACCEPTED
```

### Logs côté Backend

```
✅ POST /api/breakdowns 201 - 203ms
✅ Breakdown created: 6756e8f8abc123
✅ Status: PENDING
🔍 Looking for nearby garages...
📍 Breakdown location: 36.8065, 10.1815
👥 Found 1 verified garage owners
📤 Sending notification to prop.garage@example.com...
✅ Notification sent successfully!
---
✅ PATCH /api/breakdowns/123 200 - 45ms
✅ Status updated: ACCEPTED
```

---

## ⚠️ Gestion des erreurs

### Erreurs courantes et messages

| Code | Erreur Backend | Message User-Friendly |
|------|----------------|----------------------|
| 400 | Invalid request | "Données invalides : vérifiez le type et la description." |
| 401 | Not authenticated | "Non authentifié : veuillez vous reconnecter." |
| 403 | Forbidden | "Non autorisé : votre session peut avoir expiré." |
| Network | Connection failed | "Erreur réseau : vérifiez votre connexion." |

### Exemple de gestion d'erreur

```kotlin
when (val state = uiState) {
    is BreakdownUiState.Error -> {
        Snackbar(
            message = state.message,
            actionLabel = "Réessayer",
            onAction = { viewModel.declareBreakdown(request) }
        )
    }
}
```

---

## 🎯 Checklist d'intégration

- [x] BreakdownViewModel créé avec polling
- [x] StatusChanged state ajouté
- [x] startPollingBreakdown() implémenté
- [x] stopPolling() dans onCleared()
- [ ] BreakdownSOSScreen utilise declareBreakdown()
- [ ] SOSStatusScreen démarre le polling
- [ ] Navigation automatique sur ACCEPTED
- [ ] BreakdownDetailScreen utilise updateBreakdownStatus()
- [ ] Tests du flux complet

---

## 📚 Références

- **ViewModel:** `BreakdownViewModel.kt`
- **Repository:** `BreakdownsRepository.kt`
- **API:** `BreakdownsApi.kt`
- **Screens:**
  - `BreakdownSOSScreen.kt`
  - `SOSStatusScreen.kt`
  - `BreakdownDetailScreen.kt`
  - `BreakdownTrackingScreen.kt`

---

## 🚀 Prochaines améliorations

1. **WebSocket en temps réel** - Remplacer le polling par WebSocket
2. **Retry automatique** - En cas d'échec réseau
3. **Cache local** - Sauvegarder les SOS en attente
4. **Notifications locales** - Alerter l'utilisateur du changement de statut
5. **Analytics** - Tracker les temps de réponse des garages

---

**Dernière mise à jour:** 14 décembre 2025
**Version:** 1.0.0
**Auteur:** Karhebti Dev Team

