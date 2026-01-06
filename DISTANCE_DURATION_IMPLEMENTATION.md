# 📍 Affichage Distance et Durée - Suivi SOS

## ✅ Fonctionnalité Implémentée

L'écran de suivi SOS (`BreakdownTrackingScreen`) affiche maintenant **la distance réelle** et **le temps d'arrivée estimé (ETA)** de l'assistant vers le client.

---

## 🎯 Ce qui a été fait

### 1. **Modification du modèle de données** (`BreakdownResponse.kt`)
- ✅ Ajout d'un champ `assignedToDetails` pour recevoir les informations complètes du garage assigné
- ✅ Création de la classe `AssignedGarageDetails` contenant :
  - ID du garage
  - Nom et prénom
  - Téléphone
  - **Coordonnées GPS (latitude, longitude)**

```kotlin
data class AssignedGarageDetails(
    @SerializedName("_id")
    val id: String?,
    val nom: String?,
    val prenom: String?,
    val telephone: String?,
    val latitude: Double?,
    val longitude: Double?
)
```

### 2. **Amélioration de l'écran de suivi** (`BreakdownTrackingScreen.kt`)
- ✅ Remplacement de la position simulée par les **vraies coordonnées GPS** du garage
- ✅ Calcul de la distance réelle avec la formule de Haversine
- ✅ Estimation du temps d'arrivée (ETA) basé sur une vitesse moyenne de 40 km/h

### 3. **Nouvelle carte de distance améliorée** (`DistanceCard`)
- ✅ Affichage visuel de la distance (en km ou mètres)
- ✅ Affichage du temps d'arrivée estimé (en minutes ou heures)
- ✅ Messages contextuels selon le statut :
  - **ACCEPTED** : "L'assistant se dirige vers votre position"
  - **IN_PROGRESS** : "L'assistant est sur place et travaille sur votre véhicule"
- ✅ Design amélioré avec icônes et séparateurs visuels

### 4. **Carte interactive améliorée** (`OpenStreetMapViewMultiple.kt`)
- ✅ Affichage simultané de **deux marqueurs** :
  - 📍 **Marqueur rouge** : Position du client
  - 📍 **Marqueur bleu** : Position de l'assistant/garage
- ✅ **Ligne bleue** reliant les deux positions
- ✅ Zoom automatique pour afficher les deux points
- ✅ Fallback vers la carte simple si la position du garage n'est pas disponible

---

## 📊 Calculs utilisés

### Distance (Formule de Haversine)
```kotlin
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Rayon de la Terre en km
    // ... calcul Haversine
    return earthRadius * c
}
```

### ETA (Temps estimé d'arrivée)
```kotlin
fun estimateETA(distanceKm: Double, speedKmh: Double = 40.0): String {
    val hours = distanceKm / speedKmh
    val minutes = (hours * 60).toInt()
    // Formatage : "< 1 min", "15 min", "1 h 30 min"
}
```

---

## 🔧 Configuration Backend Requise

Pour que la distance et la durée s'affichent correctement, le **backend doit** :

### ✅ Populer le champ `assignedToDetails` dans la réponse

Lorsque l'API `/breakdowns/{id}` retourne un breakdown, elle doit inclure :

```json
{
  "_id": "...",
  "latitude": 36.8065,
  "longitude": 10.1815,
  "status": "ACCEPTED",
  "assignedTo": "garage_id_123",
  "assignedToDetails": {
    "_id": "garage_id_123",
    "nom": "Garage",
    "prenom": "Central",
    "telephone": "+216 12 345 678",
    "latitude": 36.8500,
    "longitude": 10.2100
  }
}
```

### Option 1 : Population Mongoose (Recommandé)
```javascript
// Backend NestJS/Express
await this.breakdownModel
  .findById(id)
  .populate({
    path: 'assignedTo',
    select: 'nom prenom telephone latitude longitude'
  })
  .exec();
```

### Option 2 : Récupération manuelle
Si le backend ne peut pas populer automatiquement, l'app Android peut :
1. Récupérer le breakdown
2. Si `assignedTo` existe, appeler `/users/{id}` pour obtenir les détails
3. Fusionner les données côté client

---

## 📱 Affichage UI

### Carte de Distance (visible si statut = ACCEPTED ou IN_PROGRESS)

```
┌─────────────────────────────────────┐
│ ℹ️ L'assistant est en route         │
│                                     │
│   🚗          |        ⏱️           │
│  Distance    |   Arrivée estimée   │
│   5.2 km     |       8 min         │
│                                     │
│ 🧭 L'assistant se dirige vers      │
│    votre position                   │
└─────────────────────────────────────┘
```

