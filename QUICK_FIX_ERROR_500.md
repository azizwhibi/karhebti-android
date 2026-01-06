# 🔧 TEST RAPIDE - Erreur 500 Backend

## ⚠️ L'ERREUR VIENT DU BACKEND !

```
❌ Response code: 500
❌ Error: {"statusCode":500,"message":"Internal server error"}
```

---

## 🚀 Actions Immédiates (5 minutes)

### 1️⃣ Vérifier les logs du backend

**Ouvrez la console où tourne votre backend :**

```bash
# Si vous utilisez PM2
pm2 logs

# Si vous utilisez nodemon/node
# Regardez la console
```

**Vous devriez voir l'erreur exacte et la stack trace.**

---

### 2️⃣ Vérifier MongoDB

```bash
# Est-ce que MongoDB tourne ?
sudo systemctl status mongodb

# Si arrêté, démarrez-le
sudo systemctl start mongodb

# Testez la connexion
mongosh mongodb://192.168.1.190:27017/karhebti
```

---

### 3️⃣ Tester l'API directement

```bash
# Remplacer YOUR_TOKEN et DOCUMENT_ID par les vraies valeurs
curl -v -H "Authorization: Bearer YOUR_TOKEN" \
  http://172.16.8.131:3000/documents/DOCUMENT_ID
```

**Si erreur 500 ici aussi → Problème backend confirmé**

---

## 🔍 Causes Fréquentes

### 1. Document avec référence cassée
```javascript
// Le document référence une voiture qui n'existe plus
{
  _id: "abc123",
  type: "assurance",
  voiture: "xyz789" // ← Cette voiture n'existe pas !
}
```

**Solution :** Corriger ou supprimer le document problématique

```javascript
// Dans MongoDB shell
use karhebti
db.documents.findOne({_id: ObjectId("DOCUMENT_ID")})
// Si voiture invalide :
db.documents.updateOne(
  {_id: ObjectId("DOCUMENT_ID")},
  {$unset: {voiture: ""}}
)
```

### 2. Backend essaie de populate une référence invalide

**Code problématique :**
```javascript
// ❌ Crash si voiture n'existe pas
const doc = await Document.findById(id).populate('voiture');
```

**Solution :**
```javascript
// ✅ Gérer l'erreur
const doc = await Document.findById(id)
  .populate({
    path: 'voiture',
    options: { strictPopulate: false }
  });
```

### 3. Dates corrompues

**Vérifier :**
```javascript
db.documents.find({
  $or: [
    {dateEmission: {$type: "string"}},
    {dateExpiration: {$type: "string"}}
  ]
})
```

---

## 🛠️ Fix Backend Express/NestJS

### Express.js

```javascript
// documents.routes.js
router.get('/:id', async (req, res) => {
  try {
    console.log('Fetching document:', req.params.id);
    
    const document = await Document.findById(req.params.id)
      .populate('voiture')
      .lean();
    
    if (!document) {
      return res.status(404).json({ error: 'Document not found' });
    }
    
    res.json(document);
  } catch (error) {
    console.error('Error fetching document:', error);
    res.status(500).json({ 
      error: 'Internal server error',
      message: error.message 
    });
  }
});
```

### NestJS

```typescript
// documents.controller.ts
@Get(':id')
async findOne(@Param('id') id: string) {
  try {
    console.log('Fetching document:', id);
    
    if (!mongoose.Types.ObjectId.isValid(id)) {
      throw new BadRequestException('Invalid ID');
    }
    
    const document = await this.documentsService.findOne(id);
    
    if (!document) {
      throw new NotFoundException('Document not found');
    }
    
    return document;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}

// documents.service.ts
async findOne(id: string) {
  return this.documentModel
    .findById(id)
    .populate('voiture')
    .exec();
}
```

---

## ✅ Vérification Après Fix

### 1. Redémarrer le backend
```bash
# PM2
pm2 restart all

# Node
Ctrl+C puis npm start
```

### 2. Tester avec curl
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://172.16.8.131:3000/documents/DOCUMENT_ID
```

**Résultat attendu :**
```json
{
  "_id": "...",
  "type": "assurance",
  "dateEmission": "2024-01-01T00:00:00.000Z",
  "dateExpiration": "2025-01-01T00:00:00.000Z",
  ...
}
```

### 3. Tester dans l'app Android

**Logs attendus :**
```
D/DocumentRepository: Response code: 200
D/DocumentRepository: Document retrieved successfully
```

---

## 🆘 Si Ça Ne Marche Toujours Pas

### Option 1 : Voir les logs complets
```bash
# Logs détaillés
pm2 logs --lines 100
```

### Option 2 : Vérifier tous les documents
```javascript
// MongoDB shell
use karhebti
db.documents.find().forEach(doc => {
  print("Checking doc:", doc._id);
  if (doc.voiture) {
    const car = db.voitures.findOne({_id: doc.voiture});
    if (!car) {
      print("❌ Invalid voiture reference:", doc.voiture);
    }
  }
});
```

### Option 3 : Mode debug backend
```javascript
// Ajouter plus de logs
console.log('Request ID:', req.params.id);
console.log('Found document:', document);
console.log('Populated voiture:', document?.voiture);
```

---

## 📌 Checklist Rapide

- [ ] Logs backend vérifiés
- [ ] MongoDB démarré
- [ ] Testé avec curl
- [ ] Try-catch ajouté
- [ ] Backend redémarré
- [ ] App Android retestée

---

## 💡 Astuce

**Pour éviter ce problème à l'avenir :**

1. Toujours ajouter des try-catch
2. Valider les références avant populate
3. Logger toutes les erreurs
4. Utiliser `lean()` pour de meilleures performances
5. Gérer les références nulles

---

**Action Immédiate :** 🔴 **Vérifier les logs du backend Node.js maintenant !**

