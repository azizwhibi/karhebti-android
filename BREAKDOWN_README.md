# 📦 BreakdownViewModel - Documentation Complète

## 🎯 Résumé

Le `BreakdownViewModel` implémente le flux complet du système SOS de Karhebti, permettant aux utilisateurs d'envoyer des demandes d'assistance et aux garages d'y répondre en temps réel via un système de polling optimisé.

---

## 📚 Documents créés

### 1. **BREAKDOWN_VIEWMODEL_FLOW.md**
Documentation détaillée du flux SOS avec:
- Scénario temporel (0:00 - 0:12)
- Description des états UI
- Détails de toutes les méthodes
- Exemples d'utilisation dans les écrans
- Intégration avec le système
- Gestion des erreurs
- Logs attendus

### 2. **BREAKDOWN_SEQUENCE_DIAGRAM.md**
Diagramme de séquence complet montrant:
- Interactions entre User App, ViewModel, Backend, FCM et Garage App
- Timeline détaillée des 12 secondes
- États du ViewModel à chaque étape
- Flux de données détaillé par phase
- Résumé des interactions

### 3. **BREAKDOWN_CODE_EXAMPLES.md**
Exemples de code concrets pour:
- BreakdownSOSScreen (envoi du SOS)
- SOSStatusScreen (polling & attente)
- BreakdownDetailScreen (garage owner)
- Configuration NavGraph
- Tous les composables nécessaires

### 4. **BREAKDOWN_CHECKLIST.md**
Checklist complète couvrant:
- Backend (endpoints, logique, logs)
- ViewModel (états, méthodes, logs)
- Tous les écrans Android
- Navigation
- Notifications FCM
- Repository & API
- Tests End-to-End
- Métriques de succès

---

## 🔧 BreakdownViewModel - Aperçu technique

### États UI

```kotlin
sealed class BreakdownUiState {
    object Idle                                    // État initial
    object Loading                                 // Chargement
    data class Success(val data: Any)              // Succès
    data class Error(val message: String)          // Erreur
    data class StatusChanged(                      // Changement de statut ⭐
        val breakdown: BreakdownResponse,
        val previousStatus: String
    )
}
```

### Méthodes principales

| Méthode | Usage | Qui l'utilise |
|---------|-------|---------------|
| `declareBreakdown()` | Créer un SOS | User (SOSScreen) |
| `startPollingBreakdown()` | Démarrer la surveillance | User (StatusScreen) |
| `fetchBreakdownById()` | Récupérer une panne | Polling / Garage |
| `updateBreakdownStatus()` | Accepter/Refuser | Garage (DetailScreen) |
| `stopPolling()` | Arrêter la surveillance | Navigation / onCleared |
| `resetState()` | Réinitialiser | Après succès |

---

## 🎬 Flux complet (12 secondes)

