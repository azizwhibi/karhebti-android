# ✅ Checklist - Implémentation du Flux SOS Complet

## 📋 Vue d'ensemble

Ce document contient une checklist complète pour vérifier que le flux SOS est correctement implémenté selon le scénario attendu.

---

## 🎯 Scénario de référence

```
0:00  User sends SOS → declareBreakdown()
0:01  Backend creates breakdown (status: PENDING)
0:02  Backend finds garage owners
0:03  Backend sends FCM notification
0:04  Garage owner receives notification
0:05  Garage owner taps notification
0:06  Garage owner sees SOS details
0:07  Garage owner clicks "Accepter"
0:08  Backend updates status to ACCEPTED
0:10  User app polls and detects status change
0:11  User app navigates to tracking screen
0:12  ✅ Both parties connected!
```

---

## 1️⃣ Backend (Node.js)

### Endpoints API

- [ ] **POST /api/breakdowns**
  - [ ] Crée une nouvelle panne avec status: PENDING
  - [ ] Extrait userId du JWT token
  - [ ] Sauvegarde latitude et longitude
  - [ ] Retourne l'ID de la panne
  - [ ] Code 201 en cas de succès

- [ ] **GET /api/breakdowns/:id**
  - [ ] Retourne les détails d'une panne
  - [ ] Inclut le statut actuel
  - [ ] Accessible au créateur et aux garages
  - [ ] Code 200 en cas de succès

- [ ] **PATCH /api/breakdowns/:id**
  - [ ] Met à jour le statut (ACCEPTED, REFUSED, etc.)
  - [ ] Vérifie les permissions (garage owner seulement)
  - [ ] Retourne la panne mise à jour
  - [ ] Code 200 en cas de succès

### Logique métier

- [ ] **Recherche de garages**
  - [ ] Trouve les garages dans un rayon défini
  - [ ] Filtre par garages vérifiés (isVerifiedGarage: true)
  - [ ] Exclut les garages déjà occupés
  - [ ] Log le nombre de garages trouvés

- [ ] **Notifications FCM**
  - [ ] Envoie notification à tous les garages trouvés
  - [ ] Payload contient: type, description, latitude, longitude, breakdownId
  - [ ] Click action navigue vers breakdown_detail/:id
  - [ ] Log succès/échec pour chaque envoi
  - [ ] Sauvegarde l'historique des notifications

### Logs attendus

```bash
✅ POST /api/breakdowns 201 - 203ms
✅ JWT Auth Successful
✅ Breakdown created: 6756e8f8...
✅ Status: PENDING

🔍 Looking for nearby garages...
📍 Breakdown location: 36.8065, 10.1815
👥 Found 1 verified garage owners:
   - prop.garage@example.com
   
📤 Sending notification to prop.garage@example.com...
🔐 FCM Token: eYxRk7F_Sa2...
✅ Notification sent successfully!
   Response: projects/karhebti/messages/0:1234567890

💾 Notification saved to database
📊 Summary: 1 sent, 0 failed
```

---

## 2️⃣ Android - BreakdownViewModel

### Classe & États

- [ ] **BreakdownUiState**
  - [ ] `Idle` - État initial
  - [ ] `Loading` - Opération en cours
  - [ ] `Success(data: Any)` - Succès avec données
  - [ ] `Error(message: String)` - Erreur avec message
  - [ ] `StatusChanged(breakdown, previousStatus)` - Changement de statut

### Propriétés

- [ ] `_uiState: MutableStateFlow<BreakdownUiState>`
- [ ] `uiState: StateFlow<BreakdownUiState>` (exposé publiquement)
- [ ] `pollingJob: Job?` - Job pour le polling
- [ ] `lastKnownStatus: String?` - Dernier statut connu

### Méthodes

- [ ] **declareBreakdown(request: CreateBreakdownRequest)**
  - [ ] Émet `Loading` avant l'appel
  - [ ] Appelle `repo.createBreakdown()`
  - [ ] Émet `Success(breakdown)` en cas de succès
  - [ ] Émet `Error(message)` en cas d'erreur
  - [ ] Sauvegarde `lastKnownStatus`
  - [ ] Log détaillé

