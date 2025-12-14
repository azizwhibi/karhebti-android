# ✅ CHECKLIST RAPIDE - Flux SOS Complet

## 📋 Date: 14 décembre 2024

---

## 🔍 VÉRIFICATIONS FRONTEND (Android)

### 1. KarhebtiMessagingService ✅

**Fichier:** `KarhebtiMessagingService.kt`

```kotlin
// ✅ Vérifié - Service prêt
- onMessageReceived() gère "new_breakdown" et "breakdown_status_update"
- showSOSNotification() affiche notification avec extras
- Notification contient: breakdownId, type, from_notification
```

**Test:**
```bash
adb logcat | grep "KarhebtiMessaging"
```

Vous devriez voir:
```
✅ MESSAGE REÇU!
Type: new_breakdown
🚨 Création notification SOS...
✅✅✅ NOTIFICATION SOS AFFICHÉE
```

---

### 2. MainActivity ✅

**Fichier:** `MainActivity.kt`

```kotlin
// ✅ Vérifié - Navigation prête
- handleNotificationIntent() gère les extras
- Navigue vers BreakdownDetail quand type = "sos" ou "new_breakdown"
- Navigue vers BreakdownTracking quand type = "status_update"
```

**Test:**
```bash
# Simuler une notification
adb shell am start -n com.example.karhebti_android/.MainActivity \
  --ez "from_notification" true \
  --es "notification_type" "sos" \
  --es "breakdownId" "675c9876543210abcdef"
```

---

### 3. SOSStatusScreen ✅

**Fichier:** `SOSStatusScreen.kt`

```kotlin
// ✅ Vérifié - Polling actif
- Poll GET /breakdowns/:id toutes les 5 secondes
- Détecte changement status PENDING → ACCEPTED
- Auto-navigation vers BreakdownTracking
```

**Test:**
```bash
adb logcat | grep "SOSStatus"
```

Vous devriez voir:
```
Starting polling for breakdown 675c...
✅ Status changed to ACCEPTED! Navigating to tracking...
```

---

### 4. FCMTokenService ✅

**Fichier:** `FCMTokenService.kt`

```kotlin
// ✅ Vérifié - Token envoyé au backend
- registerDeviceToken() obtient le FCM token
- sendTokenToBackend() envoie POST /auth/fcm-token
- Utilise JWT du TokenManager
```

**Test:**
```bash
adb logcat | grep "FCMTokenService"
```

Vous devriez voir:
```
✅ Token FCM obtenu: eXXX...XXX
✅ Token envoyé au backend avec succès
```

---

### 5. AndroidManifest.xml ⚠️ À vérifier

**Fichier:** `app/src/main/AndroidManifest.xml`

