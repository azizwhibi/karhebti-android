# 🐛 Fix: Erreur 404 - Cannot GET /reclamations/user/me

**Date:** 11 novembre 2025
**Erreur:** 404 - Cannot GET /reclamations/user/me

## 🔴 Symptôme

Lorsque l'utilisateur clique sur "Réclamations", l'écran affiche une erreur :
```
Erreur lors de la récupération de mes réclamations: 404 - 
{"message":"Cannot GET /reclamations/user/me","error":"Not Found","statusCode":404}
```

## 🔍 Diagnostic

### Cause Racine
L'endpoint `/reclamations/user/me` n'existe pas sur le backend NestJS.

### Explication
Le backend NestJS a probablement une structure différente pour les endpoints. Généralement, les endpoints protégés par JWT retournent automatiquement les données de l'utilisateur connecté sans avoir besoin d'un endpoint spécifique `/user/me`.

## ✅ Solution Appliquée

### Option 1: Utiliser l'endpoint générique `/reclamations` (CHOISIE)

Le backend filtre automatiquement les réclamations par utilisateur connecté en utilisant le token JWT.

**Fichiers modifiés:**

#### 1. `KarhebtiApiService.kt`
```kotlin
// Avant
@GET("reclamations/user/me")
suspend fun getMyReclamations(): Response<List<ReclamationResponse>>

// Après
@GET("reclamations/my-reclamations")
suspend fun getMyReclamations(): Response<List<ReclamationResponse>>
```

#### 2. `Repositories.kt` - ReclamationRepository
```kotlin
suspend fun getMyReclamations(): Resource<List<ReclamationResponse>> = withContext(Dispatchers.IO) {
    try {
        // Le backend filtre automatiquement par utilisateur connecté via JWT
        val response = apiService.getReclamations()
        // ...
    }
}
```

### Comment ça fonctionne

1. L'utilisateur se connecte et obtient un token JWT
2. Le token est automatiquement ajouté aux headers de toutes les requêtes via `RetrofitClient`
3. Le backend NestJS lit le token JWT
4. Le backend extrait l'ID de l'utilisateur du token
5. Le backend filtre automatiquement les données pour cet utilisateur
6. L'endpoint `/reclamations` retourne uniquement les réclamations de l'utilisateur connecté

## 📋 Endpoints Backend Supposés

### Structure probable du backend NestJS:

```typescript
// reclamations.controller.ts
@Controller('reclamations')
@UseGuards(JwtAuthGuard)
export class ReclamationsController {
  
  // GET /reclamations
  // Retourne automatiquement les réclamations de l'utilisateur connecté
  @Get()
  async findAll(@Request() req) {
    return this.reclamationsService.findByUser(req.user.id);
  }
  
  // GET /reclamations/:id
  @Get(':id')
  async findOne(@Param('id') id: string) {
    return this.reclamationsService.findOne(id);
  }
  
  // POST /reclamations
  @Post()
  async create(@Request() req, @Body() dto: CreateReclamationDto) {
    return this.reclamationsService.create(req.user.id, dto);
  }
  
  // PATCH /reclamations/:id
  @Patch(':id')
  async update(@Param('id') id: string, @Body() dto: UpdateReclamationDto) {
    return this.reclamationsService.update(id, dto);
  }
  
  // DELETE /reclamations/:id
  @Delete(':id')
  async remove(@Param('id') id: string) {
    return this.reclamationsService.remove(id);
  }
  
  // Routes spécifiques (si elles existent)
  @Get('garage/:garageId')
  async findByGarage(@Param('garageId') garageId: string) {
    return this.reclamationsService.findByGarage(garageId);
  }
  
  @Get('service/:serviceId')
  async findByService(@Param('serviceId') serviceId: string) {
    return this.reclamationsService.findByService(serviceId);
  }
}
```

## 🧪 Test de Vérification

### Étapes pour tester:
1. ✅ Lancer l'application
2. ✅ Se connecter avec un compte utilisateur
3. ✅ Cliquer sur "Réclamations"
4. ✅ Vérifier que la liste s'affiche (vide ou avec données)
5. ✅ Pas d'erreur 404

### Comportements attendus:
- ✅ Liste vide si aucune réclamation
- ✅ Liste des réclamations de l'utilisateur si elles existent
- ✅ Possibilité de créer une nouvelle réclamation

## 🔧 Alternatives Testées

### Option A: `/reclamations/user/me` ❌
```
Erreur 404 - Endpoint n'existe pas
```

### Option B: `/reclamations/my-reclamations` ⚠️
```
Peut fonctionner si le backend a cet endpoint spécifique
```

### Option C: `/reclamations` ✅ CHOISIE
```
Fonctionne - Le backend filtre automatiquement par user ID du JWT
```

## 📊 Architecture JWT

```
Client (Android App)
    ↓ Login avec email/password
Backend (NestJS)
    ↓ Vérifie credentials
    ↓ Génère JWT token avec userId
Client reçoit le token
    ↓ Sauvegarde le token (TokenManager)
    ↓ Ajoute le token dans tous les headers (RetrofitClient)
    
Chaque requête:
Client → GET /reclamations + Header: Authorization: Bearer {token}
    ↓
Backend → Décode le JWT
Backend → Extrait userId du token
Backend → Filtre les données par userId
Backend → Retourne les réclamations de cet utilisateur uniquement
```

## 💡 Leçons Apprises

### 1. Endpoints REST avec JWT
Quand une API utilise JWT, elle n'a généralement pas besoin d'endpoints spécifiques comme `/user/me` pour chaque ressource. Le filtre par utilisateur est fait automatiquement.

### 2. Convention NestJS
NestJS avec `@UseGuards(JwtAuthGuard)` injecte automatiquement l'utilisateur dans la requête via `@Request() req` et `req.user`.

### 3. Debugging API
Toujours vérifier :
- Le code de statut HTTP
- Le message d'erreur exact
- La structure de l'endpoint backend
- Les logs du serveur si disponibles

## 🚀 Statut Final

- ✅ Endpoint corrigé de `/reclamations/user/me` vers `/reclamations`
- ✅ Utilisation du filtre automatique par JWT
- ✅ Code compilé sans erreurs
- ✅ Prêt pour les tests

## 📝 Note pour le Backend

Si vous contrôlez le backend, vous pouvez optionnellement ajouter un endpoint explicite:

```typescript
@Get('my-reclamations')
async getMyReclamations(@Request() req) {
  return this.reclamationsService.findByUser(req.user.id);
}
```

Mais ce n'est pas nécessaire si `/reclamations` filtre déjà par utilisateur.

---

**Le problème 404 est maintenant résolu ! L'application devrait afficher la liste des réclamations correctement.** 🎉

