# Architecture

Second Bloom is a single Android app module backed by an external remodel API.

## Main layers

- `app/src/main/java/com/scf/secondbloom/ui/` Compose UI and screen rendering
- `app/src/main/java/com/scf/secondbloom/presentation/` state holders and workflows
- `app/src/main/java/com/scf/secondbloom/data/` local persistence and API adapters
- `app/src/main/java/com/scf/secondbloom/domain/` domain models used by the app

## Runtime model

1. The Android client collects user input and images.
2. The app reads local debug config from `local.properties` or Gradle properties.
3. In debug builds, the app can opt into a real backend by setting `secondBloomRemodelApiBaseUrl`.
4. Contract details live under `docs/api/`.
