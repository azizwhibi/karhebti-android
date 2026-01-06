# 📊 DIAGRAMME VISUEL - Flux SOS Complet

**Date:** 14 décembre 2025  
**Version:** 1.0.0

---

## 🎬 FLUX COMPLET (Vue d'ensemble)

```
╔═══════════════════════════════════════════════════════════════════════╗
║                     FLUX SOS - DE BOUT EN BOUT                        ║
║                         (Timeline: 11 secondes)                       ║
╚═══════════════════════════════════════════════════════════════════════╝

┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   📱 USER   │         │  🖥️ BACKEND │         │ 🔧 GARAGE   │
│    APP      │         │             │         │    OWNER    │
└──────┬──────┘         └──────┬──────┘         └──────┬──────┘
       │                       │                       │
       │ [0s] POST /breakdowns │                       │
       │─────────────────────>│                       │
       │                       │                       │
       │                       │ [1s] Crée breakdown   │
       │                       │ Status: PENDING       │
       │                       │                       │
       │  [1s] ✅ 201 Created  │                       │
       │←─────────────────────┤                       │
       │ Navigate to           │                       │
       │ SOSStatusScreen       │                       │
       │                       │                       │
       │                       │ [2s] Cherche garages  │
       │                       │ MongoDB $near query   │
       │                       │ Trouve 5 garages      │
       │                       │                       │
       │                       │ [3s] Envoie FCM       │
       │                       │──────────────────────>│
       │                       │      FCM notification │
       │                       │──────────────────────>│
       │                       │                       │
       │                       │                       │ [4s] 🔔
       │                       │                       │ Notification
       │                       │                       │ s'affiche
       │                       │                       │
       │ [5s] Poll: GET /:id   │                       │ [5s] 👆 TAP
       │─────────────────────>│                       │ notification
       │ Status: PENDING       │                       │
       │←─────────────────────┤                       │ MainActivity
       │                       │                       │ detecte intent
       │                       │                       │ Navigate to
       │                       │                       │ DetailScreen
       │                       │                       │
       │ [10s] Poll: GET /:id  │                       │ [7s] Accepte
       │─────────────────────>│<──────────────────────┤ PATCH /:id
       │                       │  Status: ACCEPTED     │
       │ Status: ACCEPTED ✅   │                       │
       │←─────────────────────┤ [8s] ✅ 200 OK        │
       │                       │──────────────────────>│
       │ [11s] AUTO-NAVIGATE   │                       │
       │ to TrackingScreen     │                       │ Navigate to
       │                       │                       │ TrackingScreen
       │                       │                       │
       │ 🎉 TRACKING           │                       │ 🎉 TRACKING
       │                       │                       │
       ▼                       ▼                       ▼
```

---

## 📱 ÉCRANS ANDROID - Navigation Flow

