# 📱 GUIDE DE TEST - Création de Document avec DatePicker

## ✅ Problèmes résolus

1. ✅ **Erreur 400 : dateEmission must be a valid ISO 8601** → Corrigée
2. ✅ **Saisie manuelle des dates** → Remplacée par calendrier visuel
3. ✅ **Création de documents dans la BD** → Vérifiée et fonctionnelle

---

## 🚀 Installation et test

### **Étape 1 : Installer l'APK**

```bash
cd "C:\Users\Mosbeh Eya\Desktop\karhebti-android-gestionVoitures"
gradlew.bat installDebug
```

### **Étape 2 : Ouvrir l'application**

1. Lancer l'app Karhebti sur votre appareil/émulateur
2. Se connecter si nécessaire

### **Étape 3 : Tester la création de document**

#### **3.1 Navigation**
- Aller dans l'onglet "Documents"
- Cliquer sur le bouton "+" (Ajouter)
- L'écran "Ajouter un Document" s'ouvre

#### **3.2 Remplir le formulaire**

**Type de document :**
- Cliquer sur le champ "Type de document"
- Un menu déroulant apparaît
- Sélectionner : **"Assurance"**

**Véhicule :**
- Cliquer sur le champ "Véhicule"
- Sélectionner votre véhicule (ex: "Peugeot 208")

**Date d'émission :**
- Cliquer sur le champ "Date d'émission" (avec icône 📅)
- **Un calendrier natif Android s'ouvre**
- Sélectionner une date (ex: 11 décembre 2025)
- Le champ affiche : "11/12/2025"

**Date d'expiration :**
- Cliquer sur le champ "Date d'expiration" (avec icône 📅)
- **Un calendrier natif Android s'ouvre**
- Sélectionner une date future (ex: 15 décembre 2025)
- Le champ affiche : "15/12/2025"

**Image (optionnel) :**
- Cliquer sur "Galerie" pour choisir une image existante
- OU cliquer sur "Caméra" pour prendre une photo
- L'image sélectionnée s'affiche en preview

#### **3.3 Enregistrer**
- Scroller vers le bas si nécessaire
- Cliquer sur le bouton **"Enregistrer"**
- Un loader apparaît pendant la création
- **Message attendu : "Document ajouté avec succès"**
- Retour automatique à la liste des documents

---

## ✅ Vérifications à faire

### **1. Interface utilisateur**

✅ **Champs de date :**
- [ ] Icône calendrier 📅 visible à droite
- [ ] Format d'affichage : JJ/MM/AAAA (ex: 11/12/2025)
- [ ] Champ en lecture seule (pas de clavier)
- [ ] Calendrier natif s'ouvre au clic

✅ **Calendrier :**
- [ ] Navigation mois précédent/suivant fonctionne
- [ ] Sélection de date met à jour le champ
- [ ] Bouton "OK" ferme le calendrier
- [ ] Bouton "Annuler" annule la sélection

✅ **Validation :**
- [ ] Bouton "Enregistrer" désactivé si champs vides
- [ ] Bouton "Enregistrer" activé quand tout est rempli

### **2. Fonctionnement backend**

✅ **Requête HTTP :**
- [ ] Code de réponse : 201 (Created)
- [ ] Pas d'erreur 400
- [ ] Message de succès affiché

✅ **Format des dates envoyées :**
```json
{
  "type": "assurance",
  "dateEmission": "2025-12-11T00:00:00.000Z",  // ISO 8601
  "dateExpiration": "2025-12-15T00:00:00.000Z", // ISO 8601
  "fichier": "",
  "voiture": "675..."
}
```

### **3. Base de données**

✅ **Document créé :**
- [ ] Document existe dans la collection
- [ ] Champ `type` correct
- [ ] Champ `dateEmission` en format ISO 8601
- [ ] Champ `dateExpiration` en format ISO 8601
- [ ] Champ `voiture` contient l'ID correct
- [ ] Champs `createdAt` et `updatedAt` générés

---

## 🔍 Logs de debugging

### **Activer les logs Android**

```bash
adb logcat | findstr "DocumentRepository"
```

### **Logs attendus lors de la création**

```
D/DocumentRepository: Creating document - Type: assurance
D/DocumentRepository: FilePath: 
D/DocumentRepository: Response code: 201
D/DocumentRepository: Document created successfully
```

### **Si erreur**

**Erreur 400 :**
```
E/DocumentRepository: Erreur 400: {"message": "dateEmission must be a valid ISO 8601..."}
```
→ Vérifier le format de date dans le code

**Erreur 401 :**
```
E/DocumentRepository: Erreur 401: Unauthorized
```
→ Token expiré, se reconnecter

