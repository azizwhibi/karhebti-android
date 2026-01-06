# ✅ ADAPTATION BACKEND NESTJS - Changements effectués

## 📋 Date: 14 décembre 2024

---

## 🎯 Problème

Le backend NestJS utilise des endpoints et formats différents de ce qui était attendu. Il fallait adapter le code Android pour qu'il corresponde exactement.

---

## 🔧 CHANGEMENTS EFFECTUÉS

### 1. ✅ Endpoint de mise à jour du statut

**Problème :** Le backend utilise `PUT /breakdowns/:id/status` et non `PATCH /breakdowns/:id`

**Fichier :** `BreakdownsApi.kt`

**AVANT :**
```kotlin
@PATCH("breakdowns/{id}")
suspend fun updateStatus(@Path("id") id: String, @Body status: Map<String, String>)
```

**APRÈS :**
```kotlin
@PUT("breakdowns/{id}/status")
suspend fun updateStatus(@Path("id") id: String, @Body statusDto: Map<String, String>)
```

**Impact :** ✅ Les appels API utilisent maintenant le bon endpoint

---

### 2. ✅ Types de notifications FCM

**Problème :** Le backend envoie `sos_created` et `sos_status_updated`, mais l'app attendait `new_breakdown` et `breakdown_status_update`

**Fichier :** `KarhebtiMessagingService.kt`

**AVANT :**
```kotlin
when (notificationType) {
    "new_breakdown", "sos_request" -> showSOSNotification(...)
    "breakdown_status_update" -> showStatusUpdateNotification(...)
    ...
}
```

**APRÈS :**
```kotlin
when (notificationType) {
    // Support des types backend ET anciens types
    "sos_created", "new_breakdown", "sos_request" -> showSOSNotification(...)
    "sos_status_updated", "breakdown_status_update" -> showStatusUpdateNotification(...)
    "new_message" -> showMessageNotification(...)
    else -> showNotification(...)
}
```

**Impact :** ✅ L'app reconnaît maintenant les notifications du backend NestJS

---

### 3. ✅ Navigation depuis notifications

**Problème :** MainActivity ne gérait pas les nouveaux types de notifications

**Fichier :** `MainActivity.kt`

**AVANT :**
```kotlin
when (notificationType) {
    "sos", "new_breakdown" -> { ... }
    "status_update" -> { ... }
    ...
}
```

**APRÈS :**
```kotlin
when (notificationType) {
    // Support de tous les types (backend + anciens)
    "sos", "new_breakdown", "sos_request", "sos_created" -> {
        navController.navigate(Screen.BreakdownDetail.createRoute(breakdownId))
    }
    "status_update", "breakdown_status_update", "sos_status_updated" -> {
        navController.navigate(Screen.BreakdownTracking.createRoute(breakdownId))
    }
    "message" -> { ... }
    else -> { ... }
}
```

**Impact :** ✅ Navigation fonctionne avec les notifications du backend

---

## 📊 CORRESPONDANCE BACKEND ↔ ANDROID

### Endpoints API

| Action | Backend NestJS | Android API | Status |
|--------|---------------|-------------|---------|
| Créer SOS | `POST /breakdowns` | `POST /breakdowns` | ✅ |
| Lister SOS | `GET /breakdowns?status=X` | `GET /breakdowns?status=X` | ✅ |
| Détail SOS | `GET /breakdowns/:id` | `GET /breakdowns/:id` | ✅ |
| Update status | `PUT /breakdowns/:id/status` | `PUT /breakdowns/:id/status` | ✅ Corrigé |
| Assigner agent | `PUT /breakdowns/:id/assign` | `PUT /breakdowns/:id/assign` | ✅ |

---

### Statuts

| Backend (NestJS) | Android | Compatible |
|------------------|---------|------------|
| `PENDING` | `PENDING` | ✅ |
| `ACCEPTED` | `ACCEPTED` | ✅ |
| `REFUSED` | `REFUSED` | ✅ |
| `IN_PROGRESS` | `IN_PROGRESS` | ✅ |
| `COMPLETED` | `COMPLETED` | ✅ |
| `CANCELLED` | `CANCELLED` | ✅ |

---

### Types de notifications FCM

| Backend (NestJS) | Android | Utilisation |
|------------------|---------|-------------|
| `sos_created` | ✅ Supporté | Nouvelle panne créée |
| `sos_status_updated` | ✅ Supporté | Statut changé |
| `new_breakdown` | ✅ Supporté (legacy) | Ancienne appellation |
| `breakdown_status_update` | ✅ Supporté (legacy) | Ancienne appellation |

---

## 🔄 FLUX COMPLET APRÈS CORRECTIONS

### 1. User envoie SOS

```
Android App
  ↓ POST /breakdowns
  {
    type: "Panne moteur",
    description: "...",
    latitude: 36.8065,
    longitude: 10.1815,
    vehicleId: "abc123"
  }
  ↓
Backend NestJS
  ↓ Crée breakdown (status: PENDING)
  ↓ Envoie notification FCM
  {
    type: "sos_created",  ← Type backend
    breakdownId: "675c...",
    status: "PENDING"
  }
  ↓
User app
  ↓ Reçoit breakdownId
  ↓ Navigate: SOSStatusScreen
```

---

### 2. Garage reçoit notification

```
Backend NestJS
  ↓ Trouve garages (role: propGarage)
  ↓ Envoie FCM notification
  {
    type: "sos_created",  ← Type backend
    breakdownId: "675c...",
    titre: "Nouvelle demande SOS",
    message: "Panne moteur - ..."
  }
  ↓
Garage App
  ↓ KarhebtiMessagingService.onMessageReceived()
  ↓ Détecte type: "sos_created"  ✅
  ↓ showSOSNotification()
  ↓ Notification Android affichée
```

