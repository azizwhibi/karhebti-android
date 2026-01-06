# 🧪 GUIDE DE TEST RAPIDE - Flux SOS

**Date:** 14 décembre 2025  
**Durée:** 5 minutes  
**Objectif:** Valider le flux SOS complet

---

## ⚡ TEST EXPRESS (2 minutes)

### Pré-requis
- ✅ Backend avec modifications FCM
- ✅ 2 téléphones/émulateurs:
  - Device 1: User normal
  - Device 2: Garage owner (propGarage)
- ✅ Les 2 apps connectées

### Test 1: Envoi SOS (30 secondes)

```bash
1. Sur Device 1 (User):
   - Ouvrir l'app
   - Aller sur HomeScreen
   - Appuyer sur bouton "🚨 SOS"
   - Sélectionner type: "PNEU"
   - Entrer description: "Pneu crevé"
   - Permettre accès localisation
   - Appuyer "Envoyer"
   
2. ✅ Vérifier:
   - SOSStatusScreen s'affiche
   - Message "Recherche de garages à proximité..."
   - Badge "En attente" (orange)
```

---

### Test 2: Notification garage (30 secondes)

```bash
1. Sur Device 2 (Garage):
   - Attendre notification (~3 secondes)
   
2. ✅ Vérifier notification apparaît:
   ┌─────────────────────────────┐
   │ 🚨 Nouvelle demande SOS     │
   │ Assistance PNEU demandée... │
   └─────────────────────────────┘
   
3. ✅ Vérifier:
   - Son joué
   - Téléphone vibre
   - Badge sur icône app
```

---

### Test 3: Navigation garage (30 secondes)

```bash
1. Sur Device 2:
   - Tap sur la notification
   
2. ✅ Vérifier:
   - App s'ouvre
   - BreakdownDetailScreen s'affiche
   - Carte visible avec position
   - Type: "PNEU"
   - Description visible
   - Boutons [Accepter] [Refuser]
```

---

### Test 4: Acceptation (30 secondes)

```bash
1. Sur Device 2:
   - Tap sur "Accepter"
   - Dialog: "Accepter cette demande SOS?"
   - Tap "Confirmer"
   
2. ✅ Vérifier:
   - Message "Demande acceptée ✓"
   - Navigation vers BreakdownTrackingScreen
   - Badge "Accepté ✓" (bleu)
```

---

### Test 5: Auto-navigation user (30 secondes)

```bash
1. Sur Device 1:
   - Attendre ~5 secondes (polling)
   
2. ✅ Vérifier AUTO-NAVIGATION:
   - SOSStatusScreen → BreakdownTrackingScreen
   - Badge "Accepté ✓" (bleu)
   - Message "🎉 Garage trouvé!"
   - Carte visible
   - Timeline de progression
   - Bouton "Appeler le garage"
```

---

## 🎯 RÉSULTAT ATTENDU

**Temps total:** ~11 secondes  
**Étapes:** 5/5 réussies ✅

```
0s   User envoie SOS
3s   Garage reçoit notification
5s   Garage ouvre app
7s   Garage accepte
11s  User voit tracking
```

---

## 📱 VÉRIFICATION LOGCAT

### Logs attendus - Device 1 (User)

```bash
adb logcat | grep -E "BreakdownSOSScreen|SOSStatus"

# Attendu:
BreakdownSOSScreen: Sending SOS...
BreakdownSOSScreen: ✅ SOS sent successfully
SOSStatus: Starting polling for breakdown 12345
SOSStatus: Fetching breakdown 12345...
SOSStatus: Status: PENDING
SOSStatus: Fetching breakdown 12345...
SOSStatus: Status: ACCEPTED
SOSStatus: ✅ Status changed! Navigating to tracking...
```

---

### Logs attendus - Device 2 (Garage)

```bash
adb logcat | grep -E "KarhebtiMessaging|MainActivity|BreakdownDetail"

# Attendu:
KarhebtiMessaging: ✅ MESSAGE REÇU!
KarhebtiMessaging: Type: new_breakdown
KarhebtiMessaging: ✅✅✅ NOTIFICATION SOS AFFICHÉE
MainActivity: 📱 Navigation depuis notification: sos
MainActivity: 🚨 Navigation vers BreakdownDetail: 12345
BreakdownDetail: Loading breakdown 12345
BreakdownDetail: ✅ Breakdown loaded
BreakdownDetail: Updating status to ACCEPTED
BreakdownDetail: ✅ Status updated
```

---

## ❌ DÉPANNAGE RAPIDE

### Problème: Garage ne reçoit pas notification

```bash
# Vérifier backend logs:
👥 0 garages trouvés  # ❌ PROBLÈME!

# Solution:
- Vérifier garages ont role: 'propGarage'
- Vérifier garages ont fcmToken
- Vérifier location coordinates
```

---

### Problème: User app ne navigue pas auto

```bash
# Vérifier polling:
adb logcat | grep "SOSStatus"

# Si pas de logs toutes les 5s:
- Vérifier que SOSStatusScreen est visible
- Vérifier que breakdownId n'est pas null
```

---

### Problème: App crash sur notification tap

```bash
# Logcat:
IllegalArgumentException: breakdownId parameter wasn't found

# Solution:
- Vérifier que notification data contient "breakdownId"
- Vérifier que MainActivity lit bien l'intent extra
```

---

## ✅ CRITÈRES DE SUCCÈS

- [ ] User peut envoyer SOS
- [ ] Garage reçoit notification en ~3 secondes
- [ ] Notification affiche titre + body
- [ ] Tap notification ouvre BreakdownDetailScreen
- [ ] Garage peut accepter
- [ ] User navigue auto vers tracking en ~5 secondes
- [ ] Tracking screen affiche toutes les infos
- [ ] Pas de crash

**Si tous les critères sont ✅ : FLUX SOS FONCTIONNEL! 🎉**

---

## 📊 TESTS ADDITIONNELS

### Test refus

```bash
1. Garage tap "Refuser"
2. ✅ Vérifier:
   - Dialog de confirmation
   - Status → REFUSED
   - Navigation retour
```

---

### Test plusieurs garages

```bash
1. User envoie SOS
2. ✅ Vérifier:
   - Plusieurs garages reçoivent notification
   - Premier qui accepte gagne
   - Autres ne peuvent plus accepter
```

---

### Test hors ligne

```bash
1. User envoie SOS sans réseau
2. ✅ Vérifier:
   - Message d'erreur
   - Pas de crash
   - Possibilité de réessayer
```

---

**Version:** 1.0.0  
**Auteur:** AI Assistant  
**Statut:** ✅ Prêt pour tests

