@echo off
setlocal
cd /d "%~dp0"
where mvn >nul 2>nul || (
  echo Maven was not found. Install Apache Maven and add it to PATH.
  pause
  exit /b 1
)
echo Creating a Windows runtime image with JavaFX...
mvn clean javafx:jlink
if errorlevel 1 (
  echo Packaging failed.
  pause
  exit /b 1
)
echo Done. Launch target\vortex\bin\vortex.bat
pause
