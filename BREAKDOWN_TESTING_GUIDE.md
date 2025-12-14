# 🧪 Tests du flux SOS - Guide pratique

## 📱 Prérequis

- Backend démarré sur `http://172.18.1.246:3000`
- 2 appareils ou émulateurs (User + Garage Owner)
- Les deux apps installées et connectées
- FCM configuré et tokens enregistrés

---

## 🔍 Test 1: Vérification des écrans

### Commande (dans Android Studio)
```bash
# Rechercher tous les écrans Breakdown/SOS
Get-ChildItem -Path "app\src\main\java" -Recurse -Filter "*Breakdown*.kt" | Select-Object Name
Get-ChildItem -Path "app\src\main\java" -Recurse -Filter "*SOS*.kt" | Select-Object Name
```

### Résultat attendu
```
BreakdownSOSScreen.kt ✅
SOSStatusScreen.kt ✅
BreakdownTrackingScreen.kt ✅
BreakdownDetailScreen.kt ✅
BreakdownHistoryScreen.kt ✅
BreakdownViewModel.kt ✅
```

---

## 🧪 Test 2: Vérification du ViewModel

### Commande (grep dans le code)
```kotlin
// Chercher les méthodes clés
startPollingBreakdown  ✅
stopPolling           ✅
StatusChanged         ✅
fetchBreakdownById    ✅
updateBreakdownStatus ✅
```

### Test dans le code
```kotlin
// Dans BreakdownViewModel.kt
class BreakdownViewModel {
    fun startPollingBreakdown() { ... } // ✅ Doit exister
    fun stopPolling() { ... }           // ✅ Doit exister
}

sealed class BreakdownUiState {
    data class StatusChanged(...) // ✅ Doit exister
}
```

---

## 🧪 Test 3: Vérification de la navigation

### Fichier à vérifier
`app/src/main/java/com/example/karhebti_android/navigation/NavGraph.kt`

### Routes à chercher
```kotlin
object BreakdownTracking : Screen("breakdown_tracking/{breakdownId}") ✅
object BreakdownDetail : Screen("breakdown_detail/{breakdownId}")     ✅

composable(Screen.BreakdownTracking.route) { ... }  ✅
composable(Screen.BreakdownDetail.route) { ... }    ✅
```

### Test grep
```bash
# Dans PowerShell
Select-String -Path "app\src\main\java\com\example\karhebti_android\navigation\NavGraph.kt" -Pattern "BreakdownTracking"
Select-String -Path "app\src\main\java\com\example\karhebti_android\navigation\NavGraph.kt" -Pattern "BreakdownDetail"
```

---

## 🧪 Test 4: Test E2E manuel (User side)

### Étapes
1. ✅ Lancer l'app (User)
2. ✅ Se connecter
3. ✅ Aller sur Home
4. ✅ Cliquer sur bouton SOS
5. ✅ **Vérifier:** Navigation vers BreakdownSOSScreen
6. ✅ Sélectionner "PNEU"
7. ✅ Entrer description "Pneu crevé sur autoroute"
8. ✅ **Vérifier:** Position GPS détectée
9. ✅ Cliquer "Envoyer SOS"
10. ✅ Confirmer dans le dialog
11. ✅ **Vérifier:** Navigation vers SOSStatusScreen
12. ✅ **Vérifier logs:**
    ```
    SOSStatus: 🔄 Démarrage du polling pour breakdown XXX
    ```

### Logs attendus (Logcat)
```
BreakdownVM: ✅ SOS créé: 6756e8f8..., status: PENDING
SOSStatus: 🔄 Démarrage du polling pour breakdown 123 (interval: 5000ms)
SOSStatus: 📊 Status: PENDING
```

### Backend logs attendus
```
✅ POST /api/breakdowns 201 - 203ms
✅ Breakdown created: 6756e8f8...
✅ Status: PENDING
🔍 Looking for nearby garages...
📤 Sending notification...
✅ Notification sent successfully!
```

