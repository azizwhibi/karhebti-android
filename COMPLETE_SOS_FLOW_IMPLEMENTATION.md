# 🚨 FLUX SOS COMPLET - Implémentation finale

**Date:** 14 décembre 2025  
**Version:** 2.0.0  
**Statut:** ✅ **ANDROID COMPLET** | ⚠️ **BACKEND REQUIS**

---

## 📋 TABLE DES MATIÈRES

1. [Vue d'ensemble](#vue-densemble)
2. [Flux complet 0-11 secondes](#flux-complet)
3. [Implémentations Android](#implémentations-android)
4. [Code Backend requis](#code-backend-requis)
5. [Tests & Validation](#tests--validation)
6. [Troubleshooting](#troubleshooting)

---

## 🎯 VUE D'ENSEMBLE

### Ce qui est FAIT ✅

#### Android - Côté User
- ✅ `BreakdownSOSScreen` - Interface d'envoi SOS
- ✅ `SOSStatusScreen` - Attente avec polling (5s)
- ✅ Auto-navigation vers tracking quand accepté
- ✅ `BreakdownTrackingScreen` - Suivi en temps réel

#### Android - Côté Garage Owner
- ✅ `KarhebtiMessagingService` - Réception FCM
- ✅ `MainActivity` - Navigation depuis notification
- ✅ `BreakdownDetailScreen` - Accepter/Refuser
- ✅ `NavGraph` - Routes complètes

#### Android - Infrastructure
- ✅ `FCMTokenService` - Envoi token au backend
- ✅ `BreakdownViewModel` - Gestion état
- ✅ `BreakdownsRepository` - API calls
- ✅ Polling automatique optimisé

### Ce qui manque ❌

#### Backend (CRITIQUE)
- ❌ Logique pour trouver garages à proximité
- ❌ Envoi notifications FCM après création SOS
- ❌ Endpoint `PUT /users/fcm-token`
- ❌ Firebase Admin SDK configuré

---

## ⏱️ FLUX COMPLET

### Scénario: User envoie SOS → Garage accepte → User navigue vers tracking

```
┌─────────────────────────────────────────────────────────────────┐
│                    TIMELINE COMPLÈTE                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  0:00  📱 User appuie sur "Envoyer" dans BreakdownSOSScreen    │
│        └─> POST /breakdowns                                      │
│                                                                  │
│  0:01  ✅ Backend crée breakdown (status: PENDING)              │
│        └─> Retourne breakdownId                                 │
│                                                                  │
│  0:02  🚀 Backend cherche garages à proximité                   │
│        └─> Query MongoDB avec $near                             │
│                                                                  │
│  0:03  📤 Backend envoie FCM aux garages trouvés                │
│        └─> Firebase Admin SDK                                   │
│                                                                  │
│  0:04  🔔 Garage owner reçoit notification                      │
│        └─> "🚨 Nouvelle demande SOS"                            │
│                                                                  │
│  0:05  👆 Garage owner TAP sur notification                     │
│        └─> MainActivity.handleNotificationIntent()              │
│        └─> Navigate to BreakdownDetailScreen(breakdownId)      │
│                                                                  │
│  0:06  👀 Garage owner voit les détails                         │
│        ├─ Type: PNEU                                            │
│        ├─ Description: "Pneu crevé sur autoroute..."           │
│        ├─ Distance: 5.2 km                                      │
│        └─ Carte avec position                                   │
│                                                                  │
│  0:07  ✅ Garage owner appuie sur "Accepter"                    │
│        └─> PATCH /breakdowns/{id} { status: "ACCEPTED" }       │
│                                                                  │
│  0:08  ✅ Backend met à jour status → ACCEPTED                  │
│                                                                  │
│  0:09  🔄 User app polling détecte le changement                │
│        └─> SOSStatusScreen polling (5 secondes)                │
│                                                                  │
│  0:10  🎉 Auto-navigation vers BreakdownTrackingScreen          │
│        └─> LaunchedEffect détecte PENDING → ACCEPTED           │
│                                                                  │
│  0:11  ✅ User voit l'écran de tracking                         │
│        ├─ Status: "Accepté ✓"                                  │
│        ├─ Carte avec position                                   │
│        ├─ Timeline de progression                               │
│        └─ Bouton "Appeler le garage"                           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📱 IMPLÉMENTATIONS ANDROID

### 1. MainActivity - Navigation depuis notification

**Fichier:** `app/src/main/java/com/example/karhebti_android/MainActivity.kt`

```kotlin
/**
 * Gérer la navigation depuis une notification
 */
private fun handleNotificationIntent(
    intent: Intent,
    navController: androidx.navigation.NavHostController
) {
    val fromNotification = intent.getBooleanExtra("from_notification", false)
    if (!fromNotification) return

    val notificationType = intent.getStringExtra("notification_type")

    when (notificationType) {
        "sos", "new_breakdown" -> {
            val breakdownId = intent.getStringExtra("breakdownId")
            if (breakdownId != null) {
                navController.navigate(Screen.BreakdownDetail.createRoute(breakdownId))
            }
        }
        "status_update" -> {
            val breakdownId = intent.getStringExtra("breakdownId")
            if (breakdownId != null) {
                navController.navigate(Screen.BreakdownTracking.createRoute(breakdownId))
            }
        }
    }
}
```

**✅ Implémenté**

---

### 2. NavGraph - Routes complètes

**Fichier:** `app/src/main/java/com/example/karhebti_android/navigation/NavGraph.kt`

**Nouvelles routes ajoutées:**

```kotlin
object BreakdownDetail : Screen("breakdown_detail/{breakdownId}") {
    fun createRoute(breakdownId: String) = "breakdown_detail/$breakdownId"
}

object BreakdownTracking : Screen("breakdown_tracking/{breakdownId}") {
    fun createRoute(breakdownId: String) = "breakdown_tracking/$breakdownId"
}
```

**Composables:**

```kotlin
// Route: breakdown_detail/{breakdownId}
composable(Screen.BreakdownDetail.route) { backStackEntry ->
    val breakdownId = backStackEntry.arguments?.getString("breakdownId")!!
    
    BreakdownDetailScreen(
        breakdownId = breakdownId,
        onBackClick = { navController.popBackStack() },
        onAccepted = {
            navController.navigate(Screen.BreakdownTracking.createRoute(breakdownId))
        }
    )
}

// Route: breakdown_tracking/{breakdownId}
composable(Screen.BreakdownTracking.route) { backStackEntry ->
    val breakdownId = backStackEntry.arguments?.getString("breakdownId")!!
    
    BreakdownTrackingScreenWrapper(
        breakdownId = breakdownId,
        onBackClick = { navController.popBackStack() }
    )
}
```

**✅ Implémenté**

---

### 3. SOSStatusScreen - Polling & Auto-navigation

**Fichier:** `app/src/main/java/com/example/karhebti_android/ui/screens/SOSStatusScreen.kt`

**Fonctionnalités:**

```kotlin
// Polling toutes les 5 secondes
LaunchedEffect(breakdownId) {
    if (breakdownId != null) {
        while (true) {
            viewModel.fetchBreakdownById(breakdownId.toInt())
            delay(5000) // Poll every 5 seconds
        }
    }
}

// Auto-navigation quand status change
LaunchedEffect(uiState) {
    when (val state = uiState) {
        is BreakdownUiState.Success -> {
            val newStatus = (state.data as BreakdownResponse).status
            
            // Détection du changement PENDING → ACCEPTED
            if (currentStatus == "PENDING" && newStatus == "ACCEPTED") {
                Log.d("SOSStatus", "✅ Navigating to tracking...")
                onNavigateToTracking(breakdownId ?: "")
            }
            
            currentStatus = newStatus
        }
    }
}
```

**✅ Déjà implémenté**

---

### 4. BreakdownTrackingScreen - Suivi complet

**Fichier:** `app/src/main/java/com/example/karhebti_android/ui/screens/BreakdownTrackingScreen.kt`

**Nouvelles fonctionnalités:**

```kotlin
@Composable
fun BreakdownTrackingScreenWrapper(
    breakdownId: String,
    onBackClick: () -> Unit = {}
) {
    // Chargement des données
    // Polling toutes les 10 secondes
    // Affichage:
    // - Badge de statut coloré
    // - Carte OpenStreetMap
    // - Détails du breakdown
    // - Timeline de progression
    // - Bouton d'appel
}
```

**Composants:**
- ✅ `StatusCard` - Badge coloré selon statut
- ✅ `BreakdownInfoCard` - Détails de la demande
- ✅ `TimelineCard` - Progression visuelle (4 étapes)
- ✅ Carte intégrée avec `OpenStreetMapView`
- ✅ Bouton "Appeler le garage"

**✅ Implémenté**

---

### 5. BreakdownDetailScreen - Accepter/Refuser

**Fichier:** `app/src/main/java/com/example/karhebti_android/ui/screens/BreakdownDetailScreen.kt`

**Fonctionnalités:**

```kotlin
@Composable
fun BreakdownDetailScreen(
    breakdownId: String,
    onBackClick: () -> Unit,
    onAccepted: () -> Unit
) {
    // Affiche:
    // - Type de panne
    // - Description
    // - Localisation (carte)
    // - Distance
    // - Infos client
    // 
    // Actions:
    // - Bouton "Accepter" (dialog de confirmation)
    // - Bouton "Refuser"
}
```

**✅ Déjà implémenté**

---

### 6. KarhebtiMessagingService - Réception FCM

**Fichier:** `app/src/main/java/com/example/karhebti_android/data/notifications/KarhebtiMessagingService.kt`

**Gestion des notifications:**

```kotlin
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val notificationType = remoteMessage.data["type"]
    
    when (notificationType) {
        "new_breakdown", "sos_request" -> {
            showSOSNotification(title, body, remoteMessage.data)
        }
        "breakdown_status_update" -> {
            showStatusUpdateNotification(title, body, remoteMessage.data)
        }
    }
}

private fun showSOSNotification(...) {
    val intent = Intent(this, MainActivity::class.java).apply {
        putExtra("from_notification", true)
        putExtra("notification_type", "sos")
        putExtra("breakdownId", data["breakdownId"])
    }
    // Affiche notification avec son, vibration, priorité HIGH
}
```

**✅ Déjà implémenté**

---

## 🔧 CODE BACKEND REQUIS

### 1. Endpoint POST /breakdowns - Envoi notifications

**Fichier:** `backend/routes/breakdowns.js`

```javascript
const admin = require('firebase-admin');
const User = require('../models/User');
const Breakdown = require('../models/Breakdown');

router.post('/api/breakdowns', authenticateToken, async (req, res) => {
  try {
    console.log('📥 Création breakdown...');
    
    // 1. Créer le breakdown
    const breakdown = await Breakdown.create({
      userId: req.user.id,
      type: req.body.type,
      description: req.body.description,
      latitude: req.body.latitude,
      longitude: req.body.longitude,
      status: 'PENDING'
    });
    
    console.log(`✅ Breakdown créé: ${breakdown._id}`);
    console.log(`📍 Position: ${breakdown.latitude}, ${breakdown.longitude}`);
    
    // 2. Chercher garages à proximité (10 km)
    const nearbyGarages = await User.find({
      role: 'propGarage',
      'location': {
        $near: {
          $geometry: {
            type: 'Point',
            coordinates: [breakdown.longitude, breakdown.latitude]
          },
          $maxDistance: 10000 // 10 km en mètres
        }
      },
      fcmToken: { $exists: true, $ne: null }
    }).limit(20);
    
    console.log(`👥 ${nearbyGarages.length} garages trouvés`);
    
    // 3. Envoyer notification FCM à chaque garage
    let sentCount = 0;
    let failedCount = 0;
    
    for (const garage of nearbyGarages) {
      try {
        const message = {
          token: garage.fcmToken,
          notification: {
            title: '🚨 Nouvelle demande SOS',
            body: `Assistance ${breakdown.type} demandée à proximité`
          },
          data: {
            type: 'new_breakdown',
            breakdownId: breakdown._id.toString(),
            breakdownType: breakdown.type,
            latitude: breakdown.latitude.toString(),
            longitude: breakdown.longitude.toString(),
            userId: breakdown.userId.toString()
          },
          android: {
            priority: 'high',
            notification: {
              channelId: 'sos_notifications',
              sound: 'default',
              priority: 'high',
              defaultVibrateTimings: true
            }
          }
        };
        
        const response = await admin.messaging().send(message);
        console.log(`✅ Notification envoyée à ${garage.email}`);
        console.log(`   Response: ${response}`);
        sentCount++;
        
        // Enregistrer dans la base de données (optionnel)
        await Notification.create({
          userId: garage._id,
          breakdownId: breakdown._id,
          type: 'new_breakdown',
          sentAt: new Date(),
          status: 'sent'
        });
        
      } catch (error) {
        console.error(`❌ Erreur envoi à ${garage.email}:`, error.message);
        failedCount++;
      }
    }
    
    console.log(`📊 Résumé: ${sentCount} envoyés, ${failedCount} échoués`);
    
    // 4. Retourner le breakdown créé
    res.status(201).json({
      breakdown,
      notificationsSent: sentCount,
      notificationsFailed: failedCount
    });
    
  } catch (error) {
    console.error('❌ Erreur création breakdown:', error);
    res.status(500).json({ 
      error: 'Erreur lors de la création du breakdown',
      details: error.message 
    });
  }
});
```

**❌ À IMPLÉMENTER**

---

### 2. Endpoint PATCH /breakdowns/:id - Mise à jour statut

**Fichier:** `backend/routes/breakdowns.js`

```javascript
router.patch('/api/breakdowns/:id', authenticateToken, async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;
    
    console.log(`📝 Mise à jour breakdown ${id} → ${status}`);
    
    // 1. Mettre à jour le breakdown
    const breakdown = await Breakdown.findByIdAndUpdate(
      id,
      { 
        status,
        acceptedBy: status === 'ACCEPTED' ? req.user.id : undefined,
        acceptedAt: status === 'ACCEPTED' ? new Date() : undefined
      },
      { new: true }
    ).populate('userId', 'name email');
    
    if (!breakdown) {
      return res.status(404).json({ error: 'Breakdown non trouvé' });
    }
    
    console.log(`✅ Status mis à jour: ${status}`);
    
    // 2. Si accepté, notifier le user
    if (status === 'ACCEPTED' && breakdown.userId.fcmToken) {
      try {
        await admin.messaging().send({
          token: breakdown.userId.fcmToken,
          notification: {
            title: '✅ Garage trouvé!',
            body: 'Un garage a accepté votre demande SOS'
          },
          data: {
            type: 'breakdown_status_update',
            breakdownId: breakdown._id.toString(),
            status: 'ACCEPTED'
          }
        });
        
        console.log(`✅ Notification envoyée au user ${breakdown.userId.email}`);
      } catch (error) {
        console.error('❌ Erreur envoi notification user:', error);
      }
    }
    
    res.json(breakdown);
    
  } catch (error) {
    console.error('❌ Erreur mise à jour:', error);
    res.status(500).json({ error: error.message });
  }
});
```

**✅ Logique de base existe, ajouter notification user**

---

### 3. Endpoint PUT /users/fcm-token - Enregistrement token

**Fichier:** `backend/routes/users.js`

```javascript
router.put('/api/users/fcm-token', authenticateToken, async (req, res) => {
  try {
    const { deviceToken } = req.body;
    
    if (!deviceToken) {
      return res.status(400).json({ error: 'deviceToken requis' });
    }
    
    console.log(`📱 Enregistrement token FCM pour user ${req.user.id}`);
    console.log(`   Token: ${deviceToken.substring(0, 20)}...`);
    
    // Mettre à jour le token dans la base
    const user = await User.findByIdAndUpdate(
      req.user.id,
      { fcmToken: deviceToken },
      { new: true }
    );
    
    if (!user) {
      return res.status(404).json({ error: 'User non trouvé' });
    }
    
    console.log(`✅ Token FCM enregistré pour ${user.email}`);
    
    res.json({ 
      message: 'Token FCM enregistré avec succès',
      user: {
        id: user._id,
        email: user.email,
        role: user.role
      }
    });
    
  } catch (error) {
    console.error('❌ Erreur enregistrement token:', error);
    res.status(500).json({ error: error.message });
  }
});
```

**❌ À IMPLÉMENTER**

---

### 4. Firebase Admin SDK - Configuration

**Fichier:** `backend/config/firebase.js`

```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

// Initialiser Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'karhebti'
});

console.log('✅ Firebase Admin SDK initialisé');

module.exports = admin;
```

**Fichier:** `backend/server.js`

```javascript
const admin = require('./config/firebase');

// Le SDK est maintenant disponible partout avec:
// const admin = require('firebase-admin');
```

**❌ À CONFIGURER**

---

### 5. Modèle User - Champ fcmToken

**Fichier:** `backend/models/User.js`

```javascript
const userSchema = new mongoose.Schema({
  name: String,
  email: { type: String, unique: true },
  password: String,
  role: { type: String, enum: ['user', 'propGarage', 'admin'] },
  
  // ✅ AJOUTER CE CHAMP
  fcmToken: { type: String, default: null },
  
  // Pour la recherche géographique
  location: {
    type: {
      type: String,
      enum: ['Point'],
      default: 'Point'
    },
    coordinates: {
      type: [Number],
      default: [0, 0]
    }
  }
});

// Index géospatial pour la recherche $near
userSchema.index({ location: '2dsphere' });
```

**❌ À AJOUTER**

---

## 🧪 TESTS & VALIDATION

### Test 1: User envoie SOS

```bash
# Terminal 1: Backend logs
npm run dev

# Terminal 2: Envoi SOS
curl -X POST http://localhost:3000/api/breakdowns \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PNEU",
    "description": "Pneu crevé sur autoroute",
    "latitude": 36.8065,
    "longitude": 10.1815
  }'

