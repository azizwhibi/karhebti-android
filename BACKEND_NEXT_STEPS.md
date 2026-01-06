# 🎯 PROCHAINES ÉTAPES - Backend SOS

**Date:** 14 décembre 2025  
**Priorité:** 🔴 CRITIQUE  
**Durée estimée:** 30 minutes

---

## 📋 RÉSUMÉ

**Situation actuelle:**
- ✅ Android 100% fonctionnel
- ❌ Backend ne notifie pas les garages

**Ce qu'il faut faire:**
- Modifier 3 fichiers backend
- Ajouter 1 champ dans le modèle User
- Configurer Firebase Admin SDK

---

## 🚀 ÉTAPE 1: Configuration Firebase (5 minutes)

### 1.1 Télécharger serviceAccountKey.json

```bash
# 1. Aller sur Firebase Console:
https://console.firebase.google.com/project/karhebti/settings/serviceaccounts/adminsdk

# 2. Cliquer "Generate new private key"
# 3. Télécharger serviceAccountKey.json
# 4. Placer dans: backend/config/serviceAccountKey.json
```

### 1.2 Créer firebase.js

**Fichier:** `backend/config/firebase.js`

```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'karhebti'
});

console.log('✅ Firebase Admin SDK initialisé');

module.exports = admin;
```

### 1.3 Importer dans server.js

**Fichier:** `backend/server.js`

```javascript
// Ajouter en haut après les imports
const admin = require('./config/firebase');

// Le reste du fichier...
```

### 1.4 Installer dépendance

```bash
cd backend
npm install firebase-admin
```

---

## 🔧 ÉTAPE 2: Modifier User Model (2 minutes)

**Fichier:** `backend/models/User.js`

```javascript
const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  name: String,
  email: { type: String, unique: true },
  password: String,
  role: { type: String, enum: ['user', 'propGarage', 'admin'] },
  
  // ✅ AJOUTER CE CHAMP
  fcmToken: { type: String, default: null },
  
  // ✅ AJOUTER LOCATION (si pas déjà là)
  location: {
    type: {
      type: String,
      enum: ['Point'],
      default: 'Point'
    },
    coordinates: {
      type: [Number],
      default: [0, 0] // [longitude, latitude]
    }
  }
});

// ✅ AJOUTER INDEX GÉOSPATIAL
userSchema.index({ location: '2dsphere' });

module.exports = mongoose.model('User', userSchema);
```

---

## 📡 ÉTAPE 3: Endpoint FCM Token (3 minutes)

**Fichier:** `backend/routes/users.js` ou `backend/routes/auth.js`

```javascript
const express = require('express');
const router = express.Router();
const { authenticateToken } = require('../middleware/auth');
const User = require('../models/User');

// ✅ AJOUTER CET ENDPOINT
router.put('/api/users/fcm-token', authenticateToken, async (req, res) => {
  try {
    const { deviceToken } = req.body;
    
    if (!deviceToken) {
      return res.status(400).json({ error: 'deviceToken requis' });
    }
    
    console.log(`📱 Enregistrement token FCM pour user ${req.user.id}`);
    
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

module.exports = router;
```

---

## 🚨 ÉTAPE 4: Modifier POST /breakdowns (15 minutes)

**Fichier:** `backend/routes/breakdowns.js`

```javascript
const express = require('express');
const router = express.Router();
const admin = require('firebase-admin'); // ✅ AJOUTER
const { authenticateToken } = require('../middleware/auth');
const Breakdown = require('../models/Breakdown');
const User = require('../models/User');

router.post('/api/breakdowns', authenticateToken, async (req, res) => {
  try {
    console.log('📥 Création breakdown...');
    console.log('User:', req.user.id);
    console.log('Body:', req.body);
    
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
    
    // ✅ 2. AJOUTER: Chercher garages à proximité (10 km)
    try {
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
      
      console.log(`👥 ${nearbyGarages.length} garages trouvés à proximité`);
      
      // ✅ 3. AJOUTER: Envoyer notification FCM à chaque garage
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
          
        } catch (error) {
          console.error(`❌ Erreur envoi à ${garage.email}:`, error.message);
          if (error.code === 'messaging/invalid-registration-token' || 
              error.code === 'messaging/registration-token-not-registered') {
            // Token invalide, le supprimer
            await User.findByIdAndUpdate(garage._id, { fcmToken: null });
            console.log(`🗑️ Token FCM invalide supprimé pour ${garage.email}`);
          }
          failedCount++;
        }
      }
      
      console.log(`📊 Résumé notifications: ${sentCount} envoyées, ${failedCount} échouées`);
      
    } catch (geoError) {
      console.error('❌ Erreur recherche géographique:', geoError);
      // Continuer même si la recherche échoue
    }
    
    // 4. Retourner le breakdown créé
    res.status(201).json(breakdown);
    
  } catch (error) {
    console.error('❌ Erreur création breakdown:', error);
    res.status(500).json({ 
      error: 'Erreur lors de la création du breakdown',
      details: error.message 
    });
  }
});

module.exports = router;
```

---

## 🔄 ÉTAPE 5: Améliorer PATCH /breakdowns/:id (5 minutes)

**Fichier:** `backend/routes/breakdowns.js`

