# Android Setup

## Prerequisites

- JDK 17
- Android Studio with SDK 36

## First run

1. Copy `local.properties.example` to `local.properties`.
2. Set `sdk.dir` to your local Android SDK.
3. Optionally set `secondBloomRemodelApiBaseUrl` to enable the real backend in debug builds.

## Useful commands

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleDebugAndroidTest
./gradlew connectedDebugAndroidTest
```

If Gradle daemon state gets stale, stop it with:

```bash
./gradlew --stop
```
