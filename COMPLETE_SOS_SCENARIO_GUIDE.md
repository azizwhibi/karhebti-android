# 🔧 GUIDE COMPLET - Flux SOS Correct (Frontend + Backend)

## 📋 Date: 14 décembre 2024

---

## 🎯 Scénario Complet Corrigé

### ⏱️ Timeline du flux SOS

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         FLUX COMPLET SOS                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  0:00  👤 User clique "🆘 SOS" sur son véhicule                         │
│         └─> Ouvre BreakdownSOSScreen                                    │
│                                                                           │
│  0:01  📱 User remplit le formulaire                                     │
│         ├─> Type: Panne moteur                                          │
│         ├─> Description: "Le moteur ne démarre plus"                    │
│         └─> Position GPS: 36.8065, 10.1815                              │
│                                                                           │
│  0:02  📤 User clique "Envoyer SOS"                                      │
│         └─> POST /breakdowns                                             │
│             ├─ type: "Panne moteur"                                     │
│             ├─ description: "..."                                        │
│             ├─ latitude: 36.8065                                        │
│             ├─ longitude: 10.1815                                       │
│             ├─ userId: (extrait du JWT)                                 │
│             └─ vehicleId: "abc123"                                      │
│                                                                           │
│  0:03  🖥️  Backend reçoit la requête                                     │
│         ├─> Crée breakdown dans MongoDB                                 │
│         │   ├─ _id: "675c9876543210abcdef"                              │
│         │   ├─ status: "PENDING"                                        │
│         │   ├─ userId: "1234567890"                                     │
│         │   └─ createdAt: "2024-12-14T17:30:00Z"                        │
│         └─> Retourne breakdown au client                                │
│                                                                           │
│  0:04  📱 App reçoit breakdownId                                         │
│         └─> Navigate: SOSStatusScreen(breakdownId)                      │
│                                                                           │
│  0:05  📊 SOSStatusScreen démarre                                        │
│         ├─> Affiche "🚨 En attente..."                                  │
│         ├─> Affiche "Recherche d'un garage..."                          │
│         └─> Démarre polling (GET /breakdowns/:id) toutes les 5s        │
│                                                                           │
│  0:06  🖥️  Backend trouve les garages                                    │
│         ├─> Query: db.users.find({ role: "propGarage" })                │
│         ├─> Trouve 3 garages dans la région                             │
│         └─> Récupère leurs FCM tokens                                   │
│                                                                           │
│  0:07  🔔 Backend envoie notifications FCM                               │
│         └─> POST https://fcm.googleapis.com/fcm/send                    │
│             {                                                            │
│               "to": "garage_fcm_token_1",                               │
│               "notification": {                                          │
│                 "title": "🆘 Nouvelle demande SOS",                     │
│                 "body": "Panne moteur - 15 km"                          │
│               },                                                         │
│               "data": {                                                  │
│                 "type": "new_breakdown",                                │
│                 "breakdownId": "675c9876543210abcdef",                  │
│                 "latitude": "36.8065",                                  │
│                 "longitude": "10.1815"                                  │
│               }                                                          │
│             }                                                            │
│                                                                           │
│  0:08  📱 Garage owner reçoit notification                               │
│         └─> KarhebtiMessagingService.onMessageReceived()                │
│             ├─> Type détecté: "new_breakdown"                           │
│             ├─> Crée notification Android                               │
│             └─> Affiche notification avec son/vibration                 │
│                                                                           │
│  0:09  🔔 Garage owner voit la notification                              │
│         "🆘 Nouvelle demande SOS                                        │
│          Panne moteur - 15 km"                                          │
│                                                                           │
│  0:10  👆 Garage owner TAP sur notification                              │
│         └─> MainActivity démarre avec extras:                           │
│             ├─ from_notification: true                                  │
│             ├─ notification_type: "sos"                                 │
│             └─ breakdownId: "675c9876543210abcdef"                      │
│                                                                           │
│  0:11  🧭 MainActivity navigue automatiquement                           │
│         └─> Navigate: BreakdownsList → BreakdownDetail                 │
│                                                                           │
│  0:12  📱 BreakdownDetailScreen affiche                                  │
│         ├─> 🗺️ Carte OpenStreetMap avec position                        │
│         ├─> 📋 Détails: Type, Description, Distance                     │
│         ├─> 👤 Info client                                              │
│         └─> 🟢 Bouton [Accepter] | 🔴 [Refuser]                         │
│                                                                           │
│  0:13  👆 Garage owner clique "Accepter"                                 │
│         ├─> Dialogue de confirmation apparaît                           │
│         └─> "Êtes-vous sûr de vouloir accepter?"                        │
│                                                                           │
│  0:14  ✅ Garage owner confirme                                          │
│         └─> PATCH /breakdowns/:id                                       │
│             { "status": "ACCEPTED" }                                    │
│                                                                           │
│  0:15  🖥️  Backend met à jour                                            │
│         ├─> db.breakdowns.updateOne(                                    │
│         │     { _id: "675c..." },                                       │
│         │     { status: "ACCEPTED", assignedTo: garageId }              │
│         │   )                                                           │
│         └─> Envoie notification au user                                │
│             POST https://fcm.googleapis.com/fcm/send                    │
│             {                                                            │
│               "to": "user_fcm_token",                                   │
│               "data": {                                                  │
│                 "type": "breakdown_status_update",                      │
│                 "breakdownId": "675c...",                               │
│                 "status": "ACCEPTED"                                    │
│               }                                                          │
│             }                                                            │
│                                                                           │
│  0:16  📱 Garage app reçoit succès                                       │
│         └─> Navigate: BreakdownTracking(breakdownId)                   │
│                                                                           │
│  0:17  📱 User app poll détecte changement                               │
│         ├─> SOSStatusScreen: GET /breakdowns/:id                        │
│         ├─> Reçoit: status = "ACCEPTED"                                │
│         └─> Auto-navigate: BreakdownTracking(breakdownId)              │
│                                                                           │
│  0:18  🎉 Les deux apps sont sur BreakdownTracking                       │
│         ├─> 🗺️ Carte en temps réel                                      │
│         ├─> 📞 Bouton appel visible                                     │
│         ├─> 💬 Chat disponible                                          │
│         └─> 📍 Position mise à jour toutes les 5s                       │
│                                                                           │
│  ✅ CONNEXION RÉUSSIE!                                                   │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🖥️ GUIDE BACKEND

