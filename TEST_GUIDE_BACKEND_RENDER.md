# 🧪 GUIDE DE TEST RAPIDE - Après Mise à Jour Backend

## ✅ Checklist de Test

### Pré-requis
- [ ] Backend déployé sur Render : `https://karhebti-backend-supa.onrender.com`
- [ ] Backend accessible (testez dans un navigateur)
- [ ] Appareil/Émulateur avec connexion Internet

---

## 📱 Tests à Effectuer

### 1. Test Connexion ✅
**Objectif :** Vérifier que l'authentification fonctionne

**Étapes :**
1. Lancez l'application
2. Connectez-vous avec vos identifiants
3. Vérifiez que vous accédez à l'écran d'accueil

**Logcat à surveiller :**
```
D/AuthInterceptor: ✓ Authorization header added successfully
```

**Résultat attendu :**
- ✅ Connexion réussie
- ✅ Token JWT stocké
- ✅ Redirection vers HomeScreen

---

### 2. Test Documents (Fix Erreur 500) ✅
**Objectif :** Vérifier que l'erreur CastError est corrigée

**Étapes :**
1. Allez dans "Documents"
2. Cliquez sur un document existant
3. Vérifiez que les détails s'affichent

**Logcat à surveiller :**
```
D/DocumentRepository: Response code: 200
D/DocumentRepository: Document retrieved successfully
```

**Résultat attendu :**
- ✅ Détails du document affichés
- ✅ Pas d'erreur 500
- ✅ Image du document chargée

**Si erreur 500 persiste :**
- Vérifiez que le document dans MongoDB n'a pas de champ `voiture` corrompu
- Le deserializer devrait gérer ce cas automatiquement

---

### 3. Test Notifications (Fix Count Object) ✅
**Objectif :** Vérifier que les notifications se chargent

**Étapes :**
1. Allez dans "Notifications"
2. Vérifiez que la liste se charge
3. Vérifiez le badge de compteur

**Logcat à surveiller :**
```
D/NotificationRepository: ✅ SUCCESS: [X] notifications
D/NotificationRepository: 📊 Unread count: [X]
```

**Résultat attendu :**
- ✅ Liste des notifications affichée
- ✅ Compteur de notifications non lues
- ✅ Pas d'erreur JSON parsing

**Erreur possible (avant le fix) :**
```
❌ Expected an int but was BEGIN_OBJECT at line 1 column 26 path $.count
```
→ Maintenant corrigée avec `UnreadCountDeserializer`

---

### 4. Test Distance/Durée SOS ✅
**Objectif :** Vérifier l'affichage de la distance et ETA

**Étapes :**
1. **En tant que client :** Créez une demande SOS
2. **En tant que garage :** Acceptez la demande
3. Ouvrez l'écran de suivi
4. **Vérifiez que la carte "Distance et Durée" s'affiche**

**Logcat à surveiller :**
```
D/BreakdownTracking: Client: [lat], [lon]
D/BreakdownTracking: Garage réel: [lat], [lon]
```

**Résultat attendu :**
```
┌─────────────────────────────────────┐
│  L'assistant est en route           │
│                                     │
│  Distance        Arrivée estimée    │
│   5.2 km              12 min        │
│                                     │
│  🚗 En route vers votre position    │
└─────────────────────────────────────┘
```

**Si la distance ne s'affiche PAS :**
```
W/BreakdownTracking: Position du garage non disponible pour assignedTo=[id]
```

**Cause :** Le garage dans MongoDB n'a pas de coordonnées GPS (`latitude`, `longitude`)

**Solution :**
1. Ajoutez les coordonnées GPS au garage dans MongoDB
2. Ou créez un nouveau garage avec des coordonnées valides

---

### 5. Test Images ✅
**Objectif :** Vérifier que les images se chargent depuis Render

**Étapes :**
1. Allez dans "Mes Voitures"
2. Vérifiez les images de voiture
3. Allez dans "Marketplace"
4. Vérifiez les images des voitures à vendre

**Logcat à surveiller :**
```
D/ImageUrlHelper: Full URL: https://karhebti-backend-supa.onrender.com/uploads/...
```

**Résultat attendu :**
- ✅ Images chargées depuis Render
- ✅ Pas de 404 ou timeout

---

### 6. Test Chat/WebSocket ✅
**Objectif :** Vérifier la connexion Socket.IO

**Étapes :**
1. Allez dans "Messages"
2. Ouvrez une conversation
3. Envoyez un message

