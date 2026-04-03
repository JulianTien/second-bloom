# Stabilize the Loop Android MVP and Prepare the First Real Backend Handoff

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This document lives at `PLANS.md` in the repository root and must be maintained as the source of truth for this work in accordance with the ExecPlan guidance supplied by the user.

## Purpose / Big Picture

This change matters because it turns Loop from a static interface prototype into a small but usable Android product flow with a branded first-run experience. After the completed work in this plan, a user can open a four-tab app shell, browse the inspiration feed, enter the AI remodel flow from the center action button, choose a clothing photo, run garment analysis, review and edit the extracted clothing summary, generate remodel suggestions, and see the latest saved results reflected inside the profile experience even after the app restarts. The app still defaults to mock data so the flow can be demonstrated today, but it now also contains the code and in-repository contract needed to connect a real backend next.

You can see the current behavior by installing the Android app on an emulator or device, confirming that it opens on the inspiration screen, entering the upload flow from the center AI action button, generating a remodel plan on the workbench screen, then opening the profile screen and confirming that the latest analysis and latest plan are shown there. You can also prove the code still holds together by running the Gradle test commands listed in this plan and observing `BUILD SUCCESSFUL`.

## Progress

- [x] (2026-04-03 16:34Z) Rewrote `PLANS.md` from a short status summary into a self-contained ExecPlan that a novice can use without prior context.
- [x] (2026-04-03 16:34Z) Confirmed that the current working tree supports the MVP flow `upload image -> analyze garment -> confirm or edit summary -> generate plans -> save locally -> view in profile` when the app runs in mock mode.
- [x] (2026-04-03 16:34Z) Added a real backend scaffold with `REMODEL_API_BASE_URL`, `REMODEL_USE_REAL_API`, `app/src/main/java/com/scf/loop/data/remote/RealRemodelApi.kt`, and `docs/api/mvp-contract.md`.
- [x] (2026-04-03 16:34Z) Added local history persistence through `app/src/main/java/com/scf/loop/data/local/RemodelHistoryRepository.kt` and `app/src/main/java/com/scf/loop/data/local/FileRemodelHistoryLocalDataSource.kt`.
- [x] (2026-04-03 16:34Z) Wired `app/src/main/java/com/scf/loop/presentation/remodel/RemodelViewModel.kt` to save analysis records and plan generation records and to expose them through `RemodelUiState`.
- [x] (2026-04-03 16:34Z) Replaced the profile placeholder with a real summary screen in `app/src/main/java/com/scf/loop/ui/screens/ProfileScreen.kt`.
- [x] (2026-04-03 18:23Z) Reworked the app shell into four top-level tabs plus a center AI remodel action in `app/src/main/java/com/scf/loop/ui/MainScreen.kt`, while keeping `HomeScreen` and `WorkbenchScreen` as a subflow.
- [x] (2026-04-03 18:23Z) Updated `AGENTS.md` so it matches the repository’s current data layer, backend scaffold, persistence layer, and real verification commands.
- [x] (2026-04-03 18:23Z) Fixed an app-start crash caused by `Screen` companion object initialization order by changing `Screen.topLevelItems` and related collections from static values to functions, and added `app/src/test/java/com/scf/loop/ScreenCollectionsTest.kt` to guard the tab collections.
- [x] (2026-04-03 18:23Z) Fixed bottom-tab navigation from the upload flow back to `灵感空间` by restoring an existing top-level destination with `popBackStack(...)` before falling back to `navigate(...)`, and added a Compose UI regression test for that path in `app/src/androidTest/java/com/scf/loop/RemodelScreensTest.kt`.
- [x] (2026-04-03 18:23Z) Verified `sh gradlew testDebugUnitTest` and `sh gradlew installDebug` after the runtime navigation fixes, then manually confirmed on the emulator that the app launches, the crash buffer stays empty, and `上传旧衣 -> 灵感空间` now returns to the inspiration feed.
- [ ] Enable a live backend that conforms to `docs/api/mvp-contract.md` and switch one debug build to `REMODEL_USE_REAL_API=true`.
- [ ] Run `sh gradlew connectedDebugAndroidTest` on a connected emulator or device. This is the repository’s real integration test command and has not yet been executed in this thread.
- [ ] Perform a manual on-device smoke test with a real image upload while the real backend is enabled and confirm the app still saves analysis and plans into local history.

