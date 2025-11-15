# 📱 Intégration Firebase Cloud Messaging

## ⚠️ IMPORTANT: Configuration Firebase

Pour que les notifications push fonctionnent, vous devez configurer Firebase:

### Étape 1: Créer un projet Firebase

1. Aller sur [Firebase Console](https://console.firebase.google.com/)
2. Cliquer sur "Créer un projet"
3. Entrer le nom du projet: `karhebti-android`
4. Accepter les conditions et créer

### Étape 2: Ajouter Android à Firebase

1. Dans Firebase Console, cliquer sur "Ajouter une application"
2. Sélectionner "Android"
3. Entrer:
   - **Package name:** `com.example.karhebti_android`
   - **SHA-1:** [Voir ci-dessous]
4. Télécharger `google-services.json`
5. Placer le fichier dans le dossier: `app/google-services.json`

### Étape 3: Obtenir le SHA-1

Exécuter cette commande:

```bash
./gradlew signingReport
```

Chercher "SHA1" et copier la valeur.

### Étape 4: Valider la configuration

```bash
./gradlew build
```

La compilation réussira une fois `google-services.json` placé dans `app/`.

---

## 📦 Fichiers Créés

### 1. **KarhebtiMessagingService.kt** ✅
- Service FCM pour recevoir les notifications push
- Gère les messages de notification d'expiration
- Affiche les notifications système

### 2. **AndroidManifest.xml** - Mis à jour ✅
- Ajout des permissions FCM
- Enregistrement du service de messaging

### 3. **build.gradle.kts** - Mis à jour ✅
- Dépendances Firebase Cloud Messaging

---

## 🔧 Comment ça marche

### Architecture:

```
Backend (Node.js/Python)
  ↓
Firebase Cloud Messaging
  ↓
FCM Token → KarhebtiMessagingService
  ↓
Notification System Android
  ↓
Notification affichée même app fermée ✅
```

---

## 🚀 Tester les Notifications

### Depuis Firebase Console:

1. Aller sur **Cloud Messaging** → **Campagnes**
2. Cliquer **Créer une campagne**
3. Entrer:
   - Titre: "Test Notification"
   - Message: "Ceci est un test"
4. **Cible Android**
5. **Créer** et **Publier**

### Depuis le Backend:

```python
# Python exemple avec firebase-admin
import firebase_admin
from firebase_admin import credentials, messaging

cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)

message = messaging.Message(
    notification=messaging.Notification(
        title="Document en train d'expirer",
        body="Assurance expire demain!"
    ),
    data={
        "type": "document_expiration",
        "documentId": "doc_123",
        "daysRemaining": "1"
    },
    token=fcm_token  # Token de l'utilisateur
)

response = messaging.send(message)
print(f"Message envoyé: {response}")
```

### Depuis Node.js:

```javascript
const admin = require('firebase-admin');

admin.initializeApp({
  credential: admin.credential.cert('serviceAccountKey.json')
});

const message = {
  notification: {
    title: 'Document en train d\'expirer',
    body: 'Assurance expire demain!'
  },
  data: {
    type: 'document_expiration',
    documentId: 'doc_123',
    daysRemaining: '1'
  },
  token: fcmToken
};

admin.messaging().send(message)
  .then(response => console.log(`Message envoyé: ${response}`))
  .catch(error => console.error('Erreur:', error));
```

---

## 📱 Notifications Affichées

### Notification Standard (Priority: medium):
```
[Karhebti]
Document en train d'expirer
Assurance Automobile expire dans 2 jour(s)
```

### Notification Urgente (Priority: high):
```
[Karhebti] 🔔
URGENT: Document en train d'expirer
Assurance Automobile expire DEMAIN!
```

---

## 🔐 Étapes d'Installation Complète

### 1. Créer le projet Firebase ✅
- [Firebase Console](https://console.firebase.google.com/)

### 2. Récupérer google-services.json ✅
- Placer dans `app/google-services.json`

### 3. Compiler le projet ✅
```bash
./gradlew clean build
```

### 4. Envoyer le FCM Token au Backend ✅
```kotlin
// Dans KarhebtiMessagingService.onNewToken()
sendTokenToServer(token)
```

### 5. Backend envoie les notifications ✅
```python
# Quand un document expire
admin.messaging().send(message)
```

---

## 📋 Points de Vérification

- [ ] Projet Firebase créé
- [ ] `google-services.json` téléchargé et placé
- [ ] `build.gradle.kts` mis à jour avec Firebase
- [ ] `AndroidManifest.xml` mis à jour
- [ ] `KarhebtiMessagingService.kt` créé
- [ ] App compilée sans erreurs
- [ ] FCM Token reçu dans les logs
- [ ] Notification test envoyée avec succès

---

## 🎯 Cas d'Utilisation

### Scenario: Assurance expire demain
```
1. Backend vérifie les documents (chaque jour à minuit)
2. Détecte: Assurance expire dans 1 jour
3. Envoie push notification via FCM
4. KarhebtiMessagingService reçoit la notification
5. Affiche: "URGENT: Assurance expire DEMAIN!"
6. Utilisateur clique → App ouvre DocumentsScreen
```

---

## 🆘 Troubleshooting

### Problème: Notifications non reçues
**Solutions:**
- Vérifier que `google-services.json` existe dans `app/`
- Vérifier que FCM Token est envoyé au backend
- Consulter les logs: `adb logcat | grep "KarhebtiMessaging"`

### Problème: Erreur compilation
**Solution:**
- S'assurer que `google-services.json` est au bon endroit
- Rebuild: `./gradlew clean build`

### Problème: App crash au démarrage
**Solution:**
- Vérifier les permissions dans `AndroidManifest.xml`
- Logs: `adb logcat | grep "FATAL"`

---

## 📞 Ressources

- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Documentation Android](https://developer.android.com/studio)
- [Firebase Console](https://console.firebase.google.com/)

---

**C'est prêt! Les notifications push vont fonctionner une fois que vous configurerez Firebase!** 🎉


