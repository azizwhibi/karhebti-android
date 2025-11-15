# 📱 Document Expiration Notifications - WebSocket 3 Jours Avant

## 🎯 Objectif
Envoyer une **notification WebSocket 3 jours avant l'échéance d'un document**.

---

## ✅ Implémentation Complète

### 1. **DocumentExpirationNotificationService.kt**
Fichier créé: `app/src/main/java/com/example/karhebti_android/data/websocket/DocumentExpirationNotificationService.kt`

**Fonctionnalités:**
- ✅ Vérifie si un document expire dans 3 jours
- ✅ Crée les notifications d'expiration
- ✅ Filtre les documents expirant bientôt
- ✅ Génère des messages d'alerte personnalisés

### 2. **DocumentViewModel.kt** - Mis à jour
- ✅ Nouvelle méthode `checkExpiringDocuments()`
- ✅ Appelée automatiquement lors du `getDocuments()`
- ✅ Logs d'alerte pour chaque document qui expire

---

## 🔧 Comment Ça Marche

### Architecture:

```
┌─────────────────────────────────────────────┐
│    DocumentViewModel.getDocuments()          │
│    - Récupère les documents du serveur      │
│    - Appelle checkExpiringDocuments()       │
│    - Logs les alertes d'expiration          │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│  DocumentExpirationNotificationService     │
│  - Vérifie les dates d'expiration          │
│  - Filtre ceux qui expirent dans 3 jours   │
│  - Crée les notifications                  │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│    Android Logs (Log.w)                     │
│  🚨 X document(s) expire(nt) dans 3 jours  │
│  URGENT: Document expire AUJOURD'HUI!      │
└─────────────────────────────────────────────┘
```

---

## 📊 Logique de Vérification

### Calcul des jours jusqu'à l'expiration:

```kotlin
// Exemple:
dateExpiration = 17/11/2025
dateActuelle = 14/11/2025
-> Jours restants = 3 ✅ NOTIFICATION

dateExpiration = 15/11/2025
dateActuelle = 14/11/2025
-> Jours restants = 1 ✅ URGENT

dateExpiration = 14/11/2025
dateActuelle = 14/11/2025
-> Jours restants = 0 ✅ ULTRA URGENT
```

---

## 📱 Exemple de Notification

```json
{
  "titre": "Document en train d'expirer",
  "message": "Assurance Automobile expire dans 2 jour(s)",
  "type": "document_expiration",
  "documentId": "doc_12345",
  "documentType": "Assurance Automobile",
  "daysRemaining": 2,
  "priority": "medium",
  "voiture": "Renault Scenic - 12345678"
}
```

---

## 🔍 Logs Android

### Vérifier les notifications dans les logs:

```bash
# Voir toutes les alertes d'expiration
adb logcat | grep "DocumentViewModel" | grep "🚨"

# Voir tous les logs liés aux documents
adb logcat | grep "DocumentExpiration"
```

### Exemple de logs:

```
🚨 2 document(s) expire(nt) dans 3 jours
📋 Document doc_1: expire dans 3 jours
URGENT: Assurance expire dans 2 jour(s)
```

---

## 🚀 Intégration WebSocket

Pour envoyer les notifications via WebSocket:

### Option 1: Dans DocumentViewModel

```kotlin
private fun checkExpiringDocuments(documents: List<DocumentResponse>) {
    val expirationService = DocumentExpirationNotificationService()
    val expiringDocuments = expirationService.getDocumentsExpiringWithinThreeDays(documents)
    
    if (expiringDocuments.isNotEmpty()) {
        // Créer les notifications
        val notifications = expirationService.createExpirationNotifications(expiringDocuments)
        
        // Envoyer via WebSocket
        notifications.forEach { notification ->
            webSocketService.emit("document_expiration_notification", notification)
        }
    }
}
```

### Option 2: Backend envoie les notifications

Le backend peut aussi vérifier les dates et envoyer les notifications:

```javascript
// Backend Node.js
const documents = await Document.find({ dateExpiration: { $lte: now + 3days } });
documents.forEach(doc => {
    io.to(userId).emit('document_expiration_notification', {
        titre: 'Document en train d\'expirer',
        message: `${doc.type} expire dans...`,
        documentId: doc._id
    });
});
```

