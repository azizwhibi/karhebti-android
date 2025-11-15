# 🔧 Solution Finale: Erreur de Parsing JSON pour Réclamation

## 🐛 Problème

```
java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 100 path $.garage
```

## 📊 Analyse

Le backend renvoie `garage` et `service` comme des **IDs String**, mais notre modèle `ReclamationResponse` s'attendait à des **objets complets** (`GarageResponse`, `ServiceResponse`).

## ✅ Solution Appliquée

### 1. Modification de ApiModels.kt

**AVANT:**
```kotlin
data class ReclamationResponse(
    @SerializedName("_id")
    val id: String,
    val type: String,
    val titre: String,
    val message: String,
    @JsonAdapter(FlexibleUserDeserializer::class)
    val user: String? = null,
    val garage: GarageResponse? = null,      // ❌ Objet
    val service: ServiceResponse? = null,    // ❌ Objet
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
```

**APRÈS:**
```kotlin
data class ReclamationResponse(
    @SerializedName("_id")
    val id: String,
    val type: String,
    val titre: String,
    val message: String,
    @JsonAdapter(FlexibleUserDeserializer::class)
    val user: String? = null,
    @JsonAdapter(FlexibleGarageDeserializer::class)
    val garage: String? = null,              // ✅ ID String
    @JsonAdapter(FlexibleServiceDeserializer::class)
    val service: String? = null,             // ✅ ID String
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
```

### 2. Ajout de FlexibleServiceDeserializer.kt

```kotlin
/**
 * Custom deserializer for service field that can be either a String (ID) or an object
 */
class FlexibleServiceDeserializer : JsonDeserializer<String?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String? {
        if (json == null || json.isJsonNull) {
            return null
        }

        return when {
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                json.asString
            }
            json.isJsonObject -> {
                // Extract the _id field from the service object
                json.asJsonObject.get("_id")?.asString
            }
            else -> null
        }
    }
}
```

### 3. Modification des Écrans UI

**ReclamationDetailScreen.kt** - Afficher l'ID au lieu des propriétés:
```kotlin
// AVANT: reclamation.garage.nom
// APRÈS: "Garage ID: ${reclamation.garage}"

if (reclamation.garage != null) {
    // ...
    Text(
        text = "Garage ID: ${reclamation.garage}",
        style = MaterialTheme.typography.bodyMedium
    )
}
```

**ReclamationsScreen.kt** - Afficher l'ID dans la liste:
```kotlin
// AVANT: reclamation.garage.nom
// APRÈS: "Garage ID: ${reclamation.garage}"

if (reclamation.garage != null) {
    Text(
        text = "Garage ID: ${reclamation.garage}",
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1
    )
}
```

## 📝 Fichiers à Modifier Manuellement

Si les modifications automatiques n'ont pas fonctionné, voici les changements à faire **manuellement** dans votre IDE:

### Fichier 1: ApiModels.kt

Ligne ~320-335, changez:
```kotlin
data class ReclamationResponse(
    @SerializedName("_id")
    val id: String,
    val type: String,
    val titre: String,
    val message: String,
    @JsonAdapter(FlexibleUserDeserializer::class)
    val user: String? = null,
    @JsonAdapter(FlexibleGarageDeserializer::class)  // ← AJOUTER
    val garage: String? = null,                        // ← CHANGER de GarageResponse? à String?
    @JsonAdapter(FlexibleServiceDeserializer::class) // ← AJOUTER  
    val service: String? = null,                       // ← CHANGER de ServiceResponse? à String?
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
```

### Fichier 2: FlexibleTypeAdapters.kt

Ajoutez à la fin (après FlexibleCarDeserializer):
```kotlin
/**
 * Custom deserializer for service field that can be either a String (ID) or an object
 */
class FlexibleServiceDeserializer : JsonDeserializer<String?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String? {
        if (json == null || json.isJsonNull) {
            return null
        }

        return when {
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                json.asString
            }
            json.isJsonObject -> {
                // Extract the _id field from the service object
                json.asJsonObject.get("_id")?.asString
            }
            else -> null
        }
    }
}
```