```
╔═══════════════════════════════════════════════════════════════════════╗
║                      USER APP - NAVIGATION                            ║
╚═══════════════════════════════════════════════════════════════════════╝

┌────────────────┐
│  HomeScreen    │
│                │
│  [🚨 SOS]      │ ◄─── User appuie ici
└────────┬───────┘
         │
         ▼
┌────────────────┐
│ BreakdownSOS   │
│ Screen         │
│                │
│ Type: [PNEU ▼] │
│ Description:   │
│ [________]     │
│                │
│ 📍 Location    │
│                │
│ [📤 Envoyer]   │ ◄─── User appuie ici
└────────┬───────┘
         │ POST /breakdowns
         │
         ▼
┌────────────────┐
│ SOSStatus      │
│ Screen         │
│                │
│ 🔄 Recherche   │
│    de garages  │
│                │
│ ⏳ En attente  │
│                │
│ ┌────────────┐ │
│ │ Polling 5s │ │ ◄─── Refresh automatique
│ └────────────┘ │
└────────┬───────┘
         │
         │ Status change détecté
         │ PENDING → ACCEPTED
         │
         ▼ AUTO-NAVIGATE
┌────────────────┐
│ BreakdownTrac  │
│ king Screen    │
│                │
│ ✅ Accepté     │
│                │
│ ┌────────────┐ │
│ │    MAP     │ │
│ └────────────┘ │
│                │
│ Timeline:      │
│ ●─●─○─○        │
│                │
│ [📞 Appeler]   │
└────────────────┘

╔═══════════════════════════════════════════════════════════════════════╗
║                   GARAGE OWNER APP - NAVIGATION                       ║
╚═══════════════════════════════════════════════════════════════════════╝

        Notification FCM reçue
                 │
                 │ 🔔 "Nouvelle demande SOS"
                 │
                 ▼ User TAP
        ┌────────────────┐
        │  MainActivity  │
        │                │
        │ handleNotifi   │
        │ cationIntent() │
        └────────┬───────┘
                 │
                 │ Navigate avec breakdownId
                 │
                 ▼
        ┌────────────────┐
        │ BreakdownDe    │
        │ tail Screen    │
        │                │
        │ Type: PNEU     │
        │ Description:   │
        │ "Pneu crevé"   │
        │                │
        │ ┌────────────┐ │
        │ │    MAP     │ │
        │ └────────────┘ │
        │                │
        │ Distance: 5.2km│
        │                │
        │ [✅ Accepter]  │ ◄─── Garage owner appuie
        │ [❌ Refuser]   │
        └────────┬───────┘
                 │
                 │ Dialog confirmation
                 │ PATCH /breakdowns/:id
                 │
                 ▼
        ┌────────────────┐
        │ BreakdownTrac  │
        │ king Screen    │
        │                │
        │ ✅ Accepté     │
        │                │
        │ ┌────────────┐ │
        │ │    MAP     │ │
        │ └────────────┘ │
        │                │
        │ Client:        │
        │ Jean Dupont    │
        │ +216 XX XXX    │
        │                │
        │ [📞 Appeler]   │
        └────────────────┘
```

---

## 🔄 ÉTATS DU BREAKDOWN

```
╔═══════════════════════════════════════════════════════════════════════╗
║                    MACHINE À ÉTATS - BREAKDOWN                        ║
╚═══════════════════════════════════════════════════════════════════════╝

    ┌─────────┐
    │ START   │
    └────┬────┘
         │ User envoie SOS
         │
         ▼
    ┌─────────┐
    │ PENDING │ ◄─── Création initiale
    └────┬────┘      Couleur: 🟠 Orange
         │
         ├────────────────────┐
         │                    │
         │ Garage accepte     │ Garage refuse
         │                    │
         ▼                    ▼
    ┌─────────┐          ┌─────────┐
    │ACCEPTED │          │ REFUSED │
    └────┬────┘          └─────────┘
         │               Couleur: ⚫ Gris
         │               FIN
         │
         │ Garage commence
         │
         ▼
    ┌─────────┐
    │IN_PROG  │
    │RESS     │
    └────┬────┘
         │               Couleur: 🟡 Jaune
         │
         │ Travail terminé
         │
         ▼
    ┌─────────┐
    │COMPLETED│
    └─────────┘
    Couleur: 🟢 Vert
    FIN

Légende des couleurs:
━━━━━━━━━━━━━━━━
🟠 PENDING     - En attente de réponse
🔵 ACCEPTED    - Garage a accepté
🟡 IN_PROGRESS - Travail en cours
🟢 COMPLETED   - Terminé avec succès
⚫ REFUSED     - Demande refusée
⚫ CANCELLED   - Annulé par user
```

---

## 📊 TIMELINE DÉTAILLÉE

