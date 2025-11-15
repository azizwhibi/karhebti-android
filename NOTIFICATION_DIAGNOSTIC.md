# 🚨 DIAGNOSTIC: Pourquoi la Notification ne s'affiche pas?

## ❌ Problèmes Courants

### 1. google-services.json manquant
```
✓ Vérifier: app/google-services.json existe?
✓ Si NON: Télécharger depuis Firebase Console
✓ Si OUI: Compiler de nouveau
```

### 2. Google Play Services manquants
```
- Firebase ne fonctionne PAS sans Google Play Services
- Si device n'a pas Google Play → Notifications ne marchent pas
```

### 3. Permissions manquantes
```
❌ POST_NOTIFICATIONS permission non accordée
→ Solution: Dans les paramètres du téléphone:
   Paramètres → Applications → Karhebti → Permissions → Notifications → Activé
```

### 4. Channel de notification non créé
```
❌ Android 8+ requiert un NotificationChannel
→ Vérifier que le channel est créé
```

### 5. Token FCM pas envoyé au backend
```
❌ Backend ne peut pas envoyer si pas de token
→ Vérifier que MainActivity initialise FCMTokenManager
```

### 6. Backend n'envoie pas la notification
```
❌ Même avec token, backend peut ne pas envoyer
→ Vérifier que backend a serviceAccountKey.json
→ Vérifier que backend envoie vraiment la notif
```

---

## ✅ CHECKLIST DE DIAGNOSTIC

### Sur le Téléphone:
- [ ] Karhebti est installée?
- [ ] App fonctionne normalement?
- [ ] Notifications système sont activées (paramètres)?
- [ ] Google Play Services est installé?
- [ ] Connexion internet active?

### Dans l'App:
- [ ] app/google-services.json existe?
- [ ] App compile sans erreurs Firebase?
- [ ] Logs affichent "Token FCM reçu"?
- [ ] Logs affichent "Message reçu de:"?

### Au Backend:
- [ ] serviceAccountKey.json placé?
- [ ] Firebase Admin SDK installé?
- [ ] Script d'envoi de notification créé?
- [ ] Document d'expiration créé (pour test)?

---

## 🔍 ÉTAPES DE DIAGNOSTIC (À FAIRE DANS L'ORDRE)

### Étape 1: Vérifier Google Services
```bash
# Dans le terminal du PC
dir app | findstr "google-services"
# Doit afficher: google-services.json
```

### Étape 2: Vérifier que l'App compile
```bash
./gradlew clean build
# Doit dire: BUILD SUCCESSFUL
```

### Étape 3: Vérifier Token FCM
```bash
# App en cours d'exécution
adb logcat | grep "Token FCM"
# Doit afficher: ✅ Token FCM obtenu: dXl2nK8m...
```

### Étape 4: Vérifier Permissions Android
```
Sur le téléphone:
Paramètres → Applications → Karhebti → Permissions
→ Notifications: Activé ✅
```

### Étape 5: Envoyer Test depuis Firebase Console
```
Firebase Console → Cloud Messaging → Campagnes
→ Créer une notification de test
→ Voir si elle s'affiche sur le téléphone
```

### Étape 6: Tester via Backend
```bash
# Si backend configuré:
python -c "send_test_notification()"
# ou
node test-notification.js
```

---

## 🛠️ SOLUTIONS RAPIDES

### Solution 1: Permissions manquantes
```
Téléphone → Paramètres → Karhebti → Permissions
→ Notifications → Activé
```

### Solution 2: Recompiler
```bash
./gradlew clean build
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Solution 3: Vérifier Logcat
```bash
adb logcat | grep -i "notif\|firebase\|fcm"
```

### Solution 4: Redémarrer le téléphone
```bash
adb reboot
# Attendre 30 secondes
adb logcat
```

---

## 📞 Solutions Détaillées

### Si le Token n'est pas reçu:
1. Vérifier google-services.json existe
2. Vérifier que Firebase est initialisé dans MainActivity
3. Vérifier que FCMTokenManager est appelé au login

### Si le Message n'est pas reçu:
1. Vérifier que KarhebtiMessagingService est enregistré dans AndroidManifest.xml
2. Vérifier que l'appareil a Google Play Services
3. Vérifier que le backend envoie vraiment la notification

### Si la Notification n'est pas affichée:
1. Vérifier les permissions POST_NOTIFICATIONS
2. Vérifier que NotificationChannel est créé (ligne 107-120)
3. Vérifier que NotificationCompat.Builder est correct

---

## 🎯 PROCHAINES ÉTAPES

1. **Qu'est-ce que vous voyez dans les logs?**
   ```bash
   adb logcat | grep -i "karhebti\|firebase"
   ```

2. **Est-ce que google-services.json existe?**
   ```bash
   dir app | findstr "google-services"
   ```

3. **L'app compile-t-elle sans erreurs?**
   ```bash
   ./gradlew clean build
   ```

**Répondez à ces 3 questions et je peux vous aider directement!**


