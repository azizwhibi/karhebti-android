# ============================================
# Script PowerShell pour nettoyer la base de données MongoDB
# Utilise le script cleanup_corrupted_documents_auto.js
# ============================================

Write-Host "=== NETTOYAGE DES DOCUMENTS CORROMPUS ===" -ForegroundColor Cyan
Write-Host ""

# Configuration MongoDB
$MONGO_URI = "mongodb+srv://your-cluster-url/karhebti"  # Modifier avec votre URL
$MONGO_DB = "karhebti"
$SCRIPT_PATH = "cleanup_corrupted_documents_auto.js"

# Vérifier si mongosh est installé
Write-Host "1. Vérification de mongosh..." -ForegroundColor Yellow
try {
    $mongoVersion = mongosh --version
    Write-Host "   ✓ mongosh trouvé: $mongoVersion" -ForegroundColor Green
} catch {
    Write-Host "   ✗ mongosh n'est pas installé ou n'est pas dans le PATH" -ForegroundColor Red
    Write-Host ""
    Write-Host "Pour installer mongosh:" -ForegroundColor Yellow
    Write-Host "   1. Télécharger depuis: https://www.mongodb.com/try/download/shell" -ForegroundColor White
    Write-Host "   2. Ou utiliser winget: winget install MongoDB.Shell" -ForegroundColor White
    Write-Host ""
    exit 1
}

# Vérifier si le script existe
Write-Host "2. Vérification du script de nettoyage..." -ForegroundColor Yellow
if (-Not (Test-Path $SCRIPT_PATH)) {
    Write-Host "   ✗ Script non trouvé: $SCRIPT_PATH" -ForegroundColor Red
    exit 1
}
Write-Host "   ✓ Script trouvé" -ForegroundColor Green

Write-Host ""
Write-Host "=== CONFIGURATION ===" -ForegroundColor Cyan
Write-Host "Base de données: $MONGO_DB" -ForegroundColor White
Write-Host "Script: $SCRIPT_PATH" -ForegroundColor White
Write-Host ""

# Demander confirmation
Write-Host "⚠️  ATTENTION: Ce script va modifier la base de données !" -ForegroundColor Yellow
Write-Host "   - Il va réparer les documents avec des données corrompues" -ForegroundColor White
Write-Host "   - Les champs 'voiture' invalides seront fixés ou mis à null" -ForegroundColor White
Write-Host ""
$confirmation = Read-Host "Voulez-vous continuer ? (o/n)"

if ($confirmation -ne "o" -and $confirmation -ne "O") {
    Write-Host "Opération annulée." -ForegroundColor Yellow
    exit 0
}

# Options de connexion
Write-Host ""
Write-Host "=== OPTIONS DE CONNEXION ===" -ForegroundColor Cyan
Write-Host "1. MongoDB Atlas (cloud)" -ForegroundColor White
Write-Host "2. MongoDB local (localhost)" -ForegroundColor White
Write-Host "3. URL personnalisée" -ForegroundColor White
Write-Host ""
$choice = Read-Host "Choisissez une option (1-3)"

switch ($choice) {
    "1" {
        $atlasUrl = Read-Host "Entrez l'URL MongoDB Atlas (ex: mongodb+srv://user:pass@cluster.mongodb.net/)"
        $MONGO_URI = $atlasUrl
    }
    "2" {
        $MONGO_URI = "mongodb://localhost:27017"
        Write-Host "   ✓ Connexion locale: $MONGO_URI" -ForegroundColor Green
    }
    "3" {
        $customUrl = Read-Host "Entrez l'URL MongoDB personnalisée"
        $MONGO_URI = $customUrl
    }
    default {
        Write-Host "Option invalide. Utilisation de localhost par défaut." -ForegroundColor Yellow
        $MONGO_URI = "mongodb://localhost:27017"
    }
}

# Exécuter le script de nettoyage
Write-Host ""
Write-Host "3. Exécution du script de nettoyage..." -ForegroundColor Yellow
Write-Host ""

try {
    # Si URL fournie, utiliser --host, sinon connexion locale
    if ($MONGO_URI -eq "mongodb://localhost:27017") {
        mongosh $MONGO_DB --file $SCRIPT_PATH
    } else {
        mongosh "$MONGO_URI/$MONGO_DB" --file $SCRIPT_PATH
    }

    Write-Host ""
    Write-Host "=== NETTOYAGE TERMINÉ ===" -ForegroundColor Green
    Write-Host ""
    Write-Host "Prochaines étapes:" -ForegroundColor Cyan
    Write-Host "   1. Vérifier les logs ci-dessus pour voir les résultats" -ForegroundColor White
    Write-Host "   2. Tester l'application Android" -ForegroundColor White
    Write-Host "   3. Vérifier que le document ID 693f2e6cdc8ae671ede64f67 fonctionne maintenant" -ForegroundColor White
    Write-Host ""

} catch {
    Write-Host ""
    Write-Host "✗ ERREUR lors de l'exécution du script" -ForegroundColor Red
    Write-Host "Erreur: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Vérifiez:" -ForegroundColor Yellow
    Write-Host "   - La connexion à MongoDB" -ForegroundColor White
    Write-Host "   - Les credentials d'authentification" -ForegroundColor White
    Write-Host "   - Que la base de données '$MONGO_DB' existe" -ForegroundColor White
    exit 1
}

# Pause pour lire les résultats
Write-Host ""
Write-Host "Appuyez sur une touche pour fermer..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