```javascript
// Trouver l'endpoint PATCH existant et AJOUTER notification user:

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
    ).populate('userId', 'name email fcmToken'); // ✅ Populer avec fcmToken
    
    if (!breakdown) {
      return res.status(404).json({ error: 'Breakdown non trouvé' });
    }
    
    console.log(`✅ Status mis à jour: ${status}`);
    
    // ✅ 2. AJOUTER: Si accepté, notifier le user
    if (status === 'ACCEPTED' && breakdown.userId && breakdown.userId.fcmToken) {
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
          },
          android: {
            priority: 'high',
            notification: {
              channelId: 'sos_notifications',
              sound: 'default',
              priority: 'high'
            }
          }
        });
        
        console.log(`✅ Notification envoyée au user ${breakdown.userId.email}`);
      } catch (error) {
        console.error('❌ Erreur envoi notification user:', error);
        // Ne pas bloquer la réponse si la notification échoue
      }
    }
    
    res.json(breakdown);
    
  } catch (error) {
    console.error('❌ Erreur mise à jour:', error);
    res.status(500).json({ error: error.message });
  }
});
```

---

## 📊 ÉTAPE 6: Créer index géospatial (2 minutes)

```bash
# Dans MongoDB shell ou via script:

# Se connecter à MongoDB
mongosh

# Utiliser la base de données
use karhebti

# Créer l'index géospatial
db.users.createIndex({ location: "2dsphere" })

# Vérifier l'index
db.users.getIndexes()

# Devrait afficher:
# [
#   { v: 2, key: { _id: 1 }, name: "_id_" },
#   { v: 2, key: { location: "2dsphere" }, name: "location_2dsphere" }
# ]
```

---

## 🧪 ÉTAPE 7: Tester (5 minutes)

### Test 1: Vérifier Firebase

```bash
# Démarrer le backend
cd backend
npm run dev

# Logs attendus:
✅ Firebase Admin SDK initialisé
✅ Server listening on port 3000
```

---

### Test 2: Enregistrer token FCM

```bash
# Depuis l'app Android:
# - Se connecter
# - L'app envoie automatiquement le token

# Vérifier backend logs:
📱 Enregistrement token FCM pour user 12345
✅ Token FCM enregistré pour user@example.com

# Vérifier MongoDB:
db.users.findOne({ email: "user@example.com" }, { fcmToken: 1 })
# Devrait retourner un token
```

---

### Test 3: Envoyer SOS

```bash
# Depuis l'app Android:
# - Appuyer sur bouton SOS
# - Remplir formulaire
# - Envoyer

# Vérifier backend logs:
📥 Création breakdown...
✅ Breakdown créé: 6756e8f8...
📍 Position: 36.8065, 10.1815
👥 5 garages trouvés à proximité
✅ Notification envoyée à garage1@example.com
✅ Notification envoyée à garage2@example.com
📊 Résumé notifications: 5 envoyées, 0 échouées
```

---

### Test 4: Vérifier réception

```bash
# Sur téléphone garage owner:
# - Notification apparaît
# - Titre: "🚨 Nouvelle demande SOS"
# - Body: "Assistance PNEU demandée à proximité"

# ✅ SUCCESS!
```

---

## ❌ DÉPANNAGE

### Erreur: "app/invalid-credential"

```bash
# Cause: serviceAccountKey.json invalide ou absent

# Solution:
1. Retélécharger serviceAccountKey.json depuis Firebase Console
2. Vérifier qu'il est dans backend/config/
3. Vérifier que le fichier est valide JSON
```

---

### Erreur: "0 garages trouvés"

```bash
# Cause 1: Index géospatial manquant
# Solution:
db.users.createIndex({ location: "2dsphere" })

# Cause 2: Garages n'ont pas de location
# Solution:
db.users.updateMany(
  { role: "propGarage" },
  { $set: { 
    location: { 
      type: "Point", 
      coordinates: [10.1815, 36.8065] // [longitude, latitude]
    }
  }}
)

# Cause 3: Garages n'ont pas de fcmToken
# Solution: Ouvrir l'app garage et se connecter
```

---

### Erreur: "messaging/invalid-registration-token"

```bash
# Cause: Token FCM périmé ou invalide

# Solution automatique: Le code supprime le token automatiquement
# Solution manuelle:
db.users.updateOne(
  { email: "garage@example.com" },
  { $set: { fcmToken: null } }
)
# Puis rouvrir l'app garage pour obtenir nouveau token
```

---

## ✅ CHECKLIST FINALE

Avant de tester:

- [ ] Firebase Admin SDK configuré
- [ ] serviceAccountKey.json présent
- [ ] npm install firebase-admin exécuté
- [ ] Modèle User a champ fcmToken
- [ ] Index géospatial créé
- [ ] Endpoint PUT /users/fcm-token ajouté
- [ ] POST /breakdowns modifié
- [ ] PATCH /breakdowns/:id modifié
- [ ] Backend redémarré

Après modification:

- [ ] Backend démarre sans erreur
- [ ] App envoie token FCM au démarrage
- [ ] User peut envoyer SOS
- [ ] Backend trouve garages à proximité
- [ ] Backend envoie notifications FCM
- [ ] Garages reçoivent notifications
- [ ] Garages peuvent accepter
- [ ] User reçoit confirmation

**Si tous ✅ : FLUX SOS COMPLET FONCTIONNEL! 🎉**

---

## 📞 SUPPORT

En cas de problème:

1. Vérifier backend logs
2. Vérifier MongoDB data
3. Vérifier Firebase Console (Errors tab)
4. Vérifier Android logcat

---

**Version:** 1.0.0  
**Auteur:** AI Assistant  
**Date:** 14 décembre 2025  
**Priorité:** 🔴 CRITIQUE

