#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ -x ./gradlew ]]; then
  ./gradlew clean assembleDebug --stacktrace
elif command -v gradle >/dev/null 2>&1; then
  gradle clean assembleDebug --stacktrace
else
  echo "Gradle/gradlew not found. Open the project in Android Studio once to sync Gradle."
  exit 1
fi
find app/build/outputs/apk/debug -name '*.apk' -maxdepth 1 -print
