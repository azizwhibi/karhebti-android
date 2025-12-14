# ⚡ Quick Start - Flux SOS en 5 minutes

## 🎯 Résumé ultra-rapide

Le flux SOS de Karhebti permet à un utilisateur d'envoyer une demande d'assistance qui est automatiquement notifiée aux garages proches. Le système utilise un **polling optimisé** pour détecter quand un garage accepte la demande.

**Temps total du flux:** 12 secondes max

---

## 🚀 Implémentation en 3 étapes

### Étape 1️⃣: Le ViewModel (✅ DÉJÀ FAIT)

Le `BreakdownViewModel` est déjà implémenté dans:
```
app/src/main/java/com/example/karhebti_android/viewmodel/BreakdownViewModel.kt
```

**Ce qu'il fait:**
- Crée un SOS → `declareBreakdown()`
- Lance le polling → `startPollingBreakdown()`
- Détecte les changements → Émet `StatusChanged`
- Arrête le polling → `stopPolling()`

---

### Étape 2️⃣: Les écrans (À IMPLÉMENTER)

#### A. BreakdownSOSScreen - Envoi du SOS

```kotlin
// Copier depuis BREAKDOWN_CODE_EXAMPLES.md
@Composable
fun BreakdownSOSScreen(...) {
    val viewModel: BreakdownViewModel = viewModel(factory = BreakdownViewModelFactory(repo))
    
    // Envoyer le SOS
    Button(onClick = {
        viewModel.declareBreakdown(CreateBreakdownRequest(
            type = "PNEU",
            latitude = 36.8065,
            longitude = 10.1815
        ))
    })
    
    // Navigation après succès
    LaunchedEffect(uiState) {
        if (uiState is Success) {
            navigateToStatus()
        }
    }
}
```

#### B. SOSStatusScreen - Attente avec polling

```kotlin
// Copier depuis BREAKDOWN_CODE_EXAMPLES.md
@Composable
fun SOSStatusScreen(...) {
    val viewModel: BreakdownViewModel = viewModel(factory = BreakdownViewModelFactory(repo))
    
    // Démarrer le polling
    LaunchedEffect(breakdownId) {
        viewModel.startPollingBreakdown(breakdownId.toInt())
    }
    
    // Navigation automatique sur ACCEPTED
    LaunchedEffect(uiState) {
        if (uiState is StatusChanged && 
            uiState.breakdown.status == "ACCEPTED") {
            viewModel.stopPolling()
            navigateToTracking()
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }
}
```

#### C. BreakdownDetailScreen - Garage accepte

```kotlin
// Copier depuis BREAKDOWN_CODE_EXAMPLES.md
@Composable
fun BreakdownDetailScreen(...) {
    val viewModel: BreakdownViewModel = viewModel(factory = BreakdownViewModelFactory(repo))
    
    // Accepter la demande
    Button(onClick = {
        viewModel.updateBreakdownStatus(breakdownId, "ACCEPTED")
    })
    
    // Navigation après acceptation
    LaunchedEffect(uiState) {
        if (uiState is Success) {
            val breakdown = uiState.data as BreakdownResponse
            if (breakdown.status == "ACCEPTED") {
                navigateToTracking()
            }
        }
    }
}
```

---

### Étape 3️⃣: Navigation (À CONFIGURER)

Dans `NavGraph.kt`:

```kotlin
// Route pour l'envoi du SOS
composable(Screen.SOS.route) {
    BreakdownSOSScreen(
        onSOSSuccess = { id, type, lat, lon ->
            navController.navigate("sos_status/$id/$type/$lat/$lon")
        }
    )
}

// Route pour le statut avec polling
composable("sos_status/{breakdownId}/{type}/{lat}/{lon}") {
    SOSStatusScreen(
        breakdownId = it.arguments?.getString("breakdownId") ?: "",
        onNavigateToTracking = { id ->
            navController.navigate("tracking/$id") {
                popUpTo("sos_status/{...}") { inclusive = true }
            }
        }
    )
}

// Route pour les détails (garage)
composable("breakdown_detail/{breakdownId}") {
    BreakdownDetailScreen(
        breakdownId = it.arguments?.getInt("breakdownId") ?: 0,
        onAccepted = {
            navController.navigate("tracking/${it}")
        }
    )
}
```