## Surprises & Discoveries

- Observation: Running the two Gradle verification commands in parallel triggered a Kotlin daemon problem that looked like a code failure but was actually a build cache and compiler session issue.
  Evidence: the failed run reported `Could not close incremental caches` and `Detected multiple Kotlin daemon sessions`; running `sh gradlew --stop` and then rerunning the commands sequentially succeeded.

- Observation: A unit test that imported the `toDomain()` extension became brittle in the test compilation context, even though the production code compiled fine.
  Evidence: the failed run reported `Unresolved reference 'toDomain'` in `app/src/test/java/com/scf/loop/RemodelViewModelTest.kt`; the test became stable after replacing that assertion with direct calls to `BackgroundComplexity.fromWire(...)` and `ProcessingWarningCode.fromWire(...)`.

- Observation: The current local storage layer is intentionally not a database. It is a JSON snapshot written into the app’s private files directory with `AtomicFile`, which means writes are attempted safely and partial writes are less likely to leave broken state.
  Evidence: `app/src/main/java/com/scf/loop/data/local/FileRemodelHistoryLocalDataSource.kt` writes `remodel_history.json` through `AtomicFile.startWrite()`, `finishWrite(...)`, and `failWrite(...)`.

- Observation: `AGENTS.md` had drifted behind the repository and needed an explicit refresh after the data and persistence layers were added.
  Evidence: before the refresh, the file still described the app as UI-only even though the code already contained `app/src/main/java/com/scf/loop/data/remote/RealRemodelApi.kt` and `app/src/main/java/com/scf/loop/data/local/`. The file has since been updated.

- Observation: the four-tab redesign introduced a startup crash even though the code compiled and installed cleanly.
  Evidence: `adb logcat -b crash` reported `java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String com.scf.loop.navigation.Screen.getRoute()' on a null object reference` from `app/src/main/java/com/scf/loop/ui/components/LoopBottomNavBar.kt` during app launch. The issue disappeared after changing `Screen.topLevelItems()` and the other collections to functions that build the list after the sealed objects are initialized.

- Observation: returning from the AI remodel subflow to the inspiration feed was unreliable when the code always used `navigate(route)` for bottom tabs.
  Evidence: an emulator repro showed `上传旧衣` staying visible after tapping the bottom-bar `灵感空间` item, while the route changed correctly for non-start tabs such as `可持续星球`. After changing `MainScreen` to call `popBackStack(route, inclusive = false)` first, the same adb-driven repro showed `旧衣新生的灵感流。` on screen.

## Decision Log

- Decision: Keep `MockRemodelApi` as the default runtime path and put the real backend behind build flags.
  Rationale: the backend does not exist yet, so the Android app must remain demoable today while still allowing a clean switch to a live service later.
  Date/Author: 2026-04-03 / Codex

- Decision: Store history as a file-based JSON snapshot instead of introducing Room, which is Android’s common on-device database library.
  Rationale: the repository is still a single-module app and the MVP only needs a small amount of local history. File persistence is enough to prove that records survive app restarts without adding a heavy schema migration surface.
  Date/Author: 2026-04-03 / Codex

- Decision: Put the backend contract in `docs/api/mvp-contract.md` inside the repository instead of relying on an external document.
  Rationale: this plan must be self-contained and restartable from the working tree alone. Keeping the API contract in-repo allows the next contributor to align Android and backend work without outside context.
  Date/Author: 2026-04-03 / Codex

- Decision: Make the profile screen the first real consumer of persisted records.
  Rationale: the profile screen already existed as a placeholder, so turning it into a history summary page created a visible user benefit without expanding the navigation surface or inventing a new feature area.
  Date/Author: 2026-04-03 / Codex

