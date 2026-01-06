# 🚨 FIX RAPIDE - Section SOS Garage Owner Invisible

**Date:** 14 décembre 2025  
**Problème:** La section "Demandes SOS" ne s'affiche pas pour le garage owner  
**Cause:** Erreur de connexion backend + Rôle utilisateur

---

## ⚡ SOLUTION RAPIDE (5 MINUTES)

### Étape 1: Vérifier que le backend tourne ✅

```bash
cd backend
npm run dev

# Logs attendus:
✅ Server listening on port 3000
✅ MongoDB connected
```

**Si le backend n'est pas démarré → C'EST LE PROBLÈME!**

---

### Étape 2: Tester la connexion ✅

```bash
# Depuis votre navigateur:
http://localhost:3000/api/breakdowns

✅ Devrait afficher une page JSON
❌ Si erreur 404 ou timeout → Backend pas accessible
```

---

### Étape 3: Vérifier le rôle utilisateur ✅

**Votre utilisateur actuel "Karhebti" doit avoir le rôle `propGarage`**

```bash
# Dans MongoDB:
db.users.findOne({ email: "karhebti@example.com" })

# Vérifier le champ "role":
{ role: "propGarage" }  ✅ CORRECT
{ role: "user" }        ❌ À CHANGER
```

**Si le rôle est "user":**

```bash
db.users.updateOne(
  { email: "karhebti@example.com" },
  { $set: { role: "propGarage" } }
)
```

---

### Étape 4: Se reconnecter ✅

```bash
1. Dans l'app → Settings → Déconnexion
2. Se reconnecter avec le même compte
3. Aller sur Home
4. ✅ La section "Demandes SOS" devrait apparaître
```

---

## 🔍 DIAGNOSTIC DE L'ERREUR ACTUELLE

Dans votre screenshot, l'erreur est:

```
failed to connect to /192.168.1.190 (port 3000) 
from /10.0.2.16 (port 52612) after 10000ms
```

### Analyse

- **IP source:** `10.0.2.16` (émulateur Android)
- **IP destination:** `192.168.1.190:3000` (votre backend)
- **Problème:** L'émulateur essaie de se connecter à `192.168.1.190` mais échoue

### Pourquoi ça échoue ?

Il y a **2 URLs différentes** dans le code:

1. `ApiConfig.BASE_URL = "http://192.168.1.190:27017/"` ❌ (MongoDB, pas utilisé)
2. `RetrofitClient.BASE_URL = "http://10.0.2.2:3000/"` ✅ (Correct)

**Le problème:** Il semble que l'app utilise la mauvaise URL quelque part.

---

## 🔧 CORRECTION DÉFINITIVE

### Supprimer l'URL incorrecte

**Fichier:** `app/src/main/java/com/example/karhebti_android/data/api/ApiConfig.kt`

**Modifier:**

```kotlin
object ApiConfig {
    // ❌ SUPPRIMER CETTE LIGNE (MongoDB URL, pas pour l'API)
    // const val BASE_URL = "http://192.168.1.190:27017/"
    
    // ✅ GARDER UNIQUEMENT CECI
    const val MONGODB_URL = "mongodb://192.168.1.190:27017/karhebti"
}

object RetrofitClient {
    // ✅ CECI EST CORRECT
    private const val BASE_URL = "http://10.0.2.2:3000/"
    
    // ... reste du code
}
```

---

## 📱 VÉRIFICATION AVEC LOGCAT

```bash
adb logcat | grep -E "HomeScreen|RetrofitClient|failed to connect"

# Logs attendus après fix:
D/HomeScreen: Loading SOS requests for garage owner
D/HomeScreen: Current user: karhebti@example.com, Role: propGarage
D/HomeScreen: Token available: true

# Si vous voyez encore "failed to connect":
❌ Backend n'est pas démarré
❌ OU mauvaise URL utilisée
```

---

## ✅ CHECKLIST COMPLÈTE

Avant de tester:

- [ ] Backend démarré (`npm run dev` dans le dossier backend)
- [ ] Backend accessible (http://localhost:3000)
- [ ] Utilisateur a role `propGarage` dans MongoDB
- [ ] URL incorrecte supprimée de ApiConfig.kt
- [ ] App recompilée: `./gradlew clean installDebug`

Pendant le test:

- [ ] Se déconnecter de l'app
- [ ] Se reconnecter
- [ ] Aller sur Home
- [ ] Vérifier les logs: `adb logcat | grep HomeScreen`

Résultat attendu:

- [ ] Pas d'erreur "failed to connect"
- [ ] Section "🆘 Demandes SOS" visible
- [ ] Liste des SOS s'affiche (ou "Aucune demande" si vide)

---

## 🎯 SI ÇA NE MARCHE TOUJOURS PAS

### 1. Vérifier les logs complets

```bash
adb logcat -c  # Clear
adb logcat | grep -E "HomeScreen|BreakdownViewModel|Retrofit" > logs.txt
```

### 2. Vérifier le rôle dans les logs

```bash
adb logcat | grep "Current user"

# Devrait afficher:
D/HomeScreen: Current user: xxx@example.com, Role: propGarage
```

### 3. Créer un nouveau compte garage

Si le problème persiste avec votre compte actuel:

```bash
# 1. Dans l'app: S'inscrire avec nouveau compte
Email: garage@test.com
Password: Test123!

# 2. Dans MongoDB: Changer le rôle
db.users.updateOne(
  { email: "garage@test.com" },
  { $set: { role: "propGarage" } }
)

# 3. Se reconnecter avec ce nouveau compte
```

---

## 📊 RÉSULTAT ATTENDU

```
┌────────────────────────────────────┐
│  Home - Garage Owner                │
├────────────────────────────────────┤
│  👤 Bonjour, Karhebti              │
│                                     │
│  📊 Aperçu                          │
│  [Statistiques...]                  │
│                                     │
│  🆘 Demandes SOS                   │ ← CETTE SECTION
│                                     │
│  ┌──────────────────────────────┐  │
│  │ 🆘 Demande SOS   [PENDING]   │  │
│  │ ─────────────────────────────│  │
│  │ Type: PNEU                    │  │
│  │ Description: Pneu crevé...    │  │
│  │ Distance: 5.2 km              │  │
│  │                               │  │
│  │ [Accepter] [Refuser]          │  │
│  └──────────────────────────────┘  │
│                                     │
│  OU                                 │
│                                     │
│  📭 Aucune demande SOS en attente  │
│                                     │
└────────────────────────────────────┘
```

---

## 🆘 BESOIN D'AIDE ?

**Documentation complète:** `GARAGE_OWNER_SOS_TROUBLESHOOTING.md`

**Guide backend:** `BACKEND_NEXT_STEPS.md`

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025  
**Statut:** 🔧 **GUIDE DE FIX RAPIDE**

