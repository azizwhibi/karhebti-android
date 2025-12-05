# Scénarios de Test - Gestion des Créneaux de Réservation

## 📋 Prérequis

Avant de commencer les tests, assurez-vous que :
- ✅ Un garage existe avec `numberOfBays` configuré (exemple : 2 créneaux)
- ✅ Des utilisateurs peuvent créer des réservations
- ✅ Vous êtes connecté en tant que propriétaire du garage

## 🧪 Scénario 1 : Garage avec 2 créneaux - Acceptation normale

### Configuration
- **Garage** : "Garage Test" avec 2 créneaux (`numberOfBays = 2`)
- **Date de test** : Demain
- **Heure** : 10:00 - 12:00

### Étapes

1. **Créer 4 réservations au même moment** (même date, même heure)
   - Réservation A : Client "Alice" - 10:00-12:00
   - Réservation B : Client "Bob" - 10:00-12:00
   - Réservation C : Client "Charlie" - 10:00-12:00
   - Réservation D : Client "Diana" - 10:00-12:00

2. **Ouvrir l'écran de gestion des réservations**
   - Toutes les 4 réservations doivent apparaître avec le statut "En attente"

3. **Vérifier les informations sur la Réservation A**
   - Badge : "Créneaux disponibles: 2 / 2" (couleur verte)
   - Bouton "Confirmer" : Actif

4. **Confirmer la Réservation A**
   - Cliquer sur "Confirmer"
   - Dialogue de confirmation s'affiche
   - Confirmer
   - ✅ Réservation A passe en "Confirmé"

5. **Vérifier les informations sur la Réservation B**
   - Badge : "Créneaux disponibles: 1 / 2" (couleur jaune)
   - Message : "1 réservation(s) déjà confirmée(s)"
   - Bouton "Confirmer" : Actif
   - Dialogue montre : "Il restera 0 créneau(x) disponible(s) après confirmation"

6. **Confirmer la Réservation B**
   - Cliquer sur "Confirmer"
   - Confirmer dans le dialogue
   - ✅ Réservation B passe en "Confirmé"

7. **Vérifier les informations sur la Réservation C**
   - Badge : "Aucun créneau disponible" (couleur rouge)
   - Message : "2 réservation(s) déjà confirmée(s)"
   - Bouton : "Complet" (désactivé)
   - ❌ Impossible de confirmer

8. **Vérifier les informations sur la Réservation D**
   - Même résultat que la Réservation C
   - ❌ Impossible de confirmer

### ✅ Résultat Attendu
- 2 réservations confirmées (A et B)
- 2 réservations en attente mais non confirmables (C et D)
- Le garage ne peut pas accepter plus de 2 réservations simultanées

---

## 🧪 Scénario 2 : Annulation et libération de créneau

### Configuration
- Suite du Scénario 1
- État actuel : 2 confirmées (A, B), 2 en attente (C, D)

### Étapes

1. **Annuler la Réservation A**
   - Cliquer sur l'icône d'annulation ou changer le statut
   - ✅ Réservation A passe en "Annulé"

2. **Vérifier automatiquement les Réservations C et D**
   - Les informations doivent se mettre à jour automatiquement
   - Badge : "Créneaux disponibles: 1 / 2" (couleur jaune)
   - Message : "1 réservation(s) déjà confirmée(s)" (B seulement)
   - Bouton "Confirmer" : Actif maintenant ✅

3. **Confirmer la Réservation C**
   - Maintenant possible car un créneau s'est libéré
   - ✅ Réservation C passe en "Confirmé"

4. **Vérifier la Réservation D**
   - Badge : "Aucun créneau disponible" (couleur rouge)
   - Bouton : "Complet" (désactivé)
   - ❌ Impossible de confirmer (B et C occupent les 2 créneaux)

### ✅ Résultat Attendu
- État final : B confirmé, C confirmé, A annulé, D en attente
- Le système gère correctement la libération et réaffectation des créneaux

---

## 🧪 Scénario 3 : Chevauchements partiels d'horaires

### Configuration
- **Garage** : "Garage Test" avec 2 créneaux
- **Date de test** : Demain

### Étapes

1. **Créer des réservations avec chevauchements**
   - Réservation E : 09:00 - 11:00
   - Réservation F : 10:00 - 12:00 (chevauche E : 10:00-11:00)
   - Réservation G : 11:00 - 13:00 (chevauche F : 11:00-12:00)
   - Réservation H : 14:00 - 16:00 (ne chevauche aucune)

2. **Confirmer la Réservation E**
   - ✅ Confirmée

3. **Vérifier la Réservation F**
   - Badge : "Créneaux disponibles: 1 / 2" (car E chevauche)
   - ✅ Peut être confirmée

4. **Confirmer la Réservation F**
   - ✅ Confirmée

5. **Vérifier la Réservation G**
   - Badge : "Créneaux disponibles: 1 / 2" 
   - (car F chevauche, mais E ne chevauche plus G)
   - ✅ Peut être confirmée