### 📁 Structure recommandée

```
backend/
├── models/
│   ├── User.js (avec role et fcmToken)
│   └── Breakdown.js
├── routes/
│   ├── auth.js
│   ├── breakdowns.js
│   └── notifications.js
├── services/
│   ├── fcm.service.js      ← ⭐ IMPORTANT
│   └── location.service.js
├── middleware/
│   └── auth.middleware.js
└── server.js
```

### 1️⃣ Model User (MongoDB)

```javascript
// models/User.js
const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  email: { type: String, required: true, unique: true },
  password: { type: String, required: true },
  nom: String,
  prenom: String,
  telephone: String,
  role: { 
    type: String, 
    enum: ['user', 'propGarage', 'admin'], 
    default: 'user' 
  },
  
  // ⭐ FCM Token pour les notifications
  fcmToken: { type: String, default: null },
  
  // Pour les propGarage
  garageName: String,
  garageAddress: String,
  latitude: Number,
  longitude: Number,
  
  createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);
```

### 2️⃣ Model Breakdown (MongoDB)

```javascript
// models/Breakdown.js
const mongoose = require('mongoose');

const breakdownSchema = new mongoose.Schema({
  userId: { 
    type: mongoose.Schema.Types.ObjectId, 
    ref: 'User', 
    required: true 
  },
  vehicleId: { 
    type: mongoose.Schema.Types.ObjectId, 
    ref: 'Vehicle' 
  },
  
  type: { type: String, required: true }, // "Panne moteur", "Crevaison", etc.
  description: String,
  
  latitude: { type: Number, required: true },
  longitude: { type: Number, required: true },
  
  status: { 
    type: String, 
    enum: ['PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'],
    default: 'PENDING' 
  },
  
  assignedTo: { 
    type: mongoose.Schema.Types.ObjectId, 
    ref: 'User',
    default: null 
  },
  
  photo: String, // URL de la photo
  
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

// ⭐ Middleware pour mettre à jour updatedAt
breakdownSchema.pre('save', function(next) {
  this.updatedAt = Date.now();
  next();
});

module.exports = mongoose.model('Breakdown', breakdownSchema);
```