### Carte Interactive

```
┌─────────────────────────────────────┐
│                                     │
│         🏢 (Marqueur bleu)          │
│          Assistant/Garage           │
│              |                      │
│              | (Ligne bleue)        │
│              |                      │
│         📍 (Marqueur rouge)         │
│          Votre position             │
│                                     │
└─────────────────────────────────────┘
```

---

## 🧪 Test de la fonctionnalité

### Scénario de test complet

1. **Créer une demande SOS** en tant que client
2. **Accepter la demande** en tant que propriétaire de garage
3. **Ouvrir l'écran de suivi** → La carte de distance doit apparaître
4. **Vérifier l'affichage** :
   - ✅ Distance en km ou mètres
   - ✅ Temps d'arrivée estimé
   - ✅ Deux marqueurs sur la carte (client + garage)
   - ✅ Ligne bleue entre les deux positions

### Logs de débogage

Les logs suivants sont affichés dans Logcat :

```
D/BreakdownTracking: Client: 36.8065, 10.1815
D/BreakdownTracking: Garage réel: 36.8500, 10.2100
```

Si la position du garage n'est pas disponible :
```
W/BreakdownTracking: Position du garage non disponible pour assignedTo=garage_id_123
```

---

## 🔄 Mise à jour en temps réel

L'écran fait un **polling toutes les 10 secondes** pour rafraîchir les données :

```kotlin
LaunchedEffect(breakdownId) {
    while (true) {
        viewModel.fetchBreakdownById(breakdownId)
        delay(10000) // 10 secondes
    }
}
```

Cela permet de :
- Mettre à jour le statut
- Mettre à jour la position du garage (si GPS en temps réel)
- Recalculer la distance dynamiquement

---

## 🎨 Personnalisation

### Modifier la vitesse moyenne pour le calcul ETA

Dans `DistanceUtils.kt` :
```kotlin
fun estimateETA(distanceKm: Double, speedKmh: Double = 40.0): String
```

Ajustez `speedKmh` selon votre contexte :
- **30 km/h** : Ville dense avec trafic
- **40 km/h** : Ville normale (par défaut)
- **50 km/h** : Route fluide

### Modifier l'intervalle de polling

Dans `BreakdownTrackingScreenWrapper` :
```kotlin
delay(10000) // Changer à 5000 pour 5 secondes, etc.
```

---

## 📝 Fichiers modifiés

| Fichier | Modifications |
|---------|--------------|
| `BreakdownResponse.kt` | Ajout `assignedToDetails` et `AssignedGarageDetails` |
| `BreakdownTrackingScreen.kt` | Utilisation vraies coordonnées, nouvelle `DistanceCard` |
| `OpenStreetMapViewMultiple.kt` | **NOUVEAU** : Carte avec deux marqueurs + ligne |
| `DistanceUtils.kt` | Aucune modification (déjà existant) |

---

## ⚠️ Points d'attention

### Si la distance ne s'affiche pas :

1. **Vérifier que le backend retourne `assignedToDetails`**
   ```bash
   curl http://localhost:3000/breakdowns/{id}
   ```

2. **Vérifier les logs Android** pour voir si les coordonnées sont reçues

3. **Vérifier que le statut est bien `ACCEPTED` ou `IN_PROGRESS`**
   - La carte de distance n'est visible que dans ces états

4. **Position simulée désactivée** : L'ancienne simulation a été retirée

---

## 🚀 Prochaines améliorations possibles

- [ ] Utiliser GPS en temps réel de l'assistant pour mettre à jour sa position
- [ ] Ajouter une notification push quand l'assistant est proche (< 1 km)
- [ ] Afficher le trajet recommandé (Google Directions API ou OSRM)
- [ ] Permettre au client de voir le déplacement en temps réel sur la carte
- [ ] Ajouter un bouton "Partager ma position" pour envoyer un lien Google Maps

---

## ✅ Résumé

La fonctionnalité d'affichage de la **distance** et de la **durée d'arrivée** est maintenant **complète et opérationnelle** ! 

Les utilisateurs peuvent voir en temps réel :
- 📏 La distance entre eux et l'assistant
- ⏱️ Le temps estimé avant l'arrivée
- 🗺️ Les deux positions sur une carte interactive
- 🔵 Une ligne connectant les deux points

**Prêt à tester !** 🎉

