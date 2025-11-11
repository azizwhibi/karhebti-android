# Implémentation de l'Entité Réclamations (Feedback)

**Date:** 11 novembre 2025

## ✅ Implémentation Complète

### 1. **Modèles de Données (ApiModels.kt)**
- ✅ `CreateReclamationRequest` - DTO pour créer une réclamation
  - type: String ("service" ou "garage")
  - titre: String
  - message: String
  - garageId: String? (optionnel)
  - serviceId: String? (optionnel)

- ✅ `UpdateReclamationRequest` - DTO pour mettre à jour une réclamation
  - titre: String?
  - message: String?

- ✅ `ReclamationResponse` - DTO de réponse
  - id: String
  - type: String
  - titre: String
  - message: String
  - user: UserResponse?
  - garage: GarageResponse?
  - service: ServiceResponse?
  - createdAt: Date
  - updatedAt: Date

### 2. **API Service (KarhebtiApiService.kt)**
Endpoints REST implémentés :
- ✅ `GET /reclamations` - Récupérer toutes les réclamations
- ✅ `GET /reclamations/{id}` - Récupérer une réclamation par ID
- ✅ `GET /reclamations/user/me` - Récupérer les réclamations de l'utilisateur connecté
- ✅ `GET /reclamations/garage/{garageId}` - Récupérer les réclamations d'un garage
- ✅ `GET /reclamations/service/{serviceId}` - Récupérer les réclamations d'un service
- ✅ `POST /reclamations` - Créer une nouvelle réclamation
- ✅ `PATCH /reclamations/{id}` - Mettre à jour une réclamation
- ✅ `DELETE /reclamations/{id}` - Supprimer une réclamation

### 3. **Repository (Repositories.kt)**
Classe `ReclamationRepository` avec méthodes :
- ✅ `getReclamations()` - Récupérer toutes les réclamations
- ✅ `getReclamationById(id)` - Récupérer une réclamation spécifique
- ✅ `getMyReclamations()` - Récupérer les réclamations de l'utilisateur
- ✅ `getReclamationsByGarage(garageId)` - Récupérer les réclamations d'un garage
- ✅ `getReclamationsByService(serviceId)` - Récupérer les réclamations d'un service
- ✅ `createReclamation(request)` - Créer une nouvelle réclamation
- ✅ `updateReclamation(id, request)` - Mettre à jour une réclamation
- ✅ `deleteReclamation(id)` - Supprimer une réclamation

Toutes les méthodes retournent un `Resource<T>` pour gérer les états (Loading, Success, Error)

### 4. **ViewModel (ViewModels.kt)**
Classe `ReclamationViewModel` avec :
- ✅ LiveData pour tous les états :
  - `reclamationsState` - Liste des réclamations
  - `reclamationDetailState` - Détails d'une réclamation
  - `myReclamationsState` - Réclamations de l'utilisateur
  - `createReclamationState` - État de création
  - `updateReclamationState` - État de mise à jour
  - `deleteReclamationState` - État de suppression

- ✅ StateFlow pour la liste des réclamations
- ✅ Méthodes publiques :
  - `getAllReclamations()`
  - `getReclamationById(id)`
  - `getMyReclamations()`
  - `getReclamationsByGarage(garageId)`
  - `getReclamationsByService(serviceId)`
  - `createReclamation(type, titre, message, garageId?, serviceId?)`
  - `updateReclamation(id, titre?, message?)`
  - `deleteReclamation(id)`
  - `refresh()`

### 5. **Écrans UI (ui/screens/)**

#### ReclamationsScreen.kt
- ✅ Affiche la liste des réclamations de l'utilisateur
- ✅ Bouton FAB pour ajouter une nouvelle réclamation
- ✅ Gestion des états (Loading, Success, Error, Empty)
- ✅ Cards cliquables pour voir les détails
- ✅ Badge de type (Garage/Service) avec couleurs distinctes
- ✅ Affichage de la date de création
- ✅ Affichage du garage concerné si applicable

#### AddReclamationScreen.kt
- ✅ Formulaire de création de réclamation
- ✅ Sélection du type (Garage/Service) avec FilterChips
- ✅ Dropdown pour sélectionner un garage
- ✅ Champs de texte pour titre et message
- ✅ Validation des champs
- ✅ Gestion des erreurs avec AlertDialog
- ✅ Indicateur de chargement pendant la soumission

#### ReclamationDetailScreen.kt
- ✅ Affichage complet des détails de la réclamation
- ✅ Badge coloré indiquant le type
- ✅ Affichage du titre, message, date
- ✅ Informations sur le garage concerné (nom, adresse)
- ✅ Informations sur le service concerné (type)
- ✅ Boutons d'action : Modifier et Supprimer
- ✅ Dialog de confirmation pour la suppression
- ✅ Gestion des états (Loading, Error)