### 3️⃣ Service FCM

```javascript
// services/fcm.service.js
const admin = require('firebase-admin');
const User = require('../models/User');

// ⭐ Initialiser Firebase Admin SDK
const serviceAccount = require('../config/firebase-service-account.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

/**
 * Envoyer une notification à un garage owner
 */
async function notifyGarageOwners(breakdown) {
  try {
    console.log('🔔 Envoi notifications aux garages...');
    
    // 1. Trouver tous les garage owners avec FCM token
    const garageOwners = await User.find({
      role: 'propGarage',
      fcmToken: { $ne: null, $exists: true }
    });
    
    console.log(`📊 ${garageOwners.length} garages trouvés`);
    
    if (garageOwners.length === 0) {
      console.warn('⚠️  Aucun garage avec FCM token');
      return;
    }
    
    // 2. Préparer les tokens
    const tokens = garageOwners.map(g => g.fcmToken);
    
    // 3. Calculer la distance (optionnel)
    // TODO: Filtrer par proximité si nécessaire
    
    // 4. Créer le message FCM
    const message = {
      notification: {
        title: '🆘 Nouvelle demande SOS',
        body: `${breakdown.type} - ${breakdown.description || 'Assistance requise'}`
      },
      data: {
        type: 'new_breakdown',
        breakdownId: breakdown._id.toString(),
        userId: breakdown.userId.toString(),
        latitude: breakdown.latitude.toString(),
        longitude: breakdown.longitude.toString(),
        breakdownType: breakdown.type,
        status: breakdown.status
      },
      android: {
        priority: 'high',
        notification: {
          sound: 'default',
          channelId: 'sos_notifications',
          priority: 'max',
          defaultVibrateTimings: true
        }
      }
    };
    
    // 5. Envoyer à tous les tokens
    const promises = tokens.map(token => 
      admin.messaging().send({
        ...message,
        token: token
      }).catch(error => {
        console.error(`❌ Erreur envoi à ${token}:`, error.message);
        return null;
      })
    );
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r !== null).length;
    
    console.log(`✅ ${successCount}/${tokens.length} notifications envoyées`);
    
  } catch (error) {
    console.error('❌ Erreur notifyGarageOwners:', error);
    throw error;
  }
}

/**
 * Notifier l'utilisateur d'un changement de statut
 */
async function notifyUserStatusChange(breakdown, newStatus) {
  try {
    console.log(`🔔 Notification user: ${breakdown.userId}`);
    
    // 1. Récupérer le user et son FCM token
    const user = await User.findById(breakdown.userId);
    
    if (!user || !user.fcmToken) {
      console.warn('⚠️  User sans FCM token');
      return;
    }
    
    // 2. Message selon le statut
    let title, body;
    switch (newStatus) {
      case 'ACCEPTED':
        title = '✅ Demande acceptée';
        body = 'Un garage a accepté votre demande SOS!';
        break;
      case 'IN_PROGRESS':
        title = '🚗 En route';
        body = 'Le dépanneur est en route vers vous';
        break;
      case 'COMPLETED':
        title = '🎉 Terminé';
        body = 'Votre dépannage est terminé';
        break;
      default:
        title = '📊 Mise à jour';
        body = `Statut: ${newStatus}`;
    }
    
    // 3. Envoyer la notification
    const message = {
      notification: { title, body },
      data: {
        type: 'breakdown_status_update',
        breakdownId: breakdown._id.toString(),
        status: newStatus
      },
      token: user.fcmToken
    };
    
    await admin.messaging().send(message);
    console.log('✅ Notification envoyée au user');
    
  } catch (error) {
    console.error('❌ Erreur notifyUserStatusChange:', error);
  }
}

module.exports = {
  notifyGarageOwners,
  notifyUserStatusChange
};
```

### 4️⃣ Routes Breakdowns

