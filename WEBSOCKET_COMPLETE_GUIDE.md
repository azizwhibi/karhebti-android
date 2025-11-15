# 🚀 Guide Complet: Tester les Notifications WebSocket

## 📋 Résumé Complet

Vous avez maintenant un système complet pour tester les notifications WebSocket!

### 📁 Fichiers Créés:

1. **WebSocketService.kt** - Service de gestion WebSocket
2. **WebSocketDebugScreen.kt** - Écran de debug intégré
3. **build.gradle.kts** - Dépendance Socket.io ajoutée
4. **test-websocket.py** - Script de test Python
5. **test-websocket.sh** - Script de test Bash

---

## 🎯 Étapes Rapides pour Tester

### Étape 1: Configurer l'URL du serveur

**Dans WebSocketDebugScreen.kt (ligne ~50):**

```kotlin
val webSocketService = remember {
    WebSocketService.getInstance("http://192.168.1.100:3000") // ← Votre URL ici
}
```

**Remplacer par:**
- Votre URL de serveur backend
- Utiliser l'IP de votre machine (pas localhost)
- Port du serveur WebSocket (ex: 3000, 5000, etc.)

### Étape 2: Ajouter le bouton de debug au menu

**Dans HomeScreen.kt ou SettingsScreen.kt:**

```kotlin
// Ajouter un menu ou bouton
IconButton(onClick = { navController.navigate("websocket_debug") }) {
    Icon(Icons.Default.Build, "Debug WebSocket")
}
```

### Étape 3: Ajouter la route dans NavGraph.kt

```kotlin
composable("websocket_debug") {
    WebSocketDebugScreen(
        onBackClick = { navController.popBackStack() }
    )
}
```

### Étape 4: Compiler et tester

```bash
./gradlew assembleDebug
```

---

## 🧪 Options de Test

### Option A: Utiliser le Script Python (Recommandé)

```bash
# Installation
pip install python-socketio requests

# Exécution
python test-websocket.py

# Suivre le menu:
# 1. Se connecter
# 2. Envoyer des notifications
# 3. Voir les notifications reçues
```

### Option B: Écran de Debug Android

1. Lancer l'app
2. Naviguer vers "WebSocket Debug"
3. Cliquer "Connecter"
4. Observer les logs
5. Envoyer des notifications depuis le backend

### Option C: Tester depuis le Backend

**Node.js:**

```javascript
const io = require('socket.io')(3000);

io.on('connection', (socket) => {
    console.log('Client connecté');
    
    // Envoyer une notification après 2 secondes
    setTimeout(() => {
        socket.emit('notification', {
            titre: 'Notification de Test',
            message: 'Ceci fonctionne!',
            type: 'test'
        });
    }, 2000);
});
```

**Python:**

```python
from socketio import Client

sio = Client()

@sio.on('notification')
def on_notification(data):
    print(f"Reçu: {data}")

sio.connect('http://localhost:3000')
sio.emit('notification', {
    'titre': 'Test Python',
    'message': 'Message depuis Python',
    'type': 'test'
})
```

---

## 🔍 Dépannage

### ❌ Erreur: "Impossible de se connecter"

```bash
# Vérifier les logs
adb logcat | grep WebSocket

# Solutions:
# 1. Vérifier l'URL (pas localhost, utiliser l'IP)
# 2. Vérifier que le serveur est démarré
# 3. Vérifier le firewall
# 4. Vérifier le port
```

### ❌ Erreur: "Token non trouvé"

```bash
# Vérifier que vous êtes connecté à l'app
# Vérifier que TokenManager fonctionne
# Test: Essayer d'accéder à d'autres écrans
```

### ❌ Les notifications ne sont pas reçues

```bash
# Vérifier dans les logs
adb logcat | grep WebSocket

# Solutions:
# 1. Vérifier que le serveur envoie vraiment
# 2. Vérifier l'event name ('notification' ou 'notifications')
# 3. Vérifier les événements Socket.io
# 4. Tester avec curl/Postman
```

---

## 📊 Architecture Complète

