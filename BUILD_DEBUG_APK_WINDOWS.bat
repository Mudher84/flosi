@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>&1 || (echo ERROR: Java not found & pause & exit /b 1)
if exist gradlew.bat (
  call gradlew.bat clean assembleDebug --stacktrace
) else (
  where gradle >nul 2>&1 || (echo ERROR: Gradle/gradlew not found. Open this folder in Android Studio once to install/sync Gradle. & pause & exit /b 1)
  gradle clean assembleDebug --stacktrace
)
if errorlevel 1 (echo BUILD FAILED & pause & exit /b 1)
echo.
echo APK:
dir /b app\build\outputs\apk\debug\*.apk
pause
