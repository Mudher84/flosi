@echo off
setlocal
cd /d "%~dp0"
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo JAVA:
java -version
echo.
echo This project is pinned to AGP 8.13.2 / Gradle 8.13 / JVM target 17.
echo Open this folder in Android Studio and use:
echo   File ^> Sync Project with Gradle Files
echo   Build ^> Clean Project
echo   Build ^> Rebuild Project
echo   Build ^> Build APK(s)
pause