- Decision: Treat sequential Gradle verification as the reliable acceptance path for this repository when the Kotlin daemon cache issue appears.
  Rationale: the sequential run is repeatable and produced clean `BUILD SUCCESSFUL` outputs for both verification commands.
  Date/Author: 2026-04-03 / Codex

- Decision: Rewrite this file as a true ExecPlan rather than a loose project memo.
  Rationale: the prior version captured status but did not give a novice enough context to continue implementation safely or validate behavior end to end.
  Date/Author: 2026-04-03 / Codex

- Decision: Keep the four top-level tabs and center AI action button, and preserve the upload and workbench screens as a nested remodel flow instead of first-level tabs.
  Rationale: this keeps the new branded product shell while preserving the already-working MVP analysis and plan-generation logic.
  Date/Author: 2026-04-03 / Codex

- Decision: Build `Screen` tab collections through functions instead of static companion object values.
  Rationale: Kotlin `sealed class` data objects can be observed before every object is initialized during class loading, and this repository now constructs bottom navigation very early in app startup. Lazy function-based list construction avoids null entries caused by initialization order.
  Date/Author: 2026-04-03 / Codex

- Decision: When leaving the remodel subflow for a top-level tab, restore the existing destination with `popBackStack(...)` before creating a new destination with `navigate(...)`.
  Rationale: the inspiration screen is the navigation graph start destination, and this approach reliably returns to it from subflow screens while still keeping state restoration for tabs that are not already on the stack.
  Date/Author: 2026-04-03 / Codex

## Outcomes & Retrospective

The main outcome so far is that Loop now behaves like a real MVP-shaped Android app even though the backend is still mocked by default. A user can open a redesigned four-tab shell, enter the remodel flow from the center action button, complete the core remodel journey, see state transitions for analysis and plan generation, and revisit the most recent saved work in the profile screen. The remaining gap is external, not architectural: the real backend described in `docs/api/mvp-contract.md` must be built and then tested from a live device or emulator.

The biggest lesson is that the simplest working shape was the right one for this repository. A small repository factory, a small file-based history store, a single `RemodelViewModel`, and a single `NavHost` were enough to make the app useful without introducing extra modules or a dependency injection container, which is a framework that automatically constructs and passes objects around. Another lesson is that runtime validation matters as much as compile-time validation: the startup crash and the failed return from the upload flow were both navigation bugs that only became obvious on the emulator, not from `assemble` alone.

## Context and Orientation

This repository is a single Android app module named `:app`. The app is written with Jetpack Compose, which is Android’s Kotlin-based user interface toolkit. The screens live under `app/src/main/java/com/scf/loop/ui`, the screen navigation lives in `app/src/main/java/com/scf/loop/ui/MainScreen.kt`, and the workflow state lives in `app/src/main/java/com/scf/loop/presentation/remodel/RemodelViewModel.kt`.

The word “mock” in this repository means a fake implementation that returns hard-coded results without talking to a server. That implementation is `app/src/main/java/com/scf/loop/data/remote/mock/MockRemodelApi.kt`. The word “real API” means a normal network call to a backend server. That implementation is `app/src/main/java/com/scf/loop/data/remote/RealRemodelApi.kt`. The word “repository” here means a small Kotlin class that hides where data comes from. `app/src/main/java/com/scf/loop/data/repository/DefaultRemodelRepository.kt` converts between UI-friendly domain models and remote request or response models. `app/src/main/java/com/scf/loop/data/repository/RemodelRepositoryFactory.kt` chooses either the mock API or the real API based on generated build flags.

The word “BuildConfig” means a generated Kotlin-accessible configuration class that Gradle creates during the build. In this app, `app/build.gradle.kts` defines `REMODEL_API_BASE_URL` and `REMODEL_USE_REAL_API`. When `REMODEL_USE_REAL_API` is `false` or the URL is blank, the app keeps using the mock API. When it is `true` and the URL is non-empty, the app uses `RealRemodelApi`.

