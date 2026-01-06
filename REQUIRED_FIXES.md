# 🔧 CORRECTIONS NÉCESSAIRES - Flux SOS complet

## 📋 Date: 14 décembre 2025

## ✅ Ce qui FONCTIONNE déjà

### Android - Côté User
- ✅ Bouton SOS sur HomeScreen
- ✅ BreakdownSOSScreen (envoi SOS)
- ✅ API `POST /breakdowns` fonctionne
- ✅ SOSStatusScreen avec polling
- ✅ Polling optimisé (startPollingBreakdown)
- ✅ Détection automatique (StatusChanged)
- ✅ Navigation automatique vers tracking

### Android - Côté Garage Owner
- ✅ BreakdownDetailScreen (UI pour accepter/refuser)
- ✅ API `PATCH /breakdowns/{id}` existe
- ✅ ViewModel `updateBreakdownStatus()` existe
- ✅ **KarhebtiMessagingService** existe et gère les notifications SOS ⭐

---

## ❌ Ce qui NE FONCTIONNE PAS

### Problème 1: Backend ne notifie pas ❌ **CRITIQUE**

**Symptôme:**
```
User envoie SOS
└─> Backend crée breakdown (PENDING) ✅
    └─> Backend devrait notifier les garages ❌ NE LE FAIT PAS
```

**Ce qui manque CÔTÉ BACKEND:**
1. Logique pour trouver garages à proximité après création SOS
2. Logique pour envoyer notifications FCM aux garages
3. Endpoint pour que les garages enregistrent leur token FCM

**Solution BACKEND requise:**
```javascript
// backend/routes/breakdowns.js

router.post('/api/breakdowns', async (req, res) => {
  // 1. Créer le breakdown
  const breakdown = await Breakdown.create({
    ...req.body,
    userId: req.user.id,
    status: 'PENDING'
  });
  
  // 2. ✅ AJOUTER: Trouver garages à proximité
  const nearbyGarages = await User.find({
    role: 'propGarage',
    'location': {
      $near: {
        $geometry: {
          type: 'Point',
          coordinates: [breakdown.longitude, breakdown.latitude]
        },
        $maxDistance: 10000 // 10 km
      }
    },
    fcmToken: { $exists: true, $ne: null }
  });
  
  console.log(`📍 Found ${nearbyGarages.length} nearby garages`);
  
  // 3. ✅ AJOUTER: Envoyer FCM à chaque garage
  const admin = require('firebase-admin');
  
  for (const garage of nearbyGarages) {
    try {
      await admin.messaging().send({
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
          longitude: breakdown.longitude.toString()
        }
      });
      
      console.log(`✅ Notification sent to ${garage.email}`);
    } catch (error) {
      console.error(`❌ Error sending to ${garage.email}:`, error);
    }
  }
  
  res.json(breakdown);
});
```

---

### Problème 2: MainActivity ne gère pas les notifications ❌ **CRITIQUE**

**Symptôme:**
```
Garage owner reçoit notification FCM ✅
└─> Tap notification
    └─> App s'ouvre sur HomeScreen ❌ PAS SUR BreakdownDetailScreen
```

**Ce qui manque CÔTÉ ANDROID:**

MainActivity ne lit pas les extras de l'Intent pour naviguer vers le bon écran.

**Solution ANDROID requise:**

Modifier `MainActivity.kt` pour gérer la navigation depuis les notifications.

---

## 🔧 Solutions à implémenter

### Solution 1: Backend - Envoyer notifications FCM

**Fichier:** `backend/routes/breakdowns.js`

**Code à ajouter après la création du breakdown:**

```javascript
// Après: const breakdown = await Breakdown.create(...)

console.log('🔍 Looking for nearby garages...');

// Trouver garages à proximité
const nearbyGarages = await User.find({
  role: 'propGarage',
  fcmToken: { $exists: true, $ne: null }
  // TODO: Ajouter filtre géographique si location existe
}).limit(10);

console.log(`👥 Found ${nearbyGarages.length} garage owners`);

// Envoyer FCM
const admin = require('firebase-admin');

for (const garage of nearbyGarages) {
  try {
    const message = {
      token: garage.fcmToken,
      notification: {
        title: '🚨 Nouvelle demande SOS',
        body: `Assistance ${breakdown.type} demandée`
      },
      data: {
        type: 'new_breakdown',
        breakdownId: breakdown._id.toString(),
        breakdownType: breakdown.type
      }
    };
    
    const response = await admin.messaging().send(message);
    console.log(`✅ Notification sent to ${garage.email}: ${response}`);
  } catch (error) {
    console.error(`❌ Error sending to ${garage.email}:`, error.message);
  }
}
```

---

### Solution 2: Android - Gérer navigation depuis notification

**Fichier:** `MainActivity.kt`

**Code à ajouter dans `onCreate()`:**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // ...existing code...
    
    setContent {
        KarhebtiTheme {
            val navController = rememberNavController()
            
            // ✅ NOUVEAU: Gérer navigation depuis notification
            LaunchedEffect(Unit) {
                handleNotificationIntent(intent, navController)
            }
            
            NavHost(/*...*/) {
                // ...existing routes...
            }
        }
    }
}

