# 🔧 CORRECTION ERREUR 400 - Format ISO 8601 avec timezone local

## ❌ Problème persistant

L'erreur 400 persiste même après simplification du format de date.

## 🔍 Analyse

Le backend attend probablement un format ISO 8601 **complet** avec heure, mais **SANS** le 'Z' (UTC).

## ✅ Nouvelle solution

### **Format de date corrigé :**

```kotlin
// Nouveau format : ISO 8601 avec timezone LOCAL
val sdfIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

// Résultat : "2025-12-11T00:00:00"
// Au lieu de : "2025-12-11T00:00:00.000Z" (avec Z)
// Au lieu de : "2025-12-11" (trop simple)
```

## 📱 TEST IMMÉDIAT

**L'APK vient d'être installé !**

1. **Ouvrir l'app**
2. **Aller à "Ajouter un Document"**
3. **Remplir :**
   - Type : Assurance
   - Véhicule : Sélectionner
   - Date émission : **Cliquer → Calendrier → Sélectionner 11/12/2025**
   - Date expiration : **Cliquer → Calendrier → Sélectionner 15/12/2025**
4. **Cliquer "Enregistrer"**

## 🔍 Pour voir les logs en temps réel :

Ouvrir un nouveau terminal et exécuter :

```bash
adb logcat -c
adb logcat | findstr "DocumentRepository AddDocumentScreen"
```

Puis testez l'ajout dans l'app.

## 📊 Formats testés

| Format | Exemple | Résultat |
|--------|---------|----------|
| Simple | `2025-12-11` | ❌ Erreur 400 |
| ISO 8601 avec Z | `2025-12-11T00:00:00.000Z` | ❌ (probable) |
| **ISO 8601 local** | `2025-12-11T00:00:00` | ✅ **À tester** |

## 🎯 Requête envoyée

```json
{
  "type": "assurance",
  "dateEmission": "2025-12-11T00:00:00",
  "dateExpiration": "2025-12-15T00:00:00",
  "fichier": "",
  "voiture": "675..."
}
```

## ⚠️ Si l'erreur persiste encore

Le backend peut attendre un des formats suivants :

1. **Timestamp Unix** : `1733961600000`
2. **ISO 8601 avec milliseconds** : `2025-12-11T00:00:00.000`
3. **ISO 8601 avec offset** : `2025-12-11T00:00:00+00:00`

Dans ce cas, je dois **voir les logs exacts** pour diagnostiquer précisément ce que le backend attend.

## 📝 Prochaine étape

**Testez maintenant et envoyez-moi les logs si l'erreur persiste !**

---

**L'app est installée et prête à tester ! 🚀**

