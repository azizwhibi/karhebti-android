@echo off
echo ================================================
echo Test de compilation avec filtrage des garages
echo ================================================
echo.

cd /d "%~dp0"

echo Nettoyage du build...
call gradlew clean

echo.
echo Compilation en cours...
call gradlew assembleDebug --stacktrace

echo.
echo ================================================
echo Compilation terminee
echo ================================================
pause

