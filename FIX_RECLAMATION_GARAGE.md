# Fix: Création de Réclamation pour Garage

## 🐛 Problème
L'utilisateur recevait une erreur générique "Erreur lors de la création de la réclamation" lors de la tentative de création d'une réclamation à propos d'un garage.

## ✅ Corrections Apportées

### 1. Amélioration de la Gestion d'Erreur (Repositories.kt)
**Modifications dans `ReclamationRepository.createReclamation()`:**

```kotlin
// Ajout de logs détaillés
android.util.Log.d("ReclamationRepository", "Creating reclamation: type=$type, titre=$titre, garageId=$garageId")
android.util.Log.d("ReclamationRepository", "Response code: ${response.code()}")

// Parsing du message d'erreur du backend
val errorMsg = try {
    val gson = com.google.gson.Gson()
    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
    errorResponse.message.joinToString(", ")
} catch (e: Exception) {
    errorBody ?: "Erreur lors de la création de la réclamation"
}
```

**Avantages:**
- ✅ Messages d'erreur détaillés du backend
- ✅ Logs pour débogage
- ✅ Meilleure visibilité sur les erreurs API

### 2. Validation des Champs (AddReclamationScreen.kt)
**Validation améliorée avant soumission:**

```kotlin
when {
    titre.isBlank() -> {
        errorMessage = "Veuillez entrer un titre"
        showErrorDialog = true
    }
    message.isBlank() -> {
        errorMessage = "Veuillez entrer un message"
        showErrorDialog = true
    }
    type == "garage" && selectedGarageId == null -> {
        errorMessage = "Veuillez sélectionner un garage"
        showErrorDialog = true
    }
    else -> {
        // Créer la réclamation
    }
}
```

**Avantages:**
- ✅ Vérifie que tous les champs sont remplis
- ✅ S'assure qu'un garage est sélectionné pour une réclamation de type "garage"
- ✅ Messages d'erreur spécifiques et clairs

### 3. Amélioration du Dropdown de Garage
**Interface utilisateur améliorée:**

```kotlin
// Affichage clair du garage sélectionné
OutlinedTextField(
    value = selectedGarageName,
    placeholder = { Text("Sélectionner un garage") },
    label = { Text("Garage") },
    isError = selectedGarageId == null  // Indicateur visuel si non sélectionné
)

// Liste des garages avec nom et adresse
DropdownMenuItem(
    text = { 
        Column {
            Text(text = garage.nom, fontWeight = FontWeight.Medium)
            Text(text = garage.adresse, style = MaterialTheme.typography.bodySmall)
        }
    }
)
```

**Avantages:**
- ✅ Placeholder clair "Sélectionner un garage"
- ✅ Affichage du nom ET de l'adresse du garage
- ✅ Indicateur d'erreur visuel (bordure rouge si non sélectionné)
- ✅ Message si aucun garage disponible
- ✅ Gestion des états (Loading, Error)

### 4. Logs de Débogage
**Ajout de logs pour tracer le flux:**

```kotlin
// Au chargement des garages
LaunchedEffect(garagesState) {
    when (val state = garagesState) {
        is Resource.Success -> {
            Log.d("AddReclamation", "Garages loaded: ${state.data?.size} garages")
        }
        is Resource.Error -> Log.e("AddReclamation", "Error: ${state.message}")
    }
}

// À la sélection d'un garage
onClick = {
    selectedGarageId = garage.id
    Log.d("AddReclamation", "Selected garage: ${garage.id} - ${garage.nom}")
}

// À la soumission
Log.d("AddReclamation", "Creating reclamation: type=$type, titre=$titre, garageId=$selectedGarageId")
```

**Avantages:**
- ✅ Traçabilité complète du processus
- ✅ Identification facile des problèmes
- ✅ Vérification de l'état des données

## 🎯 Résultat

### Avant:
- ❌ Message d'erreur générique
- ❌ Pas de validation de sélection de garage
- ❌ Pas de feedback visuel
- ❌ Difficile à déboguer

### Après:
- ✅ Messages d'erreur détaillés du backend
- ✅ Validation stricte des champs obligatoires
- ✅ Feedback visuel clair (bordure rouge si garage non sélectionné)
- ✅ Affichage enrichi des garages (nom + adresse)
- ✅ Logs complets pour débogage
- ✅ Gestion de tous les états (Loading, Error, Empty)

## 📝 Guide d'Utilisation

### Pour créer une réclamation à propos d'un garage:

1. **Ouvrir Settings** → Cliquer sur "Réclamations"
2. **Cliquer sur le bouton "+"** (en bas à droite)
3. **Sélectionner "Garage"** dans le type de réclamation
4. **Choisir un garage** dans la liste déroulante
   - Les garages s'affichent avec leur nom et adresse
   - Si la liste est vide, un message "Aucun garage disponible" s'affiche
5. **Entrer un titre** (obligatoire)
6. **Entrer un message** détaillé (obligatoire)
7. **Cliquer sur "Soumettre la réclamation"**

### Messages de Validation:
- Si vous oubliez le titre: "Veuillez entrer un titre"
- Si vous oubliez le message: "Veuillez entrer un message"
- Si vous oubliez de sélectionner un garage: "Veuillez sélectionner un garage"

### En Cas d'Erreur Backend:
- Le message d'erreur exact du serveur s'affichera
- Les logs dans Logcat montreront:
  - Les données envoyées
  - Le code de réponse HTTP
  - Le corps de l'erreur

## 🔍 Débogage

Si l'erreur persiste, vérifiez dans Logcat (filtrer par "AddReclamation" ou "ReclamationRepository"):

```
# Logs à chercher:
AddReclamation: Loading garages...
AddReclamation: Garages loaded: X garages
AddReclamation: Garage: [id] - [nom]
AddReclamation: Selected garage: [id] - [nom]
AddReclamation: Creating reclamation: type=garage, titre=..., garageId=...
ReclamationRepository: Creating reclamation: type=garage, titre=..., garageId=...
ReclamationRepository: Response code: XXX
```

## 🚀 Prochaines Étapes

1. **Tester** la création de réclamation avec un garage réel
2. **Vérifier** que le backend reçoit bien les données
3. **Confirmer** que la réclamation est créée dans la base de données
4. **Optionnel:** Implémenter la sélection de service pour le type "Service"

## ✅ Compilation

Le projet compile avec succès sans erreurs:
```
BUILD SUCCESSFUL
```

Seuls quelques avertissements non-bloquants (deprecations) subsistent.