The word “local persistence” means saving app data onto the device so it is still there after the app closes and reopens. That logic lives under `app/src/main/java/com/scf/loop/data/local/`. `RemodelHistoryRepository.kt` is the interface. `DefaultRemodelHistoryRepository.kt` is the implementation that shapes saved records. `FileRemodelHistoryLocalDataSource.kt` writes and reads the JSON snapshot. `RemodelHistoryRepositoryFactory.kt` builds the repository for the app.

The current navigation shell has four top-level tabs defined in `app/src/main/java/com/scf/loop/navigation/Screen.kt`: `灵感空间`, `数字衣橱`, `可持续星球`, and `我的主页`. The upload and plan screens are still present, but they now act as a remodel subflow opened from the center floating action button in `app/src/main/java/com/scf/loop/ui/components/LoopBottomNavBar.kt`. The user-visible screens involved in this plan are `app/src/main/java/com/scf/loop/ui/screens/InspirationScreen.kt`, `app/src/main/java/com/scf/loop/ui/screens/HomeScreen.kt`, `app/src/main/java/com/scf/loop/ui/screens/WorkbenchScreen.kt`, and `app/src/main/java/com/scf/loop/ui/screens/ProfileScreen.kt`. `InspirationScreen` is the first screen a user sees. `HomeScreen` handles image selection and analysis. `WorkbenchScreen` handles user confirmation and plan generation. `ProfileScreen` now shows saved results as part of the new profile-style layout. The state passed into the remodel screens and profile comes from `RemodelUiState` in `app/src/main/java/com/scf/loop/domain/model/RemodelModels.kt`.

The backend contract for the future service is `docs/api/mvp-contract.md`. That file describes the expected request and response bodies for `POST /analyze-garment` and `POST /generate-remodel-plans`. Any backend implementation for this app must either follow that contract or update the contract and the Android DTO mapping together in the same change.

The test commands for this repository are described in `AGENTS.md`. The most important commands are `sh gradlew testDebugUnitTest`, which runs host-side unit tests, and `sh gradlew connectedDebugAndroidTest`, which runs instrumentation tests on a device or emulator. `sh gradlew assembleDebugAndroidTest` only builds the instrumentation test application package. It is a useful build check, but it is not the same as actually running the tests on a device.

## Milestones

### Milestone 1: Make the main clothing remodel loop persist locally

This milestone is complete in the current working tree. The goal was to ensure that the app does more than animate between screens. The end result is that analysis results and generated plan results are stored locally and surfaced back to the user. The work happened in `app/src/main/java/com/scf/loop/presentation/remodel/RemodelViewModel.kt`, `app/src/main/java/com/scf/loop/domain/model/RemodelModels.kt`, and `app/src/main/java/com/scf/loop/data/local/`. You can verify the milestone by installing the app, walking through the home and workbench screens, then opening the profile screen and confirming it shows the latest saved analysis and plan. The proof command is `sh gradlew testDebugUnitTest`, which passes in the current tree.

### Milestone 2: Add a safe handoff point for a future real backend

This milestone is also complete in the current working tree. The goal was to preserve today’s demo flow while making room for a real server tomorrow. The end result is that `RemodelRepositoryFactory` can choose `RealRemodelApi` instead of `MockRemodelApi`, and the exact expected backend contract now lives in `docs/api/mvp-contract.md`. The proof commands are `sh gradlew testDebugUnitTest` and `sh gradlew assembleDebugAndroidTest`, both of which pass in the current tree. A backend developer can now open the repository and build against the contract without guessing Android’s request format.

### Milestone 3: Stabilize the four-tab shell and remodel subflow

This milestone is complete in the current working tree. The goal was to move the app from a narrow MVP tab layout into a branded first-run shell without breaking the remodel flow. The end result is that the app opens on `灵感空间`, the center action button still opens the upload flow, the app no longer crashes at startup due to `Screen` initialization order, and the bottom bar can now return from `上传旧衣` to `灵感空间`. The key files are `app/src/main/java/com/scf/loop/ui/MainScreen.kt`, `app/src/main/java/com/scf/loop/ui/components/LoopBottomNavBar.kt`, `app/src/main/java/com/scf/loop/navigation/Screen.kt`, `app/src/test/java/com/scf/loop/ScreenCollectionsTest.kt`, and `app/src/androidTest/java/com/scf/loop/RemodelScreensTest.kt`. The proof was a real emulator run that showed the app launch successfully, followed by an adb-driven tap flow that returned from the upload screen to the inspiration feed.

