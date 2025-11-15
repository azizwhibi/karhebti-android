# ✅ CORRECTION ERREUR 400 - Format de date simplifié

## 🔧 Problème résolu

**Erreur 400** lors de l'enregistrement du document.

**Cause probable :** Format de date ISO 8601 trop complexe (`yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`) non accepté par le backend.

## ✅ Solution appliquée

### **1. Format de date simplifié**

**Avant :**
```kotlin
val sdfIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
// Résultat : "2025-12-11T00:00:00.000Z"
```

**Maintenant :**
```kotlin
val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
// Résultat : "2025-12-11"
```

### **2. Logs de debugging ajoutés**

Pour diagnostiquer facilement les erreurs futures :

```kotlin
android.util.Log.d("AddDocumentScreen", "CREATE - Type: $typeStr")
android.util.Log.d("AddDocumentScreen", "CREATE - DateEmission: $dateEmissionStr")
android.util.Log.d("AddDocumentScreen", "CREATE - DateExpiration: $dateExpirationStr")
android.util.Log.d("AddDocumentScreen", "CREATE - Voiture: $selectedCarId")
android.util.Log.d("AddDocumentScreen", "CREATE - Fichier: ${selectedFilePath ?: "none"}")
```

**Dans le Repository :**
```kotlin
android.util.Log.d("DocumentRepository", "=== Creating document ===")
android.util.Log.d("DocumentRepository", "Type: ${request.type}")
android.util.Log.d("DocumentRepository", "DateEmission: ${request.dateEmission}")
android.util.Log.d("DocumentRepository", "DateExpiration: ${request.dateExpiration}")
android.util.Log.d("DocumentRepository", "Voiture: ${request.voiture}")
android.util.Log.e("DocumentRepository", "ERROR DETAILS: $errorMsg")
```

## 📱 Test maintenant

### **L'APK a été installé automatiquement**

1. Ouvrir l'app
2. Aller à "Ajouter un Document"
3. Remplir le formulaire :
   - Type : Assurance
   - Véhicule : Sélectionner
   - Date émission : Calendrier → 11/12/2025
   - Date expiration : Calendrier → 15/12/2025
4. Cliquer "Enregistrer"
5. ✅ **Le document devrait être créé sans erreur 400**

### **Voir les logs en temps réel :**

```bash
adb logcat | findstr "AddDocumentScreen DocumentRepository"
```

**Logs attendus :**
```
D/AddDocumentScreen: CREATE - Type: assurance
D/AddDocumentScreen: CREATE - DateEmission: 2025-12-11
D/AddDocumentScreen: CREATE - DateExpiration: 2025-12-15
D/AddDocumentScreen: CREATE - Voiture: 675...
D/DocumentRepository: === Creating document ===
D/DocumentRepository: Type: assurance
D/DocumentRepository: DateEmission: 2025-12-11
D/DocumentRepository: DateExpiration: 2025-12-15
D/DocumentRepository: Response code: 201
D/DocumentRepository: Document created successfully
```

## 🎯 Format de requête final

**Envoyé au backend :**
```json
{
  "type": "assurance",
  "dateEmission": "2025-12-11",
  "dateExpiration": "2025-12-15",
  "fichier": "",
  "voiture": "675e123..."
}
```

## ✅ Résultat

- ✅ Format de date simplifié (YYYY-MM-DD)
- ✅ Logs complets pour debugging
- ✅ Erreur 400 devrait être résolue
- ✅ Calendrier fonctionnel
- ✅ APK installé et prêt à tester

**Si l'erreur 400 persiste, les logs montreront exactement le message d'erreur du backend pour diagnostic précis.**

