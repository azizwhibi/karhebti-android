# Guide Complet: Tester les Notifications WebSocket

## 1. Configuration du Backend (Socket.io)

### Vérifier votre serveur backend
Assurez-vous que votre backend utilise Socket.io:

```javascript
// Exemple Node.js avec Socket.io
const io = require('socket.io')(3000, {
  cors: { origin: "*" }
});

io.on('connection', (socket) => {
  console.log('Utilisateur connecté:', socket.id);
  
  // Envoyer une notification à un utilisateur
  socket.emit('notification', {
    titre: 'Nouvelle Notification',
    message: 'Ceci est un test',
    type: 'test'
  });
});
```

## 2. Configuration Android

### Ajouter la dépendance Socket.io dans build.gradle.kts:

```kotlin
dependencies {
    implementation("io.socket:socket.io-client:4.5.4")
}
```

### Mettre à jour l'URL WebSocket dans WebSocketDebugScreen.kt:

```kotlin
val webSocketService = remember {
    WebSocketService.getInstance("http://your-backend-url:3000")
}
```

Remplacer `your-backend-url:3000` par votre URL réelle.

## 3. Options de Test

### Option 1: Écran de Debug intégré
1. Ajouter la route dans NavGraph.kt:
   ```kotlin
   composable("websocket_debug") {
       WebSocketDebugScreen(
           onBackClick = { navController.popBackStack() }
       )
   }
   ```

2. Ajouter un bouton dans le menu pour accéder au debug

3. L'écran affichera:
   - Statut de connexion (vert = connecté, rouge = déconnecté)
   - Boutons pour connecter/déconnecter
   - Logs en temps réel
   - Liste des notifications reçues

### Option 2: Tester avec cURL/Postman (si votre backend l'accepte)

```bash
# Envoyer une notification via WebSocket
curl -X POST http://your-backend-url:3000/send-notification \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_id",
    "titre": "Test Notification",
    "message": "Message de test",
    "type": "test"
  }'
```

### Option 3: Tester directement depuis le backend

```javascript
// Dans votre serveur backend
io.to(userId).emit('notification', {
  titre: 'Notification de Test',
  message: 'Ceci est une notification de test',
  type: 'test',
  createdAt: new Date()
});
```

## 4. Étapes de Test Complètes

### Étape 1: Configurer l'URL
- Ouvrir WebSocketDebugScreen.kt
- Remplacer `"http://your-backend-url"` par l'URL réelle

### Étape 2: Lancer l'application
```bash
./gradlew assembleDebug
# ou construire depuis Android Studio
```

### Étape 3: Accéder à l'écran de debug
- Naviguer vers WebSocketDebugScreen
- Voir le statut de connexion

### Étape 4: Connecter au serveur
- Cliquer sur le bouton "Connecter"
- Observer les logs
- Vérifier que le statut passe à "Connecté" (vert)

### Étape 5: Tester l'envoi de notifications
Depuis votre backend:
```javascript
// Envoyer une notification au client connecté
socket.emit('notification', {
  titre: 'Test Push',
  message: 'Notification de test WebSocket',
  type: 'echeance'
});
```

### Étape 6: Valider la réception
- Les logs doivent afficher: "✅ Notification reçue: Test Push"
- L'onglet "Notifications" affichera la notification reçue

## 5. Débogage Avancé

### Vérifier les logs Android
```bash
adb logcat | grep WebSocket
```

### Vérifier la connexion
- Socket connecté ✅ = vert
- Socket déconnecté ❌ = rouge

### Erreurs courantes

| Erreur | Solution |
|--------|----------|
| "Token non trouvé" | Vérifier que l'utilisateur est connecté |
| "Échec de connexion" | Vérifier l'URL du serveur |
| Pas de notifications | Vérifier que le serveur envoie correctement |

## 6. Integration dans l'App

Une fois que ça marche, intégrer WebSocketService dans:

1. **MainActivity.kt**: Initialiser au lancement
2. **NotificationViewModel.kt**: Gérer les notifications reçues
3. **Écran Home**: Afficher les notifications reçues en temps réel

### Exemple d'intégration:
```kotlin
// Dans MainActivity.kt
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val tokenManager = TokenManager.getInstance(this)
    val token = tokenManager.getToken()
    if (token != null) {
        WebSocketService.getInstance("http://your-backend-url").connect(token)
    }
}
```

## 7. Avantages du Système WebSocket

✅ Notifications en temps réel
✅ Pas d'attente ni de polling
✅ Connexion persistante
✅ Économe en batterie
✅ Scalable et performant

## 8. Notes Importantes

- L'URL doit être accessible depuis votre téléphone
- Si vous testez en local, utilisez l'IP de votre machine (pas localhost)
- Le token doit être valide et non expiré
- La connexion est automatiquement rétablie en cas de déconnexion

---

**Maintenant, testez et envoyez-moi les logs si vous rencontrez des problèmes!**