#### EditReclamationScreen.kt
- ✅ Formulaire de modification de réclamation
- ✅ Pré-remplissage des champs avec les données existantes
- ✅ Modification du titre et du message
- ✅ Validation des champs
- ✅ Gestion des erreurs
- ✅ Indicateur de chargement

### 6. **Navigation (NavGraph.kt)**

Routes ajoutées :
- ✅ `Screen.Reclamations` - Liste des réclamations
- ✅ `Screen.AddReclamation` - Ajouter une réclamation
- ✅ `Screen.ReclamationDetail/{reclamationId}` - Détails d'une réclamation
- ✅ `Screen.EditReclamation/{reclamationId}` - Modifier une réclamation

Composables de navigation configurés avec :
- ✅ Navigation entre les écrans
- ✅ Passage de paramètres (reclamationId)
- ✅ Gestion du retour (popBackStack)
- ✅ Callbacks onReclamationCreated et onReclamationUpdated

### 7. **Intégration dans HomeScreen**
- ✅ Ajout du paramètre `onReclamationsClick`
- ✅ Bouton "Réclamations" dans les actions rapides
- ✅ Icône `Icons.Default.Feedback`
- ✅ Navigation vers l'écran des réclamations

## 🎨 Design et UX

### Éléments visuels :
- **Material Design 3** avec support du Dark Mode
- **Cards élevées** pour les réclamations
- **Badges colorés** :
  - 🟣 Violet (Primary) pour les réclamations Garage
  - 🟢 Vert (Secondary) pour les réclamations Service
- **Icons** :
  - 📝 Feedback pour la section réclamations
  - 🏪 Store pour les garages
  - 🔧 Build pour les services
  - ⏰ Schedule pour les dates
  - ✏️ Edit pour la modification
  - 🗑️ Delete pour la suppression

### Feedback utilisateur :
- ✅ Indicateurs de chargement (CircularProgressIndicator)
- ✅ Messages d'erreur dans des AlertDialogs
- ✅ États vides avec messages informatifs
- ✅ Confirmation avant suppression
- ✅ Navigation automatique après création/modification

## 📊 Flux de données

```
User Action
    ↓
UI Screen (Composable)
    ↓
ViewModel (ReclamationViewModel)
    ↓
Repository (ReclamationRepository)
    ↓
API Service (KarhebtiApiService)
    ↓
Backend REST API
    ↓
Response (Success/Error)
    ↓
Resource<T> wrapper
    ↓
LiveData/StateFlow update
    ↓
UI recomposition
```

## 🔒 Sécurité

- ✅ Toutes les routes protégées par JWT (gérées par le backend)
- ✅ L'utilisateur ne peut voir/modifier que ses propres réclamations
- ✅ Token d'authentification géré automatiquement par RetrofitClient
- ✅ Validation des données côté client et serveur

## 📱 Fonctionnalités

### Utilisateur peut :
1. ✅ Voir toutes ses réclamations
2. ✅ Créer une réclamation sur un garage
3. ✅ Créer une réclamation sur un service
4. ✅ Voir les détails d'une réclamation
5. ✅ Modifier une réclamation existante
6. ✅ Supprimer une réclamation
7. ✅ Naviguer facilement entre les écrans

### Filtrage et organisation :
- ✅ Réclamations triées par date de création
- ✅ Distinction visuelle entre types (Garage/Service)
- ✅ Affichage du garage/service concerné

## 🧪 Gestion des erreurs

- ✅ Erreurs réseau capturées et affichées
- ✅ Messages d'erreur traduits en français
- ✅ Logs détaillés dans Logcat pour le debug
- ✅ Boutons "Réessayer" sur les erreurs
- ✅ Validation des champs avant soumission

## 📝 Notes techniques

### Backend requis :
- API NestJS avec module `reclamations`
- Schema Mongoose avec références User, Garage, Service
- Routes protégées par JWT
- CRUD complet implémenté

### Format des données :
- Dates en ISO 8601 (converties automatiquement par Gson)
- IDs MongoDB ObjectId (format String)
- Type enum: "service" | "garage"

## 🚀 Prochaines étapes possibles

- [ ] Ajouter des notifications pour les nouvelles réclamations
- [ ] Implémenter un système de statut (En attente, En cours, Résolue)
- [ ] Ajouter des filtres (par type, par date)
- [ ] Implémenter la recherche de réclamations
- [ ] Ajouter des photos/pièces jointes aux réclamations
- [ ] Permettre les réponses du garage aux réclamations
- [ ] Système de notation après résolution

## ✨ Résumé

L'entité Réclamations (Feedback) est maintenant **complètement implémentée** avec :
- 📡 8 endpoints API
- 💾 8 méthodes repository
- 🎯 1 ViewModel complet
- 📱 4 écrans UI fonctionnels
- 🧭 Navigation complète
- 🎨 Design Material 3 cohérent

L'utilisateur peut maintenant soumettre, consulter, modifier et supprimer des réclamations sur les garages et services ! 🎉

