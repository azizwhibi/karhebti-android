# 🚀 Guide de Test Rapide - Distance & Durée

## 🎯 Objectif
Tester l'affichage de la **distance** et de la **durée d'arrivée** de l'assistant dans l'écran de suivi SOS.

---

## 📋 Prérequis Backend

### ⚡ Configuration Express/NestJS requise

Le backend doit populer le champ `assignedToDetails` dans la réponse du breakdown.

**Exemple de modification dans le contrôleur :**

```javascript
// breakdowns.controller.js ou breakdowns.service.ts

async getBreakdownById(id: string) {
  const breakdown = await this.breakdownModel
    .findById(id)
    .populate({
      path: 'assignedTo',
      select: 'nom prenom telephone latitude longitude',
      transform: (doc) => {
        if (!doc) return null;
        return {
          _id: doc._id,
          nom: doc.nom,
          prenom: doc.prenom,
          telephone: doc.telephone,
          latitude: doc.latitude,
          longitude: doc.longitude
        };
      }
    })
    .exec();
    
  // Renommer le champ populé
  if (breakdown.assignedTo) {
    breakdown.assignedToDetails = breakdown.assignedTo;
  }
  
  return breakdown;
}
```

**OU solution temporaire pour test :**

```javascript
// Ajouter manuellement des coordonnées GPS à vos utilisateurs garage
db.users.updateOne(
  { email: "garage@test.com" },
  { 
    $set: { 
      latitude: 36.8500,   // Position du garage
      longitude: 10.2100 
    } 
  }
);
```

---

## 🧪 Scénario de Test

### Étape 1 : Créer un SOS (Client)
1. Connectez-vous en tant que **client**
2. Allez dans **"SOS"**
3. Créez une demande de panne avec :
   - Type : Remorquage
   - Description : Test distance
   - Position GPS activée

### Étape 2 : Accepter le SOS (Garage)
1. Déconnectez-vous
2. Connectez-vous en tant que **propriétaire de garage**
3. Ouvrez **"Liste SOS"**
4. **Acceptez** la demande du client

### Étape 3 : Vérifier l'affichage
1. Reconnectez-vous en tant que **client**
2. Ouvrez **"Mes SOS"** → Cliquez sur la demande acceptée
3. **Vérifiez l'écran de suivi** :

**✅ Ce que vous devez voir :**

```
┌──────────────────────────────────────┐
│      ✓ Accepté                       │
├──────────────────────────────────────┤
│                                      │
│  ℹ️ L'assistant est en route         │
│                                      │
│     🚗           |        ⏱️         │
│   Distance      |   Arrivée estimée │
│    5.2 km       |      8 min        │
│                                      │
│  🧭 L'assistant se dirige vers      │
│     votre position                   │
│                                      │
├──────────────────────────────────────┤
│                                      │
│         [CARTE]                      │
│      🏢 Marqueur garage (bleu)       │
│         |                            │
│         | Ligne bleue                │
│         |                            │
│      📍 Votre position (rouge)       │
│                                      │
├──────────────────────────────────────┤
│  Détails de la demande               │
│  🔧 Type: Remorquage                 │
│  📝 Description: Test distance       │
│  📅 Créé le: 2025-12-14              │
├──────────────────────────────────────┤
│  Progression                         │
│   ✓     2️⃣     3️⃣     4️⃣            │
│  Attente Accepté En cours Terminé    │
├──────────────────────────────────────┤
│                                      │
│  📞 Appeler l'assistant              │
│     Contacter le garage              │
│                                      │
└──────────────────────────────────────┘
```

---

## 🔍 Vérifications dans Logcat

Ouvrez **Logcat** et filtrez par `BreakdownTracking` :

**✅ Logs attendus si tout fonctionne :**
```
D/BreakdownTracking: Client: 36.8065, 10.1815
D/BreakdownTracking: Garage réel: 36.8500, 10.2100
```

**❌ Si problème :**
```
W/BreakdownTracking: Position du garage non disponible pour assignedTo=garage_id_123
```
→ Le backend ne retourne pas `assignedToDetails`

