# 🔧 DÉPANNAGE - Demandes SOS Garage Owner

**Date:** 14 décembre 2025  
**Problème:** "the screen for GarageOwner, i cant see Demandes SOS"  
**Statut:** 🔍 **EN DIAGNOSTIC**

---

## 🎯 PROBLÈME IDENTIFIÉ

D'après le screenshot, il y a **2 problèmes**:

1. ❌ **Erreur de connexion backend** (priorité haute)
2. ❓ **Utilisateur actuel n'est peut-être pas garage owner**

---

## 🔴 PROBLÈME #1: Erreur Backend (CRITIQUE)

### Message d'erreur visible

```
Erreur de chargement: failed to connect to /192.168.1.190 
(port 3000) from /10.0.2.16 (port 52612) after 10000ms
```

### Analyse

```
App Android (émulateur)       Backend
     10.0.2.16          →      192.168.1.190:3000
                        ✗      Connexion échoue
```

**Problème:** L'émulateur ne peut pas accéder à `192.168.1.190:3000`

---

## ✅ SOLUTION #1: Corriger l'URL Backend

### Pour émulateur Android Studio

L'émulateur Android ne peut pas accéder directement à `192.168.1.x`. Vous devez utiliser:

**Option A: Utiliser 10.0.2.2 (loopback de l'émulateur)**

```kotlin
// Si backend tourne sur votre PC
baseUrl = "http://10.0.2.2:3000/"
```

**Option B: Utiliser l'IP réelle de votre PC**

```bash
# Trouver votre IP locale
ipconfig  # Windows
ifconfig  # Mac/Linux

# Exemple de résultat:
IPv4 Address: 192.168.1.100

# Utiliser cette IP:
baseUrl = "http://192.168.1.100:3000/"
```

---

### Comment modifier l'URL

**Fichier:** `app/src/main/java/com/example/karhebti_android/data/api/ApiConfig.kt`

**Chercher:**
```kotlin
private const val BASE_URL = "http://192.168.1.190:3000/"
```

**Remplacer par (pour émulateur):**
```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/"
```

**OU (pour device réel):**
```kotlin
private const val BASE_URL = "http://192.168.1.100:3000/"  // Votre IP PC
```

---

### Vérifier que le backend tourne

```bash
# 1. Vérifier que le backend est démarré
cd backend
npm run dev

# 2. Tester depuis le navigateur
http://localhost:3000/api/breakdowns

# 3. Vérifier que le port 3000 écoute
netstat -ano | findstr :3000  # Windows
lsof -i :3000                  # Mac/Linux
```

---

## 🟡 PROBLÈME #2: Utilisateur n'est pas Garage Owner

### Vérifier le rôle de l'utilisateur

Dans le screenshot, vous êtes connecté avec **"Karhebti"** (visible en haut: "KR").

**La section "Demandes SOS" s'affiche UNIQUEMENT si:**
```kotlin
userRole == "propGarage"
```

---

### Comment vérifier le rôle

**Option 1: Via logs Android**

```bash
adb logcat | grep HomeScreen

# Chercher:
D/HomeScreen: Current user: xxx@example.com, Role: propGarage
```

**Option 2: Via backend/base de données**

```bash
# MongoDB
db.users.findOne({ email: "karhebti@example.com" }, { role: 1 })

# Devrait retourner:
{ role: "propGarage" }  # ✅ Garage owner
{ role: "user" }        # ❌ Utilisateur normal
```

---

### Si l'utilisateur n'est PAS garage owner

Vous avez 2 options:

**Option A: Créer un nouveau compte garage owner**

```bash
1. Se déconnecter
2. S'inscrire avec un nouveau compte
3. Dans la base de données, changer le rôle:

db.users.updateOne(
  { email: "nouveau@garage.com" },
  { $set: { role: "propGarage" } }
)

4. Se reconnecter avec ce compte
```

**Option B: Changer le rôle du compte actuel**

```bash
# Dans MongoDB
db.users.updateOne(
  { email: "karhebti@example.com" },
  { $set: { role: "propGarage" } }
)

# Puis dans l'app:
1. Se déconnecter
2. Se reconnecter
```

---

## 📊 DIAGNOSTIC COMPLET

### Checklist de vérification

```bash
# 1. Backend accessible ?
curl http://10.0.2.2:3000/api/breakdowns
✅ Devrait retourner une réponse
❌ Si erreur → Backend n'est pas démarré

# 2. URL correcte dans l'app ?
grep -r "BASE_URL" app/src/main/java/
✅ Devrait être http://10.0.2.2:3000/ pour émulateur
❌ Si 192.168.x.x → Changer pour 10.0.2.2

# 3. Utilisateur est garage owner ?
adb logcat | grep "Current user.*Role"
✅ Role: propGarage
❌ Role: user → Changer le rôle dans la BDD

# 4. Token JWT valide ?
adb logcat | grep "Token available"
✅ Token available: true
❌ Token available: false → Se reconnecter
```

---

## 🔧 PROCÉDURE DE CORRECTION COMPLÈTE

### Étape 1: Vérifier et démarrer le backend

```bash
cd backend
npm run dev

# Logs attendus:
✅ Server listening on port 3000
✅ MongoDB connected
```

---

### Étape 2: Corriger l'URL dans l'app

**Fichier:** `ApiConfig.kt`

```kotlin
// AVANT
private const val BASE_URL = "http://192.168.1.190:3000/"

// APRÈS (pour émulateur)
private const val BASE_URL = "http://10.0.2.2:3000/"
```

---

### Étape 3: Recompiler l'app

```bash
./gradlew clean assembleDebug installDebug
```

---

### Étape 4: Vérifier le rôle utilisateur

```bash
# MongoDB
db.users.find({}, { email: 1, role: 1 })

# Si l'utilisateur actuel n'est pas propGarage:
db.users.updateOne(
  { email: "karhebti@example.com" },
  { $set: { role: "propGarage" } }
)
```

---

### Étape 5: Se reconnecter

```bash
1. Ouvrir l'app
2. Se déconnecter (Settings → Déconnexion)
3. Se reconnecter avec le même compte
4. Aller sur Home
```

---

### Étape 6: Vérifier les logs

```bash
adb logcat | grep -E "HomeScreen|BreakdownViewModel"

# Logs attendus:
D/HomeScreen: Loading SOS requests for garage owner
D/HomeScreen: Current user: xxx@example.com, Role: propGarage
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
│                                     │
│  🏠 Aperçu                          │
│  [Statistiques...]                  │
│                                     │
│  🆘 Demandes SOS                   │
│  ┌──────────────────────────────┐  │
│  │ 🆘 Demande SOS    [PENDING]  │  │
│  │ Type: PNEU                    │  │
│  │ Description: Pneu crevé...    │  │
│  │ Distance: 5.2 km              │  │
│  │ [Voir détails]                │  │
│  └──────────────────────────────┘  │
│  ┌──────────────────────────────┐  │
│  │ 🆘 Demande SOS    [PENDING]  │  │
│  │ Type: BATTERIE                │  │
│  │ ...                           │  │
│  └──────────────────────────────┘  │
│                                     │
└────────────────────────────────────┘
```

---

## 🧪 TESTS DE VALIDATION

### Test 1: Backend accessible

```bash
# Depuis l'émulateur
adb shell
curl http://10.0.2.2:3000/api/breakdowns

✅ Attendu: Réponse JSON
❌ Si erreur: Backend pas accessible
```

---

### Test 2: Rôle garage owner

```bash
# Se connecter comme garage owner
# Aller sur Home
# Vérifier logs

adb logcat | grep "Role:"

✅ Attendu: Role: propGarage
❌ Si "user": Changer rôle dans BDD
```

---

### Test 3: Section SOS visible

```bash
1. Ouvrir l'app
2. Se connecter comme garage owner
3. Aller sur Home
4. Scroller vers le bas

✅ Attendu: Section "🆘 Demandes SOS" visible
❌ Si erreur réseau: Vérifier URL backend
❌ Si section absente: Vérifier rôle utilisateur
```

---

## 🆘 SI LE PROBLÈME PERSISTE

### Logs complets à capturer

```bash
# Tous les logs pertinents
adb logcat -c  # Clear logs
adb logcat | grep -E "HomeScreen|BreakdownViewModel|RetrofitClient|AUTH" > logs.txt
```

---

### Informations à vérifier

1. **URL Backend:**
   ```bash
   grep -r "BASE_URL" app/src/main/java/ | head -5
   ```

2. **Backend status:**
   ```bash
   curl http://10.0.2.2:3000/api/breakdowns
   ```

3. **User role:**
   ```bash
   db.users.findOne({ email: "karhebti@example.com" })
   ```

4. **Token JWT:**
   ```bash
   adb logcat | grep "Token"
   ```

---

## 📝 CHECKLIST FINALE

Avant de tester:

- [ ] Backend démarré (`npm run dev`)
- [ ] URL correcte dans ApiConfig.kt (`10.0.2.2` pour émulateur)
- [ ] App recompilée
- [ ] Utilisateur a role `propGarage` dans BDD
- [ ] Se déconnecter/reconnecter
- [ ] Vérifier logs (role + token)

Après modifications:

- [ ] Backend accessible (curl)
- [ ] Logs montrent "Loading SOS requests for garage owner"
- [ ] Logs montrent "Role: propGarage"
- [ ] Section "Demandes SOS" visible sur Home
- [ ] Liste des SOS s'affiche (ou message si vide)

---

## 🎯 SOLUTION RAPIDE (2 MINUTES)

Si vous voulez tester rapidement:

```bash
# 1. Corriger URL (émulateur)
# Dans ApiConfig.kt, ligne ~20:
BASE_URL = "http://10.0.2.2:3000/"

# 2. Changer rôle en propGarage
db.users.updateOne(
  { email: "karhebti@example.com" },
  { $set: { role: "propGarage" } }
)

# 3. Recompiler
./gradlew installDebug

# 4. Tester
- Se déconnecter
- Se reconnecter
- Aller sur Home
- ✅ Section SOS devrait apparaître
```

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025  
**Auteur:** AI Assistant  
**Statut:** 🔍 **GUIDE DE DÉPANNAGE**