```javascript
// routes/breakdowns.js
const express = require('express');
const router = express.Router();
const Breakdown = require('../models/Breakdown');
const { authMiddleware } = require('../middleware/auth.middleware');
const { notifyGarageOwners, notifyUserStatusChange } = require('../services/fcm.service');

/**
 * POST /breakdowns - Créer une demande SOS
 */
router.post('/', authMiddleware, async (req, res) => {
  try {
    console.log('📥 POST /breakdowns');
    console.log('Body:', req.body);
    console.log('User:', req.user);
    
    const { type, description, latitude, longitude, vehicleId, photo } = req.body;
    
    // Validation
    if (!type || !latitude || !longitude) {
      return res.status(400).json({ 
        error: 'Type, latitude et longitude requis' 
      });
    }
    
    // Créer le breakdown
    const breakdown = new Breakdown({
      userId: req.user.id, // ⭐ Extrait du JWT
      vehicleId,
      type,
      description,
      latitude,
      longitude,
      photo,
      status: 'PENDING'
    });
    
    await breakdown.save();
    console.log(`✅ Breakdown créé: ${breakdown._id}`);
    
    // ⭐ NOTIFIER LES GARAGES
    await notifyGarageOwners(breakdown);
    
    // Retourner au client
    res.status(201).json(breakdown);
    
  } catch (error) {
    console.error('❌ Erreur POST /breakdowns:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /breakdowns/:id - Récupérer un breakdown
 */
router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const breakdown = await Breakdown.findById(req.params.id)
      .populate('userId', 'nom prenom email telephone')
      .populate('assignedTo', 'garageName garageAddress telephone');
    
    if (!breakdown) {
      return res.status(404).json({ error: 'Breakdown non trouvé' });
    }
    
    res.json(breakdown);
    
  } catch (error) {
    console.error('❌ Erreur GET /breakdowns/:id:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /breakdowns - Liste des breakdowns (pour garages)
 */
router.get('/', authMiddleware, async (req, res) => {
  try {
    const { status, userId } = req.query;
    
    let query = {};
    
    // Si c'est un garage, voir tous les PENDING
    if (req.user.role === 'propGarage') {
      query.status = status || 'PENDING';
    } 
    // Si c'est un user, voir seulement ses propres
    else {
      query.userId = req.user.id;
      if (status) query.status = status;
    }
    
    const breakdowns = await Breakdown.find(query)
      .populate('userId', 'nom prenom telephone')
      .populate('assignedTo', 'garageName telephone')
      .sort({ createdAt: -1 });
    
    res.json({ breakdowns });
    
  } catch (error) {
    console.error('❌ Erreur GET /breakdowns:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * PATCH /breakdowns/:id - Mettre à jour le statut
 */
router.patch('/:id', authMiddleware, async (req, res) => {
  try {
    const { status } = req.body;
    const breakdownId = req.params.id;
    
    console.log(`📝 PATCH /breakdowns/${breakdownId}`);
    console.log(`Nouveau statut: ${status}`);
    
    // Valider le statut
    const validStatuses = ['PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
    if (!validStatuses.includes(status)) {
      return res.status(400).json({ error: 'Statut invalide' });
    }
    
    // Mettre à jour
    const breakdown = await Breakdown.findById(breakdownId);
    if (!breakdown) {
      return res.status(404).json({ error: 'Breakdown non trouvé' });
    }
    
    breakdown.status = status;
    
    // Si accepté, assigner au garage
    if (status === 'ACCEPTED' && req.user.role === 'propGarage') {
      breakdown.assignedTo = req.user.id;
    }
    
    breakdown.updatedAt = Date.now();
    await breakdown.save();
    
    console.log(`✅ Breakdown ${breakdownId} mis à jour: ${status}`);
    
    // ⭐ NOTIFIER LE USER
    await notifyUserStatusChange(breakdown, status);
    
    res.json(breakdown);
    
  } catch (error) {
    console.error('❌ Erreur PATCH /breakdowns/:id:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
```

### 5️⃣ Route pour enregistrer FCM Token

```javascript
// routes/auth.js (ajouter cette route)

/**
 * POST /auth/fcm-token - Enregistrer le FCM token
 */
router.post('/fcm-token', authMiddleware, async (req, res) => {
  try {
    const { fcmToken } = req.body;
    
    if (!fcmToken) {
      return res.status(400).json({ error: 'FCM token requis' });
    }
    
    // Mettre à jour le token du user
    await User.findByIdAndUpdate(req.user.id, {
      fcmToken: fcmToken
    });
    
    console.log(`✅ FCM token enregistré pour user ${req.user.id}`);
    
    res.json({ message: 'Token enregistré' });
    
  } catch (error) {
    console.error('❌ Erreur /auth/fcm-token:', error);
    res.status(500).json({ error: error.message });
  }
});
```

