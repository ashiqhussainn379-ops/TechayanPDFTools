# AGENTS.md

## Cursor Cloud specific instructions

This repo is a single-module native **Android** app — **"Techayan PDF Tools"** (`applicationId com.techayan.pdfeditor`), an image-to-PDF converter built with Kotlin + Jetpack Compose. The only Gradle module is `:app`.

### Toolchain / SDK
- JDK 21 is preinstalled and is what Gradle uses.
- The Android SDK lives at `$HOME/android-sdk` (installed during environment setup, persisted in the VM snapshot). `local.properties` (gitignored) points Gradle at it via `sdk.dir`. The update script recreates `local.properties` if it is missing.
- `gradlew` in a fresh checkout does **not** have the executable bit set. Run wrapper commands as `sh ./gradlew ...` (the update script also `chmod +x gradlew`).

### Build / test / lint (fast, no emulator needed)
- Build debug APK: `sh ./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
- Unit tests (JVM): `sh ./gradlew test`
- Lint: `sh ./gradlew lint` → report at `app/build/reports/lint-results-debug.html`

### Running the app / instrumented tests (emulator) — important caveat
- **There is no KVM / hardware virtualization in this VM.** The Android emulator therefore runs in pure software mode (`-no-accel -gpu swiftshader_indirect`), which is **slow and unstable**: cold boot takes roughly 5–10 minutes, and `system_server`/`systemui` frequently raise "Process system isn't responding" ANR dialogs that block input dispatch. Building/unit-test/lint do not need the emulator and are the reliable path.
- An AVD named `test34` (system image `android-34;google_apis;x86_64`) is preinstalled. Start it headless with extra resources to reduce ANRs:
  - `emulator -avd test34 -no-window -no-audio -no-boot-anim -no-snapshot -gpu swiftshader_indirect -no-accel -memory 4096 -cores 4`
  - Wait for `adb shell getprop sys.boot_completed` to return `1`, then `adb install -r app/build/outputs/apk/debug/app-debug.apk` and `adb shell am start -n com.techayan.pdfeditor/.MainActivity`.
  - Because the emulated `system_server` is overloaded, `adb shell input tap/keyevent` events are often not dispatched while an ANR dialog is showing, so interactive UI automation and `connectedAndroidTest` are flaky here. Prefer JVM unit tests when possible.
- `connectedDebugAndroidTest` also fails on the stale sample test `app/src/androidTest/java/com/example/techayan/ExampleInstrumentedTest.kt`, which asserts the old package name `com.example.techayan` instead of the current `com.techayan.pdfeditor`.
