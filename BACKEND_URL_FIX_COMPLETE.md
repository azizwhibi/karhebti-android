# ✅ FIX FINAL - URL Backend Corrigée Partout

**Date:** 14 décembre 2025  
**Problème:** App essaie toujours de se connecter à `192.168.1.190:3000`  
**Cause:** URL incorrecte dans 4 fichiers différents  
**Statut:** ✅ **CORRIGÉ - URL: 172.18.1.246:3000**

---

## 🐛 PROBLÈME

Malgré la correction dans `ApiConfig.kt`, l'app essayait toujours de se connecter à l'ancienne URL `192.168.1.190:3000`.

**Erreur dans les logs:**
```
SocketTimeoutException: failed to connect to /192.168.1.190 (port 3000)
```

---

## ✅ FICHIERS CORRIGÉS (4 fichiers)

### 1. ApiConfig.kt ✅
```kotlin
// AVANT
const val BASE_URL = "http://192.168.1.190:27017/"  // ❌

// APRÈS
private const val BASE_URL = "http://172.18.1.246:3000/"  // ✅
```

---

### 2. HomeScreen.kt ✅
```kotlin
// AVANT (ligne 80)
.baseUrl("http://192.168.1.190:3000/")  // ❌

// APRÈS
.baseUrl("http://172.18.1.246:3000/")  // ✅
```

---

### 3. NavGraph.kt ✅
```kotlin
// AVANT (ligne 483)
.baseUrl("http://192.168.1.190:3000/")  // ❌

// APRÈS
.baseUrl("http://172.18.1.246:3000/")  // ✅
```

---

### 4. ChatWebSocketClient.kt ✅
```kotlin
// AVANT (ligne 29)
private const val SERVER_URL = "http://192.168.1.190:3000"  // ❌

// APRÈS
private const val SERVER_URL = "http://172.18.1.246:3000"  // ✅
```

---

## 🚀 RECOMPILATION OBLIGATOIRE

**IMPORTANT:** Vous DEVEZ recompiler l'app pour que les changements prennent effet !

### Méthode 1: Clean + Rebuild (RECOMMANDÉ)

```bash
cd C:\Users\rayen\Desktop\karhebti-android-NEW

# Clean le projet
./gradlew clean

# Rebuild et installer
./gradlew assembleDebug installDebug
```

---

### Méthode 2: Via Android Studio

```
1. Build → Clean Project
2. Build → Rebuild Project
3. Run → Run 'app'
```

---

## ✅ VÉRIFICATION APRÈS RECOMPILATION

### Étape 1: Vérifier que le backend tourne

```bash
cd backend
npm run dev

# Logs attendus:
✅ Server listening on port 3000
✅ MongoDB connected
```

---

### Étape 2: Lancer l'app recompilée

```
1. Désinstaller l'ancienne version de l'app
2. Installer la nouvelle version: ./gradlew installDebug
3. Ouvrir l'app
4. Se connecter
```

---

### Étape 3: Vérifier les logs

```bash
adb logcat | grep -E "connecting to|failed to connect|BASE_URL|SERVER_URL"

# Logs attendus (APRÈS recompilation):
✅ Connection to http://10.0.2.2:3000/api/breakdowns
✅ HTTP 200 OK

# Logs à NE PLUS voir:
❌ failed to connect to /192.168.1.190
```

---

### Étape 4: Tester la section SOS

```
1. Se connecter avec compte garage owner
   (role = "propGarage" dans MongoDB)

2. Aller sur Home

3. ✅ Section "Demandes SOS" devrait être visible
   (pas d'erreur "failed to connect")
```

---

## 📊 RÉSUMÉ DES CORRECTIONS

| Fichier | Ligne | Avant | Après |
|---------|-------|-------|-------|
| ApiConfig.kt | 15 | `192.168.1.190:27017` | Supprimé |
| ApiConfig.kt | 20 | - | `172.18.1.246:3000` ✅ |
| HomeScreen.kt | 80 | `192.168.1.190:3000` | `172.18.1.246:3000` ✅ |
| NavGraph.kt | 483 | `192.168.1.190:3000` | `172.18.1.246:3000` ✅ |
| ChatWebSocketClient.kt | 29 | `192.168.1.190:3000` | `172.18.1.246:3000` ✅ |

**Total:** 4 fichiers modifiés, 5 URLs corrigées

---

## 🎯 POURQUOI 172.18.1.246 ?

### URL du serveur backend

```
172.18.1.246:3000 = Adresse IP du serveur backend
```