```
╔═══════════════════════════════════════════════════════════════════════╗
║                 TIMELINE DÉTAILLÉE (Seconde par seconde)              ║
╚═══════════════════════════════════════════════════════════════════════╝

0.0s  📱 User appuie sur "Envoyer" dans BreakdownSOSScreen
      └─> Validation des champs
      └─> Affichage loading indicator

0.5s  🌐 Requête HTTP POST /breakdowns envoyée
      └─> Body: { type, description, latitude, longitude }
      └─> Header: Authorization: Bearer JWT_TOKEN

1.0s  ✅ Backend reçoit et traite la requête
      ├─> Valide le token JWT
      ├─> Extrait userId depuis token
      ├─> Crée document Breakdown dans MongoDB
      └─> Status: PENDING

1.5s  🔍 Backend cherche garages à proximité
      ├─> Query: User.find({ role: 'propGarage', location: $near })
      ├─> Radius: 10 km (10000 mètres)
      └─> Filtre: fcmToken exists et non-null

2.0s  👥 Backend trouve 5 garages
      ├─> Garage 1: 2.3 km
      ├─> Garage 2: 4.1 km
      ├─> Garage 3: 5.2 km
      ├─> Garage 4: 7.8 km
      └─> Garage 5: 9.5 km

2.5s  📤 Backend envoie notifications FCM (parallèle)
      ├─> Garage 1: admin.messaging().send() ✅
      ├─> Garage 2: admin.messaging().send() ✅
      ├─> Garage 3: admin.messaging().send() ✅
      ├─> Garage 4: admin.messaging().send() ✅
      └─> Garage 5: admin.messaging().send() ✅

3.0s  📱 User app reçoit 201 Created
      ├─> breakdownId: "6756e8f8..."
      └─> Navigate: SOSStatusScreen(breakdownId)

3.5s  🔔 Garages reçoivent notifications
      ├─> KarhebtiMessagingService.onMessageReceived()
      ├─> Affichage notification (son + vibration)
      └─> Badge sur icône app

4.0s  🔄 SOSStatusScreen démarre polling
      └─> LaunchedEffect: while(true) { fetch(); delay(5000) }

5.0s  📱 Polling #1: GET /breakdowns/:id
      ├─> Status: PENDING
      └─> Affiche "En attente de réponse..."

5.5s  👆 Garage owner 3 TAP sur notification
      ├─> MainActivity.onCreate()
      ├─> intent.getStringExtra("breakdownId")
      └─> navController.navigate("breakdown_detail/12345")

6.0s  📄 BreakdownDetailScreen charge données
      ├─> viewModel.fetchBreakdownById(12345)
      ├─> Affiche type, description, carte
      └─> Calcule distance: 5.2 km

7.0s  ✅ Garage owner 3 appuie "Accepter"
      ├─> showAcceptDialog = true
      ├─> User confirme dans dialog
      └─> PATCH /breakdowns/12345 { status: "ACCEPTED" }

8.0s  🖥️ Backend met à jour status
      ├─> Breakdown.findByIdAndUpdate()
      ├─> acceptedBy: garage3_id
      ├─> acceptedAt: new Date()
      └─> Status: ACCEPTED

8.5s  📤 Backend notifie user (optionnel)
      └─> admin.messaging().send() "Garage trouvé!"

9.0s  ✅ Garage app reçoit 200 OK
      ├─> onAccepted() callback
      └─> Navigate: BreakdownTrackingScreen

10.0s 🔄 User app polling #2: GET /breakdowns/:id
      ├─> Status: ACCEPTED ✅
      └─> LaunchedEffect détecte changement

10.5s 🎉 User app AUTO-NAVIGATE
      ├─> if (old == PENDING && new == ACCEPTED)
      └─> onNavigateToTracking(breakdownId)

11.0s ✅ BreakdownTrackingScreen affiché
      ├─> Badge "Accepté ✓" (bleu)
      ├─> Carte avec position
      ├─> Timeline: ●─●─○─○
      └─> Bouton "Appeler le garage"

═══════════════════════════════════════════════════════════════════════

✅ COMMUNICATION ÉTABLIE ENTRE USER ET GARAGE!
```

---

## 🔧 COMPOSANTS ANDROID

