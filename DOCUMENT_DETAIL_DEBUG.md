# 🔍 DIAGNOSTIC - Erreur récupération document

## ❌ Problème actuel

L'écran "Détails du Document" affiche : **"Erreur lors de la récupération du document"**

## ✅ Logs de debugging ajoutés

J'ai ajouté des logs détaillés pour diagnostiquer exactement où est le problème :

### **Dans DocumentRepository :**
```kotlin
android.util.Log.d("DocumentRepository", "=== Getting document by ID ===")
android.util.Log.d("DocumentRepository", "Document ID: $id")
android.util.Log.d("DocumentRepository", "Response code: ${response.code()}")
android.util.Log.e("DocumentRepository", "ERROR: $errorMsg")
```

### **Dans DocumentViewModel :**
```kotlin
android.util.Log.d("DocumentViewModel", "getDocumentById called with ID: $id")
android.util.Log.d("DocumentViewModel", "Fetching document from repository...")
android.util.Log.d("DocumentViewModel", "Result type: ${result::class.simpleName}")
```

### **Dans DocumentDetailScreen :**
```kotlin
android.util.Log.d("DocumentDetailScreen", "Loading document with ID: $documentId")
android.util.Log.d("DocumentDetailScreen", "Document loaded: ${state.data?.type}")
android.util.Log.e("DocumentDetailScreen", "Error: ${state.message}")
```

## 📱 L'APK est installé avec les logs !

### **Pour voir exactement l'erreur :**

1. **Ouvrir un terminal et lancer :**
```bash
adb logcat -c
adb logcat | findstr "DocumentDetailScreen DocumentViewModel DocumentRepository"
```

2. **Dans l'app :**
   - Aller à la liste des documents
   - Cliquer sur un document

3. **Observer les logs dans le terminal**

## 🔍 Logs attendus

### **Si le document se charge correctement :**
```
D/DocumentDetailScreen: Loading document with ID: 674abc...
D/DocumentViewModel: getDocumentById called with ID: 674abc...
D/DocumentViewModel: Fetching document from repository...
D/DocumentRepository: === Getting document by ID ===
D/DocumentRepository: Document ID: 674abc...
D/DocumentRepository: Response code: 200
D/DocumentRepository: Document retrieved successfully: assurance
D/DocumentViewModel: Result type: Success
D/DocumentDetailScreen: Document loaded: assurance
```

### **Si erreur 404 (document non trouvé) :**
```
D/DocumentRepository: Response code: 404
E/DocumentRepository: ERROR: Erreur 404: Document non trouvé
E/DocumentDetailScreen: Error: Erreur 404: Document non trouvé
```

### **Si erreur réseau :**
```
E/DocumentRepository: Erreur réseau: Failed to connect...
E/DocumentDetailScreen: Error: Erreur réseau: Failed to connect...
```

## 🎯 Prochaines étapes

Une fois que vous aurez les logs, on saura exactement :

1. **Si l'ID du document est correct**
2. **Quel code HTTP retourne le backend** (200, 404, 500, etc.)
3. **Le message d'erreur exact du serveur**

**Lancez les logs et testez, puis envoyez-moi ce qui s'affiche !** 🔍