### Milestone 4: Connect and validate the first live backend

This milestone is still open. The goal is that a user can upload a real clothing photo, receive a server-produced analysis and server-produced plan suggestions, and still see those results saved into the profile screen afterward. The work should start in `docs/api/mvp-contract.md`, then continue in `app/src/main/java/com/scf/loop/data/remote/RealRemodelApi.kt`, `app/src/main/java/com/scf/loop/data/remote/dto/RemodelDtos.kt`, and `app/build.gradle.kts`. After the backend exists, enable the build flags, run the app on an emulator or device, and manually verify both network success and failure paths. The milestone is only complete when `sh gradlew connectedDebugAndroidTest` passes on a device or emulator and a manual smoke test shows a real uploaded image flowing through the app.

## Plan of Work

The current code already implements the local MVP loop and the four-tab app shell, so the remaining work is about making the real backend path trustworthy and preserving the stability of the navigation shell. Start by treating `docs/api/mvp-contract.md` as the in-repo source of truth. If the backend team needs to change a field name, error payload, or endpoint shape, update the contract first and then update the Kotlin data transfer objects in `app/src/main/java/com/scf/loop/data/remote/dto/RemodelDtos.kt`. Do not change only one side.

Next, keep `app/src/main/java/com/scf/loop/data/remote/RealRemodelApi.kt` small and explicit. It already performs multipart upload for image analysis and JSON posting for plan generation. If the backend introduces structured error bodies later, parse them here and keep the mapping into `InvalidImageException`, `ModelResponseException`, or `IOException` easy to read. The goal is that `RemodelViewModel` continues to handle only app-level states and messages, not raw HTTP logic.

Then wire the live backend build by editing the two `buildConfigField(...)` lines in `app/build.gradle.kts`. Set a real base URL and set `REMODEL_USE_REAL_API` to `true` in the debug build you want to exercise. Leave the mock mode available unless the backend is proven stable, because the repository still needs a predictable demo path.

After the backend path works, validate that the saved history path remains correct. The key files are `app/src/main/java/com/scf/loop/presentation/remodel/RemodelViewModel.kt`, which calls `saveAnalysis(...)` and `savePlanGeneration(...)`, and `app/src/main/java/com/scf/loop/ui/screens/ProfileScreen.kt`, which displays those saved records. If the live backend returns extra fields, do not store them casually. Extend `SavedAnalysisRecord` or `SavedPlanGenerationRecord` in `app/src/main/java/com/scf/loop/domain/model/RemodelModels.kt` only if the new field changes what a user can see or continue editing later.

At the same time, preserve the recent navigation fixes in `app/src/main/java/com/scf/loop/ui/MainScreen.kt` and `app/src/main/java/com/scf/loop/navigation/Screen.kt`. If the navigation graph changes again, rerun a real emulator flow from `灵感空间` into `上传旧衣` and back out through the bottom tabs. This repository already proved that compile success alone is not enough to catch navigation regressions.

## Concrete Steps

Work from the repository root:

    cd /Users/peng/AndroidStudioProjects/Loop

To verify the current working tree without a device, run:

    sh gradlew testDebugUnitTest

Expected short transcript:

    > Task :app:testDebugUnitTest
    BUILD SUCCESSFUL

To verify the instrumentation test package still builds, run:

    sh gradlew assembleDebugAndroidTest

Expected short transcript:

    > Task :app:assembleDebugAndroidTest
    BUILD SUCCESSFUL

If Gradle reports Kotlin daemon cache errors such as `Could not close incremental caches`, stop the daemons and rerun sequentially:

    sh gradlew --stop
    sh gradlew testDebugUnitTest && sh gradlew assembleDebugAndroidTest