**Logcat à surveiller :**
```
D/ChatWebSocketClient: ✅ Socket.IO Connected successfully!
D/ChatWebSocketClient: Connected to: https://karhebti-backend-supa.onrender.com/chat
D/ChatWebSocketClient: 📨 NEW MESSAGE EVENT RECEIVED
```

**Résultat attendu :**
- ✅ Connexion WebSocket réussie
- ✅ Messages envoyés et reçus en temps réel
- ✅ Indicateur de saisie fonctionne

---

## 🚨 Problèmes Potentiels et Solutions

### Problème 1 : Timeout / Connexion lente
**Symptôme :** L'application met longtemps à charger

**Cause :** Cold start de Render (première requête après inactivité)

**Solution :**
- Attendez 10-30 secondes pour la première requête
- Les requêtes suivantes seront plus rapides
- Render garde le serveur actif pendant ~15 minutes après la dernière requête

---

### Problème 2 : Erreur 401 Unauthorized
**Symptôme :** 
```
D/DocumentRepository: Response code: 401
```

**Cause :** Token JWT expiré ou invalide

**Solution :**
1. Déconnectez-vous
2. Reconnectez-vous
3. Un nouveau token sera généré

---

### Problème 3 : Backend non accessible
**Symptôme :** Timeout ou erreur réseau

**Vérification :**
1. Ouvrez votre navigateur
2. Allez sur : `https://karhebti-backend-supa.onrender.com/health`
3. Vous devriez voir : `{"status":"ok"}`

**Si le backend est down :**
- Vérifiez les logs sur Render Dashboard
- Redémarrez le service si nécessaire

---

### Problème 4 : Distance ne s'affiche pas
**Symptôme :** La carte Distance/Durée n'apparaît pas dans BreakdownTrackingScreen

**Debug :**
```bash
adb logcat | findstr BreakdownTracking
```

**Si vous voyez :**
```
W/BreakdownTracking: Position du garage non disponible
```

**Solution :**
Mettez à jour le garage dans MongoDB :
```javascript
db.garages.updateOne(
  { _id: ObjectId("GARAGE_ID") },
  { $set: { 
    latitude: 36.8065,  // Coordonnées de Tunis par exemple
    longitude: 10.1815 
  }}
)
```

---

## 📊 Logs de Débogage Complets

### Commande Logcat Filtrée
```powershell
adb logcat -s `
  AuthInterceptor:D `
  DocumentRepository:D `
  DocumentViewModel:D `
  DocumentDetailScreen:D `
  NotificationRepository:D `
  BreakdownTracking:D `
  ChatWebSocketClient:D
```

### Logs de Succès Attendus

**Connexion :**
```
D/AuthInterceptor: ✓ Authorization header added successfully
```

**Document :**
```
D/DocumentRepository: Response code: 200
D/DocumentRepository: Document retrieved successfully
```

**Notifications :**
```
D/NotificationRepository: ✅ SUCCESS: 5 notifications
D/NotificationRepository: 📊 Unread count: 2
```

**SOS Tracking :**
```
D/BreakdownTracking: Client: 36.8065, 10.1815
D/BreakdownTracking: Garage réel: 36.8500, 10.2000
```

**WebSocket :**
```
D/ChatWebSocketClient: ✅ Socket.IO Connected successfully!
```

---

## ✅ Checklist Finale

Avant de considérer les tests comme complets :

- [ ] Connexion fonctionne
- [ ] Documents s'affichent sans erreur 500
- [ ] Notifications se chargent correctement
- [ ] Images se chargent depuis Render
- [ ] Chat WebSocket connecté
- [ ] Distance/Durée s'affichent dans SOS (si données GPS présentes)
- [ ] Pas de crash de l'application
- [ ] Logs ne montrent pas d'erreurs majeures

---

## 🎯 Performance Attendue

**Premier appel (cold start) :**
- Délai : 10-30 secondes
- Normal pour Render free tier

**Appels suivants :**
- Délai : 0.5-2 secondes
- Backend reste actif ~15 minutes

**Si performance inacceptable :**
- Envisagez un upgrade Render (plan payant)
- Ou déployez sur un VPS avec IP fixe

---

## 📞 Support

**En cas de problème persistant :**

1. Vérifiez les logs Logcat
2. Vérifiez les logs Render Dashboard
3. Testez l'API backend directement (Postman/cURL)
4. Consultez le fichier `FIXES_APPLIED_BACKEND_URL_AND_ERRORS.md`

**Date du guide :** 2 janvier 2026
**Version :** 1.0
**Status :** ✅ PRÊT POUR LES TESTS

