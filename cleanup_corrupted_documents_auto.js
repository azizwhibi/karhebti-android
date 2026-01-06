// ============================================
// SCRIPT DE NETTOYAGE MONGODB - VERSION AUTO
// Répare automatiquement les documents corrompus
// ============================================

// Connexion à la base de données
use karhebti

print("=== DÉBUT DU NETTOYAGE AUTOMATIQUE DES DOCUMENTS CORROMPUS ===\n");

// 1. ANALYSE : Trouver tous les documents problématiques
print("1. Recherche des documents avec champ 'voiture' corrompu...");

// Trouver les documents où voiture est un objet au lieu d'un ObjectId
const corruptedDocs = db.documents.find({
  voiture: { $type: "object" }
}).toArray();

print(`   Trouvé ${corruptedDocs.length} document(s) corrompu(s) avec objet dans 'voiture'\n`);

// 2. AFFICHER LES DÉTAILS
if (corruptedDocs.length > 0) {
  print("2. Détails des documents corrompus :");
  corruptedDocs.forEach((doc, index) => {
    print(`\n   Document ${index + 1}:`);
    print(`   - ID: ${doc._id}`);
    print(`   - Type: ${doc.type}`);
    print(`   - Voiture (structure): ${JSON.stringify(doc.voiture).substring(0, 150)}...`);

    // Vérifier si on peut extraire un ID
    if (doc.voiture && doc.voiture._id) {
      print(`   - ID extractible: ${doc.voiture._id}`);
    }
  });

  // 3. RÉPARATION AUTOMATIQUE
  print("\n3. Réparation automatique en cours...");
  let repaired = 0;
  let setToNull = 0;

  corruptedDocs.forEach(doc => {
    if (doc.voiture && doc.voiture._id) {
      // Extraire l'ID de la voiture
      const carId = doc.voiture._id.toString();

      // Vérifier que la voiture existe
      const carExists = db.voitures.findOne({ _id: ObjectId(carId) });

      if (carExists) {
        // Réparer en mettant juste l'ID
        db.documents.updateOne(
          { _id: doc._id },
          { $set: { voiture: ObjectId(carId) } }
        );
        print(`   ✅ Document ${doc._id} réparé (voiture: ${carId})`);
        repaired++;
      } else {
        // La voiture n'existe pas, mettre à null
        db.documents.updateOne(
          { _id: doc._id },
          { $set: { voiture: null } }
        );
        print(`   ⚠️ Document ${doc._id} : voiture inexistante, mis à null`);
        setToNull++;
      }
    } else {
      // Impossible d'extraire l'ID, mettre à null
      db.documents.updateOne(
        { _id: doc._id },
        { $set: { voiture: null } }
      );
      print(`   ⚠️ Document ${doc._id} : ID non extractible, mis à null`);
      setToNull++;
    }
  });

  print(`\n📊 RÉSULTATS :`);
  print(`   ✅ Documents réparés avec succès: ${repaired}`);
  print(`   ⚠️ Documents mis à null (voiture inexistante/invalide): ${setToNull}`);

} else {
  print("✅ Aucun document corrompu trouvé !");
}

// 4. VÉRIFICATION FINALE
print("\n4. Vérification finale...");
const remainingCorrupted = db.documents.countDocuments({
  voiture: { $type: "object" }
});

if (remainingCorrupted === 0) {
  print("✅✅✅ SUCCÈS ! Tous les documents sont maintenant valides !");
} else {
  print(`❌ ATTENTION ! Il reste ${remainingCorrupted} document(s) corrompu(s)`);
  print("   Veuillez vérifier manuellement ou réexécuter le script.");
}

// 5. STATISTIQUES FINALES
print("\n5. Statistiques finales...");
const totalDocs = db.documents.countDocuments({});
const docsWithCar = db.documents.countDocuments({ voiture: { $ne: null, $exists: true } });
const docsWithoutCar = db.documents.countDocuments({ $or: [{ voiture: null }, { voiture: { $exists: false } }] });

print(`   📄 Total de documents: ${totalDocs}`);
print(`   🚗 Documents avec voiture: ${docsWithCar}`);
print(`   ⚪ Documents sans voiture: ${docsWithoutCar}`);

print("\n=== FIN DU SCRIPT - NETTOYAGE TERMINÉ ===");

