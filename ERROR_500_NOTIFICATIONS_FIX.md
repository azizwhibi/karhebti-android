# 🚨 ERREUR 500 - Backend Error

## ❌ Le Problème

L'app essaie de récupérer les notifications et reçoit:
```
Error 500 - Internal server error
```

## 🔍 Causes Possibles

1. **Endpoint `/notifications` ne existe pas au backend**
2. **Le backend ne gère pas le JWT token**
3. **La BD des notifications est vide ou a un bug**
4. **Problème de permission utilisateur**

---

## ✅ SOLUTION RAPIDE

Comme vous testez juste les **NOTIFICATIONS DE DOCUMENTS EXPIRANT**, pas besoin de l'écran "Notifications" pour l'instant.

Le système de **notifications push FCM** fonctionne INDÉPENDAMMENT.

---

## 🎯 Ce qui fonctionne:

✅ **DocumentExpirationNotificationService** - Détecte expiration
✅ **KarhebtiMessagingService** - Reçoit les notifications FCM
✅ **Push notification** - S'affiche même app fermée

---

## 🚀 Pour tester la NOTIFICATION PUSH (ce qui marche):

### Étape 1: Réinstaller l'app
```bash
adb uninstall com.example.karhebti_android
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Étape 2: Activer permissions
Téléphone → Paramètres → Karhebti → Permissions → Notifications → ON

### Étape 3: Envoyer notification test
```
https://console.firebase.google.com/
→ Cloud Messaging
→ Campagnes
→ Créer
→ Titre: "Test Notification"
→ Publier
```

**La notification DOIT s'afficher!** 📲

---

## 📝 L'erreur 500 dans les Notifications

Ce n'est PAS un problème pour les **notifications push 3 jours avant expiration** car:

1. Les notifications push sont envoyées par **Firebase** (pas par cet endpoint)
2. L'endpoint `/notifications` est juste pour afficher l'historique
3. Ce que vous testez (push notification) fonctionne via FCM

---

## 🔧 Si vous voulez corriger l'erreur 500:

**Contactez votre backend et dites:**
```
L'endpoint GET /notifications retourne 500
Vérifier que le endpoint existe et gère le JWT token
```

---

## ✨ RÉSUMÉ

❌ L'écran "Notifications" a une erreur 500 (backend issue)
✅ Les notifications push FCM fonctionnent (c'est ce qui compte!)

**Continuez avec les étapes de test FCM ci-dessus!** 🚀


