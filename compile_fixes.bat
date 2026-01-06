@echo off
echo ========================================
echo Compilation de l'application Karhebti
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] Nettoyage du build precedent...
call gradlew clean
if errorlevel 1 (
    echo ERREUR: Echec du nettoyage
    pause
    exit /b 1
)

echo.
echo [2/3] Compilation en mode Debug...
call gradlew assembleDebug
if errorlevel 1 (
    echo ERREUR: Echec de la compilation
    pause
    exit /b 1
)

echo.
echo [3/3] Verification des erreurs...
call gradlew lint --continue
if errorlevel 1 (
    echo ATTENTION: Quelques warnings detectes (non bloquants)
)

echo.
echo ========================================
echo ✅ COMPILATION TERMINEE AVEC SUCCES !
echo ========================================
echo.
echo L'APK est disponible dans:
echo app\build\outputs\apk\debug\
echo.
pause
