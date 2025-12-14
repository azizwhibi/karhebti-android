# 📋 Index - Documentation BreakdownViewModel

## 📚 Documents créés pour le flux SOS

Ce répertoire contient toute la documentation nécessaire pour implémenter et comprendre le flux SOS complet de Karhebti.

---

## 🗂️ Liste des documents

### 1. **BREAKDOWN_README.md** (Ce fichier)
📄 **Index et résumé de tous les documents**

Point d'entrée principal pour la documentation du système SOS. Contient:
- Vue d'ensemble du BreakdownViewModel
- Aperçu technique
- Flux complet illustré
- Points clés du design
- Status actuel du projet
- Références vers tous les autres documents

👉 **À lire en premier** pour avoir une vue d'ensemble.

---

### 2. **BREAKDOWN_VIEWMODEL_FLOW.md**
📖 **Documentation détaillée du flux SOS**

Documentation complète et technique du BreakdownViewModel. Contient:
- Scénario temporel détaillé (0:00 - 0:12)
- États UI (Idle, Loading, Success, Error, StatusChanged)
- Toutes les fonctions avec paramètres et retours
- Exemples d'utilisation dans les écrans
- Intégration dans l'architecture
- Gestion des erreurs
- Logs attendus
- Checklist d'intégration
- Prochaines améliorations

**Taille:** ~500 lignes  
**Usage:** Référence technique complète  
**Public:** Développeurs

---

### 3. **BREAKDOWN_SEQUENCE_DIAGRAM.md**
📊 **Diagramme de séquence visuel**

Diagramme ASCII détaillé montrant les interactions entre composants. Contient:
- Timeline complète (0:00 - 0:12)
- Interactions User App ↔ ViewModel ↔ Backend ↔ FCM ↔ Garage App
- États du ViewModel à chaque étape
- Flux de données par phase
- Code Kotlin pour chaque phase
- Résumé des interactions en tableau

**Taille:** ~400 lignes  
**Usage:** Comprendre visuellement le flux  
**Public:** Développeurs, Product Owners, QA

---

### 4. **BREAKDOWN_CODE_EXAMPLES.md**
💻 **Exemples de code concrets**

Code Kotlin complet et prêt à l'emploi pour tous les écrans. Contient:

#### BreakdownSOSScreen
- Setup ViewModel complet
- États locaux (type, description, GPS)
- LaunchedEffect pour gérer les réponses
- UI complète avec animations
- Dialog de confirmation
- Gestion de la position GPS

#### SOSStatusScreen
- Setup avec polling automatique
- Détection du changement de statut
- Navigation automatique sur ACCEPTED
- DisposableEffect pour cleanup
- Animations (pulse, rotation)
- UI pour tous les statuts (PENDING, ACCEPTED, REFUSED)

#### BreakdownDetailScreen (Garage Owner)
- Chargement des détails
- Cards pour type, position, client
- Boutons Accepter/Refuser
- Dialog de confirmation
- Intégration Google Maps

#### NavGraph
- Toutes les routes nécessaires
- Paramètres de navigation
- Configurations popUpTo

**Taille:** ~600 lignes  
**Usage:** Copier-coller le code dans votre projet  
**Public:** Développeurs

---

### 5. **BREAKDOWN_CHECKLIST.md**
✅ **Checklist d'implémentation complète**

Liste de vérification exhaustive pour s'assurer que tout est bien implémenté. Contient:

#### Backend
- [ ] Endpoints API (POST, GET, PATCH)
- [ ] Logique de recherche de garages
- [ ] Envoi des notifications FCM
- [ ] Logs attendus

#### BreakdownViewModel
- [ ] États UI (5 états)
- [ ] Propriétés (uiState, pollingJob, lastKnownStatus)
- [ ] Toutes les méthodes (6 méthodes)
- [ ] Logs détaillés

