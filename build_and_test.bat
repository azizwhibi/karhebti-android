@echo off
echo ======================================
echo   COMPILATION KARHEBTI ANDROID
echo   Backend: Render (HTTPS)
echo ======================================
echo.

echo [1/3] Nettoyage du projet...
call gradlew clean

echo.
echo [2/3] Compilation en cours...
call gradlew assembleDebug --stacktrace

echo.
echo [3/3] Verification des erreurs...
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ COMPILATION REUSSIE !
    echo.
    echo APK genere : app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Pour installer sur un appareil connecte :
    echo   adb install -r app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo ❌ ERREUR DE COMPILATION
    echo Consultez les logs ci-dessus pour plus de details
    echo.
)

pause