- [ ] **startPollingBreakdown(id: Int, intervalMs: Long = 5000)**
  - [ ] Arrête le polling précédent
  - [ ] Crée un Job dans viewModelScope
  - [ ] Boucle infinie avec delay
  - [ ] Appelle `fetchBreakdownById()` à chaque itération
  - [ ] Log le démarrage

- [ ] **fetchBreakdownById(id: Int)**
  - [ ] Appelle `repo.getBreakdownById()`
  - [ ] Compare avec `lastKnownStatus`
  - [ ] Émet `StatusChanged` si différent
  - [ ] Émet `Success` sinon
  - [ ] Log les changements de statut

- [ ] **stopPolling()**
  - [ ] Annule `pollingJob`
  - [ ] Met `pollingJob` à null
  - [ ] Log l'arrêt

- [ ] **updateBreakdownStatus(id: Int, status: String)**
  - [ ] Émet `Loading`
  - [ ] Appelle `repo.updateBreakdownStatus()`
  - [ ] Émet `Success` ou `Error`
  - [ ] Met à jour `lastKnownStatus`
  - [ ] Log la mise à jour

- [ ] **resetState()**
  - [ ] Réinitialise `_uiState` à `Idle`
  - [ ] Réinitialise `lastKnownStatus` à null

- [ ] **onCleared()**
  - [ ] Appelle `stopPolling()`
  - [ ] Log le nettoyage

### Logs attendus

```
BreakdownVM: ✅ SOS créé: 6756e8f8abc123, status: PENDING
BreakdownVM: 🔄 Démarrage du polling pour breakdown #123 (interval: 5000ms)
BreakdownVM: 📋 Récupéré breakdown, status: PENDING
BreakdownVM: 📋 Récupéré breakdown, status: PENDING
BreakdownVM: 🔄 Changement de statut détecté: PENDING → ACCEPTED
BreakdownVM: ⏹️ Polling arrêté
BreakdownVM: 🧹 ViewModel nettoyé
```

---

## 3️⃣ Android - BreakdownSOSScreen

### Setup

- [ ] Injection du ViewModel avec Repository et API
- [ ] Retrofit configuré avec AuthInterceptor
- [ ] Base URL: `http://172.18.1.246:3000/`

### États locaux

- [ ] `selectedType: String` - Type de panne
- [ ] `description: String` - Description
- [ ] `latitude: Double?` - Position GPS
- [ ] `longitude: Double?` - Position GPS
- [ ] `showConfirmDialog: Boolean` - Dialog de confirmation

### LaunchedEffect

- [ ] **uiState observer**
  - [ ] Sur `Success(breakdown)` → Navigation vers SOSStatusScreen
  - [ ] Sur `Error` → Affichage du message d'erreur
  - [ ] Appelle `viewModel.resetState()` après succès

### UI

- [ ] Sélection du type de panne (PNEU, BATTERIE, ACCIDENT)
- [ ] Champ description (optionnel)
- [ ] Affichage de la position GPS
- [ ] Bouton "Envoyer SOS" avec état Loading
- [ ] Dialog de confirmation avant envoi
- [ ] Désactivation du bouton pendant Loading

### Actions

- [ ] Détection de la position GPS
- [ ] Validation (position obligatoire)
- [ ] Création du `CreateBreakdownRequest`
- [ ] Appel à `viewModel.declareBreakdown(request)`
- [ ] Navigation après succès

---

## 4️⃣ Android - SOSStatusScreen

### Setup

- [ ] Injection du ViewModel
- [ ] Paramètres: `breakdownId`, `type`, `latitude`, `longitude`

### États locaux

- [ ] `currentBreakdown: BreakdownResponse?`
- [ ] `currentStatus: String` (initial: "PENDING")
- [ ] `hasNavigated: Boolean` (pour éviter double navigation)

### LaunchedEffects