// ✅ NOUVEAU: Gérer l'intent de notification
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    // TODO: Trigger navigation avec le nouveau intent
}

// ✅ NOUVEAU: Fonction helper
private fun handleNotificationIntent(intent: Intent?, navController: NavHostController) {
    val fromNotification = intent?.getBooleanExtra("from_notification", false) ?: false
    
    if (fromNotification) {
        val notificationType = intent.getStringExtra("notification_type")
        val breakdownId = intent.getStringExtra("breakdownId")
        
        Log.d("MainActivity", "📱 Notification tap détecté")
        Log.d("MainActivity", "Type: $notificationType")
        Log.d("MainActivity", "BreakdownID: $breakdownId")
        
        when (notificationType) {
            "sos" -> {
                // Garage owner - naviguer vers détail
                if (breakdownId != null) {
                    navController.navigate(Screen.BreakdownDetail.createRoute(breakdownId))
                }
            }
            "status_update" -> {
                // User - naviguer vers tracking
                if (breakdownId != null) {
                    navController.navigate(Screen.BreakdownTracking.createRoute(breakdownId))
                }
            }
        }
    }
}
```

---

### Solution 3: Backend - Endpoint pour enregistrer token FCM

**Fichier:** `backend/routes/users.js` ou `backend/routes/auth.js`

**Nouveau endpoint:**

```javascript
// PUT /api/users/fcm-token
router.put('/fcm-token', authenticateToken, async (req, res) => {
  try {
    const { fcmToken } = req.body;
    
    await User.findByIdAndUpdate(req.user.id, {
      fcmToken: fcmToken
    });
    
    console.log(`✅ FCM token updated for user ${req.user.email}`);
    
    res.json({ message: 'Token updated successfully' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
```

**Code Android pour envoyer le token:**

```kotlin
// Dans MainActivity ou Application
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        Log.d("FCM", "Token: $token")
        
        // Envoyer au backend
        viewModelScope.launch {
            api.updateFCMToken(token)
        }
    }
}
```

---

## 📊 Résumé des actions

### 🔴 URGENT - CÔTÉ BACKEND
1. [ ] **Ajouter logique de notification FCM dans POST /breakdowns**
   - Trouver garages à proximité
   - Envoyer notification FCM à chaque garage
2. [ ] **Créer endpoint PUT /users/fcm-token**
   - Permettre aux users d'enregistrer leur token FCM
3. [ ] **Tester avec Firebase Admin SDK**
   - Vérifier que les messages FCM partent

### 🟡 IMPORTANT - CÔTÉ ANDROID  
1. [ ] **Modifier MainActivity.kt**
   - Gérer navigation depuis notification
   - Ajouter `handleNotificationIntent()`
   - Ajouter `onNewIntent()`
2. [ ] **Envoyer token FCM au backend**
   - Au démarrage de l'app
   - Quand le token change

### 🟢 VÉRIFICATION - DÉJÀ FAIT ✅
- [x] KarhebtiMessagingService existe
- [x] Gère les notifications "new_breakdown"
- [x] BreakdownDetailScreen existe
- [x] API updateBreakdownStatus existe
- [x] Polling fonctionne côté user

---

## 🧪 Tests après implémentation

### Test 1: Backend envoie notification
```bash
# Terminal 1: Logs backend
npm run dev

# Terminal 2: Créer un SOS
curl -X POST http://172.18.1.246:3000/api/breakdowns \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"PNEU","latitude":36.8,"longitude":10.1}'

# Vérifier logs backend:
# ✅ 🔍 Looking for nearby garages...
# ✅ 👥 Found 2 garage owners
# ✅ 📤 Notification sent to prop.garage@example.com
```

### Test 2: Android reçoit notification
```
# Logcat Android (garage owner)
KarhebtiMessaging: ✅ MESSAGE REÇU!
KarhebtiMessaging: Type: new_breakdown
KarhebtiMessaging: 🚨 Création notification SOS...
KarhebtiMessaging: ✅✅✅ NOTIFICATION SOS AFFICHÉE
```

### Test 3: Tap notification ouvre détail
```
# Tap notification
MainActivity: 📱 Notification tap détecté
MainActivity: Type: sos
MainActivity: BreakdownID: 693ed35d...
MainActivity: Navigation vers BreakdownDetailScreen
```

### Test 4: Polling détecte changement
```
# User app (Logcat)
SOSStatus: 🔄 Démarrage du polling
BreakdownVM: 📊 Status: PENDING
[garage accepte]
BreakdownVM: 🔄 Changement détecté: PENDING → ACCEPTED
SOSStatus: ✅ Navigation vers tracking...
```

---

## 🎯 Priorités

### Priorité 1 (BLOQUANT): Backend FCM
Sans cela, **aucune notification** n'est envoyée aux garages.

### Priorité 2 (CRITIQUE): MainActivity navigation
Sans cela, les garages ne peuvent pas **accéder rapidement** au détail du SOS.

### Priorité 3 (IMPORTANT): Token FCM registration
Sans cela, le backend ne sait pas **où envoyer** les notifications.

---

**Version:** 1.3.0  
**Date:** 14 décembre 2025  
**Status:** 🔴 **BACKEND MODIFICATIONS REQUISES**

