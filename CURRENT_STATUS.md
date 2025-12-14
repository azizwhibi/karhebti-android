# ⚠️ ÉTAT ACTUEL DU FLUX SOS

## ✅ CE QUI FONCTIONNE

### Android - User
- ✅ Bouton SOS sur HomeScreen
- ✅ Envoi SOS (pas de crash)
- ✅ SOSStatusScreen avec polling
- ✅ Détection automatique (code prêt)
- ✅ Navigation auto vers tracking (code prêt)

### Android - Garage Owner  
- ✅ KarhebtiMessagingService (reçoit FCM)
- ✅ BreakdownDetailScreen (accepter/refuser)
- ✅ API updateBreakdownStatus

---

## ❌ CE QUI NE FONCTIONNE PAS

### 🔴 BLOQUANT #1: Backend ne notifie pas
```
POST /breakdowns créé ✅
└─> Chercher garages ❌ PAS IMPLÉMENTÉ
    └─> Envoyer FCM ❌ PAS IMPLÉMENTÉ
```

**Impact:** Garage owners ne reçoivent JAMAIS de notifications

---

### 🟡 IMPORTANT #2: MainActivity navigation
```
Garage tap notification
└─> App s'ouvre sur Home ❌ PAS SUR BreakdownDetailScreen
```

**Impact:** Garage owner doit chercher manuellement le SOS

---

### 🟡 IMPORTANT #3: Token FCM non enregistré
```
App démarre
└─> Devrait envoyer token FCM au backend ❌ PAS FAIT
```

**Impact:** Backend ne sait pas où envoyer les notifications

---

## 🔧 SOLUTIONS

### 1. Backend (URGENT)
Fichier: `backend/routes/breakdowns.js`

```javascript
router.post('/api/breakdowns', async (req, res) => {
  const breakdown = await Breakdown.create(req.body);
  
  // ✅ AJOUTER CECI:
  const garages = await User.find({
    role: 'propGarage',
    fcmToken: { $exists: true }
  });
  
  for (const garage of garages) {
    await admin.messaging().send({
      token: garage.fcmToken,
      data: {
        type: 'new_breakdown',
        breakdownId: breakdown._id.toString()
      }
    });
  }
  
  res.json(breakdown);
});
```

### 2. MainActivity (IMPORTANT)
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    LaunchedEffect(Unit) {
        handleNotificationIntent(intent, navController)
    }
}
```

### 3. Token FCM (IMPORTANT)
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener {
    val token = it.result
    api.updateFCMToken(token)
}
```

---

## 📊 PRIORITÉS

1. 🔴 **Backend FCM** - Sans cela, RIEN ne fonctionne
2. 🟡 MainActivity navigation - UX important
3. 🟡 Token registration - Nécessaire pour FCM

---

**Voir:** `REQUIRED_FIXES.md` pour plus de détails

**Status:** ⚠️ **BACKEND MODIFICATIONS REQUISES**

