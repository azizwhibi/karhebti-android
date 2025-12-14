# 🧪 TESTS DE VALIDATION - Crash SOS Résolu

**Date:** 14 décembre 2025  
**Objectif:** Valider que le crash SOS est complètement résolu  
**Durée:** 5 minutes

---

## ✅ CHECKLIST RAPIDE

Avant de commencer les tests:

- [ ] Code compilé sans erreurs
- [ ] App installée sur device/émulateur
- [ ] Connexion établie avec un compte
- [ ] GPS activé sur le device
- [ ] Backend accessible (optionnel pour certains tests)

---

## 🧪 TEST 1: Envoi SOS Normal (Succès)

**Objectif:** Vérifier que l'envoi SOS fonctionne sans crash

### Étapes

```
1. Lancer l'app
2. Se connecter
3. Aller sur Home
4. Appuyer sur bouton "🚨 SOS"
5. Autoriser la localisation si demandé
6. Attendre que la carte s'affiche
7. Sélectionner type: "PNEU"
8. Entrer description: "Test crash fix"
9. Appuyer sur "Envoyer"
10. Confirmer dans le dialog
```

### Résultat attendu ✅

```
✅ Pas de crash
✅ Dialog de confirmation s'affiche
✅ Message "Envoi de la demande SOS..." visible
✅ Indicateur de chargement apparaît
✅ Navigation vers SOSStatusScreen
✅ breakdownId reçu
```

### Logs attendus

```bash
adb logcat | grep BreakdownSOSScreen

D/BreakdownSOSScreen: Sending SOS: {"type":"PNEU","description":"Test crash fix",...}
D/BreakdownSOSScreen: ✅ SOS sent successfully! ID: 6756e8f8...
```

---

## 🧪 TEST 2: Validation GPS Manquant

**Objectif:** Vérifier que l'app ne crash pas sans position GPS

### Étapes

```
1. Désactiver le GPS sur le device
2. Ouvrir l'app et aller sur SOS
3. Observer le comportement
```

### Résultat attendu ✅

```
✅ Pas de crash
✅ Message "GPS désactivé" affiché
✅ Bouton "Activer le GPS" visible
✅ Possibilité de retour en arrière
```

### Scénario alternatif

```
Si GPS activé mais pas de fix:
1. Aller en intérieur/sous-sol
2. Essayer d'envoyer SOS

✅ Message "Position GPS non disponible"
✅ Bouton "Envoyer" désactivé (grisé)
✅ Pas de crash
```

---

## 🧪 TEST 3: Validation Type Non Sélectionné

**Objectif:** Vérifier qu'on ne peut pas envoyer sans type

### Étapes

```
1. Ouvrir SOS avec GPS activé
2. Carte s'affiche
3. NE PAS sélectionner de type
4. Entrer description: "Test"
5. Observer le bouton "Envoyer"
```

### Résultat attendu ✅

```
✅ Bouton "Envoyer" est désactivé (grisé)
✅ Impossible de cliquer
✅ Pas de crash
```

### Scénario alternatif

```
Si on arrive à cliquer (ne devrait pas arriver):
1. Essayer de cliquer quand même

✅ Message "Veuillez sélectionner un type de panne"
✅ Pas de crash
```

---

## 🧪 TEST 4: Erreur Backend

**Objectif:** Vérifier la gestion d'erreur réseau

### Étapes

```
1. Arrêter le backend (ou mode avion)
2. Remplir formulaire SOS correctement
3. Envoyer
```

### Résultat attendu ✅

```
✅ Pas de crash
✅ Message d'erreur affiché
✅ "Erreur: Unable to resolve host" ou similaire
✅ Possibilité de réessayer
```

### Logs attendus

```bash
E/BreakdownSOSScreen: ❌ SOS error: Unable to resolve host
```

---

## 🧪 TEST 5: Clics Rapides Multiples

**Objectif:** Tester la robustesse avec spam de clics

### Étapes

```
1. Remplir formulaire SOS
2. Appuyer "Envoyer"
3. Appuyer RAPIDEMENT plusieurs fois sur "Confirmer"
```

### Résultat attendu ✅

```
✅ Pas de crash
✅ Une seule requête envoyée
✅ Dialog se ferme après le premier clic
✅ Pas d'envois multiples
```

---

## 🧪 TEST 6: Changement d'Orientation

**Objectif:** Tester la persistance des données

### Étapes

```
1. Remplir formulaire SOS
   - Type: PNEU
   - Description: Test orientation
2. Tourner le device (portrait ↔ landscape)
3. Vérifier les données
4. Envoyer
```

### Résultat attendu ✅

```
✅ Pas de crash
✅ Données préservées après rotation
✅ Position GPS maintenue
✅ Envoi réussit
```

---