# Vérifier logs backend:
# ✅ Breakdown créé
# ✅ X garages trouvés
# ✅ Notifications envoyées
```

---

### Test 2: Garage reçoit notification

```bash
# Vérifier sur le téléphone du garage:
# 1. Notification apparaît avec son/vibration
# 2. Titre: "🚨 Nouvelle demande SOS"
# 3. Body: "Assistance PNEU demandée à proximité"

# Logcat Android:
adb logcat | grep "KarhebtiMessaging"

# Attendu:
# ✅ MESSAGE REÇU!
# ✅ Type: new_breakdown
# ✅ NOTIFICATION SOS AFFICHÉE
```

---

### Test 3: Navigation depuis notification

```bash
# 1. Tap sur notification
# 2. App s'ouvre
# 3. Navigate vers BreakdownDetailScreen

# Logcat:
adb logcat | grep "MainActivity"

# Attendu:
# 📱 Navigation depuis notification: sos
# 🚨 Navigation vers BreakdownDetail: 12345
```

---

### Test 4: Garage accepte

```bash
# 1. Dans BreakdownDetailScreen, tap "Accepter"
# 2. Dialog de confirmation
# 3. Tap "Confirmer"

# Backend logs:
# 📝 Mise à jour breakdown 12345 → ACCEPTED
# ✅ Status mis à jour: ACCEPTED
# ✅ Notification envoyée au user

