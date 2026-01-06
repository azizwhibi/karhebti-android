# Test rapide des corrections SOS

## Avant de recompiler

### Vérification manuelle du code
✅ ID utilisateur retiré de BreakdownDetailScreen.kt
✅ ID utilisateur retiré de BreakdownTrackingScreen.kt
✅ Validation des coordonnées GPS ajoutée
✅ Affichage conditionnel de la distance selon la validité

## Commande de compilation

```powershell
# Depuis le dossier racine du projet
cd C:\Users\rayen\Desktop\karhebti-android-NEW

# Nettoyer le build
.\gradlew clean

# Recompiler
.\gradlew assembleDebug
```

## Tests à effectuer après compilation

### Test 1: Vérifier que l'ID utilisateur n'apparaît plus
1. Ouvrir l'application
2. Se connecter en tant que garagiste
3. Ouvrir une demande SOS
4. **Vérifier**: L'ID utilisateur ne doit plus être visible
5. **Résultat attendu**: "Client en attente d'assistance"

### Test 2: Vérifier la distance (GPS activé)
1. Activer le GPS sur l'appareil
2. Accorder les permissions de localisation à l'app
3. Ouvrir une demande SOS proche (< 50 km)
4. **Vérifier**: La distance affichée est raisonnable
5. **Résultat attendu**: Ex: "2.5 km" avec "≈ 4 min"

### Test 3: Vérifier la distance (GPS désactivé)
1. Désactiver le GPS sur l'appareil
2. Ouvrir une demande SOS
3. **Vérifier**: Un message d'erreur s'affiche
4. **Résultat attendu**: "Position GPS non disponible. Veuillez activer votre localisation."

### Test 4: Vérifier dans BreakdownTrackingScreen
1. Accepter une demande SOS
2. Aller sur l'écran de suivi
3. Regarder le bouton "Appeler le client"
4. **Vérifier**: L'ID utilisateur ne doit plus être visible
5. **Résultat attendu**: "Contacter pour plus d'informations"

## Checklist de validation

- [ ] Application se compile sans erreur
- [ ] Aucun ID utilisateur visible sur l'écran de détails SOS
- [ ] Aucun ID utilisateur visible sur l'écran de suivi SOS
- [ ] Distance correcte affichée quand GPS est activé
- [ ] Message d'erreur affiché quand GPS est désactivé ou invalide
- [ ] Temps estimé d'arrivée calculé correctement

## En cas de problème

### La distance est toujours incorrecte
**Cause possible**: Position du garage non récupérée
**Solution**: 
1. Vérifier que les permissions GPS sont accordées
2. Vérifier dans les logs que `garageLatitude` et `garageLongitude` ne sont pas null
3. Tester sur un appareil physique (l'émulateur peut avoir des problèmes GPS)

### L'ID utilisateur apparaît encore
**Cause possible**: Cache de build
**Solution**:
```powershell
.\gradlew clean
.\gradlew assembleDebug --rerun-tasks
```

### La distance ne s'affiche jamais (toujours "Calcul de la distance...")
**Cause possible**: Permission GPS refusée
**Solution**:
1. Aller dans Paramètres → Applications → Karhebti → Permissions
2. Accorder la permission "Localisation"
3. Redémarrer l'application

## Notes techniques

### Formule de calcul de distance
La distance est calculée avec la formule de Haversine qui donne la distance "à vol d'oiseau" entre deux points GPS.

**Limitation**: Cette distance ne prend PAS en compte:
- Les routes
- Le trafic
- Les obstacles

Pour une distance routière précise, il faudrait intégrer Google Maps Distance Matrix API ou OSRM.

### Seuil de validation
- Distance < 500 km → Considérée comme valide
- Distance ≥ 500 km → Considérée comme erreur de GPS

Ce seuil de 500 km est volontairement large. En pratique, un garage ne devrait jamais recevoir de demandes SOS à plus de 50-100 km.