- [ ] **Démarrage du polling**
  - [ ] `viewModel.startPollingBreakdown(breakdownId.toInt())`
  - [ ] Appelé une seule fois au lancement

- [ ] **Observer uiState**
  - [ ] Sur `Success` → Met à jour `currentBreakdown` et `currentStatus`
  - [ ] Sur `StatusChanged` → Détecte PENDING → ACCEPTED
  - [ ] Navigation automatique sur ACCEPTED
  - [ ] Délai de 1s avant navigation (pour animation)
  - [ ] Appelle `viewModel.stopPolling()` avant navigation

### DisposableEffect

- [ ] Arrête le polling dans `onDispose`

### UI

- [ ] **Status PENDING**
  - [ ] Icône de recherche animée (rotation)
  - [ ] Texte "Recherche d'un garage..."
  - [ ] CircularProgressIndicator
  - [ ] Card avec détails de la demande
  - [ ] Temps d'attente estimé

- [ ] **Status ACCEPTED**
  - [ ] Icône CheckCircle (vert)
  - [ ] Texte "Garage trouvé!"
  - [ ] Texte "Redirection..."
  - [ ] CircularProgressIndicator

- [ ] **Status REFUSED**
  - [ ] Icône Cancel (rouge)
  - [ ] Texte "Demande refusée"
  - [ ] Bouton retour

### Animations

- [ ] Pulse sur l'icône (scale 1.0 → 1.1)
- [ ] Rotation de l'icône de recherche

---

## 5️⃣ Android - BreakdownDetailScreen (Garage Owner)

### Setup

- [ ] Injection du ViewModel
- [ ] Paramètre: `breakdownId: Int`

### États locaux

- [ ] `breakdown: BreakdownResponse?`
- [ ] `showConfirmDialog: Boolean`

### LaunchedEffects

- [ ] **Chargement initial**
  - [ ] `viewModel.fetchBreakdownById(breakdownId)`

- [ ] **Observer uiState**
  - [ ] Sur `Success` → Met à jour `breakdown`
  - [ ] Si status == "ACCEPTED" → Navigation auto vers tracking
  - [ ] Sur `Error` → Affichage de l'erreur

### UI

- [ ] **Card Type de panne**
  - [ ] Icône Warning
  - [ ] Type en gros
  - [ ] Description si disponible
  - [ ] Fond rouge léger

- [ ] **Card Position**
  - [ ] Latitude et longitude
  - [ ] Bouton "Voir sur la carte" (ouvre Google Maps)

- [ ] **Card Infos client**
  - [ ] ID utilisateur
  - [ ] Bouton "Appeler le client"

- [ ] **Boutons d'action** (si status == PENDING)
  - [ ] Bouton "✅ Accepter" (vert)
  - [ ] Bouton "❌ Refuser" (rouge outline)

### Dialog de confirmation

- [ ] Icône CheckCircle
- [ ] Titre "Accepter cette demande SOS?"
- [ ] Liste des engagements
- [ ] Bouton Confirmer → `viewModel.updateBreakdownStatus(id, "ACCEPTED")`
- [ ] Bouton Annuler

---

## 6️⃣ Android - BreakdownTrackingScreen

### Setup

- [ ] Paramètre: `breakdownId: String`
- [ ] Chargement des détails de la panne

### UI

- [ ] Carte avec positions (user + garage)
- [ ] Distance et ETA
- [ ] Infos du garage/client
- [ ] Bouton d'appel
- [ ] Statut actuel (IN_PROGRESS, COMPLETED)
- [ ] Bouton "Marquer comme complété" (garage owner)

---

## 7️⃣ Navigation (NavGraph)

### Routes

- [ ] `Screen.SOS.route` → BreakdownSOSScreen
- [ ] `"sos_status/{breakdownId}/{type}/{lat}/{lon}"` → SOSStatusScreen
- [ ] `"breakdown_detail/{breakdownId}"` → BreakdownDetailScreen
- [ ] `"breakdown_tracking/{breakdownId}"` → BreakdownTrackingScreen