# Garage navigate vers BreakdownTrackingScreen
```

---

### Test 5: User détecte changement

```bash
# SOSStatusScreen polling détecte:
# - Status: PENDING → ACCEPTED
# - Auto-navigation vers BreakdownTrackingScreen

# Logcat:
adb logcat | grep "SOSStatus"

# Attendu:
# ✅ Status changed to ACCEPTED! Navigating to tracking...
```

---

### Test 6: Tracking screen

```bash
# Vérifier affichage:
# ✅ Badge "Accepté ✓" (bleu)
# ✅ Carte avec position
# ✅ Détails du breakdown
# ✅ Timeline: PENDING → ACCEPTED → IN_PROGRESS → COMPLETED
# ✅ Bouton "Appeler le garage"
```

---

## 🔍 TROUBLESHOOTING

### ❌ Garage ne reçoit pas de notification

**Vérifications:**

1. **Backend trouve-t-il des garages?**
   ```bash
   # Logs backend après POST /breakdowns:
   👥 0 garages trouvés  # ❌ PROBLÈME!
   ```
   
   **Solution:** Vérifier:
   - Garages ont `role: 'propGarage'`
   - Garages ont `fcmToken` non null
   - Index géospatial existe: `db.users.getIndexes()`

2. **FCM token existe?**
   ```bash
   # MongoDB:
   db.users.find({ role: 'propGarage' }, { email: 1, fcmToken: 1 })
   ```
   
   **Solution:** Si null, vérifier que l'app envoie le token:
   - `FCMTokenService.registerDeviceToken()`
   - Backend reçoit `PUT /users/fcm-token`

3. **Firebase Admin SDK configuré?**
   ```bash
   # Backend logs:
   ❌ Error: app/invalid-credential
   ```
   
   **Solution:** Télécharger `serviceAccountKey.json` depuis Firebase Console

---

### ❌ User app ne navigue pas auto

**Vérifications:**

1. **Polling fonctionne?**
   ```kotlin
   // Logcat:
   adb logcat | grep "SOSStatus"
   
   // Attendu toutes les 5s:
   Fetching breakdown 12345...
   ```

2. **Status change détecté?**
   ```kotlin
   // Ajouter log dans SOSStatusScreen:
   Log.d("SOSStatus", "Old: $currentStatus, New: $newStatus")
   ```
   
   **Si pas de changement:**
   - Vérifier que backend met bien à jour le status
   - Vérifier que GET /breakdowns/{id} retourne nouveau status

---

### ❌ App crash sur notification tap

**Erreur:**
```
java.lang.IllegalArgumentException: breakdownId parameter wasn't found
```

**Solution:** Vérifier intent extras:
```kotlin
// KarhebtiMessagingService:
putExtra("breakdownId", data["breakdownId"])  // ✅ String

