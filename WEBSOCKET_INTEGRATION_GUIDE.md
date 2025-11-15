# Guide d'Intégration WebSocket Debug

## Étape 1: Ajouter l'écran au NavGraph

Ouvrez votre `NavGraph.kt` et ajouter la route:

```kotlin
composable("websocket_debug") {
    WebSocketDebugScreen(
        onBackClick = { navController.popBackStack() }
    )
}
```

## Étape 2: Ajouter un bouton de debug dans HomeScreen

Modifiez HomeScreen.kt pour ajouter:

```kotlin
// Dans le FAB ou menu
FloatingActionButton(
    onClick = { navController.navigate("websocket_debug") },
    containerColor = MaterialTheme.colorScheme.error,
    modifier = Modifier
        .padding(16.dp)
        .align(Alignment.BottomEnd)
) {
    Icon(Icons.Default.Build, "Debug WebSocket")
}
```

Ou dans un menu:

```kotlin
// Dans TopAppBar actions
IconButton(onClick = { navController.navigate("websocket_debug") }) {
    Icon(Icons.Default.Build, "Debug")
}
```

## Étape 3: Configurer l'URL du serveur

Dans `WebSocketDebugScreen.kt`, remplacer:

```kotlin
// Ancienne ligne (à remplacer)
val webSocketService = remember {
    WebSocketService.getInstance("http://your-backend-url")
}

// Nouvelle ligne (avec votre URL)
val webSocketService = remember {
    WebSocketService.getInstance("http://192.168.1.100:3000") // Remplacer par votre URL
}
```

## Étape 4: Build et Test

```bash
./gradlew clean build
```

## Étape 5: Utiliser l'écran de debug

1. **Lancer l'app**
2. **Naviguer vers "WebSocket Debug"**
3. **Cliquer "Connecter"**
4. **Vérifier que le statut passe à "Connecté" (vert)**
5. **Voir les logs et les notifications reçues**

## Accès aux Logs

```bash
# Voir tous les logs WebSocket
adb logcat | grep WebSocket

# Voir tous les logs de l'app
adb logcat | grep karhebti
```

## Tester l'envoi de notification

### Depuis Node.js/JavaScript:

```javascript
const io = require('socket.io')(3000);

io.on('connection', (socket) => {
    console.log('Client connecté:', socket.id);
    
    // Envoyer une notification après 2 secondes
    setTimeout(() => {
        socket.emit('notification', {
            titre: 'Notification de Test',
            message: 'Ceci est une notification de test WebSocket',
            type: 'test'
        });
    }, 2000);
});
```

### Depuis Python:

```python
from socketio import Client
import time

sio = Client()

@sio.on('connect')
def on_connect():
    print('Connexion établie')

sio.connect('http://localhost:3000')

# Envoyer une notification
sio.emit('notification', {
    'titre': 'Test Python',
    'message': 'Notification depuis Python',
    'type': 'test'
})

time.sleep(1)
```

## Solutions aux Problèmes Courants

### La connexion échoue

**Problème:** Erreur de connexion
**Solution:**
- Vérifier que le serveur WebSocket est en cours d'exécution
- Vérifier l'URL (pas localhost, utilisez l'IP machine)
- Vérifier que le firewall n'est pas bloquant
- Test: `adb logcat | grep WebSocket`

### Pas de token

**Problème:** "Token non trouvé"
**Solution:**
- Vérifier que vous êtes connecté à l'app
- Vérifier que TokenManager sauvegarde correctement
- Test: Les autres écrans accèdent-ils au token?

### Les notifications ne sont pas reçues

**Problème:** Connexion OK mais pas de notifications
**Solution:**
- Vérifier que le serveur envoie vraiment les notifications
- Vérifier l'event name ("notification" ou "notifications")
- Vérifier les logs: `adb logcat | grep WebSocket`
- Utiliser postman/curl pour tester l'API backend

## Intégration Production

Une fois que vous avez testé:

1. **Initialiser WebSocket au démarrage de l'app**
2. **Intégrer avec NotificationViewModel**
3. **Afficher les notifications dans HomeScreen**
4. **Mettre à jour l'URL de production**
5. **Gérer la reconnexion automatique**

---

**Besoin d'aide? Consultez les logs WebSocket!**

