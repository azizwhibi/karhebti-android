# ⚡ SOLUTION RAPIDE - Section SOS Invisible PropGarage

**Problème:** La section "🆘 Demandes SOS" ne s'affiche pas  
**Cause:** Backend inaccessible + Rôle utilisateur  
**Statut:** ✅ **SOLUTION PRÊTE**

---

## 🎯 PROBLÈME DANS LE SCREENSHOT

```
❌ Erreur visible: "failed to connect to /192.168.1.190 (port 3000)"
❌ Section "Demandes SOS" absente
❓ Utilisateur: "Karhebti" (rôle à vérifier)
```

---

## ⚡ SOLUTION EN 3 ÉTAPES (5 MINUTES)

### Étape 1: Démarrer le backend ✅

```bash
cd backend
npm run dev

# Logs attendus:
✅ Server listening on port 3000
✅ MongoDB connected
```

**SI LE BACKEND N'EST PAS DÉMARRÉ → C'EST LA CAUSE!**

---

### Étape 2: Changer le rôle en `propGarage` ✅

```bash
# Dans MongoDB
mongosh
use karhebti

db.users.updateOne(
  { email: "karhebti@example.com" },  # Votre email
  { $set: { role: "propGarage" } }
)

# Résultat:
{ acknowledged: true, modifiedCount: 1 }
```

---

### Étape 3: Se reconnecter ✅

```
1. Dans l'app → Settings → Déconnexion
2. Se reconnecter avec le même compte
3. Aller sur Home
4. ✅ Section "Demandes SOS" devrait apparaître
```

---

## 🔧 CORRECTION APPLIQUÉE

**Fichier:** `ApiConfig.kt`

**Changement:** Suppression de l'URL incorrecte qui pointait vers MongoDB (port 27017) au lieu du backend Node.js (port 3000).

**URL correcte utilisée:** `http://10.0.2.2:3000/`
- `10.0.2.2` = localhost pour l'émulateur Android
- `3000` = port du backend Node.js

---

## ✅ RÉSULTAT ATTENDU

Après les 3 étapes:

```
┌────────────────────────────────┐
│ Home - Garage Owner             │
│                                 │
│ 🆘 Demandes SOS                │ ← APPARAÎT ICI
│                                 │
│ ┌─────────────────────────────┐│
│ │ 🆘 Demande SOS  [PENDING]   ││
│ │ Type: PNEU                   ││
│ │ Distance: 5.2 km             ││
│ │ [Voir détails]               ││
│ └─────────────────────────────┘│
│                                 │
│ OU (si aucune demande):         │
│ 📭 Aucune demande SOS           │
│                                 │
└────────────────────────────────┘
```

---

## 🧪 VÉRIFIER QUE ÇA MARCHE

```bash
# 1. Backend accessible?
curl http://localhost:3000/api/breakdowns
✅ Retourne JSON

# 2. Rôle correct?
db.users.findOne({ email: "karhebti@example.com" }, { role: 1 })
✅ { role: "propGarage" }

# 3. Logs Android
adb logcat | grep "HomeScreen"
✅ "Loading SOS requests for garage owner"
✅ "Role: propGarage"
```

---

## 🆘 SI ÇA NE MARCHE PAS

### Vérifier les 3 points:

1. **Backend tourne ?**
   ```bash
   # Windows
   netstat -ano | findstr :3000
   # Devrait montrer un processus
   ```

2. **Rôle changé ?**
   ```bash
   db.users.find({ email: "karhebti@example.com" }, { role: 1 })
   # Devrait retourner: { role: "propGarage" }
   ```

3. **Reconnecté ?**
   - Se déconnecter PUIS se reconnecter
   - Sinon le rôle ne sera pas rafraîchi

---

## 📚 DOCUMENTATION COMPLÈTE

**Guide détaillé:** `PROPGARAGE_SOS_FIX.md`

---

**Une fois le backend démarré et le rôle changé, la section SOS apparaît immédiatement ! 🎉**

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025