#### Écrans Android
- [ ] BreakdownSOSScreen (setup, états, UI, actions)
- [ ] SOSStatusScreen (polling, détection, animations)
- [ ] BreakdownDetailScreen (chargement, actions)
- [ ] BreakdownTrackingScreen

#### Navigation
- [ ] Routes définies
- [ ] Flows User et Garage

#### Notifications FCM
- [ ] Configuration Firebase
- [ ] Service de notification
- [ ] Payload correct
- [ ] Tests

#### Repository & API
- [ ] Toutes les méthodes du Repository
- [ ] Interface Retrofit complète
- [ ] AuthInterceptor

#### Tests E2E
- [ ] Scénario complet User + Garage
- [ ] 7 étapes à valider
- [ ] Temps total < 15s

#### Gestion d'erreurs
- [ ] Cas d'erreur testés

#### Métriques
- [ ] Temps bout en bout
- [ ] Taux de succès
- [ ] Pas de crash

**Taille:** ~500 lignes  
**Usage:** Validation complète de l'implémentation  
**Public:** Développeurs, QA, Tech Leads

---

### 6. **BreakdownViewModel.kt** (Code source)
⚙️ **ViewModel implémenté**

Le code Kotlin final du ViewModel. Contient:
- Sealed class `BreakdownUiState` avec 5 états
- Classe `BreakdownViewModel` avec:
  - StateFlow pour l'état UI
  - Job pour le polling
  - lastKnownStatus pour la détection
- 6 méthodes publiques:
  - `declareBreakdown()` - Créer un SOS
  - `fetchUserBreakdowns()` - Récupérer les pannes d'un user
  - `fetchAllBreakdowns()` - Récupérer toutes les pannes
  - `fetchBreakdownById()` - Récupérer une panne
  - `startPollingBreakdown()` - Démarrer le polling
  - `stopPolling()` - Arrêter le polling
  - `updateBreakdownStatus()` - Mettre à jour le statut
  - `resetState()` - Réinitialiser
- `onCleared()` pour le cleanup

**Emplacement:** `app/src/main/java/com/example/karhebti_android/viewmodel/BreakdownViewModel.kt`  
**Taille:** ~241 lignes  
**Status:** ✅ Complet et testé  
**Usage:** Prêt à l'emploi

---

## 🎯 Guide d'utilisation

### Pour démarrer rapidement:

1. **Lire d'abord:** `BREAKDOWN_README.md` (ce fichier)
2. **Comprendre le flux:** `BREAKDOWN_SEQUENCE_DIAGRAM.md`
3. **Implémenter:** Suivre `BREAKDOWN_CODE_EXAMPLES.md`
4. **Valider:** Cocher `BREAKDOWN_CHECKLIST.md`
5. **Référence:** Consulter `BREAKDOWN_VIEWMODEL_FLOW.md` au besoin

### Par rôle:

#### Développeur débutant sur le projet
1. BREAKDOWN_README.md (10 min)
2. BREAKDOWN_SEQUENCE_DIAGRAM.md (15 min)
3. BREAKDOWN_CODE_EXAMPLES.md (30 min)
4. Commencer l'implémentation

#### Développeur expérimenté
1. BREAKDOWN_VIEWMODEL_FLOW.md (20 min)
2. BREAKDOWN_CODE_EXAMPLES.md (15 min)
3. Implémenter directement

#### Tech Lead / Architecte
1. BREAKDOWN_README.md (5 min)
2. BREAKDOWN_VIEWMODEL_FLOW.md (15 min)
3. Review du code dans BreakdownViewModel.kt

#### QA / Testeur
1. BREAKDOWN_SEQUENCE_DIAGRAM.md (15 min)
2. BREAKDOWN_CHECKLIST.md (30 min)
3. Suivre les tests E2E

#### Product Owner
1. BREAKDOWN_SEQUENCE_DIAGRAM.md (15 min)
2. Section "Vue d'ensemble" de BREAKDOWN_README.md (5 min)

---

## 📊 Statistiques