### 6️⃣ Fichier de configuration Firebase

```json
// config/firebase-service-account.json
{
  "type": "service_account",
  "project_id": "karhebti-xxxxx",
  "private_key_id": "xxxxx",
  "private_key": "-----BEGIN PRIVATE KEY-----\nXXXXX\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-xxxxx@karhebti-xxxxx.iam.gserviceaccount.com",
  "client_id": "xxxxx",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-xxxxx%40karhebti-xxxxx.iam.gserviceaccount.com"
}
```

**Comment obtenir ce fichier:**
1. Aller sur [Firebase Console](https://console.firebase.google.com/)
2. Sélectionner votre projet "Karhebti"
3. Project Settings → Service Accounts
4. Cliquer "Generate new private key"
5. Sauvegarder le fichier JSON

---

## 📱 GUIDE FRONTEND (Déjà implémenté ✅)

### Fichiers clés déjà en place:

1. **KarhebtiMessagingService.kt** ✅
   - Reçoit les notifications FCM
   - Gère `new_breakdown` et `breakdown_status_update`
   - Affiche les notifications Android

2. **SOSStatusScreen.kt** ✅
   - Polling toutes les 5 secondes
   - Auto-navigation quand status = ACCEPTED

3. **BreakdownDetailScreen.kt** ✅
   - Affiche les détails + carte
   - Boutons Accepter/Refuser
   - Envoie PATCH /breakdowns/:id

4. **MainActivity.kt** ⚠️ À vérifier
   - Doit gérer les extras de notification
   - Doit naviguer automatiquement

---

## 🔍 VÉRIFICATIONS NÉCESSAIRES

### Backend
- [ ] Firebase Admin SDK configuré
- [ ] Service account JSON en place
- [ ] Routes breakdowns créées
- [ ] Middleware auth fonctionne
- [ ] FCM notifications testées

### Frontend
- [ ] KarhebtiMessagingService enregistré dans AndroidManifest
- [ ] FCM token envoyé au backend
- [ ] Notifications affichées correctement
- [ ] Navigation depuis notification fonctionne
- [ ] Polling SOSStatusScreen actif

---

## 🧪 TESTS

### Test 1: Notification backend → frontend

```bash
# Dans le backend
curl -X POST http://localhost:3000/test/send-notification \
  -H "Content-Type: application/json" \
  -d '{
    "fcmToken": "eXXX...XXX",
    "title": "Test SOS",
    "body": "Ceci est un test"
  }'
```

### Test 2: Flux complet

1. User envoie SOS depuis l'app
2. Vérifier logs backend: `Breakdown créé`
3. Vérifier logs backend: `Notifications envoyées`
4. Vérifier notification apparaît sur téléphone garage
5. Taper notification
6. Vérifier ouverture BreakdownDetailScreen
7. Accepter la demande
8. Vérifier logs backend: `Status ACCEPTED`
9. Vérifier user app navigue vers tracking

---

## 📝 CHECKLIST FINALE

### Backend
- [ ] MongoDB installé et en marche
- [ ] Models User et Breakdown créés
- [ ] Firebase Admin SDK configuré
- [ ] Service FCM implémenté
- [ ] Routes breakdowns complètes
- [ ] Route /auth/fcm-token pour enregistrer tokens
- [ ] Middleware auth fonctionne
- [ ] Tests de notifications réussis

### Frontend
- [ ] KarhebtiMessagingService fonctionne
- [ ] FCM token envoyé au backend au login
- [ ] SOSStatusScreen polling actif
- [ ] MainActivity gère navigation depuis notification
- [ ] BreakdownDetailScreen affiche et accepte SOS
- [ ] BreakdownTracking connecte les deux parties

---

## 🚀 DÉMARRAGE

### Backend
```bash
cd backend
npm install firebase-admin
node server.js
```

### Frontend
```bash
cd android
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Logs en temps réel
```bash
# Backend
tail -f logs/app.log

# Frontend
adb logcat | grep -E "KarhebtiMessaging|SOSStatus|BreakdownDetail"
```

---

**Status:** 📋 Guide complet prêt  
**Date:** 14 décembre 2024