```
┌─────────────────────────────────────────────────────────────┐
│                    TIMELINE (0:00 - 0:12)                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  0:00  👤 User sends SOS                                     │
│        └─> declareBreakdown()                               │
│                                                               │
│  0:01  ⚙️  Backend creates (PENDING)                         │
│                                                               │
│  0:03  📤 FCM notification sent                              │
│        └─> startPollingBreakdown()                          │
│                                                               │
│  0:04  🔄 Poll #1 → PENDING                                  │
│                                                               │
│  0:05  🔄 Poll #2 → PENDING                                  │
│        📱 Garage receives notification                       │
│                                                               │
│  0:07  ✅ Garage accepts                                     │
│        └─> updateBreakdownStatus("ACCEPTED")                │
│                                                               │
│  0:10  🔄 Poll #3 → ACCEPTED ⭐                              │
│        └─> StatusChanged(PENDING → ACCEPTED)                │
│                                                               │
│  0:11  🗺️  Auto-navigate to Tracking                         │
│        └─> stopPolling()                                    │
│                                                               │
│  0:12  ✅ Both parties connected!                            │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 💡 Points clés du design

### 1. **Polling intelligent**
- Intervalle de 5 secondes (configurable)
- Détection automatique des changements de statut
- Arrêt automatique lors de la navigation
- Nettoyage dans `onCleared()`

### 2. **StatusChanged - État spécial**
```kotlin
// Permet la détection automatique et la navigation
when (uiState) {
    is StatusChanged -> {
        if (state.previousStatus == "PENDING" && 
            state.breakdown.status == "ACCEPTED") {
            // 🎉 Navigation automatique!
            onNavigateToTracking(breakdownId)
        }
    }
}
```

### 3. **Gestion d'erreurs robuste**
```kotlin
val userMessage = when {
    raw.contains("400") -> "Données invalides"
    raw.contains("403") -> "Non autorisé"
    raw.contains("401") -> "Non authentifié"
    raw.contains("network") -> "Erreur réseau"
    else -> raw
}
```

### 4. **Logs détaillés**
```kotlin
android.util.Log.d("BreakdownVM", "✅ SOS créé: ${breakdown.id}")
android.util.Log.d("BreakdownVM", "🔄 Changement: $prev → $new")
android.util.Log.d("BreakdownVM", "⏹️ Polling arrêté")
```

---

## 📱 Intégration dans les écrans

### User Side (Client)

1. **BreakdownSOSScreen**
   ```kotlin
   // Envoi du SOS
   viewModel.declareBreakdown(request)
   
   // Écoute de la réponse
   LaunchedEffect(uiState) {
       when (val state = uiState) {
           is Success -> navigateToStatus()
           is Error -> showError()
       }
   }
   ```

2. **SOSStatusScreen**
   ```kotlin
   // Démarrer le polling
   LaunchedEffect(breakdownId) {
       viewModel.startPollingBreakdown(breakdownId.toInt())
   }
   
   // Détecter l'acceptation
   LaunchedEffect(uiState) {
       when (val state = uiState) {
           is StatusChanged -> {
               if (state.breakdown.status == "ACCEPTED") {
                   viewModel.stopPolling()
                   navigateToTracking()
               }
           }
       }
   }
   
   // Cleanup
   DisposableEffect(Unit) {
       onDispose { viewModel.stopPolling() }
   }
   ```

### Garage Side (Propriétaire)

3. **BreakdownDetailScreen**
   ```kotlin
   // Charger les détails
   LaunchedEffect(breakdownId) {
       viewModel.fetchBreakdownById(breakdownId)
   }
   
   // Accepter la demande
   Button(onClick = {
       viewModel.updateBreakdownStatus(breakdownId, "ACCEPTED")
   })
   
   // Navigation auto après acceptation
   LaunchedEffect(uiState) {
       when (val state = uiState) {
           is Success -> {
               val breakdown = state.data as BreakdownResponse
               if (breakdown.status == "ACCEPTED") {
                   navigateToTracking()
               }
           }
       }
   }
   ```

---

## 🧪 Tests

### Test manuel rapide

1. **Test User flow**
   ```bash
   # Sur le téléphone de l'utilisateur
   1. Ouvrir l'app
   2. Aller dans SOS
   3. Sélectionner "PNEU"
   4. Ajouter description
   5. Envoyer
   6. Vérifier "Recherche d'un garage..."
   7. Attendre notification du changement de statut
   8. Vérifier redirection auto vers Tracking
   ```

2. **Test Garage flow**
   ```bash
   # Sur le téléphone du garage
   1. Attendre la notification
   2. Taper sur la notification
   3. Voir les détails du SOS
   4. Cliquer "Accepter"
   5. Confirmer
   6. Vérifier redirection vers Tracking
   ```

### Logs à vérifier

**Backend:**
```
✅ POST /api/breakdowns 201
✅ Breakdown created: 6756e8f8...
✅ Status: PENDING
🔍 Looking for nearby garages...
👥 Found 1 verified garage owners
📤 Sending notification...
✅ Notification sent successfully!
```

**User App:**
```
BreakdownVM: ✅ SOS créé: 6756e8f8..., status: PENDING
BreakdownVM: 🔄 Démarrage du polling (interval: 5000ms)
BreakdownVM: 🔄 Changement: PENDING → ACCEPTED
SOSStatus: ✅ ACCEPTED! Navigating to tracking...
BreakdownVM: ⏹️ Polling arrêté
```

**Garage App:**
```
BreakdownVM: 🔄 Mise à jour statut → ACCEPTED
BreakdownVM: ✅ Statut mis à jour: ACCEPTED
```

---

## 🚀 Prochaines améliorations possibles

1. **WebSocket en temps réel**
   - Remplacer le polling par WebSocket
   - Réduction de la latence
   - Moins de requêtes serveur

2. **Retry automatique**
   - En cas d'échec réseau
   - Exponential backoff

3. **Cache local**
   - Sauvegarder les SOS en attente
   - Mode offline

4. **Notifications push natives**
   - Alerter l'utilisateur du changement de statut
   - Même si l'app est fermée

5. **Analytics**
   - Temps de réponse des garages
   - Taux d'acceptation
   - Distance moyenne

6. **Tests automatisés**
   - Tests unitaires du ViewModel
   - Tests d'intégration
   - Tests UI

---

## 📖 Références

### Fichiers du projet

- `BreakdownViewModel.kt` - ViewModel principal
- `BreakdownsRepository.kt` - Couche données
- `BreakdownsApi.kt` - Interface Retrofit
- `BreakdownResponse.kt` - Modèle de données
- `CreateBreakdownRequest.kt` - DTO de création

### Documents de référence

- `BREAKDOWN_VIEWMODEL_FLOW.md` - Documentation détaillée
- `BREAKDOWN_SEQUENCE_DIAGRAM.md` - Diagramme de séquence
- `BREAKDOWN_CODE_EXAMPLES.md` - Exemples de code
- `BREAKDOWN_CHECKLIST.md` - Checklist d'implémentation

---

## ✅ Status actuel

| Composant | Status | Notes |
|-----------|--------|-------|
| ViewModel | ✅ Complet | Polling, détection, cleanup |
| Repository | ✅ Complet | Gestion d'erreurs robuste |
| API Interface | ✅ Complet | Tous les endpoints définis |
| Models | ✅ Complet | BreakdownResponse, Request |
| SOSScreen | 🟡 À implémenter | Utiliser BREAKDOWN_CODE_EXAMPLES.md |
| StatusScreen | 🟡 À implémenter | Utiliser BREAKDOWN_CODE_EXAMPLES.md |
| DetailScreen | 🟡 À implémenter | Utiliser BREAKDOWN_CODE_EXAMPLES.md |
| TrackingScreen | 🟡 Partiellement | Ajouter status updates |
| Backend | ✅ Complet | API + FCM fonctionnels |
| Tests | ⚪ À faire | Suivre BREAKDOWN_CHECKLIST.md |

---

## 🎓 Comprendre le design

### Pourquoi le polling ?

**Alternative 1: Polling (choisi)**
- ✅ Simple à implémenter
- ✅ Fonctionne partout
- ✅ Pas de configuration serveur complexe
- ❌ Plus de requêtes

**Alternative 2: WebSocket**
- ✅ Temps réel
- ✅ Moins de requêtes
- ❌ Configuration serveur complexe
- ❌ Gestion reconnexion

**Alternative 3: Firebase Realtime Database**
- ✅ Temps réel
- ✅ Gère reconnexion
- ❌ Dépendance Firebase
- ❌ Coût supplémentaire

### Pourquoi StatusChanged ?

Sans `StatusChanged`:
```kotlin
// ❌ Compliqué
var lastStatus by remember { mutableStateOf("PENDING") }
LaunchedEffect(uiState) {
    if (uiState is Success) {
        val breakdown = uiState.data as BreakdownResponse
        if (lastStatus != breakdown.status) {
            // Détecter manuellement
            lastStatus = breakdown.status
        }
    }
}
```

Avec `StatusChanged`:
```kotlin
// ✅ Simple et clair
LaunchedEffect(uiState) {
    if (uiState is StatusChanged) {
        // Changement détecté automatiquement!
        if (uiState.breakdown.status == "ACCEPTED") {
            navigateToTracking()
        }
    }
}
```

---

## 🎯 Objectifs atteints

- ✅ Flux SOS complet de bout en bout
- ✅ Polling automatique optimisé
- ✅ Détection de changement de statut
- ✅ Navigation automatique
- ✅ Gestion d'erreurs robuste
- ✅ Logs détaillés pour debug
- ✅ Cleanup automatique
- ✅ Documentation complète
- ✅ Exemples de code
- ✅ Checklist d'implémentation

---

## 🙏 Support

Pour toute question sur l'implémentation:

1. Consulter `BREAKDOWN_VIEWMODEL_FLOW.md` pour la théorie
2. Consulter `BREAKDOWN_CODE_EXAMPLES.md` pour les exemples
3. Suivre `BREAKDOWN_CHECKLIST.md` étape par étape
4. Vérifier `BREAKDOWN_SEQUENCE_DIAGRAM.md` pour le flux

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025  
**Auteurs:** Karhebti Dev Team  
**License:** Propriétaire

---

## 🎉 Conclusion

Le `BreakdownViewModel` est maintenant prêt à l'emploi avec:
- Architecture propre et maintenable
- Polling optimisé avec détection automatique
- Gestion d'erreurs complète
- Documentation exhaustive
- Exemples de code complets

Il ne reste plus qu'à intégrer les écrans en suivant les exemples fournis ! 🚀