To switch from mock mode to live backend mode, edit `app/build.gradle.kts` in the `defaultConfig` block so that these two lines use real values:

    buildConfigField("String", "REMODEL_API_BASE_URL", "\"https://your-backend.example\"")
    buildConfigField("boolean", "REMODEL_USE_REAL_API", "true")

Then build and install on a connected emulator or device:

    sh gradlew installDebug

If you have a running emulator or connected device, run the real integration tests:

    sh gradlew connectedDebugAndroidTest

After installation, open the app and use the screens in this order:

1. Confirm the app opens on `灵感空间`.
2. Tap the center AI action button to open the upload screen.
3. On the upload screen, choose a demo scenario or select an image and run analysis.
4. If the image is marked low confidence, decide whether to continue.
5. On the workbench screen, select a remodel intent and generate plans.
6. Return to the top-level tabs and open the profile screen to confirm the latest analysis and latest plan are visible.
7. Close and reopen the app and confirm the profile screen still shows the saved entries.

## Validation and Acceptance

The current acceptance bar for the repository has two parts: build verification and human-observable behavior.

For build verification, run `sh gradlew testDebugUnitTest` and expect `BUILD SUCCESSFUL`. Then run `sh gradlew assembleDebugAndroidTest` and expect `BUILD SUCCESSFUL`. These commands prove that the host-side tests and the instrumentation test build still hold together.

For user-visible behavior in mock mode, install the app and confirm it opens on the inspiration screen instead of crashing. Tap the center AI button to open the upload screen, load the low-confidence demo scenario, and trigger analysis. The app should show a warning card that tells the user the photo needs confirmation and offers the two actions to continue or retake. Then move to the workbench screen, select a remodel intent, generate a plan, and open the profile screen. The profile screen should show both a latest analysis summary and a latest plan summary. From the upload screen, tapping the bottom-bar `灵感空间` item should return to the inspiration feed. After the app restarts, the saved summaries should still be present because they are stored locally.

For user-visible behavior in live backend mode, enable `REMODEL_USE_REAL_API`, set a non-empty `REMODEL_API_BASE_URL`, upload a normal clothing image from the device, and confirm the analysis result is not coming from the demo scenarios. The app should still show either a normal analysis result or one of the expected error states: invalid image, network error, or model error. After a successful live response, the profile screen should still show the saved result. The change is only fully accepted when the same flow works with a real image and `sh gradlew connectedDebugAndroidTest` passes on hardware or an emulator.

## Idempotence and Recovery

All Gradle verification commands in this plan are safe to rerun. Repeating them does not damage the repository. If the Kotlin daemon issue appears again, the safe recovery is to run `sh gradlew --stop` and then rerun the commands sequentially.

Editing `app/build.gradle.kts` to enable the real backend is also safe to repeat, but be deliberate. If the real backend is unavailable and `REMODEL_USE_REAL_API` is still `true`, the app will surface network errors instead of mock data. To recover, either restore a working base URL or set `REMODEL_USE_REAL_API` back to `false`.

The local history store is disposable MVP data. If the saved history appears corrupted or confusing during development, the safe reset path is to clear the app’s data from the emulator or device, or uninstall and reinstall the app. The next app launch will recreate the local storage file and start from an empty history state.

## Artifacts and Notes

These short outputs are the most useful evidence captured so far:

    $ sh gradlew testDebugUnitTest
    > Task :app:testDebugUnitTest
    BUILD SUCCESSFUL in 8s

    $ sh gradlew assembleDebugAndroidTest
    > Task :app:assembleDebugAndroidTest
    BUILD SUCCESSFUL in 5s

    $ adb -s emulator-5554 logcat -d -b crash
    Starting: Intent { cmp=com.scf.loop/.MainActivity }

    $ adb-driven repro after fix
    content-desc="灵感空间页面"
    text="旧衣新生的灵感流。"
    content-desc="已选中，灵感空间，灵感空间页面"

This was the transient build problem that turned out to be environmental rather than a product regression:

    Could not close incremental caches
    Detected multiple Kotlin daemon sessions