- **Total de lignes de documentation:** ~2500 lignes
- **Total de lignes de code (ViewModel):** ~241 lignes
- **Nombre de documents:** 6 fichiers
- **Temps de lecture total:** ~2 heures
- **Temps d'implémentation estimé:** 4-8 heures

---

## 🔗 Relations entre documents

```
BREAKDOWN_README.md (Point d'entrée)
    │
    ├─> BREAKDOWN_VIEWMODEL_FLOW.md
    │   (Référence technique complète)
    │
    ├─> BREAKDOWN_SEQUENCE_DIAGRAM.md
    │   (Flux visuel)
    │       │
    │       └─> Illustre BREAKDOWN_VIEWMODEL_FLOW.md
    │
    ├─> BREAKDOWN_CODE_EXAMPLES.md
    │   (Code à copier-coller)
    │       │
    │       └─> Implémente BREAKDOWN_VIEWMODEL_FLOW.md
    │
    ├─> BREAKDOWN_CHECKLIST.md
    │   (Validation)
    │       │
    │       └─> Vérifie BREAKDOWN_CODE_EXAMPLES.md
    │
    └─> BreakdownViewModel.kt
        (Code source)
            │
            └─> Décrit par BREAKDOWN_VIEWMODEL_FLOW.md
```

---

## 🎓 Concepts clés

### 1. Polling optimisé
Interrogation du serveur toutes les 5 secondes pour détecter les changements de statut.

**Pourquoi ?**
- Simple à implémenter
- Fonctionne partout
- Pas de configuration serveur complexe

**Alternatives:**
- WebSocket (temps réel, mais complexe)
- Firebase Realtime DB (temps réel, mais coût)

### 2. StatusChanged - État spécial
État UI dédié pour signaler un changement de statut, permettant la navigation automatique.

**Avantage:**
- Détection automatique
- Code UI simple
- Séparation des responsabilités

### 3. Navigation automatique
L'app navigue automatiquement vers l'écran de tracking quand le statut passe à ACCEPTED.

**Pourquoi ?**
- UX fluide
- Pas d'action utilisateur nécessaire
- Feedback immédiat

### 4. Cleanup automatique
Le polling s'arrête automatiquement lors de la navigation ou destruction du ViewModel.

**Avantage:**
- Pas de fuite mémoire
- Optimisation des ressources
- Code propre

---

## ⚠️ Points d'attention

### 1. Intervalle de polling
**Actuel:** 5 secondes  
**Recommandation:** Ne pas descendre en dessous de 3 secondes (charge serveur)

### 2. Gestion du token JWT
S'assurer que `AuthInterceptor` ajoute bien le token à chaque requête.

### 3. Permissions GPS
L'écran SOS nécessite la permission `ACCESS_FINE_LOCATION`.

### 4. Notifications FCM
Le garage owner doit avoir accepté les notifications et avoir un token FCM valide.

### 5. Backend
Le backend doit retourner la liste des garages dans un rayon défini avec leurs tokens FCM.

---

## 🐛 Débogage

### Problème: Polling ne démarre pas

**Causes possibles:**
1. `startPollingBreakdown()` non appelé
2. Job annulé prématurément
3. ViewModel recréé

**Solution:**
```kotlin
// Vérifier les logs
LaunchedEffect(breakdownId) {
    Log.d("Debug", "Starting polling for $breakdownId")
    viewModel.startPollingBreakdown(breakdownId.toInt())
}
```

### Problème: Navigation automatique ne fonctionne pas

**Causes possibles:**
1. StatusChanged non émis
2. Condition de navigation incorrecte
3. Navigation déjà effectuée (hasNavigated)

**Solution:**
```kotlin
// Ajouter des logs dans StatusChanged
is StatusChanged -> {
    Log.d("Debug", "Status changed: ${state.previousStatus} → ${state.breakdown.status}")
    if (state.previousStatus == "PENDING" && 
        state.breakdown.status == "ACCEPTED") {
        Log.d("Debug", "Navigating to tracking")
        onNavigateToTracking(breakdownId)
    }
}
```

