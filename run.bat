@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo Java JDK 17 or newer is required. Install a JDK and reopen this window.
  pause
  exit /b 1
)
if not exist sqlite-jdbc.jar (
  echo sqlite-jdbc.jar is missing from this folder.
  pause
  exit /b 1
)
echo Compiling Digital Banking Management System...
javac -d . DigitalBankingSystem.java
if errorlevel 1 (
  echo Compilation failed.
  pause
  exit /b 1
)
java --enable-native-access=ALL-UNNAMED -cp ".;sqlite-jdbc.jar" bank.banking.DigitalBankingSystem
pause