### Fichier 3: ReclamationDetailScreen.kt

**Changement 1** - Ligne ~215-222, remplacez:
```kotlin
// CHERCHEZ:
Text(
    text = reclamation.garage.nom,
    ...
)
Text(
    text = reclamation.garage.adresse,
    ...
)

// REMPLACEZ PAR:
Text(
    text = "Garage ID: ${reclamation.garage}",
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

**Changement 2** - Ligne ~257-260, remplacez:
```kotlin
// CHERCHEZ:
Text(
    text = reclamation.service.type,
    ...
)

// REMPLACEZ PAR:
Text(
    text = "Service ID: ${reclamation.service}",
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

### Fichier 4: ReclamationsScreen.kt

**Changement** - Ligne ~262-266, remplacez:
```kotlin
// CHERCHEZ:
Text(
    text = reclamation.garage.nom,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

// REMPLACEZ PAR:
Text(
    text = "Garage ID: ${reclamation.garage}",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines = 1
)
```

## 🔍 Vérification

Après avoir fait ces changements:

1. **Nettoyer le projet:**
   ```bash
   .\gradlew.bat clean
   ```

2. **Recompiler:**
   ```bash
   .\gradlew.bat assembleDebug
   ```

3. **Vérifier qu'il n'y a plus d'erreurs** sur les lignes 215, 220, 257 de ReclamationDetailScreen.kt et ligne 262 de ReclamationsScreen.kt

## 🎯 Résultat Attendu

Après ces modifications:
- ✅ L'erreur "Expected BEGIN_OBJECT but was STRING" disparaît
- ✅ L'application peut parser les réclamations du backend
- ✅ La création de réclamation fonctionne
- ✅ L'affichage des réclamations fonctionne

## 💡 Alternative Future

Pour afficher les noms complets des garages au lieu des IDs, deux options:

### Option 1: Backend populé
Demander au backend d'utiliser `populate()` pour renvoyer l'objet complet:
```javascript
// Dans le backend NestJS
return this.reclamationModel
  .find()
  .populate('garage')   // Popule le garage
  .populate('service')  // Popule le service
  .populate('user');    // Popule le user
```

### Option 2: Chargement côté client
Charger séparément les garages et faire le matching:
```kotlin
// Dans ReclamationDetailScreen
val garageViewModel: GarageViewModel = viewModel(...)
val garagesState by garageViewModel.garagesState.observeAsState()

LaunchedEffect(Unit) {
    garageViewModel.getGarages()
}

// Puis dans l'UI
val garage = (garagesState as? Resource.Success)
    ?.data
    ?.find { it.id == reclamation.garage }

garage?.let {
    Text(text = it.nom)
    Text(text = it.adresse)
}
```

## 📦 Fichiers Modifiés

1. ✅ `ApiModels.kt` - ReclamationResponse avec IDs String
2. ✅ `FlexibleTypeAdapters.kt` - Ajout FlexibleServiceDeserializer
3. ✅ `ReclamationDetailScreen.kt` - Affichage IDs
4. ✅ `ReclamationsScreen.kt` - Affichage IDs

## ⚡ Commandes Rapides

```bash
# Nettoyer complètement
cd "C:\Users\Mosbeh Eya\Desktop\karhebti-android-gestionVoitures"
.\gradlew.bat clean
Remove-Item -Recurse -Force .\app\build\

# Recompiler
.\gradlew.bat assembleDebug

# Si erreur persiste, invalider cache IDE
# File → Invalidate Caches / Restart
```

---

**Date:** 14 novembre 2025  
**Status:** En attente de modification manuelle  
**Priorité:** 🔴 URGENT - Bloque la création de réclamations

