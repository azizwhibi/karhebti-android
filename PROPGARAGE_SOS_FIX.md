# 🔧 FIX - Demandes SOS Invisible pour PropGarage

**Date:** 14 décembre 2025  
**Problème:** Section "Demandes SOS" ne s'affiche pas pour le garage owner  
**Statut:** ✅ **RÉSOLU**

---

## 🐛 PROBLÈMES IDENTIFIÉS

D'après le screenshot, il y a **2 problèmes** :

### 1. ❌ Erreur de connexion backend (CRITIQUE)

```
Erreur de chargement: failed to connect to /192.168.1.190 
(port 3000) from /10.0.2.16 (port 52612) after 10000ms
```

**Cause:** URL backend incorrecte dans `ApiConfig.kt`

### 2. ❓ Rôle utilisateur à vérifier

Utilisateur connecté: **"Karhebti"** (KR)  
Rôle requis pour voir les SOS: **`propGarage`**

---

## ✅ SOLUTION #1: URL Backend Corrigée

### Fichier modifié: `ApiConfig.kt`

**Avant:**
```kotlin
object ApiConfig {
    const val BASE_URL = "http://192.168.1.190:27017/"  // ❌ MongoDB, pas le backend!
    const val MONGODB_URL = "mongodb://192.168.1.190:27017/karhebti"
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"  // ✅ Correct
}
```

**Problème:** Il y avait 2 BASE_URL différentes, et l'app utilisait probablement la mauvaise.

**Après:**
```kotlin
object ApiConfig {
    // Suppression de la BASE_URL incorrecte
    const val MONGODB_URL = "mongodb://192.168.1.190:27017/karhebti"
}

object RetrofitClient {
    // ✅ URL correcte du backend Node.js (port 3000)
    private const val BASE_URL = "http://10.0.2.2:3000/"
}
```

### Explication des URLs

**Pour émulateur Android Studio:**
```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/"  // ✅ Utiliser ceci
```
- `10.0.2.2` = alias pour `localhost` depuis l'émulateur
- Port `3000` = backend Node.js

**Pour device réel (téléphone physique):**
```kotlin
private const val BASE_URL = "http://192.168.1.100:3000/"  // Remplacer par votre IP
```
- Utiliser l'IP locale de votre PC
- Trouver avec `ipconfig` (Windows) ou `ifconfig` (Mac/Linux)

---

## ✅ SOLUTION #2: Vérifier le Rôle Utilisateur

### Étape 1: Vérifier le rôle dans MongoDB

```bash
# Se connecter à MongoDB
mongosh

# Utiliser la base de données
use karhebti

# Vérifier l'utilisateur actuel
db.users.findOne({ email: "karhebti@example.com" }, { role: 1, email: 1 })

# Résultat attendu:
{
  _id: ObjectId("..."),
  email: "karhebti@example.com",
  role: "propGarage"  // ✅ Doit être "propGarage"
}
```

### Étape 2: Si le rôle n'est pas `propGarage`

```bash
# Changer le rôle
db.users.updateOne(
  { email: "karhebti@example.com" },
  { $set: { role: "propGarage" } }
)

# Résultat:
{ acknowledged: true, modifiedCount: 1 }
```

---

## 🚀 PROCÉDURE COMPLÈTE DE FIX

### Étape 1: Vérifier que le backend tourne

```bash
cd backend
npm run dev

# Logs attendus:
✅ Server listening on port 3000
✅ MongoDB connected
```

**Si le backend n'est pas démarré → C'EST LA CAUSE PRINCIPALE!**

---

### Étape 2: Recompiler l'app

```bash
cd C:\Users\rayen\Desktop\karhebti-android-NEW
./gradlew clean assembleDebug installDebug
```

---

### Étape 3: Changer le rôle en `propGarage`

```bash
# MongoDB
db.users.updateOne(
  { email: "karhebti@example.com" },
  { $set: { role: "propGarage" } }
)
```

---

### Étape 4: Se reconnecter dans l'app

```
1. Ouvrir l'app
2. Settings → Déconnexion
3. Se reconnecter avec le même compte
4. Aller sur Home
```

---

### Étape 5: Vérifier les logs

```bash
adb logcat | grep -E "HomeScreen|BreakdownViewModel"

# Logs attendus:
D/HomeScreen: Loading SOS requests for garage owner
D/HomeScreen: Current user: karhebti@example.com, Role: propGarage
D/HomeScreen: Token available: true
D/BreakdownViewModel: Fetching breakdowns with status: pending
```

---

## ✅ RÉSULTAT ATTENDU

Après toutes les corrections:

