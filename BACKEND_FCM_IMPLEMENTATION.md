# 🖥️ BACKEND - Configuration Notifications FCM

## 📋 Objectif
Le backend vérifie les documents qui expirent dans 3 jours et envoie les notifications push via Firebase Cloud Messaging.

---

## 🔧 Configuration Backend

### Étape 1: Installer Firebase Admin SDK

#### Python
```bash
pip install firebase-admin
```

#### Node.js
```bash
npm install firebase-admin
```

---

## 🚀 Implémentation Backend

### Option 1: Python (Django/FastAPI)

#### Installation
```bash
pip install firebase-admin celery redis  # Pour les tâches programmées
```

#### Configuration initiale
```python
# settings.py ou config.py
import firebase_admin
from firebase_admin import credentials, messaging

# Initialiser Firebase (télécharger serviceAccountKey.json depuis Firebase Console)
cred = credentials.Certificate("path/to/serviceAccountKey.json")
firebase_admin.initialize_app(cred)
```

#### Modèle utilisateur avec FCM Token
```python
from django.db import models

class User(models.Model):
    email = models.EmailField(unique=True)
    fcm_token = models.CharField(max_length=500, blank=True, null=True)
    fcm_token_updated_at = models.DateTimeField(auto_now=True)
    
    def __str__(self):
        return self.email
```

#### Vérifier et envoyer les notifications
```python
from datetime import datetime, timedelta
from django.utils import timezone
from firebase_admin import messaging
from .models import User, Document

def check_and_send_expiration_notifications():
    """
    Vérifie les documents qui expirent dans 3 jours
    et envoie les notifications FCM
    """
    today = timezone.now().date()
    expiration_date = today + timedelta(days=3)
    
    # Trouver les documents expirante
    expiring_docs = Document.objects.filter(
        date_expiration__lte=expiration_date,
        date_expiration__gte=today
    )
    
    print(f"🔍 Vérification: {expiring_docs.count()} document(s) trouvé(s)")
    
    for doc in expiring_docs:
        # Récupérer l'utilisateur
        user = doc.user  # Supposant qu'il y a une FK vers User
        
        if not user.fcm_token:
            print(f"⚠️  Utilisateur {user.email} n'a pas de FCM token")
            continue
        
        # Calculer les jours restants
        days_remaining = (doc.date_expiration - today).days
        
        # Créer le message
        message = messaging.Message(
            notification=messaging.Notification(
                title="🚨 Document en train d'expirer",
                body=f"{doc.type} expire dans {days_remaining} jour(s)"
            ),
            data={
                "type": "document_expiration",
                "documentId": str(doc.id),
                "documentType": doc.type,
                "daysRemaining": str(days_remaining),
                "priority": "high" if days_remaining <= 1 else "medium",
                "voiture": doc.voiture or "Non spécifiée"
            },
            token=user.fcm_token
        )
        
        try:
            response = messaging.send(message)
            print(f"✅ Notification envoyée à {user.email}: {response}")
        except Exception as e:
            print(f"❌ Erreur lors de l'envoi: {e}")

# API Endpoint pour mettre à jour le token FCM
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

@api_view(['POST'])
@permission_classes([IsAuthenticated])
def update_fcm_token(request):
    """
    Endpoint pour que l'app envoie le token FCM au serveur
    POST /api/users/update-fcm-token/
    {
        "fcm_token": "dXl2nK8m9J7xQ2pR1sT0uV..."
    }
    """
    fcm_token = request.data.get('fcm_token')
    
    if not fcm_token:
        return Response({'error': 'FCM token required'}, status=400)
    
    user = request.user
    user.fcm_token = fcm_token
    user.save()
    
    return Response({
        'message': 'FCM token updated successfully',
        'email': user.email
    })

# Task Celery pour vérifier les documents chaque jour
from celery import shared_task

@shared_task
def task_check_document_expiration():
    """
    Tâche Celery pour vérifier les documents chaque jour à minuit
    À configurer dans celery beat schedule
    """
    check_and_send_expiration_notifications()
    return "Task completed"
```

#### Configuration Celery Beat
```python
# celery.py
from celery.schedules import crontab

app.conf.beat_schedule = {
    'check-document-expiration': {
        'task': 'your_app.tasks.task_check_document_expiration',
        'schedule': crontab(hour=0, minute=0),  # Chaque jour à minuit
    },
}
```

---

### Option 2: Node.js (Express)

#### Installation
```bash
npm install firebase-admin express cors
```

#### Configuration initiale
```javascript
const admin = require('firebase-admin');

const serviceAccount = require('./path/to/serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const messaging = admin.messaging();
```

#### Modèle utilisateur avec FCM Token
```javascript
const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  email: { type: String, unique: true, required: true },
  fcmToken: { type: String, default: '' },
  fcmTokenUpdatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);
```