Cette IP est l'adresse du serveur où tourne le backend Node.js.  
L'app peut se connecter directement à cette adresse depuis l'émulateur ou un device réel.

---

### Pour configuration réseau

Si vous testez avec un serveur backend sur une IP spécifique:

```kotlin
// Utiliser l'IP du serveur backend
private const val BASE_URL = "http://172.18.1.246:3000/"
```

**Note:** L'IP `172.18.1.246` doit être accessible depuis votre émulateur/device.

---

## 🧪 TEST COMPLET

### 1. Backend accessible depuis l'émulateur

```bash
# Vérifier que le backend répond
curl http://172.18.1.246:3000/api/breakdowns

✅ Devrait retourner JSON
❌ Si erreur: Backend pas accessible sur 172.18.1.246
```

---

### 2. App se connecte correctement

```bash
# Logs Android après recompilation
adb logcat | grep "HTTP"

✅ Logs attendus:
D/OkHttp: --> GET http://172.18.1.246:3000/api/breakdowns
D/OkHttp: <-- 200 OK

❌ À NE PLUS voir:
E/OkHttp: failed to connect to /192.168.1.190
```

---

### 3. Section SOS visible

```
1. Recompiler l'app
2. Changer role → "propGarage"
3. Se connecter
4. Aller sur Home

✅ Section "Demandes SOS" visible
✅ Pas d'erreur réseau
```

---

## ⚠️ ERREURS FRÉQUENTES

### Erreur 1: "Still connecting to 192.168.1.190"

**Cause:** App pas recompilée ou cache pas nettoyé

**Solution:**
```bash
./gradlew clean
./gradlew assembleDebug installDebug
```

---

### Erreur 2: "Connection refused"

**Cause:** Backend pas démarré

**Solution:**
```bash
cd backend
npm run dev
```

---

### Erreur 3: "Timeout after 10000ms"

**Cause:** 
- Backend pas accessible sur port 3000
- Firewall bloque la connexion

**Solution:**
```bash
# Vérifier que le port 3000 écoute
netstat -ano | findstr :3000  # Windows

# Devrait montrer:
TCP    0.0.0.0:3000    0.0.0.0:0    LISTENING
```

---

## 📝 CHECKLIST FINALE

Avant de tester:

- [x] URLs corrigées dans 4 fichiers
- [ ] Backend démarré (`npm run dev`)
- [ ] App clean (`./gradlew clean`)
- [ ] App recompilée (`./gradlew assembleDebug`)
- [ ] App installée (`./gradlew installDebug`)
- [ ] Rôle = `propGarage` dans MongoDB
- [ ] Se déconnecter/reconnecter dans l'app

Après recompilation:

- [ ] Pas d'erreur "failed to connect to 192.168.1.190"
- [ ] Logs montrent "172.18.1.246:3000"
- [ ] Section SOS visible sur Home
- [ ] Requêtes HTTP retournent 200 OK

---

## 🎉 RÉSULTAT ATTENDU

**Avant (avec anciennes URLs):**
```
App → Essaie de se connecter à 192.168.1.190:3000
       │
       ▼
❌ SocketTimeoutException
❌ Section SOS: "Erreur de chargement"
```

**Après (URLs corrigées + recompilé):**
```
App → Se connecte à 172.18.1.246:3000
       │
       ▼
✅ Backend répond (sur 172.18.1.246)
✅ Section SOS: Liste des demandes affichée
```

---

## 🆘 SI ÇA NE MARCHE TOUJOURS PAS

### 1. Vérifier que le backend écoute sur toutes les interfaces

**Fichier backend:** `server.js` ou `app.js`

```javascript
// CORRECT
app.listen(3000, '0.0.0.0', () => {
  console.log('Server listening on port 3000');
});

// ❌ INCORRECT (n'écoute que sur localhost)
app.listen(3000, 'localhost', () => {
  // ...
});
```

---

### 2. Vérifier les logs complets

```bash
adb logcat -c  # Clear
adb logcat > logs.txt  # Capturer tous les logs

# Puis chercher:
grep "192.168.1.190" logs.txt  # Ne devrait rien retourner
grep "172.18.1.246" logs.txt  # Devrait montrer les nouvelles URLs
```

---

### 3. Désinstaller complètement l'app

```bash
adb uninstall com.example.karhebti_android
./gradlew installDebug
```

---

**IMPORTANT:** N'oubliez pas de **RECOMPILER** l'app après les modifications !

```bash
./gradlew clean assembleDebug installDebug
```

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025  
**Auteur:** AI Assistant  
**Statut:** ✅ **CORRIGÉ - RECOMPILATION REQUISE**