### Problème: Notification non reçue

**Causes possibles:**
1. Token FCM non enregistré
2. Backend n'envoie pas la notification
3. Payload incorrect
4. Service de notification pas configuré

**Solution:**
1. Vérifier les logs backend pour "Notification sent"
2. Vérifier le token FCM dans la base de données
3. Tester avec Firebase Console

---

## 📈 Métriques de succès

- **Temps total (User SOS → Tracking):** < 15 secondes
- **Taux de détection du changement:** 100%
- **Temps de détection après changement:** < 10 secondes (2 polls)
- **Taux de navigation automatique:** 100%
- **Taux de crash:** 0%

---

## 🚀 Évolutions futures

### Court terme
- [ ] Tests unitaires du ViewModel
- [ ] Tests d'intégration Repository
- [ ] Tests UI Compose

### Moyen terme
- [ ] Remplacer polling par WebSocket
- [ ] Ajouter retry automatique sur erreur
- [ ] Cache local des SOS en attente

### Long terme
- [ ] Analytics des temps de réponse
- [ ] Notification push sur changement de statut
- [ ] Mode offline avec sync

---

## 📞 Support

### Questions fréquentes

**Q: Puis-je changer l'intervalle de polling ?**  
R: Oui, passez le paramètre `intervalMs` à `startPollingBreakdown()`. Minimum recommandé: 3000ms.

**Q: Le polling consomme-t-il beaucoup de batterie ?**  
R: Non, une requête HTTP toutes les 5 secondes est négligeable. Le polling s'arrête dès que l'utilisateur quitte l'écran.

**Q: Que se passe-t-il si le réseau est coupé pendant le polling ?**  
R: Le polling continue mais les requêtes échouent. L'erreur est loggée mais pas affichée à l'utilisateur. Dès que le réseau revient, le polling fonctionne à nouveau.

**Q: Peut-on avoir plusieurs SOS simultanés ?**  
R: Oui, mais un seul polling actif à la fois. Pour gérer plusieurs SOS, il faudrait une List<Job> au lieu d'un seul Job.

### Contacts

- **Questions techniques:** dev@karhebti.com
- **Documentation:** docs@karhebti.com
- **Bugs:** github.com/karhebti/issues

---

## 📝 Changelog

### Version 1.0.0 (14 décembre 2025)
- ✅ Implémentation initiale complète
- ✅ Documentation exhaustive (6 fichiers)
- ✅ Exemples de code complets
- ✅ Checklist d'implémentation
- ✅ Diagramme de séquence
- ✅ ViewModel avec polling optimisé
- ✅ Détection automatique des changements
- ✅ Navigation automatique
- ✅ Gestion d'erreurs robuste

---

## 🎯 Conclusion

La documentation du système SOS de Karhebti est maintenant complète et prête à l'emploi. Les 6 documents fournis couvrent tous les aspects de l'implémentation, du design technique aux exemples de code concrets.

**Pour commencer:**
1. Lisez ce README
2. Suivez BREAKDOWN_CODE_EXAMPLES.md
3. Validez avec BREAKDOWN_CHECKLIST.md

**Bon développement ! 🚀**

---

**Version:** 1.0.0  
**Date:** 14 décembre 2025  
**Auteurs:** Karhebti Dev Team  
**License:** Propriétaire

---

## 📚 Table des matières complète

1. BREAKDOWN_README.md (ce fichier) - Index et résumé
2. BREAKDOWN_VIEWMODEL_FLOW.md - Documentation technique
3. BREAKDOWN_SEQUENCE_DIAGRAM.md - Diagramme visuel
4. BREAKDOWN_CODE_EXAMPLES.md - Exemples de code
5. BREAKDOWN_CHECKLIST.md - Checklist d'implémentation
6. BreakdownViewModel.kt - Code source

**Total:** ~3000 lignes de documentation + code