---

## 🐛 Dépannage

### Problème 1 : La carte de distance n'apparaît pas

**Causes possibles :**
- ❌ Le statut n'est pas `ACCEPTED` ou `IN_PROGRESS`
- ❌ Le backend ne retourne pas `assignedToDetails`
- ❌ Les coordonnées GPS du garage sont nulles

**Solutions :**
1. Vérifiez la réponse API avec Postman :
   ```
   GET http://localhost:3000/breakdowns/{id}
   ```
   
2. Cherchez `assignedToDetails` dans la réponse JSON

3. Si absent, ajoutez la population dans le backend (voir section Backend ci-dessus)

---

### Problème 2 : La carte affiche seulement un marqueur

**Cause :** Position du garage non disponible

**Solution temporaire :**
Ajoutez manuellement des coordonnées GPS au garage dans MongoDB :

```javascript
db.users.updateOne(
  { role: "propGarage", email: "garage@test.com" },
  { 
    $set: { 
      latitude: 36.8500,
      longitude: 10.2100 
    } 
  }
);
```

---

### Problème 3 : Distance affichée = 0 km

**Cause :** Client et garage ont les mêmes coordonnées

**Solution :**
- Vérifiez que le garage a des coordonnées différentes du client
- Utilisez des coordonnées réalistes (exemple : Tunis)

---

## 🧪 Test avec données simulées

Si vous ne pouvez pas modifier le backend immédiatement, testez avec des données en dur :

**Dans `BreakdownTrackingScreen.kt`, ajoutez temporairement :**

```kotlin
// POUR TEST UNIQUEMENT - À RETIRER EN PROD
val garageLatitude = 36.8500
val garageLongitude = 10.2100
```

Cela simule une position de garage à ~5 km du client pour tester l'UI.

---

## 📊 Valeurs de test recommandées

### Position Client (Tunis Centre)
```
Latitude:  36.8065
Longitude: 10.1815
```

### Position Garage (La Marsa)
```
Latitude:  36.8500
Longitude: 10.2100
```

**Distance attendue :** ~5.8 km  
**ETA attendu :** ~9 min (à 40 km/h)

---

## ✅ Checklist de validation

- [ ] La carte de distance apparaît quand le statut = ACCEPTED
- [ ] La distance est affichée en km (ex: "5.2 km")
- [ ] Le temps estimé est affiché (ex: "8 min")
- [ ] La carte montre 2 marqueurs (client rouge + garage bleu)
- [ ] Une ligne bleue relie les deux marqueurs
- [ ] Le message "L'assistant se dirige vers votre position" est visible
- [ ] Les logs Logcat affichent les coordonnées correctes
- [ ] Le polling rafraîchit les données toutes les 10 secondes

---

## 🎯 Test de bout en bout

### Scénario complet (10 min)

1. **[CLIENT]** Créer un SOS → Status = PENDING
   - ❌ Carte de distance non visible (normal)

2. **[GARAGE]** Accepter le SOS → Status = ACCEPTED
   - ✅ Carte de distance apparaît

3. **[CLIENT]** Rafraîchir l'écran de suivi
   - ✅ Voir distance + ETA
   - ✅ Voir 2 marqueurs sur la carte

4. **[GARAGE]** Changer statut à IN_PROGRESS
   - ✅ Message change : "L'assistant est sur place..."

5. **[GARAGE]** Terminer → Status = COMPLETED
   - ❌ Carte de distance disparaît (normal)

---

## 📞 Support

Si vous rencontrez des problèmes :

1. **Vérifiez les logs** : `adb logcat | grep BreakdownTracking`
2. **Vérifiez l'API** : Testez avec Postman/curl
3. **Vérifiez la DB** : Les users garage ont-ils latitude/longitude ?

---

## 🎉 Résultat attendu

Vous devriez avoir un écran de suivi SOS moderne et fonctionnel qui affiche en temps réel la distance et le temps d'arrivée de l'assistant, avec une carte interactive montrant les deux positions !

**Prêt pour la production !** ✨