---

## 🧪 Test 5: Test polling (User side)

### Observations
1. ✅ Écran SOSStatusScreen affiché
2. ✅ Animation de recherche
3. ✅ Texte "Recherche d'un garage..."
4. ✅ **Attendre 5 secondes**
5. ✅ **Vérifier logs:** Poll #1

### Logs attendus
```
SOSStatus: 📊 Status: PENDING
[5 secondes]
SOSStatus: 📊 Status: PENDING
[5 secondes]
SOSStatus: 📊 Status: PENDING
```

### Backend logs (toutes les 5s)
```
GET /api/breakdowns/123
Status: PENDING
```

---

## 🧪 Test 6: Test notification (Garage side)

### Étapes
1. ✅ **Attendre notification** sur le téléphone du garage
2. ✅ **Vérifier:** Notification apparaît
3. ✅ **Vérifier:** Titre "🚨 Nouvelle demande SOS"
4. ✅ **Vérifier:** Body "Assistance PNEU demandée"
5. ✅ **Vérifier:** Son + vibration
6. ✅ Taper sur la notification
7. ✅ **Vérifier:** App ouvre BreakdownDetailScreen

### Notification payload attendu
```json
{
  "notification": {
    "title": "🚨 Nouvelle demande SOS",
    "body": "Assistance PNEU demandée"
  },
  "data": {
    "type": "breakdown",
    "breakdownId": "123",
    "breakdownType": "PNEU"
  }
}
```

---

## 🧪 Test 7: Test acceptation (Garage side)

### Étapes
1. ✅ Sur BreakdownDetailScreen
2. ✅ **Vérifier:** Type "PNEU" affiché
3. ✅ **Vérifier:** Description affichée
4. ✅ **Vérifier:** Position GPS affichée
5. ✅ **Vérifier:** Distance calculée
6. ✅ Cliquer "Accepter"
7. ✅ **Vérifier:** Dialog de confirmation
8. ✅ Cliquer "Confirmer"
9. ✅ **Vérifier logs:**
    ```
    BreakdownVM: 🔄 Mise à jour statut → ACCEPTED
    BreakdownVM: ✅ Statut mis à jour: ACCEPTED
    ```
10. ✅ **Vérifier:** Navigation vers BreakdownTrackingScreen

### Backend logs attendus
```
PATCH /api/breakdowns/123
Body: { "status": "ACCEPTED" }
✅ Status updated: ACCEPTED
Response: 200 OK
```

---

## 🧪 Test 8: Test détection changement (User side)

### Timeline
```
0:00  Garage accepte
0:01  Backend met à jour status
0:02  [Poll #1] Status: PENDING
0:07  [Poll #2] Status: ACCEPTED ⭐
```

### Logs attendus (User)
```
SOSStatus: 📊 Status: PENDING
[5 secondes]
SOSStatus: 🔄 Changement détecté: PENDING → ACCEPTED
SOSStatus: ✅ ACCEPTED! Navigation vers tracking...
[1 seconde délai]
SOSStatus: 🧹 Arrêt du polling
[Navigation]
```

### Vérifications
1. ✅ Log "🔄 Changement détecté"
2. ✅ Log "✅ ACCEPTED!"
3. ✅ Log "🧹 Arrêt du polling"
4. ✅ Navigation automatique vers tracking
5. ✅ UI change: "Garage trouvé!" pendant 1s
6. ✅ Puis redirection

---

## 🧪 Test 9: Test tracking (Both sides)

### User side
1. ✅ BreakdownTrackingScreen affiché
2. ✅ Carte avec 2 positions
3. ✅ Info garage affichée
4. ✅ Distance affichée
5. ✅ ETA affiché
6. ✅ Bouton "Appeler" fonctionnel

### Garage side
1. ✅ BreakdownTrackingScreen affiché
2. ✅ Carte avec 2 positions
3. ✅ Info client affichée
4. ✅ Distance affichée
5. ✅ Bouton "Appeler" fonctionnel
6. ✅ Bouton "Marquer comme complété" visible