### Navigation flows

- [ ] **User flow**
  - [ ] Home → SOS → SOSStatus → Tracking
  - [ ] PopUpTo pour éviter retour arrière

- [ ] **Garage flow**
  - [ ] Notification → Detail → Tracking
  - [ ] PopUpTo pour éviter retour arrière

---

## 8️⃣ Notifications FCM

### Configuration

- [ ] `google-services.json` configuré
- [ ] Firebase dépendances ajoutées
- [ ] Service de notification créé

### Service (MyFirebaseMessagingService)

- [ ] Override `onMessageReceived()`
- [ ] Parse le payload (type, breakdownId, etc.)
- [ ] Crée une notification locale
- [ ] PendingIntent vers BreakdownDetailScreen
- [ ] Affiche la notification

### Payload attendu

```json
{
  "notification": {
    "title": "🚨 Nouvelle demande SOS",
    "body": "Assistance PNEU demandée"
  },
  "data": {
    "type": "breakdown",
    "breakdownId": "123",
    "breakdownType": "PNEU",
    "latitude": "36.8065",
    "longitude": "10.1815"
  }
}
```

### Tests

- [ ] Notification reçue sur l'appareil
- [ ] Son et vibration
- [ ] Clic ouvre BreakdownDetailScreen
- [ ] Données passées correctement

---

## 9️⃣ Repository & API

### BreakdownsRepository

- [ ] **createBreakdown(request)** → Flow<Result<BreakdownResponse>>
  - [ ] Gestion des erreurs HTTP
  - [ ] Messages d'erreur personnalisés

- [ ] **getBreakdownById(id)** → Flow<Result<BreakdownResponse>>
  - [ ] Retourne la panne avec statut actuel

- [ ] **updateBreakdownStatus(id, status)** → Flow<Result<BreakdownResponse>>
  - [ ] Envoie Map<String, String> au backend

- [ ] **getAllBreakdowns(status?, userId?)** → Flow<Result<List<BreakdownResponse>>>

- [ ] **getUserBreakdowns(userId)** → Flow<Result<List<BreakdownResponse>>>

### BreakdownsApi (Retrofit)

- [ ] `@POST("breakdowns")` createBreakdown
- [ ] `@GET("breakdowns/{id}")` getBreakdown
- [ ] `@PATCH("breakdowns/{id}")` updateStatus
- [ ] `@GET("breakdowns")` getAllBreakdowns
- [ ] `@GET("breakdowns/user/{userId}")` getUserBreakdowns

### AuthInterceptor

- [ ] Ajoute le header `Authorization: Bearer <token>`
- [ ] Récupère le token depuis TokenManager

---

## 🔟 Tests End-to-End

### Scénario complet (User + Garage)

1. [ ] **User envoie SOS**
   - [ ] Ouvrir BreakdownSOSScreen
   - [ ] Sélectionner type "PNEU"
   - [ ] Entrer description "Pneu crevé"
   - [ ] Vérifier position GPS détectée
   - [ ] Cliquer "Envoyer SOS"
   - [ ] Confirmer dans le dialog
   - [ ] Vérifier navigation vers SOSStatusScreen

2. [ ] **Backend traite la demande**
   - [ ] Vérifier log "Breakdown created"
   - [ ] Vérifier status: PENDING
   - [ ] Vérifier "Looking for nearby garages"
   - [ ] Vérifier "Found X garage owners"
   - [ ] Vérifier "Notification sent successfully"

3. [ ] **Garage Owner reçoit notification**
   - [ ] Notification apparaît
   - [ ] Son/vibration
   - [ ] Titre et body corrects
   - [ ] Taper sur la notification
   - [ ] App ouvre BreakdownDetailScreen

4. [ ] **Garage Owner voit détails**
   - [ ] Type de panne affiché
   - [ ] Description affichée
   - [ ] Position GPS affichée
   - [ ] Boutons "Accepter" et "Refuser" visibles

