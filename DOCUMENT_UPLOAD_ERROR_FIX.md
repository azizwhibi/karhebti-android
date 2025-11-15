# Résolution : Erreur lors de la création du document (upload)

**Date:** 13 novembre 2025

## ❌ Problème identifié

Lors de la création d'un document avec une image, l'erreur suivante apparaissait :
```
Erreur: Erreur lors de la création du document (upload)
```

## 🔍 Causes possibles

1. **Backend pas configuré pour multipart/form-data** ❌ (Cause principale)
2. Fichier non trouvé ou corrompu
3. Timeout réseau
4. Format de fichier non supporté
5. Taille de fichier trop grande

## ✅ Solution temporaire mise en place

### **Modification 1 : Désactivation de l'upload multipart**

En attendant que le backend soit configuré pour accepter les uploads multipart, le code utilise maintenant l'endpoint JSON normal :

```kotlin
// AVANT (ne fonctionnait pas)
val response = apiService.createDocumentMultipart(...)

// MAINTENANT (fonctionne)
val response = apiService.createDocument(request)
```

### **Modification 2 : Stockage local des images**

Les images sont maintenant stockées dans le dossier interne de l'app au lieu du cache temporaire :

```kotlin
// AVANT : Cache temporaire (supprimé au redémarrage)
val file = File(context.cacheDir, "doc_${timestamp}.jpg")

// MAINTENANT : Stockage permanent
val documentsDir = File(context.filesDir, "documents")
val file = File(documentsDir, "doc_${timestamp}.jpg")
```

**Chemin de stockage :** `/data/data/com.example.karhebti_android/files/documents/`

### **Modification 3 : Logs détaillés**

Ajout de logs pour déboguer facilement :

```kotlin
android.util.Log.d("DocumentRepository", "Creating document - Type: ${request.type}")
android.util.Log.d("DocumentRepository", "FilePath: $filePath")
android.util.Log.d("DocumentRepository", "Response code: ${response.code()}")
```

## 🎯 Résultat actuel

✅ **La création de documents fonctionne maintenant**

### Workflow actuel :

