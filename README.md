# mc-calorie-counting

An offline-first Android calorie counter for the Mobile Computing project (MSc PDCS, UPB).
This is the **M1 prototype**: the app skeleton and architecture direction, with in-memory fake data.
The full spec is in [`docs/SPEC.md`](docs/SPEC.md); AI use is logged in [`AI_USE_LOG.md`](AI_USE_LOG.md).

## Toolchain

Pinned to the course labs (`2026-mc-labs/lab2-compose`): Gradle 9.7.0 (wrapper), AGP 9.4.1,
Kotlin 2.4.21, Compose BOM 2026.09.00 (Material 3), Navigation Compose 2.10.2, compileSdk/targetSdk 37,
minSdk 26, JVM target 17. All versions live in `gradle/libs.versions.toml`.

## Build and run

You need a full **JDK 17** (with `javac`; a JRE is not enough) and the Android SDK with **Android
SDK Platform 37** (Android Studio: *Settings → Languages & Frameworks → Android SDK*; AGP can also
download it on the first build). Open the folder in Android Studio and press Run, or from a terminal
(point `JAVA_HOME` at a JDK 17 if your default `java` is a JRE):

```bash
./gradlew assembleDebug          # build the debug APK
./gradlew testDebugUnitTest      # JVM unit tests
./gradlew installDebug           # install on a running emulator/device
```