```
╔═══════════════════════════════════════════════════════════════════════╗
║                    ARCHITECTURE ANDROID                               ║
╚═══════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────────┐
│                           MainActivity                               │
│                                                                      │
│  - onCreate()                                                        │
│  - handleNotificationIntent()  ◄─── Gère navigation depuis FCM      │
│  - initializeFCM()                                                   │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ Fournit NavController
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                            NavGraph                                  │
│                                                                      │
│  Sealed Class Screen {                                              │
│    - BreakdownDetail: "breakdown_detail/{id}"                       │
│    - BreakdownTracking: "breakdown_tracking/{id}"                   │
│  }                                                                   │
│                                                                      │
│  Composables:                                                        │
│    - composable(BreakdownDetail.route) { ... }                      │
│    - composable(BreakdownTracking.route) { ... }                    │
└─────────────────────────┬───────────────┬───────────────────────────┘
                          │               │
            ┌─────────────┘               └─────────────┐
            │                                           │
            ▼                                           ▼
┌───────────────────────┐                   ┌───────────────────────┐
│ BreakdownDetailScreen │                   │BreakdownTrackingScreen│
│                       │                   │                       │
│ - Affiche détails     │                   │ - Wrapper (ViewModel) │
│ - Carte localisation  │                   │ - StatusCard          │
│ - Accepter/Refuser    │                   │ - BreakdownInfoCard   │
│ - Dialogs confirm     │                   │ - TimelineCard        │
└───────────┬───────────┘                   │ - Carte OSM           │
            │                               │ - Bouton appel        │
            │                               └───────────┬───────────┘
            │                                           │
            └───────────────┬───────────────────────────┘
                            │
                            │ Utilise
                            │
                            ▼
            ┌───────────────────────────────┐
            │    BreakdownViewModel         │
            │                               │
            │  - uiState: StateFlow         │
            │  - fetchBreakdownById()       │
            │  - updateBreakdownStatus()    │
            └────────────┬──────────────────┘
                         │
                         │ Utilise
                         │
                         ▼
            ┌───────────────────────────────┐
            │   BreakdownsRepository        │
            │                               │
            │  - createBreakdown()          │
            │  - getBreakdownById()         │
            │  - updateBreakdownStatus()    │
            └────────────┬──────────────────┘
                         │
                         │ Utilise
                         │
                         ▼
            ┌───────────────────────────────┐
            │      BreakdownsApi            │
            │                               │
            │  @POST("breakdowns")          │
            │  @GET("breakdowns/{id}")      │
            │  @PATCH("breakdowns/{id}")    │
            └───────────────────────────────┘
```

---

## 🔔 FLUX NOTIFICATIONS FCM

```
╔═══════════════════════════════════════════════════════════════════════╗
║                     FLUX NOTIFICATIONS FCM                            ║
╚═══════════════════════════════════════════════════════════════════════╝

Backend                           Firebase Cloud              Android App
                                  Messaging
                                  
┌──────────┐                    ┌──────────┐                ┌──────────┐
│          │                    │          │                │          │
│  POST    │  1. Send message   │          │   2. Deliver  │ Karhebti │
│  /break  │─────────────────> │   FCM    │──────────────>│ Messaging│
│  downs   │                    │  Server  │   notification │ Service  │
│          │                    │          │                │          │
└──────────┘                    └──────────┘                └────┬─────┘
                                                                 │
                                                                 │
                                3. onMessageReceived()           │
                                                                 │
                                                                 ▼
                                                     ┌───────────────────┐
                                                     │ showSOSNotifica   │
                                                     │ tion()            │
                                                     │                   │
                                                     │ - Titre           │
                                                     │ - Body            │
                                                     │ - Icon            │
                                                     │ - Son/Vibration   │
                                                     │ - PendingIntent   │
                                                     └────────┬──────────┘
                                                              │
                                                              │
                                                              ▼
                                                     ┌───────────────────┐
                                                     │ NotificationMana  │
                                                     │ ger.notify()      │
                                                     │                   │
                                                     │ Affiche la        │
                                                     │ notification      │
                                                     └───────────────────┘

Message FCM Structure:
━━━━━━━━━━━━━━━━━━━━━

{
  "token": "eYxRk7F_Sa2...",
  "notification": {
    "title": "🚨 Nouvelle demande SOS",
    "body": "Assistance PNEU demandée à proximité"
  },
  "data": {
    "type": "new_breakdown",
    "breakdownId": "6756e8f8...",
    "breakdownType": "PNEU",
    "latitude": "36.8065",
    "longitude": "10.1815"
  },
  "android": {
    "priority": "high",
    "notification": {
      "channelId": "sos_notifications",
      "sound": "default",
      "priority": "high"
    }
  }
}
```

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025  
**Auteur:** AI Assistant