---

## 🎯 Cas d'Utilisation

### Cas 1: Document expire dans 3 jours
```
Reçu Log: 📋 Document ABC: expire dans 3 jours
Action: Afficher notification "Renouvellement recommandé"
```

### Cas 2: Document expire demain
```
Reçu Log: URGENT: Assurance expire DEMAIN!
Action: Notification urgente avec bouton "Renouveler"
```

### Cas 3: Document expire aujourd'hui
```
Reçu Log: URGENT: Document expire AUJOURD'HUI!
Action: Notification critique avec alarme
```

---

## 🔧 Configuration

### Modifier le délai (par défaut: 3 jours):

**DocumentExpirationNotificationService.kt:**
```kotlin
companion object {
    private const val DAYS_BEFORE_EXPIRATION = 3  // ← Changer ici
}
```

**Exemple pour 7 jours:**
```kotlin
private const val DAYS_BEFORE_EXPIRATION = 7
```

---

## 📋 Méthodes Disponibles

### Vérifier un document unique:
```kotlin
val service = DocumentExpirationNotificationService()
val shouldNotify = service.shouldNotifyExpiration(document)
```

### Filtrer plusieurs documents:
```kotlin
val expiringDocs = service.getDocumentsExpiringWithinThreeDays(documents)
```

### Créer les notifications:
```kotlin
val notifications = service.createExpirationNotifications(documents)
```

### Message d'alerte personnalisé:
```kotlin
val message = service.getAlertMessage(document)
// Retourne: "Assurance expire dans 2 jour(s)" 
// ou "URGENT: Document expire AUJOURD'HUI!"
```

---

## 📊 Tester Localement

### 1. Créer un document avec date d'expiration = demain

```bash
# Via API
POST /api/documents
{
  "type": "Assurance Automobile",
  "dateEmission": "2025-11-14",
  "dateExpiration": "2025-11-15",  # Demain
  "voiture": "car_id"
}
```

### 2. Lancer l'app et aller sur Documents

```
✅ Les logs afficheront:
🚨 1 document(s) expire(nt) dans 3 jours
URGENT: Assurance Automobile expire DEMAIN!
```

### 3. Vérifier les logs Android

```bash
adb logcat | grep "DocumentViewModel"
```

---

## 🔐 Points de Vérification

- ✅ Dates d'expiration correctes dans la BD
- ✅ Format de date ISO8601 (2025-11-15)
- ✅ Fuseau horaire correct
- ✅ Logs Android affichent les alertes
- ✅ Notifications envoyées via WebSocket (futur)

---

## 📈 Prochaines Étapes

### Court terme:
1. ✅ Vérification automatique des échéances
2. ✅ Logs d'alerte en place
3. ⏳ Intégrer WebSocket pour envoyer les notifications

### Moyen terme:
1. Afficher les notifications dans HomeScreen
2. Badge de nombre de documents expirant
3. Boutons rapides "Renouveler"

### Long terme:
1. Notifications push (FCM)
2. Rappels programmés
3. Archivage automatique

---

## 💡 Astuces

### Déboguer les dates:
```bash
# Voir les dates d'expiration de tous les documents
adb logcat | grep "expire dans"
```

### Tester avec une date passée:
```kotlin
// Modifier la date d'expiration à hier
document.dateExpiration = Date(System.currentTimeMillis() - 86400000)
// -1 jours = "Document en retard" → envoyer alerte
```

### Format d'affichage:
```kotlin
val formatted = service.formatExpirationDate(document.dateExpiration)
// Retourne: "17/11/2025"
```

---

## 🎉 Résumé

✅ **Service complet créé** - DocumentExpirationNotificationService
✅ **Intégré dans DocumentViewModel** - Vérification automatique
✅ **Logs d'alerte fonctionnels** - Voir dans Logcat
✅ **Prêt pour WebSocket** - Notifications à ajouter

**Commencez à tester maintenant!**

```bash
# 1. Lancer l'app
./gradlew assembleDebug

# 2. Aller sur Documents
# 3. Observer les logs
adb logcat | grep "DocumentViewModel"
```


