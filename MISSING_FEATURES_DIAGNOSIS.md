# 🚨 PROBLÈME IDENTIFIÉ - Étapes manquantes dans le flux SOS

## 📋 Date: 14 décembre 2025

## ❌ Étapes non fonctionnelles

```
0:04  Garage owner reçoit notification ❌ NON FONCTIONNEL
0:07  Garage owner accepte ❌ NON FONCTIONNEL  
0:08  Backend met à jour (ACCEPTED) ❌ NON FONCTIONNEL
0:10  Polling détecte changement ❌ NON FONCTIONNEL
0:11  Navigation auto → Tracking ❌ NON FONCTIONNEL
```

---

## 🔍 Diagnostic

### Problème 1: Backend ne notifie pas les garages ❌

**Ce qui devrait se passer:**
```
User envoie SOS
└─> Backend crée breakdown (status: PENDING)
    └─> Backend cherche garages à proximité
        └─> Backend envoie notification FCM aux garages ❌ MANQUANT
```

**Ce qui manque:**
- Endpoint backend pour trouver garages à proximité
- Logique backend pour envoyer FCM aux garages
- Base de données des tokens FCM des garage owners

---

### Problème 2: Garage owner ne peut pas accepter ❌

**Ce qui devrait se passer:**
```
Garage owner reçoit notification
└─> Tap notification
    └─> App ouvre BreakdownDetailScreen
        └─> Clique "Accepter"
            └─> Backend met à jour status → ACCEPTED ❌ PEUT-ÊTRE MANQUANT
```

**Ce qui existe déjà:**
- ✅ BreakdownDetailScreen (UI pour accepter/refuser)
- ✅ API `PATCH /breakdowns/{id}` (mise à jour status)
- ✅ ViewModel `updateBreakdownStatus()`

**Ce qui pourrait manquer:**
- Configuration FCM côté garage owner
- Gestion des notifications FCM dans l'app
- Navigation depuis notification vers BreakdownDetailScreen

---

### Problème 3: Polling ne détecte pas le changement ❌

**Ce qui devrait se passer:**
```
User app poll toutes les 5s
└─> GET /breakdowns/{id}
    └─> Status: PENDING... PENDING... PENDING...
        └─> Status: ACCEPTED ✅ CHANGEMENT DÉTECTÉ
            └─> StatusChanged émis
                └─> Navigation automatique vers Tracking
```

**Ce qui existe déjà:**
- ✅ `startPollingBreakdown()` dans ViewModel
- ✅ `StatusChanged` dans BreakdownUiState
- ✅ Logique de détection dans `fetchBreakdownById()`
- ✅ Navigation automatique dans SOSStatusScreen

**Ce qui pourrait manquer:**
- Backend ne met pas à jour le status correctement
- Polling ne s'exécute pas vraiment
- StatusChanged pas géré dans SOSStatusScreen

---

## 🔧 Solutions à implémenter

### Solution 1: Backend - Notifications FCM (CÔTÉ BACKEND)

**Fichier backend à créer/modifier:**
```javascript
// backend/routes/breakdowns.js

router.post('/breakdowns', async (req, res) => {
  // 1. Créer le breakdown
  const breakdown = await Breakdown.create(req.body);
  
  // 2. ✅ NOUVEAU: Trouver garages à proximité
  const nearbyGarages = await findNearbyGarages(
    breakdown.latitude,
    breakdown.longitude,
    10 // 10 km radius
  );
  
  // 3. ✅ NOUVEAU: Envoyer FCM à chaque garage
  for (const garage of nearbyGarages) {
    await sendFCMNotification(garage.fcmToken, {
      title: '🚨 Nouvelle demande SOS',
      body: `Assistance ${breakdown.type} demandée`,
      data: {
        type: 'breakdown',
        breakdownId: breakdown._id.toString(),
        breakdownType: breakdown.type
      }
    });
  }
  
  res.json(breakdown);
});
```

**⚠️ ATTENTION:** Ceci doit être fait **CÔTÉ BACKEND**, pas Android !

---

### Solution 2: Android - Gestion notification FCM (CÔTÉ ANDROID)

**Fichier à vérifier/créer:**
```kotlin
// MyFirebaseMessagingService.kt

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        
        if (data["type"] == "breakdown") {
            val breakdownId = data["breakdownId"]
            
            // ✅ Créer notification Android
            showNotification(
                title = message.notification?.title ?: "SOS",
                body = message.notification?.body ?: "",
                data = data
            )
        }
    }
    
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        // Créer PendingIntent vers BreakdownDetailScreen
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "breakdown_detail")
            putExtra("breakdown_id", data["breakdownId"])
        }
        
        // Afficher notification
        // ...
    }
}
```

---