Doit contenir:
```xml
<service
    android:name=".data.notifications.KarhebtiMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

**Vérifier:**
```bash
grep -r "KarhebtiMessagingService" app/src/main/AndroidManifest.xml
```

---

## 🖥️ VÉRIFICATIONS BACKEND

### 1. Firebase Admin SDK

**Fichier:** `services/fcm.service.js`

```javascript
// ⭐ À créer si n'existe pas
const admin = require('firebase-admin');
const serviceAccount = require('../config/firebase-service-account.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});
```

**Vérifier:**
```bash
# Dans le backend
ls -la config/firebase-service-account.json
```

---

### 2. Model User avec fcmToken

**Fichier:** `models/User.js`

```javascript
// ⭐ Champ requis
const userSchema = new mongoose.Schema({
  // ...autres champs...
  fcmToken: { type: String, default: null },  // ← IMPORTANT
  role: { 
    type: String, 
    enum: ['user', 'propGarage', 'admin'], 
    default: 'user' 
  }
});
```

**Vérifier dans MongoDB:**
```javascript
db.users.findOne({ role: "propGarage" })
// Doit avoir un champ fcmToken
```

---

### 3. Route POST /auth/fcm-token

**Fichier:** `routes/auth.js`

```javascript
// ⭐ Route pour enregistrer le FCM token
router.post('/fcm-token', authMiddleware, async (req, res) => {
  const { fcmToken } = req.body;
  await User.findByIdAndUpdate(req.user.id, { fcmToken });
  res.json({ message: 'Token enregistré' });
});
```

**Test:**
```bash
curl -X POST http://localhost:3000/auth/fcm-token \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fcmToken":"eXXX...XXX"}'
```

Réponse attendue:
```json
{"message":"Token enregistré"}
```

---

### 4. POST /breakdowns envoie notifications

**Fichier:** `routes/breakdowns.js`

```javascript
router.post('/', authMiddleware, async (req, res) => {
  // Créer breakdown
  const breakdown = new Breakdown({ ... });
  await breakdown.save();
  
  // ⭐ IMPORTANT: Envoyer notifications
  await notifyGarageOwners(breakdown);
  
  res.status(201).json(breakdown);
});
```

**Test:**
```bash
# Logs backend
tail -f logs/app.log | grep "notif"
```

Doit afficher:
```
🔔 Envoi notifications aux garages...
📊 3 garages trouvés
✅ 3/3 notifications envoyées
```

---

### 5. PATCH /breakdowns/:id notifie le user

**Fichier:** `routes/breakdowns.js`

```javascript
router.patch('/:id', authMiddleware, async (req, res) => {
  // Mettre à jour status
  breakdown.status = status;
  if (status === 'ACCEPTED') {
    breakdown.assignedTo = req.user.id;
  }
  await breakdown.save();
  
  // ⭐ IMPORTANT: Notifier le user
  await notifyUserStatusChange(breakdown, status);
  
  res.json(breakdown);
});
```

---

## 🧪 TEST COMPLET DU FLUX

### Étape 1: Préparer l'environnement

```bash
# Terminal 1: Backend
cd backend
npm start

# Terminal 2: Android Logs
adb logcat -c  # Clear logs
adb logcat | grep -E "KarhebtiMessaging|SOSStatus|FCMToken|MainActivity"

# Terminal 3: Backend Logs
tail -f logs/app.log
```

---

### Étape 2: User envoie SOS

1. Ouvrir l'app sur téléphone user
2. Aller dans "Véhicules"
3. Sélectionner un véhicule
4. Cliquer "🆘 Déclarer une panne"
5. Remplir: Type = "Panne moteur", Description = "Test"
6. Cliquer "Envoyer SOS"

**Vérifier:**
- App navigue vers SOSStatusScreen ✅
- Backend logs: "Breakdown créé" ✅
- Backend logs: "Notifications envoyées" ✅

---

### Étape 3: Garage reçoit notification

**Sur téléphone garage, vérifier:**
- Notification apparaît ✅
- Titre: "🆘 Nouvelle demande SOS" ✅
- Body: "Panne moteur - ..." ✅
- Son/vibration ✅

**Logs Android:**
```
KarhebtiMessaging: ✅ MESSAGE REÇU!
KarhebtiMessaging: Type: new_breakdown
KarhebtiMessaging: BreakdownID: 675c...
KarhebtiMessaging: ✅✅✅ NOTIFICATION SOS AFFICHÉE
```

---

### Étape 4: Garage ouvre notification

1. Taper sur la notification

**Vérifier:**
- App s'ouvre ou passe au premier plan ✅
- Navigate vers BreakdownDetailScreen ✅
- Carte affichée avec position ✅
- Bouton "Accepter" visible ✅

**Logs Android:**
```
MainActivity: 📱 Navigation depuis notification: sos
MainActivity: 🚨 Navigation vers BreakdownDetail: 675c...
```

---

### Étape 5: Garage accepte la demande

1. Cliquer "Accepter"
2. Confirmer dans le dialogue

**Vérifier:**
- Backend logs: "PATCH /breakdowns/675c..." ✅
- Backend logs: "Status ACCEPTED" ✅
- Backend logs: "Notification envoyée au user" ✅
- App garage navigue vers BreakdownTracking ✅

---

### Étape 6: User détecte l'acceptation

**Sur téléphone user, vérifier:**
- SOSStatusScreen poll détecte status = ACCEPTED ✅
- Auto-navigation vers BreakdownTracking ✅
- Carte affichée avec les deux positions ✅

**Logs Android:**
```
SOSStatus: ✅ Status changed to ACCEPTED! Navigating to tracking...
```

---

### Étape 7: Les deux sont connectés

**Vérifier sur les deux téléphones:**
- BreakdownTrackingScreen ouvert ✅
- Carte avec 2 marqueurs (user + garage) ✅
- Bouton appel visible ✅
- Chat disponible ✅
- Positions mises à jour toutes les 5s ✅

**✅ SUCCÈS COMPLET!**

---

## 🐛 DÉPANNAGE

### Problème: Notification n'apparaît pas

**Causes possibles:**
1. FCM token pas envoyé au backend
   ```bash
   # Vérifier logs
   adb logcat | grep "FCMToken"
   # Doit afficher: "✅ Token envoyé au backend"
   ```

2. Garage n'a pas de FCM token en base
   ```javascript
   // Vérifier dans MongoDB
   db.users.findOne({ role: "propGarage" })
   ```

3. Firebase Admin SDK mal configuré
   ```bash
   # Vérifier fichier existe
   ls backend/config/firebase-service-account.json
   ```

4. Service non enregistré dans manifest
   ```bash
   grep "KarhebtiMessagingService" app/src/main/AndroidManifest.xml
   ```

---

### Problème: Notification reçue mais pas de navigation

**Causes possibles:**
1. MainActivity ne gère pas les extras
   ```bash
   # Vérifier logs
   adb logcat | grep "MainActivity"
   # Doit afficher: "📱 Navigation depuis notification: sos"
   ```

2. BreakdownId invalide ou null
   ```bash
   # Vérifier dans KarhebtiMessagingService.kt
   data["breakdownId"]?.let { putExtra("breakdownId", it) }
   ```

---

### Problème: User app ne détecte pas l'acceptation

**Causes possibles:**
1. Polling pas actif
   ```bash
   # Vérifier logs
   adb logcat | grep "SOSStatus"
   # Doit afficher des appels GET toutes les 5s
   ```

2. Backend ne met pas à jour le status
   ```javascript
   // Vérifier dans routes/breakdowns.js
   breakdown.status = status;
   await breakdown.save();
   ```

---

## 📝 CHECKLIST FINALE

### Backend
- [ ] Firebase Admin SDK initialisé
- [ ] Service account JSON en place
- [ ] Model User a champ `fcmToken`
- [ ] Route POST /auth/fcm-token créée
- [ ] POST /breakdowns appelle `notifyGarageOwners()`
- [ ] PATCH /breakdowns/:id appelle `notifyUserStatusChange()`
- [ ] Tests de notifications FCM réussis

### Frontend
- [ ] KarhebtiMessagingService dans AndroidManifest
- [ ] FCMTokenService envoie token au backend
- [ ] MainActivity gère navigation depuis notification
- [ ] SOSStatusScreen polling actif
- [ ] Auto-navigation PENDING → ACCEPTED fonctionne
- [ ] BreakdownTracking affiche les deux positions

### Tests E2E
- [ ] User envoie SOS → Backend crée breakdown
- [ ] Backend → Notification garage
- [ ] Garage tap notification → Détails affichés
- [ ] Garage accepte → Backend met à jour
- [ ] User app détecte → Navigation tracking
- [ ] Les deux sur BreakdownTracking
- [ ] Appel/Chat fonctionnent

---

**Status:** ✅ Guide de vérification complet  
**Date:** 14 décembre 2024  
**Flux:** User SOS → Garage Notification → Acceptation → Tracking

