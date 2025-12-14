# Script de configuration GPS pour les tests

## MongoDB - Ajouter coordonnées GPS aux garages

```javascript
// Connectez-vous à MongoDB
use karhebti_db;

// 1. Ajouter des coordonnées GPS à tous les garages existants
// Position par défaut : Tunis, Tunisie avec légères variations

db.users.updateMany(
  { role: "propGarage" },
  { 
    $set: { 
      latitude: 36.8500,
      longitude: 10.2100 
    } 
  }
);

// 2. Créer des garages de test avec différentes positions

// Garage 1 - La Marsa
db.users.updateOne(
  { email: "garage1@test.com" },
  { 
    $set: {
      nom: "Garage",
      prenom: "La Marsa",
      email: "garage1@test.com",
      role: "propGarage",
      telephone: "+216 71 123 456",
      latitude: 36.8650,
      longitude: 10.3250
    }
  },
  { upsert: true }
);

// Garage 2 - Ariana
db.users.updateOne(
  { email: "garage2@test.com" },
  { 
    $set: {
      nom: "Garage",
      prenom: "Ariana",
      email: "garage2@test.com",
      role: "propGarage",
      telephone: "+216 71 234 567",
      latitude: 36.8607,
      longitude: 10.1947
    }
  },
  { upsert: true }
);

// Garage 3 - Centre Ville Tunis
db.users.updateOne(
  { email: "garage3@test.com" },
  { 
    $set: {
      nom: "Garage",
      prenom: "Centre Ville",
      email: "garage3@test.com",
      role: "propGarage",
      telephone: "+216 71 345 678",
      latitude: 36.8065,
      longitude: 10.1815
    }
  },
  { upsert: true }
);

// Garage 4 - Ben Arous
db.users.updateOne(
  { email: "garage4@test.com" },
  { 
    $set: {
      nom: "Garage",
      prenom: "Ben Arous",
      email: "garage4@test.com",
      role: "propGarage",
      telephone: "+216 71 456 789",
      latitude: 36.7548,
      longitude: 10.2218
    }
  },
  { upsert: true }
);

// 3. Créer un client de test (position variable)
db.users.updateOne(
  { email: "client@test.com" },
  { 
    $set: {
      nom: "Test",
      prenom: "Client",
      email: "client@test.com",
      role: "user",
      telephone: "+216 50 123 456"
    }
  },
  { upsert: true }
);

// 4. Vérifier les coordonnées
print("\n=== GARAGES AVEC COORDONNÉES GPS ===");
db.users.find(
  { role: "propGarage", latitude: { $exists: true } },
  { nom: 1, prenom: 1, email: 1, latitude: 1, longitude: 1 }
).forEach(printjson);

print("\n=== DISTANCES ENTRE GARAGES ===");
print("Distance La Marsa <-> Ariana: ~8 km");
print("Distance Centre Ville <-> Ben Arous: ~7 km");
print("Distance La Marsa <-> Centre Ville: ~6 km");
```

## PostgreSQL (si utilisé)

```sql
-- Ajouter colonnes GPS si elles n'existent pas
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

-- Ajouter des coordonnées GPS aux garages existants
UPDATE users 
SET latitude = 36.8500, longitude = 10.2100
WHERE role = 'propGarage';

-- Créer des garages de test avec différentes positions

-- Garage 1 - La Marsa
INSERT INTO users (nom, prenom, email, role, telephone, latitude, longitude)
VALUES ('Garage', 'La Marsa', 'garage1@test.com', 'propGarage', '+216 71 123 456', 36.8650, 10.3250)
ON CONFLICT (email) 
DO UPDATE SET latitude = 36.8650, longitude = 10.3250;

-- Garage 2 - Ariana
INSERT INTO users (nom, prenom, email, role, telephone, latitude, longitude)
VALUES ('Garage', 'Ariana', 'garage2@test.com', 'propGarage', '+216 71 234 567', 36.8607, 10.1947)
ON CONFLICT (email) 
DO UPDATE SET latitude = 36.8607, longitude = 10.1947;

-- Garage 3 - Centre Ville
INSERT INTO users (nom, prenom, email, role, telephone, latitude, longitude)
VALUES ('Garage', 'Centre Ville', 'garage3@test.com', 'propGarage', '+216 71 345 678', 36.8065, 10.1815)
ON CONFLICT (email) 
DO UPDATE SET latitude = 36.8065, longitude = 10.1815;

-- Garage 4 - Ben Arous
INSERT INTO users (nom, prenom, email, role, telephone, latitude, longitude)
VALUES ('Garage', 'Ben Arous', 'garage4@test.com', 'propGarage', '+216 71 456 789', 36.7548, 10.2218)
ON CONFLICT (email) 
DO UPDATE SET latitude = 36.7548, longitude = 10.2218;

-- Vérifier
SELECT nom, prenom, email, latitude, longitude 
FROM users 
WHERE role = 'propGarage' AND latitude IS NOT NULL;
```