This was the unit test compile failure that led to the simpler and more robust explicit enum conversion assertion:

    Unresolved reference 'toDomain'

## Interfaces and Dependencies

At the end of this work, the repository must continue to expose these stable interfaces and entry points.

`app/src/main/java/com/scf/loop/data/remote/RemodelApi.kt` is the remote abstraction. It must continue to define the two suspend functions that the repository relies on for analysis and plan generation.

`app/src/main/java/com/scf/loop/data/repository/DefaultRemodelRepository.kt` is the app-facing repository. It must continue to expose:

    suspend fun analyze(image: SelectedImage): GarmentAnalysis
    suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: GarmentAnalysis,
        userPreferences: String
    ): List<RemodelPlan>

`app/src/main/java/com/scf/loop/data/repository/RemodelRepositoryFactory.kt` must continue to expose `fun create(context: Context): RemodelRepository` and must keep the choice between mock and real remote implementations behind build flags.

`app/src/main/java/com/scf/loop/data/remote/RealRemodelApi.kt` must continue to accept a base URL and a function that opens an image stream from a URI string. This keeps the network code testable and keeps Android-specific URI reading out of the repository layer.

`app/src/main/java/com/scf/loop/data/local/RemodelHistoryRepository.kt` must continue to define save and read operations for the latest and recent analysis and plan records. The current methods are:

    suspend fun saveAnalysis(sourceImage: SelectedImage, analysis: GarmentAnalysis, savedAtEpochMillis: Long = System.currentTimeMillis()): SavedAnalysisRecord
    suspend fun savePlanGeneration(sourceImage: SelectedImage, analysis: GarmentAnalysis, intent: RemodelIntent, userPreferences: String, plans: List<RemodelPlan>, savedAtEpochMillis: Long = System.currentTimeMillis()): SavedPlanGenerationRecord
    suspend fun getLatestAnalysis(): SavedAnalysisRecord?
    suspend fun getLatestPlanGeneration(): SavedPlanGenerationRecord?
    suspend fun getRecentAnalyses(limit: Int = DefaultListLimit): List<SavedAnalysisRecord>
    suspend fun getRecentPlanGenerations(limit: Int = DefaultListLimit): List<SavedPlanGenerationRecord>

`app/src/main/java/com/scf/loop/presentation/remodel/RemodelViewModel.kt` must continue to be the single workflow entry point. The functions that the UI depends on are `onImageSelected(...)`, `analyzeSelectedImage()`, `continueWithLowConfidence()`, `clearError()`, the `update...` field editors, `selectIntent(...)`, `updateUserPreferences(...)`, and `generatePlans()`.

`app/src/main/java/com/scf/loop/ui/screens/ProfileScreen.kt` must continue to accept `state: RemodelUiState` and render the latest saved records from that state inside the profile-style layout. This is the current visible proof that local persistence works.

`app/src/main/java/com/scf/loop/navigation/Screen.kt` must continue to expose stable top-level and remodel-flow collections. The current API is:

    fun topLevelItems(): List<Screen>
    fun flowItems(): List<Screen>
    fun allItems(): List<Screen>

These functions intentionally replace static companion object lists because the static form caused a startup crash during object initialization.

`docs/api/mvp-contract.md` is a dependency for human coordination rather than for Kotlin compilation. It must remain aligned with `app/src/main/java/com/scf/loop/data/remote/dto/RemodelDtos.kt` and `app/src/main/java/com/scf/loop/data/remote/RealRemodelApi.kt`.

## Revision Note

Revision note (2026-04-03 16:34Z, Codex): rewrote `PLANS.md` from a short project-status memo into a full ExecPlan so that a contributor with only the repository and this file can understand the current MVP state, verify it, and continue the real-backend handoff safely.

Revision note (2026-04-03 18:23Z, Codex): updated the plan to reflect the current four-tab navigation shell, the completed `AGENTS.md` refresh, the startup crash fix, the bottom-tab return fix from the upload flow, and the latest emulator-based validation evidence.
