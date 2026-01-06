# 🚨 ERREUR 500 - Backend Internal Server Error

## 🔴 Problème Détecté

```
Response code: 500
Error body: {"statusCode":500,"message":"Internal server error"}
```

**L'erreur vient du BACKEND, pas de l'application Android !**

---

## 🔍 Diagnostic

L'application Android fonctionne correctement :
- ✅ La requête est envoyée
- ✅ Le backend répond
- ❌ Le backend renvoie une erreur 500

**Cause probable :** Problème dans le code backend lors du traitement de la requête `/documents/{id}`

---

## 🛠️ Solutions à Tester

### 1️⃣ Vérifier les logs du backend

**Action :** Ouvrez les logs du serveur Node.js/NestJS

```bash
# Si vous utilisez PM2
pm2 logs

# Si vous lancez directement
# Regardez la console où le backend tourne
```

**À chercher dans les logs :**
- Stack trace de l'erreur
- Message d'erreur spécifique
- Ligne de code qui cause l'erreur

### 2️⃣ Vérifier la base de données

**Problèmes courants :**

#### A. MongoDB n'est pas démarré
```bash
# Vérifier si MongoDB tourne
sudo systemctl status mongodb
# ou
mongosh
```

**Solution :**
```bash
sudo systemctl start mongodb
```

#### B. Problème de connexion MongoDB
Vérifiez dans votre backend :
```javascript
// Exemple NestJS/Node.js
mongoose.connect('mongodb://192.168.1.190:27017/karhebti')
```

**Tester la connexion :**
```bash
mongosh mongodb://192.168.1.190:27017/karhebti
```

#### C. Document avec structure invalide
Un document dans la BDD pourrait avoir une structure corrompue.

**Vérifier :**
```javascript
// Dans MongoDB shell
use karhebti
db.documents.findOne({_id: ObjectId("DOCUMENT_ID")})
```

### 3️⃣ Problèmes Backend Courants

#### A. Population des références échoue

**Problème :** Le backend essaie de "populate" un champ (ex: `voiture`) mais la référence est invalide.

**Code backend problématique (exemple) :**
```javascript
// ❌ Peut causer erreur 500 si voiture n'existe pas
const document = await Document.findById(id).populate('voiture');
```

**Solution :**
```javascript
// ✅ Gérer les erreurs
try {
  const document = await Document.findById(id).populate('voiture');
  if (!document) {
    throw new NotFoundException('Document not found');
  }
  return document;
} catch (error) {
  console.error('Error fetching document:', error);
  throw new InternalServerErrorException(error.message);
}
```

#### B. Champ date mal formaté

**Problème :** Les dates `dateEmission` ou `dateExpiration` sont corrompues.

**Solution backend :**
```javascript
// Ajouter une validation
if (!document.dateEmission || !document.dateExpiration) {
  throw new BadRequestException('Invalid date format');
}
```

#### C. Token JWT mal configuré

**Problème :** Le middleware d'authentification cause une erreur.

**Vérifier :**
```javascript
// Backend JWT middleware
app.use(authenticateJWT);
```

### 4️⃣ Tester l'API directement

**Testez avec curl pour isoler le problème :**

```bash
# Remplacer YOUR_TOKEN et DOCUMENT_ID
curl -v \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://172.16.8.131:3000/documents/DOCUMENT_ID
```

**Résultat attendu :**
- Si erreur 500 → Problème backend (voir logs)
- Si succès → Problème Android (peu probable ici)

---

## 🔧 Corrections Backend Recommandées

### Solution 1 : Ajouter un try-catch global

**Fichier : `documents.controller.ts` (NestJS) ou équivalent**

```typescript
@Get(':id')
async getDocumentById(@Param('id') id: string) {
  try {
    console.log(`Fetching document with ID: ${id}`);
    
    // Vérifier que l'ID est valide
    if (!mongoose.Types.ObjectId.isValid(id)) {
      throw new BadRequestException('Invalid document ID format');
    }
    
    const document = await this.documentsService.findById(id);
    
    if (!document) {
      throw new NotFoundException(`Document with ID ${id} not found`);
    }
    
    return document;
  } catch (error) {
    console.error('Error in getDocumentById:', error);
    
    if (error instanceof NotFoundException || error instanceof BadRequestException) {
      throw error;
    }
    
    throw new InternalServerErrorException({
      message: 'Failed to fetch document',
      details: error.message
    });
  }
}
```

### Solution 2 : Vérifier le service documents

**Fichier : `documents.service.ts`**

```typescript
async findById(id: string): Promise<Document> {
  try {
    const document = await this.documentModel
      .findById(id)
      .populate('voiture', 'marque modele annee immatriculation') // Peupler seulement les champs nécessaires
      .exec();
    
    if (!document) {
      throw new NotFoundException('Document not found');
    }
    
    return document;
  } catch (error) {
    console.error('Error in documents.service.findById:', error);
    throw error;
  }
}
```

### Solution 3 : Valider les dates

```typescript
// Dans le model ou service
if (document.dateEmission instanceof Date && isNaN(document.dateEmission.getTime())) {
  console.error('Invalid dateEmission');
  // Remplacer par une date par défaut ou supprimer
}
```

---

## 📋 Checklist de Débogage

- [ ] Vérifier les logs du backend
- [ ] Vérifier que MongoDB est démarré
- [ ] Tester la connexion MongoDB
- [ ] Vérifier la structure du document dans la BDD
- [ ] Tester l'API avec curl/Postman
- [ ] Ajouter des try-catch dans le backend
- [ ] Valider le format de l'ID
- [ ] Vérifier les références/population
- [ ] Vérifier les dates
- [ ] Redémarrer le backend

---

## 🚀 Test Après Correction

### 1. Redémarrer le backend
```bash
# Si PM2
pm2 restart karhebti-backend

# Si node
# Ctrl+C puis relancer
npm start
```

### 2. Tester dans l'app Android
1. Relancer l'application
2. Aller dans Documents
3. Cliquer sur un document
4. Vérifier Logcat

**Logs attendus après correction :**
```
D/DocumentRepository: Response code: 200
D/DocumentRepository: Document retrieved successfully
```

---

## 💡 Prévention Future

### Ajouter un middleware de gestion d'erreur globale

**Backend (NestJS) :**
```typescript
@Catch()
export class AllExceptionsFilter implements ExceptionFilter {
  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse();
    
    console.error('Exception caught:', exception);
    
    const status = exception instanceof HttpException
      ? exception.getStatus()
      : 500;
    
    response.status(status).json({
      statusCode: status,
      message: exception instanceof Error ? exception.message : 'Internal server error',
      timestamp: new Date().toISOString()
    });
  }
}
```

### Activer les logs détaillés
```typescript
// main.ts
app.useLogger(app.get(Logger));
```

---

## 📞 Support

Si l'erreur persiste :

1. **Capturez les logs complets du backend**
2. **Vérifiez la structure du document dans MongoDB**
3. **Testez l'endpoint avec curl**
4. **Partagez :**
   - Les logs du backend
   - La structure du document problématique
   - Le code du controller/service

---

## 📝 Résumé

| Aspect | Status |
|--------|--------|
| Android App | ✅ Fonctionne correctement |
| Requête HTTP | ✅ Envoyée correctement |
| Backend | ❌ Erreur 500 |
| Solution | 🔧 Déboguer le backend |

**Prochaine étape :** Vérifier les logs du backend Node.js/NestJS

---

**Date :** 2025-12-15  
**Erreur :** 500 Internal Server Error  
**Action requise :** ⚠️ **DÉBOGUER LE BACKEND**