### Solution 3: Vérifier le polling (DÉJÀ IMPLÉMENTÉ ✅)

Le polling est **déjà implémenté** correctement. Vérifions juste qu'il s'exécute :

```kotlin
// SOSStatusScreen.kt - DÉJÀ CORRECT ✅

LaunchedEffect(breakdownId) {
    if (breakdownId != null) {
        viewModel.startPollingBreakdown(
            breakdownId = breakdownId,
            intervalMs = 5000L
        )
    }
}
```

**Test à faire:**
1. Envoyer un SOS
2. Ouvrir Logcat
3. Chercher: `"🔄 Démarrage du polling"`
4. Chercher toutes les 5s: `"📊 Status: PENDING"`

---

## 📊 Résumé des actions nécessaires

### ✅ Déjà implémenté (Android)
- [x] BreakdownViewModel avec polling
- [x] StatusChanged pour détection
- [x] Navigation automatique
- [x] BreakdownDetailScreen pour accepter/refuser
- [x] API calls pour mettre à jour status

### ❌ À implémenter (Backend)
- [ ] **Trouver garages à proximité après création SOS**
- [ ] **Envoyer notifications FCM aux garages**
- [ ] **Sauvegarder les tokens FCM des garage owners**

### ❌ À vérifier/implémenter (Android)
- [ ] **MyFirebaseMessagingService** pour gérer les notifications
- [ ] **Navigation depuis notification vers BreakdownDetailScreen**
- [ ] **Vérifier que le polling s'exécute vraiment**

---

## 🧪 Tests de diagnostic

### Test 1: Vérifier que le backend envoie les notifications

**Commande curl:**
```bash
# Créer un SOS
curl -X POST http://172.18.1.246:3000/api/breakdowns \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "type": "PNEU",
    "description": "Test",
    "latitude": 36.8065,
    "longitude": 10.1815
  }'

# Vérifier les logs backend
# Doit afficher:
# ✅ Breakdown created
# 🔍 Looking for nearby garages...
# 📤 Sending notification...
# ✅ Notification sent!
```

---

### Test 2: Vérifier que l'app Android reçoit les notifications

**Logs Android (Logcat):**
```
MyFirebaseMessagingService: 🔔 Notification reçue
MyFirebaseMessagingService: Type: breakdown
MyFirebaseMessagingService: BreakdownID: 693ed35d...
```

---

### Test 3: Vérifier que le polling fonctionne

**Logs Android (Logcat):**
```
SOSStatus: 🔄 Démarrage du polling pour breakdown 693ed35d...
BreakdownVM: 🔍 Récupération breakdown #693ed35d...
BreakdownVM: 📊 Status: PENDING
[5 secondes]
BreakdownVM: 📊 Status: PENDING
[5 secondes]
BreakdownVM: 📊 Status: ACCEPTED
SOSStatus: 🔄 Changement détecté: PENDING → ACCEPTED
SOSStatus: ✅ Navigation vers tracking...
```

---

## 🎯 Prochaines étapes

### Étape 1: Vérifier le backend
```bash
# Tester l'endpoint de création
curl -X POST http://172.18.1.246:3000/api/breakdowns ...

# Vérifier les logs backend
# Est-ce qu'il envoie des notifications FCM ?
```

### Étape 2: Implémenter MyFirebaseMessagingService (si manquant)
```bash
# Chercher le fichier
find app/src -name "*Firebase*Service*.kt"

# Si absent, le créer
```

### Étape 3: Tester le polling
```bash
# Lancer l'app
# Envoyer un SOS
# Ouvrir Logcat
# Chercher "🔄 Démarrage du polling"
# Vérifier qu'il poll toutes les 5s
```

### Étape 4: Tester manuellement le changement de status
```bash
# Pendant que le polling tourne:
curl -X PATCH http://172.18.1.246:3000/api/breakdowns/693ed35d... \
  -H "Content-Type: application/json" \
  -d '{"status": "ACCEPTED"}'

# Vérifier Logcat:
# Doit afficher: "🔄 Changement détecté: PENDING → ACCEPTED"
```

---

## 🚨 Conclusion

**Le problème principal semble être CÔTÉ BACKEND:**

1. ❌ Le backend ne cherche pas les garages à proximité
2. ❌ Le backend n'envoie pas de notifications FCM
3. ❓ Le backend met-il à jour le status correctement ?

**Actions immédiates:**
1. Vérifier les logs backend après création d'un SOS
2. Vérifier si MyFirebaseMessagingService existe dans l'app
3. Tester le polling avec des logs Logcat

---

**Date:** 14 décembre 2025  
**Version:** 1.3.0  
**Status:** 🔍 DIAGNOSTIC EN COURS - BACKEND À VÉRIFIER

