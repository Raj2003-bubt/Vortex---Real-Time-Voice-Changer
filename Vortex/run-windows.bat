@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>nul || (
  echo Java was not found. Install JDK 17 or JDK 21 and try again.
  pause
  exit /b 1
)
where mvn >nul 2>nul || (
  echo Maven was not found. Install Apache Maven and add it to PATH.
  pause
  exit /b 1
)
echo Starting Vortex - Real-Time Voice Changer...
mvn clean javafx:run
if errorlevel 1 pause
