# 🚀 Test Rapide - Détails Document

## ✅ Checklist de vérification

### 1. Backend
```bash
# Tester si le backend est accessible
curl http://172.16.8.131:3000/health
# ou
curl http://172.16.8.131:3000/api/health
```

### 2. Vérifier l'authentification
```bash
# Obtenir un token
curl -X POST http://172.16.8.131:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password"}'
```

### 3. Tester l'endpoint documents
```bash
# Liste des documents
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://172.16.8.131:3000/documents

# Document spécifique (remplacer DOCUMENT_ID)
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://172.16.8.131:3000/documents/DOCUMENT_ID
```

## 🔍 Logs à surveiller dans Logcat

Filtrez par ces tags :
```
DocumentDetailScreen
DocumentRepository
DocumentViewModel
```

Exemple de log normal :
```
D/DocumentDetailScreen: Loading document with ID: 674a5e8f1234567890abcdef
D/DocumentViewModel: getDocumentById called with ID: 674a5e8f1234567890abcdef
D/DocumentRepository: Fetching document with ID: 674a5e8f1234567890abcdef
D/DocumentRepository: Response code: 200
D/DocumentRepository: Document retrieved successfully
D/DocumentDetailScreen: Document loaded: assurance
```

Exemple de log avec erreur :
```
D/DocumentDetailScreen: Loading document with ID: 674a5e8f1234567890abcdef
D/DocumentViewModel: getDocumentById called with ID: 674a5e8f1234567890abcdef
D/DocumentRepository: Fetching document with ID: 674a5e8f1234567890abcdef
D/DocumentRepository: Response code: 404
E/DocumentRepository: Error body: {"error":"Document not found"}
D/DocumentDetailScreen: Error: Erreur 404: {"error":"Document not found"}
```

## 🛠️ Solutions rapides

### Erreur 404
- Le document n'existe pas dans la base de données
- Vérifiez l'ID dans MongoDB ou votre BDD

### Erreur 401
- Token expiré → Reconnectez-vous
- Token invalide → Vérifiez la configuration du backend

### Erreur 500
- Problème backend → Vérifiez les logs du serveur Node.js
- Base de données inaccessible → Vérifiez MongoDB

### Erreur réseau
- Backend non démarré → Lancez `npm start` ou `node server.js`
- Mauvaise IP → Vérifiez `ApiConfig.kt` ligne 22
- Pare-feu → Autorisez le port 3000

## 📱 Test dans l'application

1. Lancez l'application
2. Allez dans "Documents"
3. Cliquez sur un document
4. Si erreur → Cliquez sur "Réessayer"
5. Vérifiez Logcat pour voir les détails

## 🔄 Après modification du code

1. Recompilez : `./gradlew assembleDebug`
2. Installez : `adb install -r app/build/outputs/apk/debug/app-debug.apk`
3. Ou utilisez "Run" dans Android Studio

## 💡 Astuce

Pour voir les logs en temps réel :
```bash
adb logcat -s DocumentDetailScreen:D DocumentRepository:D DocumentViewModel:D
```