---

## 🎬 Timeline du flux

```
0:00  User: Envoyer SOS                → declareBreakdown()
0:01  Backend: Créer SOS (PENDING)
0:03  Backend: Envoyer notification FCM
      User: Démarrer polling           → startPollingBreakdown()
0:05  Garage: Recevoir notification
0:07  Garage: Accepter                → updateBreakdownStatus("ACCEPTED")
0:10  User: Détecter changement       → StatusChanged
0:11  User: Navigation automatique     → stopPolling()
0:12  ✅ Connexion établie!
```

---

## 🔍 Vérification rapide

### Backend logs attendus:
```
✅ POST /api/breakdowns 201
✅ Breakdown created: 6756e8f8...
✅ Status: PENDING
🔍 Looking for nearby garages...
📤 Sending notification...
✅ Notification sent successfully!
```

### User app logs attendus:
```
BreakdownVM: ✅ SOS créé: 6756e8f8..., status: PENDING
BreakdownVM: 🔄 Démarrage du polling (interval: 5000ms)
BreakdownVM: 🔄 Changement: PENDING → ACCEPTED
BreakdownVM: ⏹️ Polling arrêté
```

### Garage app logs attendus:
```
BreakdownVM: 🔄 Mise à jour statut → ACCEPTED
BreakdownVM: ✅ Statut mis à jour: ACCEPTED
```

---

## ✅ Checklist minimal

**ViewModel:**
- [x] BreakdownViewModel créé ✅
- [x] StatusChanged state ✅
- [x] startPollingBreakdown() ✅
- [x] stopPolling() ✅

**À faire:**
- [ ] Implémenter BreakdownSOSScreen
- [ ] Implémenter SOSStatusScreen avec polling
- [ ] Implémenter BreakdownDetailScreen
- [ ] Configurer les routes de navigation
- [ ] Tester le flux complet

---

## 📚 Documentation complète

Pour aller plus loin:

1. **BREAKDOWN_INDEX.md** - Index de tous les documents
2. **BREAKDOWN_CODE_EXAMPLES.md** - Code complet à copier
3. **BREAKDOWN_CHECKLIST.md** - Checklist détaillée
4. **BREAKDOWN_VIEWMODEL_FLOW.md** - Documentation technique
5. **BREAKDOWN_SEQUENCE_DIAGRAM.md** - Flux visuel

---

## 🎯 3 choses à retenir

1. **Le ViewModel fait tout le travail** - Polling, détection, cleanup automatique
2. **StatusChanged = Navigation automatique** - Pas d'action utilisateur nécessaire
3. **stopPolling() est crucial** - Toujours l'appeler dans DisposableEffect

---

## 🚨 Points d'attention

⚠️ **Polling:** Démarrer dans LaunchedEffect, arrêter dans DisposableEffect  
⚠️ **Navigation:** Vérifier `previousStatus == "PENDING"` avant de naviguer  
⚠️ **Cleanup:** Le ViewModel s'occupe du cleanup dans `onCleared()`

---

## 💡 Conseil pro

Testez d'abord avec les logs. Si vous voyez:
```
✅ SOS créé
🔄 Démarrage du polling
🔄 Changement: PENDING → ACCEPTED
```

Alors votre implémentation est correcte ! 🎉

---

**Temps de lecture:** 5 minutes  
**Temps d'implémentation:** 2-4 heures  
**Difficulté:** ⭐⭐⭐☆☆

**Version:** 1.0.0  
**Date:** 14 décembre 2025

---

## 🆘 Besoin d'aide ?

- Consultez **BREAKDOWN_CODE_EXAMPLES.md** pour le code complet
- Suivez **BREAKDOWN_CHECKLIST.md** étape par étape
- Regardez **BREAKDOWN_SEQUENCE_DIAGRAM.md** pour visualiser

**Bon code ! 🚀**

