# 🔧 Correction du bouton d'appel - Écran Suivi SOS

## Date: 14 décembre 2025

## 🎯 Problème identifié

Sur l'écran "Suivi SOS", **les deux types d'utilisateurs voient le même texte** sur le bouton d'appel :
- ❌ **AVANT** : Tous voient "Appeler le client"

**Problème** : C'est incorrect car :
- Le **garagiste (propGarage)** doit voir : "Appeler le client"
- Le **client normal (user)** doit voir : "Appeler l'assistant" ou "Appeler le garage"

---

## ✅ Solution implémentée

### 1. Modification de BreakdownTrackingScreen.kt

#### A. Ajout du paramètre `userRole`

**BreakdownTrackingScreenWrapper** (ligne ~40) :
```kotlin
@Composable
fun BreakdownTrackingScreenWrapper(
    breakdownId: String,
    userRole: String = "user",  // ✅ AJOUTÉ
    onBackClick: () -> Unit = {}
)
```

**BreakdownTrackingScreen** (ligne ~127) :
```kotlin
@Composable
fun BreakdownTrackingScreen(
    breakdown: BreakdownResponse,
    userRole: String = "user",  // ✅ AJOUTÉ
    modifier: Modifier = Modifier
)
```

#### B. Logique conditionnelle pour le bouton (lignes ~242-275)

```kotlin
// Déterminer le texte et l'action selon le rôle
val isGarageOwner = userRole == "propGarage"
val buttonText = if (isGarageOwner) 
    "Appeler le client" 
else 
    "Appeler l'assistant"
    
val buttonSubtext = if (isGarageOwner) 
    "Contacter pour plus d'informations" 
else 
    "Contacter le garage pour assistance"
```

### 2. Modification de NavGraph.kt

**Récupération et passage du rôle** (ligne ~582) :
```kotlin
composable(Screen.BreakdownTracking.route) { backStackEntry ->
    val context = LocalContext.current
    val breakdownId = backStackEntry.arguments?.getString("breakdownId")
    requireNotNull(breakdownId) { "breakdownId parameter wasn't found!" }
    
    // ✅ Récupération du rôle utilisateur
    val userRole = TokenManager.getInstance(context).getUser()?.role ?: "user"

    BreakdownTrackingScreenWrapper(
        breakdownId = breakdownId,
        userRole = userRole,  // ✅ Passage du rôle
        onBackClick = { navController.popBackStack() }
    )
}
```

---

## 📱 Résultat attendu

### Pour un garagiste (propGarage)
```
┌─────────────────────────────────────┐
│  📞 Appeler le client               │
│     Contacter pour plus             │
│     d'informations                  │
└─────────────────────────────────────┘
```

### Pour un client normal (user)
```
┌─────────────────────────────────────┐
│  📞 Appeler l'assistant             │
│     Contacter le garage pour        │
│     assistance                      │
└─────────────────────────────────────┘
```

---

## 🧪 Tests à effectuer

### Test 1 : Garagiste
1. Se connecter avec un compte **propGarage**
2. Accepter une demande SOS
3. Aller sur l'écran "Suivi SOS"
4. **Vérifier** : Le bouton affiche "Appeler le client"

### Test 2 : Client normal
1. Se connecter avec un compte **user**
2. Créer une demande SOS
3. Attendre qu'elle soit acceptée
4. Aller sur l'écran "Suivi SOS"
5. **Vérifier** : Le bouton affiche "Appeler l'assistant"

---

## 📊 Fichiers modifiés

| Fichier | Lignes modifiées | Changement |
|---------|------------------|------------|
| `BreakdownTrackingScreen.kt` | ~40 | Ajout paramètre `userRole` au wrapper |
| `BreakdownTrackingScreen.kt` | ~102 | Passage du `userRole` |
| `BreakdownTrackingScreen.kt` | ~127 | Ajout paramètre `userRole` à l'écran |
| `BreakdownTrackingScreen.kt` | ~242-275 | Logique conditionnelle du bouton |
| `NavGraph.kt` | ~582-592 | Récupération et passage du `userRole` |

---

## 🔍 Détails techniques

### Rôles utilisateur
- `"propGarage"` → Propriétaire de garage
- `"user"` → Client normal
- `"admin"` → Administrateur (traité comme user pour SOS)

### TokenManager
Le rôle est récupéré via :
```kotlin
TokenManager.getInstance(context).getUser()?.role ?: "user"
```

### Logique conditionnelle
```kotlin
val isGarageOwner = userRole == "propGarage"
```

---

## ⚠️ TODO futur

### Récupération du numéro de téléphone réel
Actuellement, le bouton utilise un numéro fictif :
```kotlin
val phoneNumber = "tel:+216" // Numéro fictif
```

**À implémenter** :
1. Ajouter `clientPhoneNumber` dans `BreakdownResponse`
2. Ajouter `garagePhoneNumber` dans `BreakdownResponse`
3. Modifier la logique :
```kotlin
val phoneNumber = if (isGarageOwner) {
    breakdown.clientPhoneNumber ?: "tel:+216"
} else {
    breakdown.garagePhoneNumber ?: "tel:+216"
}
```

### Backend requis
L'API doit retourner :
- `clientPhoneNumber` : Numéro du client qui a créé la demande SOS
- `garagePhoneNumber` : Numéro du garage qui a accepté (assignedTo)

---

## 📈 Statistiques

- **Problèmes résolus** : 1
- **Fichiers modifiés** : 2
- **Lignes ajoutées** : ~15
- **Erreurs de compilation** : 0 (liées à nos changements)
- **Warnings** : Quelques warnings mineurs non bloquants

---

## ✅ Checklist de validation

- [x] Paramètre `userRole` ajouté à `BreakdownTrackingScreenWrapper`
- [x] Paramètre `userRole` ajouté à `BreakdownTrackingScreen`
- [x] Passage du `userRole` dans le wrapper
- [x] Récupération du rôle dans `NavGraph`
- [x] Logique conditionnelle pour le texte du bouton
- [x] Texte différent selon le rôle
- [x] Sous-texte différent selon le rôle
- [x] Aucune erreur de compilation

---

## 🎯 Impact

### Expérience utilisateur
✅ Le texte est maintenant adapté au rôle de l'utilisateur
✅ Plus de confusion pour les clients normaux
✅ Interface plus intuitive

### Sécurité
✅ Respect des rôles utilisateur
✅ Séparation des responsabilités

### Maintenabilité
✅ Code modulaire et réutilisable
✅ Facile d'ajouter d'autres rôles si nécessaire

---

## 📝 Résumé visuel

```
AVANT (pour tous les utilisateurs) ❌
┌─────────────────────────────────────┐
│  📞 Appeler le client               │
└─────────────────────────────────────┘

APRÈS (pour garagiste) ✅
┌─────────────────────────────────────┐
│  📞 Appeler le client               │
│     Contacter pour plus             │
│     d'informations                  │
└─────────────────────────────────────┘

APRÈS (pour client) ✅
┌─────────────────────────────────────┐
│  📞 Appeler l'assistant             │
│     Contacter le garage pour        │
│     assistance                      │
└─────────────────────────────────────┘
```
