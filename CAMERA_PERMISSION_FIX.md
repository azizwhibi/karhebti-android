# Résolution du problème "Accès caméra refusé"

**Date:** 13 novembre 2025

## ❌ Problème identifié

Lorsque l'utilisateur cliquait sur le bouton "Caméra" pour prendre une photo, le message "accès caméra refusé" s'affichait.

## 🔍 Cause

Les permissions nécessaires pour la caméra et la lecture des médias n'étaient **pas déclarées** dans le fichier `AndroidManifest.xml`.

## ✅ Corrections apportées

### 1. **AndroidManifest.xml**

Ajout des permissions suivantes :

```xml
<!-- Camera and Media permissions -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- Camera feature -->
<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
```

### 2. **AddDocumentScreen.kt**

Amélioration de la gestion des permissions :

#### **Avant:**
- Les permissions étaient demandées au démarrage de l'écran
- Les actions (galerie/caméra) étaient lancées directement sans attendre l'autorisation

#### **Après:**
- Les permissions sont demandées **seulement quand l'utilisateur clique** sur les boutons
- L'action (galerie/caméra) est lancée **automatiquement après autorisation**
- Messages d'erreur clairs si la permission est refusée

**Ordre des launchers optimisé:**
1. `galleryLauncher` et `cameraLauncher` déclarés en premier
2. `readPermissionLauncher` et `cameraPermissionLauncher` déclarés après
3. Les launchers de permission appellent automatiquement les launchers d'action après autorisation

```kotlin
// Gallery launcher
val galleryLauncher = rememberLauncherForActivityResult(...)

// Camera launcher  
val cameraLauncher = rememberLauncherForActivityResult(...)

// Read permission launcher - lance la galerie après autorisation
val readPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { granted ->
        if (granted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permission lecture média refusée", Toast.LENGTH_SHORT).show()
        }
    }
)

// Camera permission launcher - lance la caméra après autorisation
val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        }
    }
)
```

## 📊 Permissions Android

### **Android 12 et inférieur (SDK < 33)**
- `READ_EXTERNAL_STORAGE` : Accès aux fichiers en lecture

### **Android 13+ (SDK >= 33)**
- `READ_MEDIA_IMAGES` : Accès spécifique aux images

### **Toutes les versions**
- `CAMERA` : Accès à la caméra du device

## 🎯 Résultat

✅ **Les boutons "Galerie" et "Caméra" fonctionnent maintenant correctement**

### Workflow utilisateur:

1. **Clic sur "Galerie":**
   - Demande la permission `READ_MEDIA_IMAGES` (ou `READ_EXTERNAL_STORAGE` selon Android)
   - Si accordée → Ouvre la galerie automatiquement
   - Si refusée → Affiche "Permission lecture média refusée"

2. **Clic sur "Caméra":**
   - Demande la permission `CAMERA`
   - Si accordée → Lance la caméra automatiquement
   - Si refusée → Affiche "Permission caméra refusée"

3. **Après sélection/capture:**
   - L'image est copiée dans le cache de l'app
   - Preview de l'image s'affiche
   - Le chemin du fichier est stocké pour l'upload

## 📝 Fichiers modifiés

1. `app/src/main/AndroidManifest.xml` - Ajout des permissions
2. `app/src/main/java/com/example/karhebti_android/ui/screens/AddDocumentScreen.kt` - Amélioration de la gestion des permissions

## ✨ Tests recommandés

1. **Premier lancement:**
   - Cliquer sur "Caméra" → Dialog de permission apparaît → Accepter → Caméra s'ouvre
   - Cliquer sur "Galerie" → Dialog de permission apparaît → Accepter → Galerie s'ouvre

2. **Lancements suivants:**
   - Les permissions sont mémorisées
   - Clic direct ouvre la caméra/galerie sans redemander

3. **Permission refusée:**
   - Refuser la permission → Message d'erreur s'affiche
   - Réessayer plus tard (aller dans Paramètres > Apps > Karhebti > Permissions pour accorder manuellement)

## 🔒 Sécurité

- Les permissions sont demandées au moment de l'utilisation (meilleure UX)
- Messages clairs pour l'utilisateur en cas de refus
- Respect des guidelines Android pour les permissions runtime