// MainActivity:
val breakdownId = intent.getStringExtra("breakdownId")  // ✅ Non null
```

---

## 📊 CHECKLIST FINALE

### Android ✅

- [x] MainActivity gère navigation depuis notification
- [x] NavGraph a routes BreakdownDetail et BreakdownTracking
- [x] SOSStatusScreen polling et auto-navigation
- [x] BreakdownTrackingScreen complet avec UI
- [x] BreakdownDetailScreen accepter/refuser
- [x] KarhebtiMessagingService reçoit FCM
- [x] FCMTokenService envoie token au backend

### Backend ❌

- [ ] POST /breakdowns envoie notifications FCM
- [ ] Recherche garages à proximité avec $near
- [ ] PUT /users/fcm-token endpoint
- [ ] Firebase Admin SDK configuré
- [ ] Modèle User a champ fcmToken
- [ ] Index géospatial sur location
- [ ] PATCH /breakdowns/:id notifie user quand accepté

---

## 🎉 RÉSULTAT FINAL

Une fois le backend implémenté, le flux complet fonctionnera:

```
👤 User envoie SOS
   ↓ (1 seconde)
🔔 Garage reçoit notification
   ↓ (5 secondes)
✅ Garage accepte
   ↓ (5 secondes)
🎉 User navigue auto vers tracking
   ↓
📞 Communication établie
```

**Temps total: ~11 secondes** ⚡

---

**Auteur:** AI Assistant  
**Date:** 14 décembre 2025  
**Version:** 2.0.0