---

## 🧪 Test 10: Test cleanup (User side)

### Étapes
1. ✅ Sur SOSStatusScreen (pendant polling)
2. ✅ Appuyer sur bouton back
3. ✅ **Vérifier logs:**
    ```
    SOSStatus: 🧹 Arrêt du polling
    ```
4. ✅ **Vérifier:** Retour à Home
5. ✅ **Vérifier:** Plus de logs de polling

### Vérification dans Logcat
```bash
# Chercher les logs après le back
# Ne doit PAS trouver:
SOSStatus: 📊 Status: PENDING

# Doit trouver:
SOSStatus: 🧹 Arrêt du polling
```

---

## 🧪 Test 11: Test complet (12 secondes)

### Timeline complète
```
0:00  User sends SOS
0:01  Backend creates (PENDING)
0:03  Backend sends notification + polling starts
0:04  Garage receives notification
0:05  Poll #1 → PENDING
0:06  Garage taps notification
0:07  Garage clicks "Accepter"
0:08  Backend updates (ACCEPTED)
0:10  Poll #2 → ACCEPTED ⭐
0:11  User navigates to tracking
0:12  Both on tracking screen ✅
```

### Chronomètre
- ✅ Démarrer chrono à l'envoi du SOS
- ✅ Arrêter chrono à l'affichage du tracking
- ✅ **Temps attendu:** < 15 secondes

---

## 📊 Checklist de vérification

### Avant les tests
- [ ] Backend démarré
- [ ] Firebase configuré
- [ ] 2 devices configurés
- [ ] User connecté
- [ ] Garage owner connecté
- [ ] Logcat ouvert sur les 2 devices

### Pendant les tests
- [ ] Logs User visibles
- [ ] Logs Garage visibles
- [ ] Logs Backend visibles
- [ ] Chronomètre prêt

### Après les tests
- [ ] Tous les logs vérifiés
- [ ] Temps total < 15s
- [ ] Aucune erreur dans Logcat
- [ ] Navigation correcte
- [ ] Polling arrêté proprement

---

## 🐛 Debugging

### Problème: Polling ne démarre pas

**Vérifier:**
```kotlin
// Dans SOSStatusScreen.kt
LaunchedEffect(breakdownId) {
    if (breakdownId != null) {
        viewModel.startPollingBreakdown(...)
    }
}
```

**Log attendu:**
```
SOSStatus: 🔄 Démarrage du polling pour breakdown XXX
```

---

### Problème: Changement non détecté

**Vérifier:**
```kotlin
// Dans SOSStatusScreen.kt
LaunchedEffect(uiState) {
    when (val state = uiState) {
        is BreakdownUiState.StatusChanged -> {
            // ...
        }
    }
}
```

**Log attendu:**
```
SOSStatus: 🔄 Changement détecté: PENDING → ACCEPTED
```

---

### Problème: Navigation ne fonctionne pas

**Vérifier NavGraph.kt:**
```kotlin
onNavigateToTracking = { id ->
    navController.navigate(Screen.BreakdownTracking.createRoute(id)) {
        popUpTo(Screen.SOSStatus.route) { inclusive = true }
    }
}
```

---

### Problème: Polling ne s'arrête pas

**Vérifier:**
```kotlin
// Dans SOSStatusScreen.kt
DisposableEffect(Unit) {
    onDispose {
        viewModel.stopPolling()
    }
}
```

**Log attendu:**
```
SOSStatus: 🧹 Arrêt du polling
```

---

## ✅ Résultat attendu

Si tous les tests passent:
- ✅ Flux complet fonctionnel
- ✅ Polling optimisé
- ✅ Détection automatique
- ✅ Navigation automatique
- ✅ Cleanup automatique
- ✅ Temps total < 15s

---

**Date:** 14 décembre 2025  
**Version:** 1.0.0