#### Vérifier et envoyer les notifications
```javascript
const User = require('./models/User');
const Document = require('./models/Document');

async function checkAndSendExpirationNotifications() {
  try {
    const today = new Date();
    const inThreeDays = new Date(today.getTime() + 3 * 24 * 60 * 60 * 1000);
    
    // Trouver les documents expirante
    const expiringDocs = await Document.find({
      dateExpiration: { $lte: inThreeDays, $gte: today }
    }).populate('userId');
    
    console.log(`🔍 Vérification: ${expiringDocs.length} document(s) trouvé(s)`);
    
    for (const doc of expiringDocs) {
      const user = doc.userId;
      
      if (!user.fcmToken) {
        console.log(`⚠️  Utilisateur ${user.email} n'a pas de FCM token`);
        continue;
      }
      
      // Calculer les jours restants
      const daysRemaining = Math.ceil(
        (doc.dateExpiration - today) / (1000 * 60 * 60 * 24)
      );
      
      const message = {
        notification: {
          title: '🚨 Document en train d\'expirer',
          body: `${doc.type} expire dans ${daysRemaining} jour(s)`
        },
        data: {
          type: 'document_expiration',
          documentId: doc._id.toString(),
          documentType: doc.type,
          daysRemaining: daysRemaining.toString(),
          priority: daysRemaining <= 1 ? 'high' : 'medium',
          voiture: doc.voiture || 'Non spécifiée'
        },
        token: user.fcmToken
      };
      
      try {
        const response = await messaging.send(message);
        console.log(`✅ Notification envoyée à ${user.email}: ${response}`);
      } catch (error) {
        console.error(`❌ Erreur lors de l'envoi: ${error.message}`);
      }
    }
  } catch (error) {
    console.error('Erreur:', error);
  }
}
```

#### API Endpoint pour mettre à jour le token FCM
```javascript
// routes/users.js
const express = require('express');
const User = require('../models/User');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.post('/update-fcm-token', authMiddleware, async (req, res) => {
  try {
    const { fcmToken } = req.body;
    
    if (!fcmToken) {
      return res.status(400).json({ error: 'FCM token required' });
    }
    
    const user = await User.findByIdAndUpdate(
      req.user.id,
      { fcmToken },
      { new: true }
    );
    
    res.json({
      message: 'FCM token updated successfully',
      email: user.email
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
```

#### Task planifiée avec node-schedule
```javascript
const schedule = require('node-schedule');

// Vérifier chaque jour à minuit
schedule.scheduleJob('0 0 * * *', () => {
  console.log('⏰ Vérification quotidienne des documents...');
  checkAndSendExpirationNotifications();
});
```

---

## 📱 Intégration App Android

### Dans MainActivity.kt
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialiser FCM et envoyer le token au backend
    FCMTokenManager(this).initializeFCMToken { token ->
        if (token.isNotEmpty()) {
            sendFCMTokenToBackend(token)
        }
    }
}

private fun sendFCMTokenToBackend(token: String) {
    val authViewModel: AuthViewModel = viewModel()
    
    viewModelScope.launch {
        try {
            val apiService = RetrofitClient.apiService
            val request = mapOf("fcmToken" to token)
            apiService.updateFCMToken(request)
            Log.d("FCM", "✅ Token envoyé au backend: ${token.take(20)}...")
        } catch (e: Exception) {
            Log.e("FCM", "❌ Erreur lors de l'envoi: ${e.message}")
        }
    }
}
```

### Dans KarhebtiApiService.kt
```kotlin
@POST("users/update-fcm-token")
suspend fun updateFCMToken(@Body request: Map<String, String>): Response<Map<String, String>>
```

---

## 🧪 Tester l'intégration

### Test 1: Vérifier que le token est envoyé
```bash
# Logs
adb logcat | grep "Token envoyé au backend"
# Doit afficher: ✅ Token envoyé au backend: dXl2nK8m9...
```

### Test 2: Vérifier que la notification est envoyée
```python
# Python: Exécuter manuellement
from your_app.tasks import check_and_send_expiration_notifications
check_and_send_expiration_notifications()
```

```javascript
// Node.js: Exécuter manuellement
checkAndSendExpirationNotifications();
```

### Test 3: Vérifier la réception
```bash
# L'app doit afficher la notification
adb logcat | grep "KarhebtiMessaging"
# Doit afficher: ✅ Notification affichée: Document en train d'expirer
```

---

## 🔐 Checklist Backend

- [ ] Firebase Admin SDK installé
- [ ] serviceAccountKey.json téléchargé depuis Firebase Console
- [ ] Application Firebase initialisée
- [ ] Modèle utilisateur avec fcmToken
- [ ] Endpoint `/update-fcm-token` créé
- [ ] Fonction `check_and_send_expiration_notifications()` implémentée
- [ ] Task planifiée (Celery ou node-schedule)
- [ ] L'app envoie le token au backend
- [ ] Notification test envoyée avec succès

---

## 📊 Format du Message FCM

```json
{
  "notification": {
    "title": "🚨 Document en train d'expirer",
    "body": "Assurance Automobile expire DEMAIN!"
  },
  "data": {
    "type": "document_expiration",
    "documentId": "doc_12345",
    "documentType": "Assurance Automobile",
    "daysRemaining": "1",
    "priority": "high",
    "voiture": "Renault Scenic - 75ABC123"
  },
  "token": "dXl2nK8m9J7xQ2pR1sT0uV..."
}
```

---

**C'est prêt! Configurez le backend et les notifications vont s'envoyer automatiquement!** 🎉