5. [ ] **Garage Owner accepte**
   - [ ] Cliquer "Accepter"
   - [ ] Dialog de confirmation
   - [ ] Confirmer
   - [ ] Vérifier log "Mise à jour statut → ACCEPTED"
   - [ ] Vérifier log "Statut mis à jour: ACCEPTED"
   - [ ] Vérifier navigation vers Tracking

6. [ ] **User détecte changement (polling)**
   - [ ] Vérifier log "Changement de statut: PENDING → ACCEPTED"
   - [ ] Vérifier StatusChanged émis
   - [ ] Vérifier UI "Garage trouvé!"
   - [ ] Vérifier navigation auto vers Tracking
   - [ ] Vérifier log "Polling arrêté"

7. [ ] **Les deux voient Tracking**
   - [ ] User voit carte avec positions
   - [ ] Garage voit carte avec positions
   - [ ] Distance et ETA corrects
   - [ ] Boutons d'appel fonctionnels

### Temps total attendu
- [ ] 0:00 → 0:12 (12 secondes max)

---

## 1️⃣1️⃣ Gestion des erreurs

### Cas d'erreur à tester

- [ ] **Pas de connexion internet**
  - [ ] Message: "Erreur réseau : vérifiez votre connexion"
  - [ ] Bouton "Réessayer"

- [ ] **Token expiré (401)**
  - [ ] Message: "Non authentifié : veuillez vous reconnecter"
  - [ ] Redirection vers login

- [ ] **Permission refusée (403)**
  - [ ] Message: "Non autorisé : votre session peut avoir expiré"

- [ ] **Données invalides (400)**
  - [ ] Message: "Données invalides : vérifiez le type et la description"

- [ ] **Aucun garage trouvé**
  - [ ] Backend log: "Found 0 garage owners"
  - [ ] Status reste PENDING
  - [ ] Afficher message après timeout

- [ ] **Polling échoue**
  - [ ] Continuer le polling
  - [ ] Logger l'erreur mais ne pas afficher à l'user

---

## 1️⃣2️⃣ Optimisations

### Performance

- [ ] Polling à 5s (pas plus fréquent)
- [ ] Arrêt du polling dès navigation
- [ ] Nettoyage dans onCleared()
- [ ] Pas de fuite mémoire (Job annulé)

### UX

- [ ] Animation de recherche fluide
- [ ] Navigation automatique rapide
- [ ] Messages d'erreur clairs
- [ ] Feedback visuel sur chaque action
- [ ] Loading states partout

### Logs

- [ ] Emoji pour faciliter le debug
- [ ] Tag cohérent ("BreakdownVM", "SOSStatus", etc.)
- [ ] Niveau approprié (Debug, Error)
- [ ] Informations utiles (ID, status, temps)

---

## 📊 Métriques de succès

- [ ] **Temps de bout en bout**: < 15 secondes
- [ ] **Taux de succès notification**: > 95%
- [ ] **Détection changement status**: < 10 secondes
- [ ] **Navigation automatique**: 100%
- [ ] **Pas de crash**: 0 crash

---

## 🚀 Prochaines étapes

Une fois la checklist complétée:

1. [ ] Tests unitaires pour BreakdownViewModel
2. [ ] Tests d'intégration Repository
3. [ ] Tests UI avec Compose
4. [ ] Documentation utilisateur
5. [ ] Video démo du flux complet
6. [ ] Déploiement en production

---

## 📝 Notes

- **Version actuelle**: 1.0.0
- **Date**: 14 décembre 2025
- **Dernière mise à jour**: Implémentation initiale complète

---

## ✅ Validation finale

Avant de marquer le projet comme terminé:

- [ ] Tous les points de la checklist sont ✅
- [ ] Tests E2E réussis 3 fois consécutivement
- [ ] Pas de warning dans les logs
- [ ] Performance acceptable (< 15s total)
- [ ] Code review fait
- [ ] Documentation à jour
- [ ] Demo enregistrée

**Status**: 🟡 En cours d'implémentation

---

**Auteurs**: Karhebti Dev Team
**Contact**: support@karhebti.com

