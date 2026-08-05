@echo off
setlocal
cd /d "%~dp0"
where mvn >nul 2>nul || (
  echo Maven was not found. Install Apache Maven and add it to PATH.
  pause
  exit /b 1
)
echo Building project...
mvn clean package
if errorlevel 1 (
  echo Build failed.
  pause
  exit /b 1
)
echo Build completed. Output is in the target folder.
pause