1. L'utilisateur remplit le formulaire (Type, Véhicule, Dates)
2. L'utilisateur sélectionne une image (Galerie ou Caméra)
3. L'image est **copiée dans le stockage interne de l'app**
4. Les **métadonnées** sont envoyées au backend (sans l'image)
5. Le document est créé avec succès
6. L'image reste disponible localement

### Limitations temporaires :

⚠️ **L'image n'est PAS encore uploadée sur le serveur**
- L'image est stockée uniquement sur l'appareil
- Le backend reçoit le chemin du fichier (qui pointe vers l'appareil local)
- Fonctionnel pour l'instant, mais pas idéal pour le long terme

## 🔧 Configuration backend nécessaire (TODO)

Pour activer l'upload complet des images, le backend doit être configuré :

### 1. **Installation de Multer (NestJS)**

```bash
npm install --save @nestjs/platform-express multer
npm install --save-dev @types/multer
```

### 2. **Configuration multer.config.ts**

```typescript
import { diskStorage } from 'multer';
import { extname } from 'path';

export const multerConfig = {
  storage: diskStorage({
    destination: './uploads/documents',
    filename: (req, file, callback) => {
      const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
      const ext = extname(file.originalname);
      callback(null, `doc-${uniqueSuffix}${ext}`);
    },
  }),
  limits: {
    fileSize: 5 * 1024 * 1024, // 5 MB
  },
  fileFilter: (req, file, callback) => {
    if (!file.mimetype.match(/\/(jpg|jpeg|png|gif|pdf)$/)) {
      return callback(new Error('Format non supporté'), false);
    }
    callback(null, true);
  },
};
```

### 3. **Modification du contrôleur Documents**

```typescript
import { 
  Controller, 
  Post, 
  Patch, 
  Body, 
  Param,
  UseInterceptors,
  UploadedFile 
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { multerConfig } from './multer.config';

@Controller('documents')
export class DocumentsController {
  
  @Post()
  @UseInterceptors(FileInterceptor('fichier', multerConfig))
  async create(
    @Body() createDocumentDto: CreateDocumentDto,
    @UploadedFile() file: Express.Multer.File,
  ) {
    const fichierUrl = file 
      ? `${process.env.BASE_URL}/uploads/documents/${file.filename}`
      : createDocumentDto.fichier;
      
    return this.documentsService.create({
      ...createDocumentDto,
      fichier: fichierUrl,
    });
  }
  
  @Patch(':id')
  @UseInterceptors(FileInterceptor('fichier', multerConfig))
  async update(
    @Param('id') id: string,
    @Body() updateDocumentDto: UpdateDocumentDto,
    @UploadedFile() file: Express.Multer.File,
  ) {
    if (file) {
      updateDocumentDto.fichier = 
        `${process.env.BASE_URL}/uploads/documents/${file.filename}`;
    }
    
    return this.documentsService.update(id, updateDocumentDto);
  }
}
```

### 4. **Configuration main.ts pour servir les fichiers statiques**

```typescript
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { NestExpressApplication } from '@nestjs/platform-express';
import { join } from 'path';

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(AppModule);
  
  // Servir les fichiers uploadés
  app.useStaticAssets(join(__dirname, '..', 'uploads'), {
    prefix: '/uploads',
  });
  
  await app.listen(3000);
}
bootstrap();
```

### 5. **Créer le dossier uploads**

```bash
mkdir -p uploads/documents
```

### 6. **.gitignore**

```
uploads/*
!uploads/.gitkeep
```

## 🔄 Quand le backend sera prêt

Une fois le backend configuré, il suffira de décommenter le code multipart dans le repository :

```kotlin
// Dans DocumentRepository.createDocument()

// Décommenter cette section :
/*
val file = File(filePath)
val mediaType = when (file.extension.lowercase()) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "gif" -> "image/gif"
    "pdf" -> "application/pdf"
    else -> "application/octet-stream"
}.toMediaTypeOrNull()

val requestFile = file.asRequestBody(mediaType)
val multipartBody = MultipartBody.Part.createFormData("fichier", file.name, requestFile)

val typeRB = request.type.toRequestBody("text/plain".toMediaTypeOrNull())
val dateEmissionRB = request.dateEmission.toRequestBody("text/plain".toMediaTypeOrNull())
val dateExpirationRB = request.dateExpiration.toRequestBody("text/plain".toMediaTypeOrNull())
val voitureRB = request.voiture.toRequestBody("text/plain".toMediaTypeOrNull())

val response = apiService.createDocumentMultipart(
    typeRB, dateEmissionRB, dateExpirationRB, voitureRB, multipartBody
)
*/
```

## 📊 Test avec Postman

Pour tester l'endpoint multipart avec Postman :

1. **Méthode :** POST
2. **URL :** `http://localhost:3000/documents`
3. **Headers :**
   ```
   Authorization: Bearer YOUR_TOKEN
   ```
4. **Body :** form-data
   ```
   type: assurance
   dateEmission: 2025-12-10
   dateExpiration: 2025-12-14
   voiture: 6756e123456789abcdef0123
   fichier: [Sélectionner un fichier image]
   ```

## 📝 Fichiers modifiés

1. **Repositories.kt**
   - Désactivation temporaire de l'upload multipart
   - Ajout de logs détaillés
   - Amélioration de la gestion d'erreur

2. **AddDocumentScreen.kt**
   - Stockage des images dans `/files/documents/` au lieu du cache
   - Logs pour le debugging

## ✅ Prochaines étapes

1. ✅ ~~Création de documents fonctionne (sans upload d'images)~~
2. 🔲 Configurer le backend pour accepter multipart/form-data
3. 🔲 Activer l'upload multipart côté Android
4. 🔲 Tester l'upload complet d'images
5. 🔲 Ajouter la visualisation des images uploadées

## 🎉 Conclusion

**Le problème est résolu pour l'instant** avec une solution temporaire. Les documents peuvent être créés avec succès, et les images sont stockées localement. Une fois le backend configuré, l'upload complet sera activé simplement en décommentant quelques lignes de code.

