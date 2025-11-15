# ✅ CORRECTION ERREUR 400 - Format du type corrigé

## 🔍 Cause identifiée

**Erreur** : "type must be one of the following val..."

Le backend attend le type en **minuscules AVEC ESPACES**, pas avec underscores !

## ✅ Correction appliquée

### **Avant (INCORRECT) :**
```kotlin
val typeStr = selectedType.lowercase().replace(" ", "_")
// Envoyait : "assurance" (OK) ou "carte_grise" (❌ INCORRECT)
```

### **Maintenant (CORRECT) :**
```kotlin
val typeStr = selectedType.lowercase()
// Envoie : "assurance" ✅
// Envoie : "carte grise" ✅
// Envoie : "contrôle technique" ✅
```

## 📊 Valeurs acceptées par le backend

Le backend attend EXACTEMENT :
- `assurance`
- `carte grise` (avec espace!)
- `contrôle technique` (avec espace!)
- `autre`

## 📱 L'APK est installé !

**Testez maintenant :**

1. **Ouvrir l'app**
2. **Aller à "Ajouter un Document"**
3. **Remplir :**
   - Type : **"Carte Grise"** (vous verrez comment c'est envoyé : "carte grise")
   - Véhicule : Sélectionner
   - Dates : Calendrier
4. **Cliquer "Enregistrer"**
5. ✅ **Le document devrait être créé sans erreur 400 !**

## 🔍 Logs pour vérifier

```bash
adb logcat -c
adb logcat | findstr "DocumentRepository"
```

**Vous devriez voir :**
```
D/AddDocumentScreen: CREATE - Type: carte grise
D/DocumentRepository: Type: carte grise
D/DocumentRepository: Response code: 201
D/DocumentRepository: Document created successfully
```

---

**L'erreur devrait être résolue ! Le type est maintenant envoyé correctement ! 🎉**

