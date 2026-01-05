// ============================================
// SCRIPT DE NETTOYAGE MONGODB
// Répare ou supprime les documents corrompus
// ============================================

// Connexion à la base de données
use karhebti

print("=== DÉBUT DU NETTOYAGE DES DOCUMENTS CORROMPUS ===\n");

// 1. ANALYSE : Trouver tous les documents problématiques
print("1. Recherche des documents avec champ 'voiture' corrompu...");

const corruptedDocs = db.documents.find({
  $or: [
    // String qui contient "ObjectId" (objet sérialisé)
    { voiture: { $regex: /ObjectId/ } },
    // String trop longue (> 24 caractères)
    { voiture: { $type: "string", $exists: true } },
  ]
}).toArray();

print(`   Trouvé ${corruptedDocs.length} document(s) potentiellement corrompu(s)\n`);

// 2. VÉRIFICATION : Afficher les documents problématiques
if (corruptedDocs.length > 0) {
  print("2. Détails des documents corrompus :");
  corruptedDocs.forEach((doc, index) => {
    print(`\n   Document ${index + 1}:`);
    print(`   - ID: ${doc._id}`);
    print(`   - Type: ${doc.type}`);
    print(`   - Voiture (corrompu): ${typeof doc.voiture === 'string' ? doc.voiture.substring(0, 100) + '...' : doc.voiture}`);

    // Essayer d'extraire l'ID si possible
    if (typeof doc.voiture === 'string') {
      const match = doc.voiture.match(/'([0-9a-fA-F]{24})'/);
      if (match) {
        print(`   - ID extractible: ${match[1]}`);
      } else {
        print(`   - ❌ Impossible d'extraire l'ID`);
      }
    }
  });

  print("\n3. OPTIONS DE RÉPARATION :");
  print("   a) Supprimer tous les documents corrompus");
  print("   b) Réparer en extrayant l'ID (si possible)");
  print("   c) Mettre 'voiture' à null");

  print("\n=== CHOISISSEZ UNE OPTION ===\n");
  print("Décommentez l'option souhaitée dans le script\n");

  // ========================================
  // OPTION A : SUPPRIMER (DÉCOMMENTER)
  // ========================================
  /*
  print("OPTION A : Suppression des documents corrompus...");
  const deleteResult = db.documents.deleteMany({
    $or: [
      { voiture: { $regex: /ObjectId/ } },
      {
        voiture: {
          $type: "string",
          $exists: true,
          $not: { $regex: /^[0-9a-fA-F]{24}$/ }
        }
      }
    ]
  });
  print(`✅ ${deleteResult.deletedCount} document(s) supprimé(s)`);
  */

  // ========================================
  // OPTION B : RÉPARER (DÉCOMMENTER)
  // ========================================
  /*
  print("OPTION B : Réparation des documents...");
  let repaired = 0;
  let failed = 0;

  corruptedDocs.forEach(doc => {
    if (typeof doc.voiture === 'string') {
      // Essayer d'extraire l'ID
      const match = doc.voiture.match(/'([0-9a-fA-F]{24})'/);

      if (match) {
        const extractedId = match[1];

        // Vérifier que la voiture existe
        const carExists = db.voitures.findOne({ _id: ObjectId(extractedId) });

        if (carExists) {
          db.documents.updateOne(
            { _id: doc._id },
            { $set: { voiture: extractedId } }
          );
          print(`✅ Document ${doc._id} réparé (voiture: ${extractedId})`);
          repaired++;
        } else {
          print(`❌ Document ${doc._id} : voiture ${extractedId} n'existe pas`);
          // Option : mettre à null
          db.documents.updateOne(
            { _id: doc._id },
            { $set: { voiture: null } }
          );
          failed++;
        }
      } else {
        print(`❌ Document ${doc._id} : impossible d'extraire l'ID`);
        db.documents.updateOne(
          { _id: doc._id },
          { $set: { voiture: null } }
        );
        failed++;
      }
    }
  });

  print(`\n✅ Réparé: ${repaired}`);
  print(`⚠️ Mis à null: ${failed}`);
  */

  // ========================================
  // OPTION C : METTRE À NULL (DÉCOMMENTER)
  // ========================================
  /*
  print("OPTION C : Mise à null du champ 'voiture'...");
  const updateResult = db.documents.updateMany(
    {
      $or: [
        { voiture: { $regex: /ObjectId/ } },
        {
          voiture: {
            $type: "string",
            $exists: true,
            $not: { $regex: /^[0-9a-fA-F]{24}$/ }
          }
        }
      ]
    },
    { $set: { voiture: null } }
  );
  print(`✅ ${updateResult.modifiedCount} document(s) mis à jour`);
  */

} else {
  print("✅ Aucun document corrompu trouvé !");
}

// 4. VÉRIFICATION FINALE
print("\n4. Vérification finale...");
const remainingCorrupted = db.documents.count({
  $or: [
    { voiture: { $regex: /ObjectId/ } },
    {
      voiture: {
        $type: "string",
        $exists: true,
        $not: { $regex: /^[0-9a-fA-F]{24}$/ }
      }
    }
  ]
});

if (remainingCorrupted === 0) {
  print("✅ Tous les documents sont maintenant valides !");
} else {
  print(`⚠️ Il reste ${remainingCorrupted} document(s) corrompu(s)`);
}

print("\n=== FIN DU SCRIPT ===");

