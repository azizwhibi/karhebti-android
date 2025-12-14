@echo off
echo ========================================
echo Push to gestionVoitures Branch
echo ========================================
echo.

REM Navigate to project directory
cd /d "%~dp0"

echo Current directory: %cd%
echo.

echo Checking current branch...
git branch --show-current
echo.

echo Adding all current changes...
git add .
echo.

echo Committing changes...
git commit -m "Complete project sync to gestionVoitures - %date% %time%"
if %errorlevel% neq 0 (
    echo No changes to commit or commit failed. Continuing anyway...
)
echo.

echo Switching to gestionVoitures branch...
git checkout gestionVoitures
if %errorlevel% neq 0 (
    echo Branch doesn't exist locally, creating it...
    git checkout -b gestionVoitures
)
echo.

echo WARNING: This will FORCE PUSH and replace all content in gestionVoitures branch!
echo Press any key to continue or Ctrl+C to cancel...
pause >nul
echo.

echo Resetting gestionVoitures branch to current state...
git checkout master
git branch -D gestionVoitures
git checkout -b gestionVoitures
echo.

echo Force pushing to origin/gestionVoitures (replacing old content)...
git push -f origin gestionVoitures
echo.

if %errorlevel% equ 0 (
    echo ========================================
    echo SUCCESS! Your project has been pushed to gestionVoitures branch.
    echo ========================================
) else (
    echo ========================================
    echo ERROR: Push failed. Please check your credentials and network.
    echo ========================================
)

echo.
echo Switching back to master branch...
git checkout master

echo.
pause