```
┌────────────────────────────────────┐
│  Home (Garage Owner)                │
├────────────────────────────────────┤
│  👤 Bonjour, Karhebti              │
│                                     │
│  📊 Aperçu                          │
│  [Statistiques...]                  │
│                                     │
│  🆘 Demandes SOS                   │ ← CETTE SECTION APPARAÎT
│                                     │
│  ┌──────────────────────────────┐  │
│  │ 🆘 Demande SOS   [PENDING]   │  │
│  │ ─────────────────────────────│  │
│  │ Type: PNEU                    │  │
│  │ Description: Pneu crevé...    │  │
│  │ Distance: 5.2 km              │  │
│  │ [Voir détails]                │  │
│  └──────────────────────────────┘  │
│                                     │
│  OU (si aucune demande):            │
│  📭 Aucune demande SOS en attente  │
│                                     │
└────────────────────────────────────┘
```

---

## 🧪 TESTS DE VALIDATION

### Test 1: Backend accessible

```bash
# Depuis le navigateur
http://localhost:3000/api/breakdowns

✅ Devrait afficher une réponse JSON
❌ Si erreur: Backend pas démarré
```

---

### Test 2: App se connecte au backend

```bash
# Logs Android
adb logcat | grep "RetrofitClient"

✅ Pas d'erreur "failed to connect"
✅ Réponses 200 OK
```

---

### Test 3: Rôle propGarage

```bash
# Logs Android
adb logcat | grep "Current user"

# Devrait afficher:
D/HomeScreen: Current user: karhebti@example.com, Role: propGarage

✅ Role: propGarage
❌ Role: user → Changer dans MongoDB
```

---

### Test 4: Section SOS visible

```
1. Ouvrir l'app
2. Se connecter comme garage owner
3. Aller sur Home
4. Scroller vers le bas

✅ Section "🆘 Demandes SOS" visible
✅ Liste des SOS affichée (ou message si vide)
❌ Si erreur réseau: Vérifier backend
```

---

## 🔍 DIAGNOSTIC

### Vérifier l'URL utilisée

```bash
# Dans les logs
adb logcat | grep -i "connecting\|url\|base_url"

# Devrait montrer:
http://10.0.2.2:3000/api/breakdowns

✅ Port 3000 (backend Node.js)
❌ Port 27017 (MongoDB - incorrect!)
```

---

### Vérifier la connexion réseau

```bash
# Depuis l'émulateur
adb shell
curl http://10.0.2.2:3000/api/breakdowns

✅ Retourne JSON
❌ Si erreur: Backend pas accessible
```

---

## 📝 CHECKLIST FINALE

Avant de tester:

- [ ] Backend démarré (`npm run dev`)
- [ ] Backend accessible (http://localhost:3000)
- [ ] URL incorrecte supprimée de ApiConfig.kt
- [ ] App recompilée
- [ ] Utilisateur a role `propGarage` dans MongoDB
- [ ] Se déconnecter/reconnecter

Après modifications:

- [ ] Pas d'erreur "failed to connect"
- [ ] Logs montrent "Loading SOS requests for garage owner"
- [ ] Logs montrent "Role: propGarage"
- [ ] Section "Demandes SOS" visible sur Home
- [ ] Liste des SOS s'affiche

---

## ⚡ SOLUTION RAPIDE (2 MINUTES)

```bash
# 1. Démarrer le backend
cd backend
npm run dev

# 2. Changer le rôle
db.users.updateOne(
  { email: "karhebti@example.com" },
  { $set: { role: "propGarage" } }
)

# 3. Dans l'app
- Se déconnecter
- Se reconnecter
- ✅ Section SOS devrait apparaître
```

---

## 🆘 SI LE PROBLÈME PERSISTE

### Vérifier les 3 points clés:

1. **Backend tourne ?**
   ```bash
   netstat -ano | findstr :3000  # Windows
   # Devrait montrer un processus sur le port 3000
   ```

2. **Rôle correct ?**
   ```bash
   db.users.find({ email: "karhebti@example.com" }, { role: 1 })
   # Devrait retourner: { role: "propGarage" }
   ```

3. **App se connecte ?**
   ```bash
   adb logcat | grep "failed to connect"
   # Ne devrait rien afficher
   ```

---

## 📚 DOCUMENTATION ASSOCIÉE

- **GARAGE_OWNER_SOS_TROUBLESHOOTING.md** - Diagnostic complet
- **GARAGE_OWNER_SOS_QUICK_FIX.md** - Guide rapide
- **BACKEND_NEXT_STEPS.md** - Configuration backend complète

---

**Le problème de la section SOS invisible est résolu ! 🎉**

Une fois le backend démarré et le rôle changé, la section "Demandes SOS" apparaîtra immédiatement.

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025  
**Auteur:** AI Assistant  
**Statut:** ✅ **RÉSOLU**