## 🧪 TEST 7: Navigation Arrière

**Objectif:** Tester l'annulation propre

### Étapes

```
1. Remplir formulaire SOS
2. Appuyer "Envoyer"
3. Dans le dialog, appuyer "Annuler"
4. Appuyer sur ← (retour)
```

### Résultat attendu ✅

```
✅ Pas de crash
✅ Dialog se ferme
✅ Retour à l'écran SOS
✅ Puis retour à Home
✅ Aucune requête envoyée
```

---

## 🧪 TEST 8: Succès puis Navigation

**Objectif:** Valider le flux complet

### Étapes

```
1. Envoyer SOS avec succès (backend actif)
2. Observer la navigation automatique
3. Vérifier SOSStatusScreen
```

### Résultat attendu ✅

```
✅ Pas de crash
✅ Navigation automatique vers SOSStatusScreen
✅ breakdownId passé correctement
✅ Type affiché: "PNEU"
✅ Latitude/Longitude affichées
✅ Status: "PENDING"
```

### Logs attendus

```bash
D/BreakdownSOSScreen: ✅ SOS sent successfully! ID: 6756e8f8abc123
D/SOSStatusScreen: Starting polling for breakdown 6756e8f8abc123
```

---

## 📊 RÉSULTATS ATTENDUS

### Score de succès: 8/8 ✅

```
✅ Test 1: Envoi normal         PASS
✅ Test 2: GPS manquant          PASS
✅ Test 3: Type non sélectionné  PASS
✅ Test 4: Erreur backend        PASS
✅ Test 5: Clics multiples       PASS
✅ Test 6: Orientation           PASS
✅ Test 7: Navigation arrière    PASS
✅ Test 8: Navigation succès     PASS
```

---

## 🔍 VÉRIFICATIONS COMPLÉMENTAIRES

### Vérifier les logs complets

```bash
# Tous les logs de l'app
adb logcat | grep -E "BreakdownSOSScreen|SOSStatusScreen|FATAL"

# Voir s'il y a des crashs
adb logcat | grep "AndroidRuntime: FATAL"

# Logs avec timestamp
adb logcat -v time | grep BreakdownSOSScreen
```

---

### Vérifier l'état de l'app

```bash
# Vérifier que l'app tourne
adb shell ps | grep karhebti

# Vérifier les crashs récents
adb logcat -d | grep "FATAL EXCEPTION"
```

---

## ❌ SI UN TEST ÉCHOUE

### Test 1 échoue (Crash lors de l'envoi)

```bash
1. Capturer les logs:
   adb logcat -d > crash_log.txt

2. Chercher la stack trace:
   grep -A 20 "FATAL EXCEPTION" crash_log.txt

3. Vérifier que les modifications sont bien appliquées:
   - Variables locales currentLat/currentLon
   - Try-catch autour de CreateBreakdownRequest

4. Recompiler et réinstaller:
   ./gradlew clean assembleDebug installDebug
```

---

### Test 4 échoue (Pas de message d'erreur)

```bash
1. Vérifier que LaunchedEffect gère les erreurs:
   - is BreakdownUiState.Error → showSnackbar

2. Vérifier les logs:
   adb logcat | grep "SOS error"

3. Si pas de logs:
   - Vérifier que viewModel.declareBreakdown() est appelé
   - Vérifier que le repository retourne bien une erreur
```

---

## ✅ VALIDATION FINALE

Une fois tous les tests passés:

- [ ] Aucun crash détecté
- [ ] Tous les messages d'erreur s'affichent
- [ ] Navigation fonctionne correctement
- [ ] Logs de debugging présents
- [ ] Expérience utilisateur fluide

**Si tous les tests passent → Crash SOS complètement résolu! 🎉**

---

## 📝 RAPPORT DE TEST

### Template à remplir

```
Date: ______________
Testeur: ______________
Device: ______________
Version Android: ______________

RÉSULTATS:
━━━━━━━━━━
Test 1 (Envoi normal):         [ ] PASS  [ ] FAIL
Test 2 (GPS manquant):          [ ] PASS  [ ] FAIL
Test 3 (Type non sélectionné):  [ ] PASS  [ ] FAIL
Test 4 (Erreur backend):        [ ] PASS  [ ] FAIL
Test 5 (Clics multiples):       [ ] PASS  [ ] FAIL
Test 6 (Orientation):           [ ] PASS  [ ] FAIL
Test 7 (Navigation arrière):    [ ] PASS  [ ] FAIL
Test 8 (Navigation succès):     [ ] PASS  [ ] FAIL

SCORE: ___/8

NOTES:
_________________________________________________
_________________________________________________
_________________________________________________
```

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025  
**Auteur:** AI Assistant  
**Statut:** ✅ **PRÊT POUR TESTS**

