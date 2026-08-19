#!/usr/bin/env bash
# Idempotent Cloud Agent setup for the Techayan PDF Tools Android app.
# Installs the Android SDK command-line components required to build/test the
# app, points Gradle at the SDK, and warms the Gradle build cache.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
export ANDROID_SDK_ROOT
export ANDROID_HOME="$ANDROID_SDK_ROOT"

CMDLINE_TOOLS_VERSION="11076708"
CMDLINE_DIR="$ANDROID_SDK_ROOT/cmdline-tools/latest"
SDKMANAGER="$CMDLINE_DIR/bin/sdkmanager"

# 1. Install the SDK command-line tools if they are not already present.
if [ ! -x "$SDKMANAGER" ]; then
  echo "Installing Android SDK command-line tools ($CMDLINE_TOOLS_VERSION)..."
  tmp_dir="$(mktemp -d)"
  curl -fsSL -o "$tmp_dir/cmdline-tools.zip" \
    "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"
  unzip -q "$tmp_dir/cmdline-tools.zip" -d "$tmp_dir/extract"
  mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
  rm -rf "$CMDLINE_DIR"
  mv "$tmp_dir/extract/cmdline-tools" "$CMDLINE_DIR"
  rm -rf "$tmp_dir"
fi

# 2. Accept licenses and install the build components used by this project
#    (compileSdk 36 / build-tools 36.0.0 per app/build.gradle.kts).
yes | "$SDKMANAGER" --licenses >/dev/null 2>&1 || true
"$SDKMANAGER" --install \
  "platform-tools" \
  "platforms;android-36" \
  "build-tools;36.0.0" >/dev/null

# 3. Point Gradle at the SDK (local.properties is git-ignored).
echo "sdk.dir=$ANDROID_SDK_ROOT" > "$REPO_ROOT/local.properties"

# 4. Warm the Gradle distribution and dependency cache with a debug build.
cd "$REPO_ROOT"
chmod +x ./gradlew
./gradlew assembleDebug --no-daemon

echo "Android SDK ready at $ANDROID_SDK_ROOT"