6. **Vérifier la Réservation H**
   - Badge : "Créneaux disponibles: 2 / 2"
   - (aucun chevauchement avec E, F, G)
   - ✅ Peut être confirmée

### ✅ Résultat Attendu
- Le système détecte correctement les chevauchements partiels
- Les créneaux sont calculés pour chaque plage horaire spécifique

---

## 🧪 Scénario 4 : Garage avec 1 seul créneau

### Configuration
- **Garage** : "Petit Garage" avec 1 créneau (`numberOfBays = 1`)
- **Date de test** : Demain
- **Heure** : 14:00 - 16:00

### Étapes

1. **Créer 3 réservations au même moment**
   - Réservation I : 14:00-16:00
   - Réservation J : 14:00-16:00
   - Réservation K : 14:00-16:00

2. **Vérifier la Réservation I**
   - Badge : "Créneaux disponibles: 1 / 1" (couleur jaune)
   - Bouton "Confirmer" : Actif

3. **Confirmer la Réservation I**
   - ✅ Confirmée

4. **Vérifier les Réservations J et K**
   - Badge : "Aucun créneau disponible" (couleur rouge)
   - Bouton : "Complet" (désactivé)
   - ❌ Impossibles à confirmer

### ✅ Résultat Attendu
- Un seul créneau = une seule réservation confirmée possible
- Comportement correct pour les garages à capacité limitée

---

## 🧪 Scénario 5 : Garage avec 5 créneaux

### Configuration
- **Garage** : "Grand Garage" avec 5 créneaux (`numberOfBays = 5`)
- **Date de test** : Demain
- **Heure** : 08:00 - 10:00

### Étapes

1. **Créer 7 réservations au même moment**
   - Réservations L, M, N, O, P, Q, R : toutes 08:00-10:00

2. **Confirmer les 5 premières (L, M, N, O, P)**
   - Chaque confirmation :
     - 1ère : 5/5 créneaux disponibles (vert)
     - 2ème : 4/5 créneaux disponibles (vert)
     - 3ème : 3/5 créneaux disponibles (vert)
     - 4ème : 2/5 créneaux disponibles (vert)
     - 5ème : 1/5 créneaux disponibles (jaune)
   - ✅ Toutes confirmées

3. **Vérifier Q et R**
   - Badge : "Aucun créneau disponible" (rouge)
   - Message : "5 réservation(s) déjà confirmée(s)"
   - ❌ Impossibles à confirmer

### ✅ Résultat Attendu
- Le système gère correctement les garages avec plusieurs créneaux
- La progression des créneaux disponibles est claire

---

## 🧪 Scénario 6 : Dates différentes (ne doivent pas interférer)

### Configuration
- **Garage** : "Garage Test" avec 2 créneaux

### Étapes

1. **Créer des réservations sur des dates différentes mais même heure**
   - Réservation S : Demain, 10:00-12:00
   - Réservation T : Demain, 10:00-12:00
   - Réservation U : Après-demain, 10:00-12:00
   - Réservation V : Après-demain, 10:00-12:00

2. **Confirmer S et T**
   - ✅ Les deux peuvent être confirmées (2 créneaux disponibles pour demain)

3. **Vérifier U et V**
   - Badge : "Créneaux disponibles: 2 / 2" (vert)
   - ✅ Les deux peuvent être confirmées (date différente = créneaux séparés)

4. **Confirmer U et V**
   - ✅ Confirmées sans problème

### ✅ Résultat Attendu
- Les réservations de dates différentes ne consomment pas les mêmes créneaux
- Chaque jour a son propre quota de créneaux

---

## 📊 Checklist de Validation Globale

Après tous les tests, vérifier :

- [ ] Le badge de créneaux s'affiche avec les bonnes couleurs
- [ ] Le compteur de créneaux est exact (X / Total)
- [ ] Le bouton "Confirmer" est désactivé quand aucun créneau disponible
- [ ] Le texte du bouton change en "Complet" quand désactivé
- [ ] Le dialogue de confirmation s'affiche correctement
- [ ] Le dialogue montre les bons détails de réservation
- [ ] L'avertissement apparaît quand peu de créneaux restent
- [ ] Les réservations se rechargent après chaque confirmation/annulation
- [ ] Les chevauchements horaires sont détectés correctement
- [ ] Les dates différentes ne consomment pas les mêmes créneaux
- [ ] Le système fonctionne avec 1, 2, 5+ créneaux
- [ ] L'annulation libère correctement un créneau
- [ ] Le bouton "Refuser" fonctionne toujours normalement

---

## 🐛 Cas Limites à Tester

1. **Garage sans numberOfBays défini**
   - Comportement attendu : Utilise 1 par défaut

2. **Plusieurs utilisateurs qui confirment simultanément**
   - À tester avec le backend pour éviter les race conditions

3. **Réservation sans heure de fin**
   - Vérifier la gestion des erreurs

4. **Changement de numberOfBays après des confirmations**
   - Le système doit recalculer correctement

---

**Date de création** : 4 décembre 2025  
**Auteur** : Test Automation  
**Version** : 1.0