```
┌─────────────────────────────────────────────────┐
│              Android App                         │
│  ┌──────────────────────────────────────────┐   │
│  │   WebSocketDebugScreen (Interface)       │   │
│  │   - État de connexion                    │   │
│  │   - Logs en temps réel                   │   │
│  │   - Notifications reçues                 │   │
│  └────────────────┬─────────────────────────┘   │
│                   │                              │
│  ┌────────────────▼─────────────────────────┐   │
│  │   WebSocketService (Gestion)             │   │
│  │   - Connexion/Déconnexion                │   │
│  │   - Listeners                            │   │
│  │   - Événements Socket.io                 │   │
│  └────────────────┬─────────────────────────┘   │
│                   │                              │
└───────────────────┼──────────────────────────────┘
                    │
                    │ Socket.io
                    │
┌───────────────────▼──────────────────────────────┐
│        Backend WebSocket Server                   │
│  ┌──────────────────────────────────────────┐   │
│  │   Socket.io Gateway                      │   │
│  │   - Gestion des connexions               │   │
│  │   - Émission de notifications            │   │
│  │   - Authentification JWT                 │   │
│  └──────────────────────────────────────────┘   │
└──────────────────────────────────────────────────┘
```

---

## ✅ Checklist de Vérification

### Avant de tester:

- [ ] URL WebSocket configurée
- [ ] Serveur WebSocket démarré
- [ ] Token disponible
- [ ] Dépendance Socket.io ajoutée
- [ ] App compilée

### Lors du test:

- [ ] WebSocket Debug screen créé
- [ ] Bouton de connexion visible
- [ ] Connexion établie (statut vert)
- [ ] Logs affichent "Connecté"
- [ ] Notifications reçues s'affichent

### Après le test:

- [ ] Désactiver le debug en production
- [ ] Intégrer WebSocket au NotificationViewModel
- [ ] Afficher les notifications dans HomeScreen
- [ ] Gérer la reconnexion automatique

---

## 🎓 Prochaines Étapes

### 1. Intégrer dans NotificationViewModel

```kotlin
class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val webSocket = WebSocketService.getInstance(BACKEND_URL)
    
    init {
        webSocket.addListener(object : WebSocketService.NotificationListener {
            override fun onNotificationReceived(notification: Map<String, Any>) {
                // Mettre à jour les notifications
                _notificationsState.value = Resource.Success(...)
            }
            
            override fun onConnectionChanged(isConnected: Boolean) {
                // Gérer l'état de connexion
            }
        })
    }
}
```

### 2. Afficher les notifications dans HomeScreen

```kotlin
// Observer les notifications WebSocket
val notifications by notificationViewModel.notificationsState.observeAsState()

// Afficher les notifications
if (notifications.isNotEmpty()) {
    LazyColumn {
        items(notifications) { notif ->
            NotificationCard(notif)
        }
    }
}
```

### 3. Gérer les reconnexions

```kotlin
// Dans MainActivity.kt
override fun onStart() {
    super.onStart()
    val token = tokenManager.getToken()
    if (token != null && !webSocket.isConnected()) {
        webSocket.connect(token)
    }
}
```

---

## 📱 Accès au Debug en Production

**NE PAS laisser l'écran de debug en production!**

Ajouter une vérification:

```kotlin
if (BuildConfig.DEBUG) {
    // Afficher le bouton de debug
    IconButton(onClick = { navController.navigate("websocket_debug") }) {
        Icon(Icons.Default.Build, "Debug")
    }
}
```

---

## 🆘 Besoin d'Aide?

**Vérifier les logs:**

```bash
# Tous les logs WebSocket
adb logcat | grep WebSocket

# Tous les logs de l'app
adb logcat | grep karhebti

# En temps réel avec filtrage
adb logcat -f karhebti_debug.log &
```

**Tester la connexion:**

```bash
# Vérifier que le serveur écoute
netstat -tulpn | grep 3000

# Tester via curl
curl -v http://localhost:3000/health
```

---

## 📝 Notes Importantes

✅ **À Faire:**
- Configurer l'URL correctement
- Tester avant de mettre en production
- Gérer les erreurs de connexion
- Implémenter la reconnexion automatique

❌ **À Éviter:**
- Laisser l'URL en dur dans le code
- Ignorer les erreurs WebSocket
- Tester uniquement en local
- Oublier de fermer la connexion

---

**Prêt à tester? Commencez par le Script Python! 🚀**

```bash
python test-websocket.py
```