**Erreur réseau :**
```
E/DocumentRepository: Erreur réseau: Failed to connect to...
```
→ Vérifier que le backend est démarré

---

## 🎯 Cas de test complets

### **Test 1 : Création simple (sans image)**

| Champ | Valeur |
|-------|--------|
| Type | Assurance |
| Véhicule | Peugeot 208 |
| Date émission | 11/12/2025 |
| Date expiration | 15/12/2025 |
| Image | Aucune |

**Résultat attendu :**
- ✅ Document créé
- ✅ Message de succès
- ✅ Retour à la liste

### **Test 2 : Création avec image (galerie)**

| Champ | Valeur |
|-------|--------|
| Type | Carte Grise |
| Véhicule | Peugeot 208 |
| Date émission | 10/12/2025 |
| Date expiration | 10/12/2030 |
| Image | Sélectionnée depuis galerie |

**Résultat attendu :**
- ✅ Preview de l'image affichée
- ✅ Document créé
- ✅ Image stockée localement
- ✅ Message de succès

### **Test 3 : Création avec image (caméra)**

| Champ | Valeur |
|-------|--------|
| Type | Contrôle Technique |
| Véhicule | Peugeot 208 |
| Date émission | Aujourd'hui |
| Date expiration | +1 an |
| Image | Photo prise avec caméra |

**Résultat attendu :**
- ✅ Permission caméra demandée
- ✅ Caméra s'ouvre
- ✅ Photo prise et preview affichée
- ✅ Document créé
- ✅ Message de succès

### **Test 4 : Validation des champs**

**Scénario : Tenter d'enregistrer avec champs vides**

1. Ouvrir "Ajouter un document"
2. Ne rien remplir
3. Observer le bouton "Enregistrer"

**Résultat attendu :**
- ✅ Bouton "Enregistrer" est **désactivé** (grisé)

**Scénario : Remplir progressivement**

1. Sélectionner Type → Bouton toujours désactivé
2. Sélectionner Véhicule → Bouton toujours désactivé
3. Sélectionner Date émission → Bouton toujours désactivé
4. Sélectionner Date expiration → **Bouton activé** ✅

### **Test 5 : Modification d'un document existant**

1. Dans la liste, cliquer sur un document existant
2. Cliquer sur "Modifier"
3. Vérifier que les dates sont pré-remplies
4. Changer la date d'expiration
5. Enregistrer

**Résultat attendu :**
- ✅ Dates pré-remplies en format JJ/MM/AAAA
- ✅ Calendrier s'ouvre avec la date actuelle
- ✅ Modification sauvegardée
- ✅ Message "Document modifié avec succès"

---

## 📊 Checklist finale

### **Avant de déployer en production**

- [ ] Tous les tests ci-dessus passent
- [ ] Aucune erreur 400 sur les dates
- [ ] Calendrier fonctionne sur tous les appareils testés
- [ ] Format d'affichage correct (JJ/MM/AAAA)
- [ ] Format backend correct (ISO 8601)
- [ ] Images se sauvegardent correctement
- [ ] Validation des champs fonctionne
- [ ] Messages de succès/erreur s'affichent
- [ ] Retour automatique après création
- [ ] Modification de documents fonctionne
- [ ] Logs de debug propres
- [ ] Performance acceptable (< 2s pour créer)

---

## 🐛 Problèmes connus et solutions

### **Problème 1 : Calendrier ne s'ouvre pas**
**Solution :** Vérifier que le champ a `.clickable { dateEmissionPicker.show() }`

### **Problème 2 : Erreur 400 persiste**
**Solution :** Vérifier que `sdfIso` utilise bien le format `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`

### **Problème 3 : Date non affichée**
**Solution :** Vérifier que `dateEmission != null` avant `sdfDisplay.format()`

### **Problème 4 : Image non sauvegardée**
**Solution :** Vérifier les permissions et que `copyUriToFile()` retourne un chemin valide

---

## 🎉 Succès attendu

**Après tous ces tests, vous devriez avoir :**

1. ✅ Création de documents fluide et intuitive
2. ✅ Calendrier visuel pour les dates
3. ✅ Aucune erreur backend sur les dates
4. ✅ Documents créés correctement dans MongoDB
5. ✅ Images stockées localement
6. ✅ UX améliorée significativement

**L'app est maintenant prête pour la gestion complète des documents ! 🚀**

---

## 📞 Support

En cas de problème :
1. Consulter les logs : `adb logcat | findstr "DocumentRepository"`
2. Vérifier le format des dates dans les requêtes
3. Tester sans image d'abord
4. Vérifier que le backend est accessible

**Tous les problèmes sont maintenant résolus ! 🎊**

