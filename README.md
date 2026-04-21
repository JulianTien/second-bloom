# Second Bloom

Second Bloom is an Android-only app for clothing remodel planning and style guidance. The repository contains the mobile client, product docs, and API contract references for the backend it talks to.

## Repository Layout

- `app/` Android application module
- `docs/` architecture, setup notes, and API references
- `gradle/`, `gradlew`, `gradlew.bat` Gradle wrapper and shared build configuration
- `PRD.md` product direction and detailed interaction notes

## Quick Start

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17
- Android SDK 36

### Local configuration

Copy the example file and fill in local values:

```properties
cp local.properties.example local.properties
```

Supported keys:

- `sdk.dir`
- `secondBloomRemodelApiBaseUrl`
- `secondBloomClerkPublishableKey`
- `secondBloomClerkJwtTemplate`

### Build and test

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleDebugAndroidTest
```

## API Contract

Second Bloom does not ship its backend in this repository. The Android client integrates against the contract documented in:

- `docs/api/mvp-contract.md`
- `docs/api/backend-architecture.md`
- `docs/api/backend-delivery-plan.md`

See [docs/api.md](docs/api.md) for a concise integration summary.

## Release and CI

- Android CI runs on changes under `app/`, Gradle files, and workflow files.
- Release workflow builds a debug APK for tags and manual runs.
- Dependabot monitors Gradle and GitHub Actions updates.

## Contributing

Start with [CONTRIBUTING.md](CONTRIBUTING.md), then review [docs/android-setup.md](docs/android-setup.md) before making build or dependency changes.