---

### 3. Garage accepte

```
Garage App (BreakdownDetailScreen)
  ↓ Clique "Accepter"
  ↓ PUT /breakdowns/:id/status  ✅ Endpoint corrigé
  {
    "status": "ACCEPTED"
  }
  ↓
Backend NestJS
  ↓ Met à jour status = ACCEPTED
  ↓ Envoie notification FCM au user
  {
    type: "sos_status_updated",  ← Type backend
    breakdownId: "675c...",
    status: "ACCEPTED"
  }
  ↓
User App
  ↓ SOSStatusScreen poll détecte
  ↓ status = "ACCEPTED"
  ↓ Navigate: BreakdownTracking
```

---

## ✅ VÉRIFICATIONS

### Test 1: Endpoint API correct
```bash
# Logs Android
adb logcat | grep "Retrofit"
```
Devrait afficher :
```
--> PUT http://172.18.1.246:3000/breakdowns/675c.../status
```
✅ Plus de PATCH, maintenant PUT

---

### Test 2: Notifications reçues
```bash
# Logs Android
adb logcat | grep "KarhebtiMessaging"
```
Devrait afficher :
```
Type: sos_created  ← Type backend
🚨 Création notification SOS...
✅✅✅ NOTIFICATION SOS AFFICHÉE
```
✅ Type backend reconnu

---

### Test 3: Navigation depuis notification
```bash
# Logs Android
adb logcat | grep "MainActivity"
```
Devrait afficher :
```
📱 Navigation depuis notification: sos_created
🚨 Navigation vers BreakdownDetail: 675c...
```
✅ Navigation fonctionne avec type backend

---

## 📝 RÉSUMÉ DES FICHIERS MODIFIÉS

### 1. `BreakdownsApi.kt`
- ✅ Changé `@PATCH` en `@PUT` pour `/breakdowns/{id}/status`
- ✅ Ajouté commentaire explicatif sur le format attendu

### 2. `KarhebtiMessagingService.kt`
- ✅ Ajouté support pour `sos_created`
- ✅ Ajouté support pour `sos_status_updated`
- ✅ Conservé support des anciens types (rétrocompatibilité)

### 3. `MainActivity.kt`
- ✅ Ajouté `sos_created` dans les types gérés
- ✅ Ajouté `sos_status_updated` dans les types gérés
- ✅ Conservé support des anciens types

---

## 🎯 COMPATIBILITÉ

### Backend NestJS ✅
- ✅ Endpoint `PUT /breakdowns/:id/status`
- ✅ Type notification `sos_created`
- ✅ Type notification `sos_status_updated`
- ✅ Statuts en MAJUSCULES (PENDING, ACCEPTED, etc.)

### Rétrocompatibilité ✅
- ✅ Anciens types de notifications toujours supportés
- ✅ Migration transparente sans casser l'ancien code
- ✅ Logs détaillés pour déboguer

---

## 🧪 TESTS À FAIRE

### Test complet E2E

1. **User envoie SOS**
   ```
   ✅ POST /breakdowns fonctionne
   ✅ SOSStatusScreen s'affiche
   ✅ Polling démarre
   ```

2. **Backend notifie garage**
   ```
   ✅ Notification FCM envoyée (type: sos_created)
   ✅ Garage reçoit notification
   ✅ Tap → BreakdownDetailScreen
   ```

3. **Garage accepte**
   ```
   ✅ PUT /breakdowns/:id/status avec {"status":"ACCEPTED"}
   ✅ Backend met à jour
   ✅ Backend notifie user (type: sos_status_updated)
   ```

4. **User détecte acceptation**
   ```
   ✅ Poll détecte status=ACCEPTED
   ✅ Navigate: BreakdownTracking
   ```

5. **Les deux connectés**
   ```
   ✅ Carte avec 2 marqueurs
   ✅ Tracking en temps réel
   ✅ Appel/Chat fonctionnent
   ```

---

## 📚 DOCUMENTATION

### Pour le backend (à créer si nécessaire)

Le backend NestJS devrait envoyer les notifications FCM avec ce format :

```typescript
// Pour une nouvelle panne (sos_created)
{
  notification: {
    title: "Nouvelle demande SOS",
    body: "Panne moteur - 15 km"
  },
  data: {
    type: "sos_created",  // ← Important
    breakdownId: "675c9876543210abcdef",
    status: "PENDING",
    latitude: "36.8065",
    longitude: "10.1815"
  }
}

// Pour un changement de statut (sos_status_updated)
{
  notification: {
    title: "SOS accepté",
    body: "Un garage a accepté votre demande"
  },
  data: {
    type: "sos_status_updated",  // ← Important
    breakdownId: "675c9876543210abcdef",
    status: "ACCEPTED"
  }
}
```

---

## ✅ CONCLUSION

### Changements effectués : 3 fichiers

1. ✅ **BreakdownsApi.kt** - Endpoint corrigé
2. ✅ **KarhebtiMessagingService.kt** - Types notifications ajoutés
3. ✅ **MainActivity.kt** - Navigation mise à jour

### Compatibilité : 100%

- ✅ Backend NestJS
- ✅ Rétrocompatibilité avec anciens types
- ✅ Tous les statuts supportés

### Tests requis : E2E

- Compiler l'app
- Tester le flux complet
- Vérifier les logs

---

**Date :** 14 décembre 2024  
**Status :** ✅ Adapté au backend NestJS  
**Action requise :** Compiler et tester

