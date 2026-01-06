# ✅ MISE À JOUR - URL Backend 172.18.1.246

**Date:** 14 décembre 2025  
**Action:** Changement de l'URL backend de `10.0.2.2` vers `172.18.1.246`  
**Statut:** ✅ **MODIFIÉ - RECOMPILATION REQUISE**

---

## 🔧 CHANGEMENT APPLIQUÉ

### URL Mise à Jour

```
❌ AVANT: http://10.0.2.2:3000/
✅ APRÈS: http://172.18.1.246:3000/
```

**Raison:** Utilisation de l'IP du serveur backend distant `172.18.1.246`

---

## 📁 FICHIERS MODIFIÉS (4 fichiers)

| # | Fichier | Ligne | Nouvelle URL |
|---|---------|-------|--------------|
| 1 | ApiConfig.kt | 20 | `http://172.18.1.246:3000/` ✅ |
| 2 | HomeScreen.kt | 80 | `http://172.18.1.246:3000/` ✅ |
| 3 | NavGraph.kt | 483 | `http://172.18.1.246:3000/` ✅ |
| 4 | ChatWebSocketClient.kt | 29 | `http://172.18.1.246:3000` ✅ |

---

## 🚀 RECOMPILATION OBLIGATOIRE

**Vous DEVEZ recompiler l'app maintenant !**

```bash
cd C:\Users\rayen\Desktop\karhebti-android-NEW

# Clean + Rebuild + Install
./gradlew clean assembleDebug installDebug
```

---

## ✅ VÉRIFICATIONS APRÈS RECOMPILATION

### 1. Vérifier que le backend est accessible

```bash
# Depuis votre PC
curl http://172.18.1.246:3000/api/breakdowns

✅ Devrait retourner JSON
❌ Si erreur: Backend pas accessible sur 172.18.1.246
```

---

### 2. Vérifier depuis l'émulateur/device

```bash
# Test de connectivité
adb shell
ping 172.18.1.246

✅ Devrait avoir une réponse
❌ Si timeout: Problème réseau
```

---

### 3. Vérifier les logs de l'app

```bash
adb logcat | grep "172.18.1.246"

# Logs attendus:
D/OkHttp: --> GET http://172.18.1.246:3000/api/breakdowns
D/OkHttp: <-- 200 OK

✅ Connexion réussie
❌ Si timeout: Vérifier firewall/réseau
```

---

## 🔍 TESTS À EFFECTUER

### Test 1: Connexion backend

```
1. Recompiler l'app
2. Lancer l'app
3. Se connecter
4. Aller sur Home

✅ Attendu: Section SOS se charge sans erreur
❌ Si erreur: Vérifier accessibilité de 172.18.1.246
```

---

### Test 2: Envoi SOS

```
1. En tant que user normal
2. Envoyer un SOS
3. Vérifier que ça fonctionne

✅ SOS envoyé avec succès
✅ Navigation vers SOSStatusScreen
```

---

### Test 3: Réception SOS (PropGarage)

```
1. Se connecter avec compte propGarage
2. Aller sur Home
3. Vérifier section "Demandes SOS"

✅ Liste des SOS affichée
✅ Pas d'erreur réseau
```

---

## ⚠️ POINTS IMPORTANTS

### Configuration Réseau

**L'IP `172.18.1.246` doit être accessible depuis:**
- ✅ Votre PC de développement
- ✅ L'émulateur Android
- ✅ Les devices réels sur le même réseau

---

### Firewall

**Le backend doit autoriser les connexions depuis:**
- Port 3000 ouvert
- Écoute sur `0.0.0.0` ou l'IP `172.18.1.246`

**Configuration backend correcte:**
```javascript
// server.js
app.listen(3000, '0.0.0.0', () => {
  console.log('Server listening on port 3000');
});
```

---

### Réseau

**Pour que 172.18.1.246 soit accessible:**
- Backend doit tourner sur une machine avec cette IP
- L'émulateur/device doit pouvoir router vers cette IP
- Pas de firewall bloquant

---

## 🧪 DIAGNOSTIC EN CAS DE PROBLÈME

### Erreur: Connection timeout

```bash
# 1. Vérifier que l'IP est accessible
ping 172.18.1.246

# 2. Vérifier que le port 3000 est ouvert
telnet 172.18.1.246 3000
# OU
curl http://172.18.1.246:3000/api/breakdowns

✅ Si ça marche: Recompiler l'app
❌ Si ça échoue: Problème réseau/firewall
```

---

### Erreur: Connection refused

**Causes possibles:**
1. Backend pas démarré sur 172.18.1.246
2. Backend écoute uniquement sur localhost
3. Firewall bloque le port 3000

**Solutions:**
```bash
# Vérifier que le backend tourne
# Sur la machine 172.18.1.246:
netstat -ano | findstr :3000  # Windows
lsof -i :3000                 # Mac/Linux

# Devrait montrer:
TCP    0.0.0.0:3000    LISTENING
```

---

### Erreur: Dans les logs Android

```bash
adb logcat | grep -E "failed to connect|SocketTimeout"

# Si vous voyez:
❌ "failed to connect to /172.18.1.246"

# Vérifier:
1. Backend accessible depuis le PC
2. Firewall autorisant les connexions
3. App recompilée avec nouvelle URL
```

---

## 📝 CHECKLIST DE DÉPLOIEMENT

Avant de tester:

- [x] URLs changées dans 4 fichiers (ApiConfig, HomeScreen, NavGraph, ChatWebSocket)
- [ ] Backend accessible sur `http://172.18.1.246:3000`
- [ ] App clean: `./gradlew clean`
- [ ] App recompilée: `./gradlew assembleDebug`
- [ ] App installée: `./gradlew installDebug`
- [ ] Backend tourne sur 172.18.1.246
- [ ] Port 3000 ouvert dans le firewall

Après recompilation:

- [ ] Logs montrent "172.18.1.246:3000"
- [ ] Pas d'erreur "failed to connect"
- [ ] Section SOS fonctionne
- [ ] Envoi SOS fonctionne
- [ ] Requêtes HTTP retournent 200 OK

---

## 🎯 RÉSULTAT ATTENDU

```
App → Se connecte à http://172.18.1.246:3000
       │
       ▼
✅ Backend répond (sur 172.18.1.246)
✅ Section SOS: Liste des demandes affichée
✅ Envoi SOS fonctionne
✅ Notifications FCM fonctionnent
```

---

## 🚀 COMMANDES RAPIDES

```bash
# 1. Recompiler l'app
cd C:\Users\rayen\Desktop\karhebti-android-NEW
./gradlew clean assembleDebug installDebug

# 2. Vérifier connectivité
curl http://172.18.1.246:3000/api/breakdowns

# 3. Voir les logs
adb logcat | grep "172.18.1.246"
```

---

**IMPORTANT:** N'oubliez pas de **RECOMPILER** l'app avec la nouvelle URL !

```bash
./gradlew clean assembleDebug installDebug
```

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025  
**Auteur:** AI Assistant  
**Statut:** ✅ **URL MISE À JOUR - RECOMPILATION REQUISE**

