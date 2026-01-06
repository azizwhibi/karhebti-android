# ⚡ ACTION IMMÉDIATE - Recompiler l'App

**Problème:** App essaie toujours de se connecter à `192.168.1.190:3000`  
**Solution:** URLs corrigées dans 4 fichiers - **RECOMPILATION REQUISE**

---

## 🚀 FAIRE MAINTENANT (2 MINUTES)

### 1. Démarrer le backend

```bash
cd backend
npm run dev
```

Attendez de voir:
```
✅ Server listening on port 3000
```

---

### 2. Recompiler l'app

```bash
cd C:\Users\rayen\Desktop\karhebti-android-NEW

# Clean + Rebuild + Install
./gradlew clean assembleDebug installDebug
```

Cela va prendre 1-2 minutes.

---

### 3. Tester

```
1. Ouvrir l'app sur l'émulateur
2. Se connecter avec votre compte
3. Aller sur Home
```

**✅ La section "Demandes SOS" devrait maintenant être visible !**

**✅ Plus d'erreur "failed to connect to 192.168.1.190" !**

---

## 📊 CE QUI A ÉTÉ CORRIGÉ

J'ai changé l'URL dans **4 fichiers** :

```
❌ AVANT: http://192.168.1.190:3000/
✅ APRÈS: http://10.0.2.2:3000/
```

| Fichier | ✅ |
|---------|---|
| ApiConfig.kt | ✅ |
| HomeScreen.kt | ✅ |
| NavGraph.kt | ✅ |
| ChatWebSocketClient.kt | ✅ |

---

## 🔍 VÉRIFIER QUE ÇA MARCHE

Après recompilation, vérifier les logs:

```bash
adb logcat | grep "10.0.2.2"

✅ Devrait montrer:
--> GET http://10.0.2.2:3000/api/breakdowns
<-- 200 OK
```

---

## 🎯 SI L'UTILISATEUR N'EST PAS GARAGE OWNER

Si la section SOS reste vide ou absente, changer le rôle dans MongoDB:

```bash
mongosh
use karhebti

db.users.updateOne(
  { email: "karhebti@example.com" },
  { $set: { role: "propGarage" } }
)
```

Puis se déconnecter/reconnecter dans l'app.

---

## 📚 DOCUMENTATION COMPLÈTE

**Guide détaillé:** `BACKEND_URL_FIX_COMPLETE.md`

---

**IMPORTANT:** Vous DEVEZ recompiler avec `./gradlew clean assembleDebug installDebug` pour que les changements prennent effet !

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025