## Positions GPS de référence (Tunis)

| Lieu | Latitude | Longitude | Description |
|------|----------|-----------|-------------|
| Centre Ville Tunis | 36.8065 | 10.1815 | Avenue Habib Bourguiba |
| La Marsa | 36.8650 | 10.3250 | Bord de mer |
| Ariana | 36.8607 | 10.1947 | Ariana Ville |
| Ben Arous | 36.7548 | 10.2218 | Sud de Tunis |
| Carthage | 36.8564 | 10.3232 | Site archéologique |

## Test de calcul de distance

```javascript
// Fonction de test dans la console MongoDB
function haversine(lat1, lon1, lat2, lon2) {
  const R = 6371; // Rayon de la Terre en km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

// Test
print("Distance Centre Ville -> La Marsa: " + 
      haversine(36.8065, 10.1815, 36.8650, 10.3250).toFixed(2) + " km");
```

## Script Backend pour populer assignedToDetails

```javascript
// breakdowns.service.ts (NestJS)

async findOne(id: string) {
  const breakdown = await this.breakdownModel
    .findById(id)
    .lean()
    .exec();
  
  if (!breakdown) {
    throw new NotFoundException('Breakdown not found');
  }
  
  // Populer les détails du garage assigné
  if (breakdown.assignedTo) {
    const garage = await this.userModel
      .findById(breakdown.assignedTo)
      .select('nom prenom telephone latitude longitude')
      .lean()
      .exec();
    
    if (garage) {
      breakdown.assignedToDetails = {
        _id: garage._id,
        nom: garage.nom,
        prenom: garage.prenom,
        telephone: garage.telephone,
        latitude: garage.latitude,
        longitude: garage.longitude
      };
    }
  }
  
  return breakdown;
}
```

## Commandes rapides

### MongoDB Shell
```bash
# Se connecter
mongosh "mongodb://localhost:27017/karhebti_db"

# Exécuter le script
load("setup_gps_coordinates.js")

# Ou en une ligne
mongosh "mongodb://localhost:27017/karhebti_db" --eval "db.users.updateMany({role:'propGarage'},{$set:{latitude:36.8500,longitude:10.2100}})"
```

### PostgreSQL
```bash
# Se connecter
psql -U postgres -d karhebti_db

# Exécuter le script
\i setup_gps_coordinates.sql
```

## Vérification finale

```javascript
// MongoDB
db.users.find(
  { role: "propGarage", latitude: { $exists: true } }
).count()
// Doit retourner > 0

// Afficher les coordonnées
db.users.aggregate([
  { $match: { role: "propGarage", latitude: { $exists: true } } },
  { $project: { 
      nom: 1, 
      prenom: 1, 
      coords: { 
        $concat: [
          { $toString: "$latitude" }, 
          ", ", 
          { $toString: "$longitude" }
        ] 
      } 
  }}
])
```

## Notes importantes

1. **Coordonnées réelles** : Ces positions sont basées sur de vraies localisations à Tunis, Tunisie
2. **Distances calculées** : Utilisent la formule de Haversine (distance à vol d'oiseau)
3. **Production** : En production, utilisez le GPS réel des garages
4. **Sécurité** : Ne partagez pas les coordonnées exactes dans un repo public

## Troubleshooting

Si les coordonnées ne s'affichent pas :
1. Vérifiez que le champ existe : `db.users.findOne({role:"propGarage"})`
2. Vérifiez le type : doit être `Number`, pas `String`
3. Re-exécutez la commande update
4. Redémarrez le backend pour rafraîchir le cache

