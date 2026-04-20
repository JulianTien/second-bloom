# Stabilize the Second Bloom Android MVP and Prepare the First Real Backend Handoff

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This document lives at `PLANS.md` in the repository root and must be maintained as the source of truth for this work in accordance with the ExecPlan guidance supplied by the user.

## Purpose / Big Picture

This change matters because it turns Second Bloom from a static interface prototype into a small but usable Android product flow with a branded first-run experience. After the completed work in this plan, a user can open a four-tab app shell, browse the inspiration feed, enter the AI remodel flow from the center action button, choose a clothing photo, run garment analysis, review and edit the extracted clothing summary, generate remodel suggestions, and see the latest saved results reflected across the profile, wardrobe, and sustainability experiences even after the app restarts. The app still defaults to mock data so the flow can be demonstrated today, but it now also contains the code and in-repository contract needed to connect a real backend next.

You can see the current behavior by installing the Android app on an emulator or device, confirming that it opens on the inspiration screen, entering the upload flow from the center AI action button, generating a remodel plan on the workbench screen, then opening the wardrobe, profile, and sustainability tabs and confirming that each one reflects the same saved history in a different way. You can also prove the code still holds together by running the Gradle test commands listed in this plan and observing `BUILD SUCCESSFUL`.

## Progress

- [x] (2026-04-20 13:13 CST) Published the latest Second Bloom submission artifacts into `/Volumes/TechGirls/2026/提交材料-最新版/Second_Bloom` as a flat package with no nested subfolders inside the project folder, copying the current project info, ethics statement, adoption-plan DOCX, learning-journey files, bibliography PDF/Markdown, app usage guide PDF, source-code zip, and team-photo summary.
- [x] (2026-04-20 11:42 CST) Removed the older `second_bloom_user_adoption_plan_final.docx` from the Obsidian `Second_Bloom_Workspace` after importing the newer `user_adoption_plan.docx`, leaving both the Obsidian workspace and NAS mirror with a single adoption-plan file under `04_user_adoption_plan`.
- [x] (2026-04-20 00:02 CST) Distributed the refreshed deliverables from `/Users/peng/Downloads/files` into the matching submission subfolders under the Obsidian `Second_Bloom_Workspace` and mirrored the same set into `/Volumes/TechGirls/2026/Second_Bloom_Workspace`, adding the updated project info, ethics statement, learning-journey files, bibliography PDF, and user-adoption-plan DOCX to both locations.
- [x] (2026-04-19 02:13 CST) Tightened the last two text deliverables toward final-submission form by rewriting `project_info.md` into explicit `Project Name` plus `Project Description` fields and replacing the loose `team_photo_summary_template.md` file with the final `team_photo_summary.md` version.
- [x] (2026-04-19 01:14 CST) Removed the stale `source_code_submission_guide.md` file from the submission folder so `/06_source_code` now keeps only the final `second_bloom_source_code_submission.zip` artifact and the standalone `second_bloom_app_usage_guide.pdf`.
- [x] (2026-04-19 01:09 CST) Rebuilt the source-code submission zip into a cleaner final shape: `Second_Bloom_Submission/` now contains `frontend_source/`, `backend_source/`, `second_bloom_app_debug.apk`, and `APP_USAGE_GUIDE.pdf`, while excluding dependency folders, cache/build output, Vercel metadata, and docs from the packaged source trees.
- [x] (2026-04-19 01:05 CST) Rebuilt the formal source-code submission zip to include the Android source tree, the debug APK (`second_bloom_app_debug.apk`), and the formal `APP_USAGE_GUIDE.pdf`, while keeping the old standalone judge-access PDF removed from the submission folder.
- [x] (2026-04-19 01:02 CST) Replaced the Markdown app-usage draft with a formal PDF usage guide that includes basic app information plus demo login information, deleted the standalone `second_bloom_judge_demo_access.pdf` artifact, and rebuilt the source-code submission zip so it now includes only `APP_USAGE_GUIDE.pdf` as the judge-facing access/usage document.
- [x] (2026-04-19 00:56 CST) Replaced the loose `judge_demo_access.md` draft with a formal judge-facing access document, exported it as `/Users/peng/Library/Mobile Documents/iCloud~md~obsidian/Documents/SCF/Loop_Second_Bloom_Workspace/01_Submission_Materials/06_source_code/second_bloom_judge_demo_access.pdf`, and rebuilt the source-code submission zip so it now includes `JUDGE_DEMO_ACCESS.pdf` together with `APP_USAGE_GUIDE.md`.
- [x] (2026-04-19 00:45 CST) Cleaned `/Users/peng/Library/Mobile Documents/iCloud~md~obsidian/Documents/SCF/Loop_Second_Bloom_Workspace/01_Submission_Materials/04_user_adoption_plan` down to the single formal submission artifact `second_bloom_user_adoption_plan_final.docx`, deleting the Markdown working drafts (`first_year_rollout.md`, `user_adoption_plan.md`, and `user_testing_feedback_log.md`) so the folder now matches the intended final-submission shape.
- [x] (2026-04-19 00:37 CST) Completed the official junior-division User Adoption Plan Word artifact at `/Users/peng/Library/Mobile Documents/iCloud~md~obsidian/Documents/SCF/Loop_Second_Bloom_Workspace/01_Submission_Materials/04_user_adoption_plan/second_bloom_user_adoption_plan_final.docx`, using the supplied official template structure, verified the exported text content, rendered it to PDF/PNG for a quick layout check, and cleaned the temporary DOCX working directory afterward.
- [x] (2026-04-19 00:28 CST) Created the formal source-code submission payload at `/Users/peng/Library/Mobile Documents/iCloud~md~obsidian/Documents/SCF/Loop_Second_Bloom_Workspace/01_Submission_Materials/06_source_code/second_bloom_source_code_submission.zip`, confirmed it contains the Android source tree, API docs, and an English `APP_USAGE_GUIDE.md`, and removed internal working-residue files (`readme_submission_checklist.md`, `evidence_gap_log.md`, `video_link_placeholders.md`, and `05_learning_journey/image_plan.md`) from the submission folder to keep the package closer to a real final-delivery state.
- [x] (2026-04-19 00:05 CST) Captured six Android emulator screenshots for Second Bloom and saved them to `/Users/peng/Library/Mobile Documents/iCloud~md~obsidian/Documents/SCF/Loop_Second_Bloom_Workspace/03_Design_Assets/App_Screenshots`, covering the inspiration home screen, wardrobe screen, planet impact screen, profile screen, upload-and-analyze entry screen, and the system photo picker step used by the app's upload flow.
- [x] (2026-04-16 03:21 CST) Removed all files from `/Users/peng/Library/Mobile Documents/iCloud~md~obsidian/Documents/SCF/Loop_Second_Bloom_Workspace/01_Submission_Materials/01_Pitch_Video` and `02_Technical_Video` at the user's request so the existing source scripts, draft subtitles, and checklist materials no longer block manual video import; verified both video directories remain present but contain no files.
- [x] (2026-04-16 03:14 CST) Migrated the youth submission materials into `/Users/peng/Library/Mobile Documents/iCloud~md~obsidian/Documents/SCF/Loop_Second_Bloom_Workspace/01_Submission_Materials` by reusing and renaming the empty legacy submission folders, preserving the existing Second Bloom source PDFs and DOCX in place, adding the required English youth-deliverable templates and support files, verifying the new package contains no `NyxGuard` references and respects the stated word limits, and deleting the mistakenly created package from `Nyx_Guard_Workspace` after verification.
- [x] (2026-04-03 16:34Z) Rewrote `PLANS.md` from a short status summary into a self-contained ExecPlan that a novice can use without prior context.
- [x] (2026-04-03 16:34Z) Confirmed that the current working tree supports the MVP flow `upload image -> analyze garment -> confirm or edit summary -> generate plans -> save locally -> view in profile` when the app runs in mock mode.
- [x] (2026-04-03 16:34Z) Added a real backend scaffold with `REMODEL_API_BASE_URL`, `REMODEL_USE_REAL_API`, `app/src/main/java/com/scf/secondbloom/data/remote/RealRemodelApi.kt`, and `docs/api/mvp-contract.md`.
- [x] (2026-04-03 16:34Z) Added local history persistence through `app/src/main/java/com/scf/secondbloom/data/local/RemodelHistoryRepository.kt` and `app/src/main/java/com/scf/secondbloom/data/local/FileRemodelHistoryLocalDataSource.kt`.
- [x] (2026-04-03 16:34Z) Wired `app/src/main/java/com/scf/secondbloom/presentation/remodel/RemodelViewModel.kt` to save analysis records and plan generation records and to expose them through `RemodelUiState`.
- [x] (2026-04-03 16:34Z) Replaced the profile placeholder with a real summary screen in `app/src/main/java/com/scf/secondbloom/ui/screens/ProfileScreen.kt`.
- [x] (2026-04-03 18:23Z) Reworked the app shell into four top-level tabs plus a center AI remodel action in `app/src/main/java/com/scf/secondbloom/ui/MainScreen.kt`, while keeping `HomeScreen` and `WorkbenchScreen` as a subflow.
- [x] (2026-04-03 18:23Z) Updated `AGENTS.md` so it matches the repository’s current data layer, backend scaffold, persistence layer, and real verification commands.
- [x] (2026-04-03 18:23Z) Fixed an app-start crash caused by `Screen` companion object initialization order by changing `Screen.topLevelItems` and related collections from static values to functions, and added `app/src/test/java/com/scf/secondbloom/ScreenCollectionsTest.kt` to guard the tab collections.
- [x] (2026-04-03 18:23Z) Fixed bottom-tab navigation from the upload flow back to `灵感空间` by restoring an existing top-level destination with `popBackStack(...)` before falling back to `navigate(...)`, and added a Compose UI regression test for that path in `app/src/androidTest/java/com/scf/secondbloom/RemodelScreensTest.kt`.
- [x] (2026-04-03 18:23Z) Verified `sh gradlew testDebugUnitTest` and `sh gradlew installDebug` after the runtime navigation fixes, then manually confirmed on the emulator that the app launches, the crash buffer stays empty, and `上传旧衣 -> 灵感空间` now returns to the inspiration feed.
- [x] (2026-04-03 18:31Z) Reworked `app/build.gradle.kts` so debug builds read `secondBloomRemodelApiBaseUrl` from Gradle properties or `local.properties`, keep mock mode as the default fallback, and automatically enable `REMODEL_USE_REAL_API` only when the URL is non-blank.
- [x] (2026-04-03 18:31Z) Hardened `app/src/main/java/com/scf/secondbloom/data/remote/RealRemodelApi.kt` so empty or invalid success payloads become `ModelResponseException`, structured JSON errors surface readable messages, and HTML or blank error bodies fall back to clear status-based user messages.
- [x] (2026-04-03 18:31Z) Added host-side verification for the real backend path with `app/src/test/java/com/scf/secondbloom/RealRemodelApiTest.kt`, `app/src/test/java/com/scf/secondbloom/RemodelDtosTest.kt`, and `app/src/test/java/com/scf/secondbloom/RemodelRepositoryFactoryTest.kt`.
- [x] (2026-04-03 18:31Z) Verified `sh gradlew testDebugUnitTest` and `sh gradlew assembleDebugAndroidTest` after the real-backend closeout changes.
- [x] (2026-04-03 18:48Z) Renamed the project branding from `Loop` to `Second Bloom` across app strings, Gradle project name, Android package and namespace (`com.scf.secondbloom`), theme/component identifiers, tests, and project documentation while preserving the existing MVP behavior.
- [x] (2026-04-03 18:48Z) Re-verified `sh gradlew testDebugUnitTest` and `sh gradlew assembleDebugAndroidTest` after the `Loop -> Second Bloom` rename.
- [x] (2026-04-03 19:01Z) Renamed the repository root directory from `/Users/peng/AndroidStudioProjects/Loop` to `/Users/peng/AndroidStudioProjects/Second Bloom`, updated Android Studio workspace metadata to the new project name and package identifiers, and re-verified Gradle from the new path.
- [x] (2026-04-03 19:48Z) Productized the local-history path into a cross-tab MVP by deriving wardrobe entries and sustainability impact summaries from saved analysis and plan records inside `RemodelUiState`, wiring `WardrobeScreen` and `PlanetScreen` to those derived models, and adding CTA buttons that route back into the AI remodel flow.
- [x] (2026-04-03 19:48Z) Added regression coverage for the new history-driven tabs with `app/src/test/java/com/scf/secondbloom/RemodelDerivedModelsTest.kt` plus expanded Compose UI coverage in `app/src/androidTest/java/com/scf/secondbloom/RemodelScreensTest.kt`, then re-verified `sh gradlew testDebugUnitTest` and `sh gradlew assembleDebugAndroidTest`.
- [x] (2026-04-03 19:48Z) Extended the same saved-history derivation approach into the profile experience by adding recent activity summaries and sustainability highlights to `app/src/main/java/com/scf/secondbloom/ui/screens/ProfileScreen.kt`, then expanded both unit and Compose coverage and re-verified `sh gradlew testDebugUnitTest` and `sh gradlew assembleDebugAndroidTest`.
- [x] (2026-04-04 00:00Z) Added `docs/api/backend-architecture.md` as the new system-level backend design document so the repository now documents backend scope, runtime components, storage boundaries, security expectations, observability, and the phased evolution path in addition to the wire contract.
- [x] (2026-04-04 00:10Z) Added `docs/api/backend-delivery-plan.md` and `docs/api/postgres-schema-draft.sql` so the repository now includes a concrete backend reference stack recommendation, endpoint implementation order, rollout checklist, and a first SQL draft for operational persistence.
- [x] (2026-04-04 02:10Z) Implemented the first runnable backend in the sibling repository path `/Users/peng/AndroidStudioProjects/second-bloom-backend` with FastAPI endpoints, fake and real provider paths, Alembic migrations, Postgres and MinIO local tooling, and automated backend tests.
- [x] (2026-04-04 02:10Z) Reconciled the backend docs with the locked Android-facing contract by keeping `intent` on `daily / occasion / diy / size_adjustment`, limiting `backgroundComplexity` to `low / high`, and documenting the public `{message}` error envelope.
- [x] (2026-04-04 04:13Z) Restored the local Android command-line toolchain on this machine by exporting `ANDROID_SDK_ROOT`, `ANDROID_HOME`, and Android Studio / SDK paths from `~/.zshrc`, then verified `adb`, `emulator`, and `studio` resolve in a fresh shell, launched the `Pixel_9_Pro_XL` AVD, confirmed `adb devices -l` reports `emulator-5554`, confirmed both `sys.boot_completed=1` and `dev.bootcomplete=1`, and captured a proof screenshot at `/tmp/secondbloom-emulator.png`.
- [x] (2026-04-04 05:00Z) Pointed `secondBloomRemodelApiBaseUrl` at the sibling backend on `http://10.0.2.2:8000`, added a debug-only cleartext manifest override, validated `POST /analyze-garment` and `POST /generate-remodel-plans` from the emulator against the live HTTP backend, and confirmed the resulting analysis and plan records were saved into local profile history.
- [x] (2026-04-04 06:00Z) Updated the repository docs for the vNext visual preview contract so `POST /generate-remodel-plans` now returns stable `planId` values and the preview flow is documented as an async API + Worker + Redis + Postgres + Object Storage pipeline with capped job size and explicit job and render statuses.
- [x] (2026-04-04 09:20Z) Implemented the first end-to-end visual preview slice across both repos: the sibling backend now exposes `POST /generate-remodel-preview-jobs`, `GET /remodel-preview-jobs/{previewJobId}`, and preview asset URLs with a fake async lifecycle, while the Android app now maps `planId`, starts preview polling after plan generation, renders before/after/comparison images on the workbench screen, and keeps local history independent from preview success.
- [x] (2026-04-04 08:35Z) Upgraded the wardrobe history flow so analyzed garments with saved plans now link back into editable plan state: wardrobe entries now carry the latest related plan-generation record id, `RemodelViewModel` can restore a saved plan generation into the active workbench context, and the wardrobe cards expose an explicit history-edit entry that reopens the plan workspace for further editing.
- [x] (2026-04-04 08:55Z) Added a lightweight local publish flow for final effect images: the preview result page now exposes a publish action once a final image is ready, published remodels persist in the app history snapshot, and the inspiration feed now prepends the user’s newest published remodel cards ahead of the static showcase feed.
- [x] (2026-04-04 09:20Z) Verified the upgraded preview flow with `sh gradlew testDebugUnitTest` in the Android repo plus `.venv/bin/pytest -q tests/contract tests/unit` and a FastAPI `TestClient` smoke sequence in the sibling backend repo that exercised `analyze -> generate plans -> create preview job -> poll running -> poll completed`.
- [x] (2026-04-04 09:55Z) Replaced the fake preview image body with a real DashScope Qwen image generation path in the sibling backend, added Qwen-specific environment variables and README guidance, configured the ignored local `.env` to use `qwen-image-2.0-pro` on the Beijing endpoint, and verified one live `analyze -> plans -> preview job -> completed` flow against the actual provider while keeping Android-side polling and rendering unchanged.
- [x] (2026-04-04 11:40Z) Upgraded the sibling backend from text-to-image preview generation to a Qwen-based "recognize -> 3 plans -> confirm 1 plan -> image edit" flow by adding Qwen Chat providers for garment analysis and plan generation, switching the preview image provider to Qwen image edit with fallback support, wiring original-image object URLs into the provider inputs, and keeping async preview jobs plus before/after/comparison assets intact.
- [x] (2026-04-04 11:40Z) Realigned the Android app with the new single-plan final-edit flow by stopping automatic preview creation after plan generation, adding per-plan confirmation in the workbench UI, creating preview jobs for only the confirmed `planId`, and updating unit tests to assert "confirm then render" instead of "auto render all".
- [x] (2026-04-04 11:40Z) Re-verified the upgraded contracts with `.venv/bin/pytest -q` in the sibling backend and `sh gradlew testDebugUnitTest --console=plain` in the Android repo; both completed successfully after the single-plan preview and Qwen-provider refactor.
- [x] (2026-04-04 12:20Z) Added a local `data:` URL fallback inside the sibling backend so Qwen-VL and Qwen image edit can still receive the original uploaded image when `OPERATIONAL_PERSISTENCE_ENABLED=false`, which makes the real backend runnable on machines without Docker or MinIO.
- [x] (2026-04-04 12:20Z) Restarted the sibling backend on `http://127.0.0.1:8000`, confirmed the latest OpenAPI now exposes the single-plan preview contract (`planId`, `POST /generate-remodel-preview-jobs`, `GET /remodel-preview-jobs/{previewJobId}`), and ran a live smoke sequence against the real Qwen path.
- [x] (2026-04-04 12:20Z) Verified the live smoke reached `analyze -> 3 plans -> create preview job -> poll running -> filtered` with the new Qwen stack. The final image was blocked by visual QA when using a synthetic locally drawn T-shirt placeholder, which validates the async true-edit pipeline and QA gate even though it is not yet proof of a production-quality result on a real garment photo.
- [x] (2026-04-04 12:45Z) Tightened the true-edit failure handling after reproducing a real QA rejection: the sibling backend now retries once with a stricter preserve-the-original prompt when visual QA rejects the first render, returns `completed_with_failures` instead of a generic hard failure for QA-filtered results, and surfaces the actual QA reason back to Android.
- [x] (2026-04-04 12:45Z) Added backend unit coverage for the new retry path in `tests/unit/test_preview_render_service.py`, re-ran `.venv/bin/pytest -q` to `17 passed, 2 skipped`, re-ran `sh gradlew testDebugUnitTest --console=plain`, and restarted the local backend on port `8000` with the updated prompt and QA rules.
- [x] (2026-04-04 13:05Z) Reproduced a new frontend-visible `analysisId 不存在，无法生成预览。` error after restarting the local backend, confirmed it was caused by Android holding stale in-memory plan cards while the backend had already dropped the cached analysis context, and added automatic recovery in `RemodelViewModel` so the app now re-syncs the current image and plan set instead of surfacing the raw backend state error.
- [x] (2026-04-04 13:05Z) Added Android unit coverage for the stale-preview-context recovery path, re-ran `sh gradlew testDebugUnitTest --console=plain`, and re-installed the latest debug build on the emulator so the next confirmation attempt uses the new recovery behavior.
- [x] (2026-04-04 13:35Z) Split the final image flow into a dedicated `真图编辑` page at `app/src/main/java/com/scf/secondbloom/ui/screens/PreviewEditorScreen.kt`, wired `Screen.PreviewEditor` into `MainScreen` navigation, and changed workbench plan cards so they now open the edit page instead of directly acting as the final submit surface.
- [x] (2026-04-04 13:35Z) Added local draft controls for preview micro-adjustments in `RemodelUiState` and `RemodelViewModel` using the existing preview edit option model, including silhouette, length, neckline, sleeve, fidelity, and extra instructions, while keeping the actual submit action connected to the existing single-plan preview job flow.
- [x] (2026-04-04 13:42Z) Re-ran `sh gradlew testDebugUnitTest --console=plain` and `sh gradlew installDebug --console=plain` after the new edit-page wiring settled, and confirmed the debug build installs cleanly with the new `preview_editor/{planId}` route and edit controls.
- [x] (2026-04-04 13:50Z) Stabilized the micro-adjustment wire contract by serializing Android-side preview tuning through a dedicated DTO with stable lowercase wire values, adding backend compatibility for `editOptions` as an alias to `tuning`, and extending unit coverage so the independent edit page submits the expected single-plan final-image payload.
- [x] (2026-04-04 14:05Z) Fixed a crash in the new `真图编辑` submit path by changing `RealRemodelApi` so JSON endpoint `400` responses are mapped to `ModelResponseException` instead of `InvalidImageException`, adding a preview-flow safety catch in `RemodelViewModel`, restarting the local backend to the latest preview schema, and reinstalling the updated debug app on the emulator.
- [x] (2026-04-04 14:38Z) Moved final-image viewing onto a dedicated `PreviewResultScreen`, routed the editor page directly into that result route after submit, and simplified the workbench plan cards so they now show preview status and a “查看最终效果图” entry instead of rendering the full Before / After / Compare gallery inline.
- [x] (2026-04-04 15:10Z) Linked the sibling backend repo to the Vercel team project `second-bloom-backend`, added Vercel Python entrypoints plus a top-level rewrite, configured production environment variables for fake-provider bring-up, and deployed the production alias `https://second-bloom-backend.vercel.app`.
- [x] (2026-04-04 15:10Z) Verified the Vercel production alias end to end for the currently safe slice: `GET /health/live` and `GET /health/ready` both return `{"status":"ok"}`, `POST /analyze-garment` succeeds with a small test image, and `POST /generate-remodel-plans` returns three plans from the hosted deployment.
- [x] (2026-04-04 15:10Z) Confirmed the hosted preview limitation remains architectural rather than transport-related: `POST /generate-remodel-preview-jobs` still returns `analysisId 不存在，无法生成预览。` on Vercel because the deployed backend still keeps preview prerequisites in process memory instead of shared persistence.
- [x] (2026-04-04 08:35Z) Reworked the sibling backend at `/Users/peng/AndroidStudioProjects/second-bloom-backend` so Vercel production now uses Neon + Vercel Blob for operational persistence: source images, plan linkage, preview jobs, preview results, and preview assets all survive cross-request execution instead of staying in process memory.
- [x] (2026-04-04 08:35Z) Added Android-side upload preprocessing in `app/src/main/java/com/scf/secondbloom/data/remote/ImageUploadPreprocessor.kt`, updated `RealRemodelApi` to send the compressed JPEG payload, and reran `sh gradlew testDebugUnitTest` successfully with the hosted base URL configured in `local.properties`.
- [x] (2026-04-04 08:35Z) Provisioned the Vercel Marketplace Neon project plus a linked public Vercel Blob store for `second-bloom-backend`, ran Alembic against Neon with the unpooled connection, and redeployed the production alias with real Qwen providers and `OPERATIONAL_PERSISTENCE_ENABLED=true`.
- [x] (2026-04-04 08:35Z) Revalidated the full hosted slice at `https://second-bloom-backend.vercel.app`: `GET /health/live`, `GET /health/ready`, `POST /analyze-garment`, `POST /generate-remodel-plans`, `POST /generate-remodel-preview-jobs`, and `GET /remodel-preview-jobs/{previewJobId}` now succeed end to end, and completed preview responses return directly accessible Vercel Blob URLs for the before / after / comparison assets.
- [x] (2026-04-04 08:35Z) Switched the local debug build property `secondBloomRemodelApiBaseUrl` to `https://second-bloom-backend.vercel.app`, reran `sh gradlew installDebug`, and installed the debug APK onto the connected `Pixel_9_Pro_XL` emulator. A true physical-device smoke test is still pending because no hardware handset was attached in this session.
- [x] (2026-04-04 08:55Z) Updated the hosted backend's Qwen credentials and base URLs to the Singapore region endpoints, then fixed the backend so `analysisId` is always generated server-side instead of trusting a model-supplied value that can repeat across requests.
- [x] (2026-04-04 08:55Z) Re-deployed `https://second-bloom-backend.vercel.app` after the Singapore-region env switch and verified that `POST /analyze-garment` succeeds again with a fresh server-generated `analysisId`, eliminating the production `uq_analysis_requests_analysis_id` collision that had been surfacing in the Android app as a generic network failure.
- [x] (2026-04-04 16:40Z) Added the Android cloud-sync foundation in `app/src/main/java/com/scf/secondbloom/data/historysync/`: public snapshot payload DTOs, bootstrap and revision-conflict merge utilities, a Clerk-bearer-aware HTTP sync client for `/me`, `/me/history`, `/me/history/bootstrap`, and `PUT /me/history`, plus a repository wrapper, local snapshot/state adapters, and unit tests for merge behavior, sync client behavior, and bootstrap persistence.
- [x] (2026-04-04 17:55Z) Landed the guest-first account foundation across Android and the sibling backend: integrated the Clerk Android SDK and auth UI entry points, added debug-only Clerk build configuration, wired `RemodelViewModel` to bootstrap and push cloud history on login, implemented FastAPI Clerk bearer validation plus `/me` and `/me/history*` endpoints, documented the new auth contracts in `docs/api/mvp-contract.md`, and re-verified both Android unit tests and backend auth/history tests.
- [x] (2026-04-04 17:55Z) Re-deployed the sibling backend with the authenticated history routes enabled and rechecked the production alias `https://second-bloom-backend.vercel.app`, confirming that both `GET /health/live` and `GET /health/ready` still return `{"status":"ok"}` after the user-management rollout.
- [x] (2026-04-04 18:35Z) Reworked the Android auth entry so login is no longer hidden only inside a secondary card action: `我的主页 / Profile` now exposes a clear top-of-screen `登录 / 注册` CTA when signed out, a clear `账号中心` CTA when signed in, and `Screen.Account` plus `AccountScreen` are now wired into `MainScreen` navigation.
- [x] (2026-04-04 18:35Z) Hardened the default-Clerk-token contract across Android and backend docs: Android now explicitly treats an empty `secondBloomClerkJwtTemplate` as "use the default session token", backend docs require `CLERK_JWT_ISSUER` to match the Clerk Frontend API URL, and backend tests now cover `JWKS + issuer` validation with audience left disabled by default.
- [x] (2026-04-04 18:35Z) Tightened auth-driven sync state handling so sign-out clears only the active cloud-sync context and any in-flight sync job, while preserving the local guest history snapshot as the offline source of truth.
- [x] (2026-04-05 07:10Z) Hardened the dedicated final-result flow against transient missing preview rows: `PreviewResultScreen` now treats an active preview job for the selected plan as "still generating" even when `results` does not yet contain that plan, `RemodelViewModel.resumePreviewPolling()` can resume from that state, and `app/src/androidTest/java/com/scf/secondbloom/RemodelScreensTest.kt` now covers the regression.
- [x] (2026-04-12 07:20Z) Fixed the hosted compare-image regression in the sibling backend by detecting non-image `comparison_image_bytes` returned from visual QA, dropping that invalid payload, and falling back to server-side before/after composition instead of storing JSON bytes as a `image/png` compare asset. Added backend unit coverage in `/Users/peng/AndroidStudioProjects/second-bloom-backend/tests/unit/test_preview_render_service.py` and re-verified that targeted preview-render tests pass.
- [x] (2026-04-05 07:35Z) Hardened final-image status recovery further by doubling preview poll attempts, catching preview status fetch failures inside `RemodelViewModel.pollPreviewJob()`, and making `PreviewResultScreen` surface `previewErrorMessage` when no preview row exists instead of silently falling back to the generic empty state.
- [x] (2026-04-05 07:50Z) Identified the hosted preview path as an HTTP timeout issue rather than a missing-result issue, then raised preview create/poll timeouts in `RealRemodelApi` to 120 seconds and translated socket timeouts into a clearer final-image-specific message.
- [x] (2026-04-05 15:40Z) Re-ran host-side verification with `sh gradlew testDebugUnitTest` and `sh gradlew assembleDebugAndroidTest`, both of which completed successfully in the current workspace while preserving the existing dirty worktree.
- [x] (2026-04-05 15:40Z) Ran `sh gradlew connectedDebugAndroidTest` against the connected `Pixel_9_Pro_XL` emulator and confirmed that all 17 Compose instrumentation tests currently fail before reaching product assertions because Espresso crashes on Android 16 with `NoSuchMethodException: android.hardware.input.InputManager.getInstance []`.
- [x] (2026-04-05 15:40Z) Performed a fresh adb-driven emulator smoke pass across `Inspiration`, `Wardrobe`, `Planet`, `Profile`, the language switcher, and the AI remodel entry, and confirmed the top-level shell renders correctly while also reproducing a real runtime bug in the demo asset flow: with the debug build currently pointed at `https://second-bloom-backend.vercel.app`, choosing `Demo assets -> Clean recognition` and tapping `Start analysis` surfaces `No content provider: demo://scenario/normal` instead of entering the intended demo recognition path.
- [ ] Stabilize `sh gradlew connectedDebugAndroidTest` on the current emulator lane. The command now runs in this thread, but all 17 tests fail inside Espresso on Android 16 before product assertions execute, so the next step is to either update the AndroidX test stack or pin CI/manual verification to a compatible device image.
- [x] (2026-04-04 05:00Z) Performed a manual emulator smoke test with a real image selected through the system photo picker while the real backend path was enabled, confirmed the analysis step returned a live HTTP response, generated a live plan response, and verified `我的主页` now shows `识别 1 条 · 方案 1 条`.

## Surprises & Discoveries

- The missing compare image was not an Android rendering bug. `PreviewResultScreen` was already passing `comparisonImage.url` into `AsyncImage` correctly. The real failure was upstream: `QwenVisualQaProvider` was emitting JSON metadata bytes as `comparison_image_bytes`, and the preview persistence path stored those bytes as if they were a PNG asset. Before/after assets remained valid, which is why the regression presented as “only compare is blank.”

- Observation: Running the two Gradle verification commands in parallel triggered a Kotlin daemon problem that looked like a code failure but was actually a build cache and compiler session issue.
  Evidence: the failed run reported `Could not close incremental caches` and `Detected multiple Kotlin daemon sessions`; running `sh gradlew --stop` and then rerunning the commands sequentially succeeded.

- Observation: the current Android 16 emulator does not provide a usable path for the existing Compose instrumentation suite because the test stack fails inside Espresso itself before any app-level assertion can run.
  Evidence: `sh gradlew connectedDebugAndroidTest` failed 17 of 17 tests on `Pixel_9_Pro_XL(AVD) - 16` with the same root cause, `java.lang.NoSuchMethodException: android.hardware.input.InputManager.getInstance []`, originating from `androidx.test.espresso.base.InputManagerEventInjectionStrategy.initialize(...)`.

- Observation: A unit test that imported the `toDomain()` extension became brittle in the test compilation context, even though the production code compiled fine.
  Evidence: the failed run reported `Unresolved reference 'toDomain'` in `app/src/test/java/com/scf/secondbloom/RemodelViewModelTest.kt`; the test became stable after replacing that assertion with direct calls to `BackgroundComplexity.fromWire(...)` and `ProcessingWarningCode.fromWire(...)`.

- Observation: The current local storage layer is intentionally not a database. It is a JSON snapshot written into the app’s private files directory with `AtomicFile`, which means writes are attempted safely and partial writes are less likely to leave broken state.
  Evidence: `app/src/main/java/com/scf/secondbloom/data/local/FileRemodelHistoryLocalDataSource.kt` writes `remodel_history.json` through `AtomicFile.startWrite()`, `finishWrite(...)`, and `failWrite(...)`.

- Observation: `AGENTS.md` had drifted behind the repository and needed an explicit refresh after the data and persistence layers were added.
  Evidence: before the refresh, the file still described the app as UI-only even though the code already contained `app/src/main/java/com/scf/secondbloom/data/remote/RealRemodelApi.kt` and `app/src/main/java/com/scf/secondbloom/data/local/`. The file has since been updated.

- Observation: the four-tab redesign introduced a startup crash even though the code compiled and installed cleanly.
  Evidence: `adb logcat -b crash` reported `java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String com.scf.secondbloom.navigation.Screen.getRoute()' on a null object reference` from `app/src/main/java/com/scf/secondbloom/ui/components/SecondBloomBottomNavBar.kt` during app launch. The issue disappeared after changing `Screen.topLevelItems()` and the other collections to functions that build the list after the sealed objects are initialized.

- Observation: returning from the AI remodel subflow to the inspiration feed was unreliable when the code always used `navigate(route)` for bottom tabs.
  Evidence: an emulator repro showed `上传旧衣` staying visible after tapping the bottom-bar `灵感空间` item, while the route changed correctly for non-start tabs such as `可持续星球`. After changing `MainScreen` to call `popBackStack(route, inclusive = false)` first, the same adb-driven repro showed `旧衣新生的灵感流。` on screen.

- Observation: the current terminal environment still cannot execute device-level Android checks because `adb` is not on `PATH`, even though host-side Gradle verification works.
  Evidence: running `adb devices` from the repository root returned `zsh:1: command not found: adb`, so `connectedDebugAndroidTest` and adb-assisted smoke checks still need Android Studio or another shell with Android SDK tools configured.

- Observation: the machine-level Android CLI path issue is now resolved for new shell sessions, and the existing `Pixel_9_Pro_XL` emulator can be driven entirely from the terminal without opening Android Studio first.
  Evidence: after adding Android exports to `~/.zshrc`, a fresh `zsh -lic` resolved `/Users/peng/Library/Android/sdk/platform-tools/adb`, `/Users/peng/Library/Android/sdk/emulator/emulator`, and `/Applications/Android Studio.app/Contents/MacOS/studio`; `adb devices -l` reported `emulator-5554`, `adb -s emulator-5554 shell echo ok` returned `ok`, and both boot-complete properties returned `1`.

- Observation: the built-in demo-scenario entry points are no longer safe when the debug build is switched to the real backend, so the app loses its scripted demo path precisely in the environment where hosted smoke checks are most useful.
  Evidence: in this workspace the generated debug `BuildConfig` resolves `REMODEL_USE_REAL_API=true` and `REMODEL_API_BASE_URL=https://second-bloom-backend.vercel.app`, while `DemoScenario.toSelectedImage()` still produces synthetic URIs like `demo://scenario/normal`. During adb validation, tapping `Demo assets -> Clean recognition -> Start analysis` surfaced `Recognition failed / No content provider: demo://scenario/normal`, which is consistent with `RemodelRepositoryFactory` opening all image URIs through `context.contentResolver.openInputStream(...)` for the real API path.

- Observation: the project-brand rename touched both filesystem layout and code symbols because the original brand name was embedded in the package path, theme names, UI helper names, and documentation paths.
  Evidence: the rename moved the Android source tree from `app/src/main/java/com/scf/loop` to `app/src/main/java/com/scf/secondbloom`, renamed component files such as `SecondBloomBottomNavBar.kt`, and still compiled successfully after `sh gradlew testDebugUnitTest` and `sh gradlew assembleDebugAndroidTest`.

- Observation: the existing JSON history snapshot was already rich enough to drive more of the product without adding a second persistence model.
  Evidence: `app/src/main/java/com/scf/secondbloom/domain/model/RemodelDerivedModels.kt` now derives wardrobe cards and sustainability summaries directly from `SavedAnalysisRecord` and `SavedPlanGenerationRecord`, and both `WardrobeScreen` and `PlanetScreen` render those derived values.

- Observation: the profile tab became more useful once it showed ordered saved-history events instead of only aggregate counts and a works wall.
  Evidence: `app/src/main/java/com/scf/secondbloom/domain/model/RemodelDerivedModels.kt` now derives `recentActivities`, and `app/src/main/java/com/scf/secondbloom/ui/screens/ProfileScreen.kt` renders the three newest items as a "最近动态" section alongside the impact summary.

- Observation: the repository already had a public API contract, but it still lacked a single in-repo document that explained how a real backend should be deployed, what should be persisted server-side, and what remained intentionally local-only in the Android MVP.
  Evidence: `docs/api/mvp-contract.md` defined only the payload shape for two endpoints, while `PRD.md` described broader future-facing capabilities such as sharing and booking without specifying backend boundaries, persistence responsibilities, or deployment shape. `docs/api/backend-architecture.md` now fills that gap.

- Observation: the new architecture document still left one practical gap for implementation handoff: backend contributors needed an opinionated stack recommendation, migration starting point, and build order to avoid turning the architecture into an abstract wish list.
  Evidence: after `docs/api/backend-architecture.md` was added, the repository still lacked a concrete reference stack, SQL draft, and rollout checklist. `docs/api/backend-delivery-plan.md` and `docs/api/postgres-schema-draft.sql` now provide those artifacts.

- Observation: the first round of backend implementation exposed contract drift that could have broken Android integration despite otherwise valid server code.
  Evidence: the earlier SQL draft still used `creative / resize`, and the earlier delivery docs still allowed `backgroundComplexity` values beyond the Android app’s `low / high` model. The docs have now been corrected and the sibling backend implementation matches the Android contract.

- Observation: the preview upgrade introduced a second backend path that is intentionally asynchronous and bounded by the public plan identifiers returned from the main plan-generation endpoint.
  Evidence: the visual preview contract now requires stable `planId` values, limits each preview job to at most three plans, and separates synchronous plan generation from the worker-driven preview lifecycle.

- Observation: once the preview flow was implemented for real, the simplest reliable MVP render asset was the original analyzed source image rather than synthetic fake bytes from the worker stub.
  Evidence: the fake preview providers already emitted pipeline status and failure signals, but their raw byte output was not a browser- or Android-decodable image format. The backend now keeps those providers for control-flow and status simulation, while preview asset URLs serve valid image bytes that Compose can actually render during fake-mode testing.

- Observation: the fastest safe path to a usable real preview was to plug Qwen into the `generate_after_image` stage only, while keeping source loading, segmentation, and QA as local providers.
  Evidence: the existing preview pipeline already had a clean `ImageEditProvider` seam. Wiring DashScope Qwen into that stage allowed the backend to preserve the async job contract, continue producing `beforeImage` and `comparisonImage` assets server-side, and validate one real provider call without rewriting the rest of the worker skeleton.

- Observation: the first real emulator-to-backend run failed before the request reached the app-level error mapping because the Android manifest still lacked the basic `INTERNET` permission.
  Evidence: after enabling `secondBloomRemodelApiBaseUrl`, tapping `开始识别` surfaced `socket failed: EPERM (Operation not permitted)` on-device. Adding `<uses-permission android:name="android.permission.INTERNET" />` to `app/src/main/AndroidManifest.xml` removed that failure mode.

- Observation: the real backend path in Android was still executing `HttpURLConnection` work on the main thread, which only became obvious once a live server was actually reachable.
  Evidence: emulator logcat showed `android.os.NetworkOnMainThreadException` with the stack rooted in `com.scf.secondbloom.data.remote.RealRemodelApi.performMultipartRequest(...)`. Wrapping `analyzeGarment(...)` and `generatePlans(...)` in `withContext(Dispatchers.IO)` fixed the crash and allowed both endpoints to return `200 OK`.

- Observation: the old "preview all plans immediately after text generation" flow no longer fit once the product requirement changed from visual simulation to original-image editing.
  Evidence: the app previously started preview polling inside `generatePlans()` and sent all `planId` values at once. The new requirement needed a user-confirmed single plan, so both the Android state machine and the backend preview request contract had to move from `planIds[]` semantics to a confirmed single-plan workflow.
- Observation: local development no longer needs object storage to exercise the real Qwen path, as long as operational persistence is disabled and the backend can pass the uploaded source image as a `data:` URL.
  Evidence: this machine does not currently expose Docker on `PATH`, so MinIO could not be started through `docker compose`. After adding a `data:` URL fallback in the backend, the live Qwen smoke flow reached recognition, plan generation, preview job creation, and worker polling without object storage.
- Observation: the first real user-visible preview failure was not a transport problem; it was a product-quality rejection from the visual QA gate after image edit succeeded.
  Evidence: the app reached `启动真图编辑中`, then later displayed a filtered-result message. Backend inspection showed the pipeline had already completed recognition, plan generation, and preview job creation, and the terminal result was caused by `qa_compose_comparison` rejecting an edited image that drifted into a full outfit / extra-garment composition.
- Observation: because the current local backend keeps `analysisId -> source image` context in memory only, restarting the backend invalidates any plan cards that are already on screen in the Android app.
  Evidence: after a backend restart, the app could still show previously generated plans locally, but clicking "生成最终效果图" immediately failed with `analysisId 不存在，无法生成预览。`, and the backend returned `404` from `POST /generate-remodel-preview-jobs` until the image was re-analyzed.
- Observation: Vercel did not expose the sibling backend as a working Python API until the repo provided explicit function entrypoints under `api/` and rewrote public paths onto those files.
  Evidence: the early deployments behaved like static output or incomplete routing. After adding `api/*.py` wrappers that re-export `app.main:app` plus a `vercel.json` rewrite from `/(.*)` to `/api/$1`, the production alias began serving the FastAPI routes correctly.
- Observation: production environment values copied into Vercel with a trailing newline can silently break backend startup when Pydantic expects exact booleans.
  Evidence: the first hosted boot failed validation because `OPERATIONAL_PERSISTENCE_ENABLED` was stored as `false\n`, which the runtime rejected as an invalid boolean input. Removing and re-adding the variables without the newline fixed startup.
- Observation: locally prebuilt Vercel output is not a safe release path for this Python backend because it can bundle host-specific binaries that do not match Vercel's Linux runtime.
  Evidence: the prebuilt deployment failed with `ModuleNotFoundError: No module named 'pydantic_core._pydantic_core'` after uploading locally generated output. A normal source-built production deployment fixed that runtime error.
- Observation: the current Vercel deployment proves the preview failure is an architecture problem, not an Android-only bug, because analysis and plan generation work remotely while preview creation still loses `analysisId` context across requests.
  Evidence: `https://second-bloom-backend.vercel.app` now returns `200` for both health probes, returns a valid analysis payload from `POST /analyze-garment`, and returns plan results from `POST /generate-remodel-plans`, but the follow-up `POST /generate-remodel-preview-jobs` still returns `{"message":"analysisId 不存在，无法生成预览。"}`.
- Observation: once Vercel production switched from in-memory preview prerequisites to Neon + Vercel Blob, the hosted preview flow behaved like a real stateless service rather than a single-process demo.
  Evidence: after landing DB-backed preview jobs and Blob-backed assets in `/Users/peng/AndroidStudioProjects/second-bloom-backend`, the production alias accepted `POST /generate-remodel-preview-jobs`, completed `GET /remodel-preview-jobs/preview-job-f325cb726057401faccb6eb8d6c0aed4`, and returned public Blob asset URLs that answered `HTTP/2 200` directly.
- Observation: the first Android auth attempt was blocked by an outdated Clerk artifact coordinate, but the integrated guest-first auth slice now builds and the host-side Android tests pass after moving to the published Clerk 1.0.10 packages and adapting the token API.
  Evidence: the earlier `Could not find com.clerk:clerk-android-ui:0.1.19` failure was replaced by a passing `sh gradlew testDebugUnitTest --console=plain` run after updating `gradle/libs.versions.toml`, switching the auth flow to `AuthView`, and adapting `ClerkHistoryAuthTokenProvider` to `Clerk.auth.getToken(...)`.
- Observation: Neon's injected `DATABASE_URL` shape is compatible with Vercel provisioning but not with the backend's SQLAlchemy runtime unless the driver scheme is normalized first.
  Evidence: the first production request failed with `ModuleNotFoundError: No module named 'psycopg2'` because SQLAlchemy interpreted `postgresql://...` as the legacy psycopg2 dialect. Normalizing those URLs to `postgresql+psycopg://...` in the backend session layer resolved the hosted runtime error.
- Observation: the Android-side "网络请求失败" banner after hosted recognition was being triggered by a backend uniqueness bug, not only by region mismatch.
  Evidence: Vercel production logs showed `duplicate key value violates unique constraint "uq_analysis_requests_analysis_id"` with the repeated model-supplied `analysisId` value `analysis-123456789`. After overriding `analysisId` on the server with a fresh `analysis-<uuid>` value, hosted recognition returned `200` again.
- Observation: the implemented account and cloud-sync code remains guest-safe by default, but full login and cross-device verification still require real Clerk credentials to be injected into both the Android debug build and the Vercel backend environment.
  Evidence: this workspace's `local.properties` currently exposes only `secondBloomRemodelApiBaseUrl`, and the new Android auth configuration reads `secondBloomClerkPublishableKey` plus `secondBloomClerkJwtTemplate` from local Gradle properties while the backend expects Clerk verification settings at runtime.
- Observation: the original auth rollout technically had a working login route, but the visible entry point was too easy to miss because it only lived inside the Profile page's auth card and the dedicated `AccountScreen` route was never mounted in the app nav graph.
  Evidence: before the latest UI pass, `MainScreen` navigated only to `Screen.Auth` from Profile, while `Screen.Account` existed in `Screen.flowItems()` and `AccountScreen.kt` existed on disk without a matching `NavHost` destination. The latest pass now exposes top-of-profile CTAs and mounts the account route directly.
- Observation: the dedicated final-result screen was too strict about requiring a per-plan preview row before it would consider a job "in progress", which could mislabel active jobs as "还没有效果图结果" when the backend job record existed but the `results` list had not yet populated for that plan.
  Evidence: `PreviewResultScreen` used `preview == null` to route directly to the empty state, and `RemodelViewModel.resumePreviewPolling()` returned early unless `state.previewFor(planId)` was non-null. The new fix now also treats `selectedPlanId == planId` plus an active `previewJob.status` as a resumable in-progress state.
- Observation: the result page also hid real preview request or polling failures whenever no preview row had been stored yet, which made backend or network errors look indistinguishable from “never started”.
  Evidence: before the latest change, `PreviewResultScreen` always rendered the generic `还没有效果图结果` card when `preview == null`, even if `previewErrorMessage` had already been populated by `createPreviewJob()` failure paths. `pollPreviewJob()` also lacked its own exception handling, so refresh failures had no dedicated user-facing state.
- Observation: the newly surfaced `timeout` state shows the final-image path is timing out at the HTTP layer while waiting for the real backend, not failing validation or losing `planId`.
  Evidence: the user’s latest screenshot displayed `当前效果图状态 / timeout`, while direct manual calls against the same hosted backend and recent `analysisId + planId` succeeded in creating preview jobs and later returned `completed` with image asset URLs. The Android client had still been using a generic 30-second read timeout for preview endpoints.

## Decision Log

- Decision: fix the compare-image regression in the sibling backend instead of adding Android-side special cases.
  Rationale: the Android result screen already consumes the contract correctly by loading the URL it receives. The broken invariant was that the backend sometimes produced a non-image payload for `comparisonImage.url`. Correcting that at the source restores every client, keeps the wire contract honest, and lets the existing persistence code fall back to server-side before/after composition without adding UI heuristics.

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

- Decision: Keep the Android CLI fix in user shell configuration instead of hiding it behind a repo-local script.
  Rationale: `adb`, `emulator`, and `studio` need to work from any terminal context, including future `connectedDebugAndroidTest`, adb-driven smoke checks, and Android Studio terminal sessions. A small `~/.zshrc` export block makes the machine state explicit and reusable across projects.
  Date/Author: 2026-04-04 / Codex

- Decision: Keep the four top-level tabs and center AI action button, and preserve the upload and workbench screens as a nested remodel flow instead of first-level tabs.
  Rationale: this keeps the new branded product shell while preserving the already-working MVP analysis and plan-generation logic.
  Date/Author: 2026-04-03 / Codex

- Decision: Build `Screen` tab collections through functions instead of static companion object values.
  Rationale: Kotlin `sealed class` data objects can be observed before every object is initialized during class loading, and this repository now constructs bottom navigation very early in app startup. Lazy function-based list construction avoids null entries caused by initialization order.
  Date/Author: 2026-04-03 / Codex

- Decision: When leaving the remodel subflow for a top-level tab, restore the existing destination with `popBackStack(...)` before creating a new destination with `navigate(...)`.
  Rationale: the inspiration screen is the navigation graph start destination, and this approach reliably returns to it from subflow screens while still keeping state restoration for tabs that are not already on the stack.
  Date/Author: 2026-04-03 / Codex

- Decision: Enable the real backend in debug builds through local developer configuration instead of editing checked-in `buildConfigField(...)` values by hand.
  Rationale: a Gradle property or `local.properties` entry makes the real API path repeatable for each developer while preserving mock mode as the default repository behavior.
  Date/Author: 2026-04-03 / Codex

- Decision: Treat empty or malformed `2xx` payloads from the real backend as `ModelResponseException` instead of allowing serialization failures to leak through the data layer.
  Rationale: the ViewModel already has a stable app-level error path for model failures, and mapping invalid server payloads into that path keeps the UI behavior predictable during backend rollout.
  Date/Author: 2026-04-03 / Codex

- Decision: Rename the app brand, package, and UI identifiers to `Second Bloom` / `com.scf.secondbloom` in one pass instead of only changing visible strings.
  Rationale: the existing project used `Loop` in package names, theme identifiers, helper component names, and docs, so a partial rename would leave the codebase inconsistent and harder to maintain.
  Date/Author: 2026-04-03 / Codex

- Decision: Derive wardrobe and sustainability state inside `RemodelViewModel` from the existing saved history records instead of introducing separate storage or per-screen business rules.
  Rationale: this keeps `SavedAnalysisRecord` and `SavedPlanGenerationRecord` as the only persisted facts, preserves the repository’s lightweight architecture, and ensures the wardrobe, profile, and sustainability tabs stay consistent after every refresh.
  Date/Author: 2026-04-03 / Codex

- Decision: Reuse the same derived-history pattern for profile activity instead of formatting recent events ad hoc inside `ProfileScreen`.
  Rationale: keeping the ordering and labeling rules in `RemodelDerivedModels.kt` makes the profile tab easier to test and keeps future history-driven screens consistent.
  Date/Author: 2026-04-03 / Codex

- Decision: Add a separate backend architecture document in `docs/api/backend-architecture.md` instead of overloading `PRD.md` or `docs/api/mvp-contract.md` with deployment, storage, and operational design details.
  Rationale: the PRD is product-facing and aspirational, while the contract is payload-focused. A dedicated architecture document gives backend contributors a clearer source of truth for runtime shape, data boundaries, and phased evolution without pretending that those product ideas are already implemented.
  Date/Author: 2026-04-04 / Codex

## Planning Revision Notes

- 2026-04-12 07:17Z: Refined the live-demo recovery plan after architect review. The canonical `.omx/plans/prd-core-flow-test-fix.md` is now a backend-first gated recovery plan instead of an implicit cross-end bugfix plan. It now requires Gate A hosted reproduction and responsibility assignment, Gate B backend/env/migration/deploy repair, Gate C Android repair only if the hosted API is already healthy, and Gate D final acceptance bound to the same Vercel deployment id or commit plus the same Android build across two consecutive real-phone successes. The aligned `.omx/plans/test-spec-core-flow-test-fix.md` now mirrors those gates and treats Android regression coverage as conditional on reproduced Android-side responsibility rather than a pre-locked fix list.

- Decision: Add a separate delivery plan and SQL draft instead of embedding stack choices and DDL inline inside the architecture document.
  Rationale: the architecture document should stay stable and conceptual, while implementation order and schema drafts are expected to evolve faster during backend bring-up. Splitting those artifacts keeps the architecture readable without losing delivery precision.
  Date/Author: 2026-04-04 / Codex

- Decision: Treat visual preview as a vNext async capability rather than a synchronous part of the current MVP.
  Rationale: the preview flow needs stable `planId` values, worker ownership, Redis queueing, and object-storage assets, which fit a separate pipeline better than the current request/response path. Keeping it as vNext avoids destabilizing the Android MVP while still documenting the contract clearly.
  Date/Author: 2026-04-04 / Codex

- Decision: Use Neon plus Vercel Blob as the production persistence pair for the sibling backend instead of keeping MinIO/S3 semantics in the hosted path.
  Rationale: Neon is the Vercel-supported operational Postgres option for this project, and Vercel Blob gives public, durable asset URLs that Qwen and the Android app can both consume without adding another vendor-specific object-storage adapter in production.
  Date/Author: 2026-04-04 / Codex

- Decision: Keep the Android debug build switch on `secondBloomRemodelApiBaseUrl` instead of adding a new release-only environment lane for the hosted backend.
  Rationale: the repository already has a safe, local-only debug configuration path, so pointing that property at the Vercel alias allows formal hosted smoke verification without changing the checked-in build flavor model.
  Date/Author: 2026-04-04 / Codex

- Decision: For the first "real final effect image" release, use Qwen Chat API for recognition and plan generation, and use Qwen image edit for the confirmed-plan effect image instead of continuing with plain text-to-image generation.
  Rationale: the product goal is now to preserve the original garment identity, subject pose, and scene realism while editing only the clothing result. That is a better match for image edit than for text-to-image, and it also keeps the user flow understandable by generating an effect image only after explicit plan confirmation.
  Date/Author: 2026-04-04 / Codex

- Decision: Treat saved wardrobe items with plan history as resumable editing entry points instead of read-only archive cards.
  Rationale: users expect the digital wardrobe to behave like a working closet of drafts and prior remodel ideas, not just a static gallery. Restoring the saved image, analysis, intent, preferences, and plan set into the active workbench gives the smallest implementation that makes history genuinely reusable.
  Date/Author: 2026-04-04 / Codex

- Decision: Keep the first publish system local-only and feed-based instead of building a remote community backend immediately.
  Rationale: the product need is to let the user see their latest finished remodel appear in the inspiration space right away. Persisting published entries in the existing local history snapshot gives that behavior with minimal new architecture, while leaving room for a real community backend later.
  Date/Author: 2026-04-04 / Codex

- Decision: Implement the first runnable backend in a sibling repository instead of embedding Python server code into the Android app repository.
  Rationale: this preserves the Android repo’s single-module structure, keeps backend tooling isolated, and makes API, database, and deployment work easier to evolve independently.
  Date/Author: 2026-04-04 / Codex

- Decision: Keep local emulator-to-backend validation on plain HTTP and solve it with a debug-only manifest override plus the standard `INTERNET` permission instead of forcing HTTPS for local bring-up.
  Rationale: the sibling backend currently runs as a local development server on `10.0.2.2:8000`, and the smallest safe Android-side change is to allow cleartext only for debug while preserving the release path for later tightening.
  Date/Author: 2026-04-04 / Codex

- Decision: Move blocking real-backend calls onto `Dispatchers.IO` inside `RealRemodelApi` instead of relying on each caller to remember the right dispatcher.
  Rationale: `HttpURLConnection` is always blocking I/O, and pushing the dispatcher boundary down into the concrete implementation makes the real backend path safe no matter which repository or ViewModel calls it later.
  Date/Author: 2026-04-04 / Codex

- Decision: Deploy the sibling backend to Vercel through source builds with explicit `api/*.py` wrappers and a top-level rewrite instead of relying on local prebuilt output.
  Rationale: the source-built path lets Vercel install Python dependencies for its own Linux runtime, avoids host-binary mismatches, and gives each public route a concrete Vercel function entrypoint while still preserving the existing FastAPI application object.
  Date/Author: 2026-04-04 / Codex

- Decision: Keep the first Vercel production deployment in fake-provider, no-operational-persistence mode until shared storage for analysis, plans, preview jobs, and assets is in place.
  Rationale: this gets a stable public HTTPS backend online quickly for Android real-device testing, while making the remaining preview failure explicit instead of pretending the current in-memory implementation is production-safe.
  Date/Author: 2026-04-04 / Codex

## Outcomes & Retrospective

The current preview/result stack is now more robust against provider drift in the compare stage. If the visual-QA layer returns analysis metadata or any other non-image payload instead of a real compare bitmap, the backend will no longer publish a broken compare asset URL; it will discard that invalid payload and let the existing server-side composition path generate a valid compare image from the before and after assets.

The main outcome so far is that Second Bloom now behaves like a real MVP-shaped Android app even though the backend is still mocked by default for contributors who do nothing. A user can open a redesigned four-tab shell, enter the remodel flow from the center action button, complete the core remodel journey, and then see that same saved work echoed back through the wardrobe, profile, and sustainability tabs without re-entering data. The biggest shift from this round is that the live HTTP path is no longer theoretical in either local or hosted form: with the sibling backend running locally, the emulator successfully completed both `POST /analyze-garment` and `POST /generate-remodel-plans`, and the same sibling backend is now deployed to Vercel at `https://second-bloom-backend.vercel.app`, where health checks, analysis, plan generation, preview job creation, preview polling, and Blob-served preview assets all succeed. The remaining external gap is now narrower and more concrete: a true physical-handset smoke test and `connectedDebugAndroidTest` execution are still pending even though the debug APK has already been rebuilt against the hosted alias and installed onto the currently attached emulator.

The biggest lesson is that the simplest working shape was the right one for this repository. A small repository factory, a small file-based history store, a single `RemodelViewModel`, and a single `NavHost` were enough to make the app useful without introducing extra modules or a dependency injection container, which is a framework that automatically constructs and passes objects around. The follow-on lesson from this round is that one persisted fact model can power several product surfaces when the derivation rules are kept explicit and tested. Another lesson is that runtime validation matters as much as compile-time validation: the startup crash and the failed return from the upload flow were both navigation bugs that only became obvious on the emulator, not from `assemble` alone. The newest deployment lesson is that getting a Python API onto Vercel is only half of productionizing it; the hosted backend only became stable after the preview pipeline was moved off process-local state, the production database URL was normalized to the installed `psycopg` dialect, and preview assets were stored in a durable Blob store that both Qwen and Android can read directly.

## Context and Orientation

This repository is a single Android app module named `:app`. The app is written with Jetpack Compose, which is Android’s Kotlin-based user interface toolkit. The screens live under `app/src/main/java/com/scf/secondbloom/ui`, the screen navigation lives in `app/src/main/java/com/scf/secondbloom/ui/MainScreen.kt`, and the workflow state lives in `app/src/main/java/com/scf/secondbloom/presentation/remodel/RemodelViewModel.kt`.

The word “mock” in this repository means a fake implementation that returns hard-coded results without talking to a server. That implementation is `app/src/main/java/com/scf/secondbloom/data/remote/mock/MockRemodelApi.kt`. The word “real API” means a normal network call to a backend server. That implementation is `app/src/main/java/com/scf/secondbloom/data/remote/RealRemodelApi.kt`. The word “repository” here means a small Kotlin class that hides where data comes from. `app/src/main/java/com/scf/secondbloom/data/repository/DefaultRemodelRepository.kt` converts between UI-friendly domain models and remote request or response models. `app/src/main/java/com/scf/secondbloom/data/repository/RemodelRepositoryFactory.kt` chooses either the mock API or the real API based on generated build flags.

The word “BuildConfig” means a generated Kotlin-accessible configuration class that Gradle creates during the build. In this app, `app/build.gradle.kts` defines `REMODEL_API_BASE_URL` and `REMODEL_USE_REAL_API`. When `REMODEL_USE_REAL_API` is `false` or the URL is blank, the app keeps using the mock API. When it is `true` and the URL is non-empty, the app uses `RealRemodelApi`.

The word “local persistence” means saving app data onto the device so it is still there after the app closes and reopens. That logic lives under `app/src/main/java/com/scf/secondbloom/data/local/`. `RemodelHistoryRepository.kt` is the interface. `DefaultRemodelHistoryRepository.kt` is the implementation that shapes saved records. `FileRemodelHistoryLocalDataSource.kt` writes and reads the JSON snapshot. `RemodelHistoryRepositoryFactory.kt` builds the repository for the app.

The current navigation shell has four top-level tabs defined in `app/src/main/java/com/scf/secondbloom/navigation/Screen.kt`: `灵感空间`, `数字衣橱`, `可持续星球`, and `我的主页`. The upload and plan screens are still present, but they now act as a remodel subflow opened from the center floating action button in `app/src/main/java/com/scf/secondbloom/ui/components/SecondBloomBottomNavBar.kt`. The user-visible screens involved in this plan are `app/src/main/java/com/scf/secondbloom/ui/screens/InspirationScreen.kt`, `app/src/main/java/com/scf/secondbloom/ui/screens/HomeScreen.kt`, `app/src/main/java/com/scf/secondbloom/ui/screens/WorkbenchScreen.kt`, and `app/src/main/java/com/scf/secondbloom/ui/screens/ProfileScreen.kt`. `InspirationScreen` is the first screen a user sees. `HomeScreen` handles image selection and analysis. `WorkbenchScreen` handles user confirmation and plan generation. `ProfileScreen` now shows saved results as part of the new profile-style layout. The state passed into the remodel screens and profile comes from `RemodelUiState` in `app/src/main/java/com/scf/secondbloom/domain/model/RemodelModels.kt`.

The backend contract for the future service is `docs/api/mvp-contract.md`. That file describes the expected request and response bodies for `POST /analyze-garment`, `POST /generate-remodel-plans`, and the vNext visual preview flow. The system-level backend design now lives in `docs/api/backend-architecture.md`. The delivery guide and SQL draft now live in `docs/api/backend-delivery-plan.md` and `docs/api/postgres-schema-draft.sql`. Any backend implementation for this app must either follow those documents or update them together with the Android DTO mapping in the same change.

The test commands for this repository are described in `AGENTS.md`. The most important commands are `sh gradlew testDebugUnitTest`, which runs host-side unit tests, and `sh gradlew connectedDebugAndroidTest`, which runs instrumentation tests on a device or emulator. `sh gradlew assembleDebugAndroidTest` only builds the instrumentation test application package. It is a useful build check, but it is not the same as actually running the tests on a device.

## Milestones

### Milestone 1: Make the main clothing remodel loop persist locally

This milestone is complete in the current working tree. The goal was to ensure that the app does more than animate between screens. The end result is that analysis results and generated plan results are stored locally and surfaced back to the user. The work happened in `app/src/main/java/com/scf/secondbloom/presentation/remodel/RemodelViewModel.kt`, `app/src/main/java/com/scf/secondbloom/domain/model/RemodelModels.kt`, and `app/src/main/java/com/scf/secondbloom/data/local/`. You can verify the milestone by installing the app, walking through the home and workbench screens, then opening the profile screen and confirming it shows the latest saved analysis and plan. The proof command is `sh gradlew testDebugUnitTest`, which passes in the current tree.

### Milestone 2: Add a safe handoff point for a future real backend

This milestone is also complete in the current working tree. The goal was to preserve today’s demo flow while making room for a real server tomorrow. The end result is that `RemodelRepositoryFactory` can choose `RealRemodelApi` instead of `MockRemodelApi`, the exact expected backend contract lives in `docs/api/mvp-contract.md`, and the broader backend system design lives in `docs/api/backend-architecture.md`. The proof commands are `sh gradlew testDebugUnitTest` and `sh gradlew assembleDebugAndroidTest`, both of which pass in the current tree. A backend developer can now open the repository and build against the contract without guessing Android’s request format or the intended runtime shape.

### Milestone 3: Stabilize the four-tab shell and remodel subflow

This milestone is complete in the current working tree. The goal was to move the app from a narrow MVP tab layout into a branded first-run shell without breaking the remodel flow. The end result is that the app opens on `灵感空间`, the center action button still opens the upload flow, the app no longer crashes at startup due to `Screen` initialization order, and the bottom bar can now return from `上传旧衣` to `灵感空间`. The key files are `app/src/main/java/com/scf/secondbloom/ui/MainScreen.kt`, `app/src/main/java/com/scf/secondbloom/ui/components/SecondBloomBottomNavBar.kt`, `app/src/main/java/com/scf/secondbloom/navigation/Screen.kt`, `app/src/test/java/com/scf/secondbloom/ScreenCollectionsTest.kt`, and `app/src/androidTest/java/com/scf/secondbloom/RemodelScreensTest.kt`. The proof was a real emulator run that showed the app launch successfully, followed by an adb-driven tap flow that returned from the upload screen to the inspiration feed.

### Milestone 4: Connect and validate the first live backend

This milestone is still open. The goal is that a user can upload a real clothing photo, receive a server-produced analysis and server-produced plan suggestions, and still see those results saved into the profile screen afterward. The work should start in `docs/api/mvp-contract.md`, `docs/api/backend-architecture.md`, `docs/api/backend-delivery-plan.md`, and `docs/api/postgres-schema-draft.sql`, then continue in `app/src/main/java/com/scf/secondbloom/data/remote/RealRemodelApi.kt`, `app/src/main/java/com/scf/secondbloom/data/remote/dto/RemodelDtos.kt`, and `app/build.gradle.kts`. After the backend exists, enable the build flags, run the app on an emulator or device, and manually verify both network success and failure paths. The milestone is only complete when `sh gradlew connectedDebugAndroidTest` passes on a device or emulator and a manual smoke test shows a real uploaded image flowing through the app.

## Plan of Work

The current code already implements the local MVP loop and the four-tab app shell, so the remaining work is about making the real backend path trustworthy and preserving the stability of the navigation shell. Start by treating `docs/api/mvp-contract.md` as the payload-level source of truth, `docs/api/backend-architecture.md` as the system-level source of truth, and `docs/api/backend-delivery-plan.md` plus `docs/api/postgres-schema-draft.sql` as the implementation handoff set. If the backend team needs to change an endpoint shape, persistence boundary, runtime responsibility, error payload, or storage model, update the docs first and then update the Kotlin data transfer objects in `app/src/main/java/com/scf/secondbloom/data/remote/dto/RemodelDtos.kt`. Do not change only one side.

Next, keep `app/src/main/java/com/scf/secondbloom/data/remote/RealRemodelApi.kt` small and explicit. It already performs multipart upload for image analysis and JSON posting for plan generation. If the backend introduces structured error bodies later, parse them here and keep the mapping into `InvalidImageException`, `ModelResponseException`, or `IOException` easy to read. The goal is that `RemodelViewModel` continues to handle only app-level states and messages, not raw HTTP logic.

Then wire the live backend build by setting `secondBloomRemodelApiBaseUrl` either in `~/.gradle/gradle.properties`, in project `gradle.properties`, or in the ignored `local.properties` file at the repository root. The debug build now turns on `REMODEL_USE_REAL_API` automatically when that URL is non-empty. Leave the value blank when you want the repository to stay in mock mode.

After the backend path works, validate that the saved history path remains correct. The key files are `app/src/main/java/com/scf/secondbloom/presentation/remodel/RemodelViewModel.kt`, which calls `saveAnalysis(...)` and `savePlanGeneration(...)`, and `app/src/main/java/com/scf/secondbloom/ui/screens/ProfileScreen.kt`, which displays those saved records. If the live backend returns extra fields, do not store them casually. Extend `SavedAnalysisRecord` or `SavedPlanGenerationRecord` in `app/src/main/java/com/scf/secondbloom/domain/model/RemodelModels.kt` only if the new field changes what a user can see or continue editing later.

At the same time, preserve the recent navigation fixes in `app/src/main/java/com/scf/secondbloom/ui/MainScreen.kt` and `app/src/main/java/com/scf/secondbloom/navigation/Screen.kt`. If the navigation graph changes again, rerun a real emulator flow from `灵感空间` into `上传旧衣` and back out through the bottom tabs. This repository already proved that compile success alone is not enough to catch navigation regressions.

## Concrete Steps

Work from the repository root:

    cd "/Users/peng/AndroidStudioProjects/Second Bloom"

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

To switch from mock mode to live backend mode without editing tracked files, add a local-only property such as this to `local.properties` or another Gradle property source:

    secondBloomRemodelApiBaseUrl=https://your-backend.example

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

For user-visible behavior in live backend mode, set a non-empty `secondBloomRemodelApiBaseUrl` so the debug build generates `REMODEL_USE_REAL_API=true`, upload a normal clothing image from the device, and confirm the analysis result is not coming from the demo scenarios. The app should still show either a normal analysis result or one of the expected error states: invalid image, network error, or model error. This has now been verified locally against `http://10.0.2.2:8000` backed by `/Users/peng/AndroidStudioProjects/second-bloom-backend`: a photo selected through the system picker produced a live analysis response, then a live plan response, and `我的主页` showed both saved records. The remaining acceptance gap is `sh gradlew connectedDebugAndroidTest` on hardware or an emulator.

## Idempotence and Recovery

All Gradle verification commands in this plan are safe to rerun. Repeating them does not damage the repository. If the Kotlin daemon issue appears again, the safe recovery is to run `sh gradlew --stop` and then rerun the commands sequentially.

Changing the local `secondBloomRemodelApiBaseUrl` property is also safe to repeat, but be deliberate. If the real backend is unavailable and the property is still non-empty, the debug app will surface network errors instead of mock data. To recover, either restore a working base URL or clear the property so the next debug build falls back to mock mode.

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
    Starting: Intent { cmp=com.scf.secondbloom/.MainActivity }

    $ adb-driven repro after fix
    content-desc="灵感空间页面"
    text="旧衣新生的灵感流。"
    content-desc="已选中，灵感空间，灵感空间页面"

This was the transient build problem that turned out to be environmental rather than a product regression:

    Could not close incremental caches
    Detected multiple Kotlin daemon sessions

This was the unit test compile failure that led to the simpler and more robust explicit enum conversion assertion:

    Unresolved reference 'toDomain'

This was the first real-backend permission failure on device before the manifest fix:

    socket failed: EPERM (Operation not permitted)

This was the first real-backend runtime crash before moving blocking HTTP work off the main thread:

    android.os.NetworkOnMainThreadException

## Interfaces and Dependencies

At the end of this work, the repository must continue to expose these stable interfaces and entry points.

`app/src/main/java/com/scf/secondbloom/data/remote/RemodelApi.kt` is the remote abstraction. It must continue to define the two suspend functions that the repository relies on for analysis and plan generation.

`app/src/main/java/com/scf/secondbloom/data/repository/DefaultRemodelRepository.kt` is the app-facing repository. It must continue to expose:

    suspend fun analyze(image: SelectedImage, responseLanguage: AppLanguage): GarmentAnalysis
    suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: GarmentAnalysis,
        userPreferences: String,
        responseLanguage: AppLanguage
    ): List<RemodelPlan>

`app/src/main/java/com/scf/secondbloom/data/repository/RemodelRepositoryFactory.kt` must continue to expose `fun create(context: Context): RemodelRepository` and must keep the choice between mock and real remote implementations behind build flags. It now also exposes a small internal helper for host-side tests so the mock-versus-real selection can be verified without Android framework setup.

`app/src/main/java/com/scf/secondbloom/data/remote/RealRemodelApi.kt` must continue to accept a base URL and a function that opens an image stream from a URI string. It now also accepts an optional connection factory for host-side tests. This keeps the network code testable and keeps Android-specific URI reading out of the repository layer.

`app/src/main/java/com/scf/secondbloom/data/local/RemodelHistoryRepository.kt` must continue to define save and read operations for the latest and recent analysis and plan records. The current methods are:

    suspend fun saveAnalysis(sourceImage: SelectedImage, analysis: GarmentAnalysis, savedAtEpochMillis: Long = System.currentTimeMillis()): SavedAnalysisRecord
    suspend fun savePlanGeneration(sourceImage: SelectedImage, analysis: GarmentAnalysis, intent: RemodelIntent, userPreferences: String, plans: List<RemodelPlan>, savedAtEpochMillis: Long = System.currentTimeMillis()): SavedPlanGenerationRecord
    suspend fun getLatestAnalysis(): SavedAnalysisRecord?
    suspend fun getLatestPlanGeneration(): SavedPlanGenerationRecord?
    suspend fun getRecentAnalyses(limit: Int = DefaultListLimit): List<SavedAnalysisRecord>
    suspend fun getRecentPlanGenerations(limit: Int = DefaultListLimit): List<SavedPlanGenerationRecord>

`app/src/main/java/com/scf/secondbloom/presentation/remodel/RemodelViewModel.kt` must continue to be the single workflow entry point. The functions that the UI depends on are `onImageSelected(...)`, `setAppLanguage(...)`, `analyzeSelectedImage()`, `continueWithLowConfidence()`, `clearError()`, the `update...` field editors, `selectIntent(...)`, `updateUserPreferences(...)`, and `generatePlans()`.

`app/src/main/java/com/scf/secondbloom/ui/screens/ProfileScreen.kt` must continue to accept `state: RemodelUiState` and `onLanguageSelected: (AppLanguage) -> Unit`, render the latest saved records from that state, and expose the app language toggle from the profile-style layout.

`app/src/main/java/com/scf/secondbloom/navigation/Screen.kt` must continue to expose stable top-level and remodel-flow collections. The current API is:

    fun topLevelItems(): List<Screen>
    fun flowItems(): List<Screen>
    fun allItems(): List<Screen>

These functions intentionally replace static companion object lists because the static form caused a startup crash during object initialization.

`docs/api/mvp-contract.md` is a dependency for human coordination rather than for Kotlin compilation. It must remain aligned with `app/src/main/java/com/scf/secondbloom/data/remote/dto/RemodelDtos.kt` and `app/src/main/java/com/scf/secondbloom/data/remote/RealRemodelApi.kt`.

`docs/api/backend-architecture.md` is the repository’s system-level backend design reference. It must remain aligned with `docs/api/mvp-contract.md`, the real backend implementation outside this repository, and any Android-side assumptions about what stays local-only versus what is persisted on the server.

`docs/api/backend-delivery-plan.md` and `docs/api/postgres-schema-draft.sql` are the repository’s implementation-oriented backend handoff artifacts. They must remain aligned with the architecture doc, the API contract, and any real backend migrations or environment conventions adopted outside this repository.

## Revision Note

Revision note (2026-04-03 16:34Z, Codex): rewrote `PLANS.md` from a short project-status memo into a full ExecPlan so that a contributor with only the repository and this file can understand the current MVP state, verify it, and continue the real-backend handoff safely.

Revision note (2026-04-03 18:23Z, Codex): updated the plan to reflect the current four-tab navigation shell, the completed `AGENTS.md` refresh, the startup crash fix, the bottom-tab return fix from the upload flow, and the latest emulator-based validation evidence.

Revision note (2026-04-03 18:31Z, Codex): updated the plan after the real-backend closeout pass to reflect the new local debug backend configuration, hardened `RealRemodelApi` error handling, added host-side verification for DTO and runtime selection behavior, and recorded that `adb` is still unavailable in the current terminal session.

Revision note (2026-04-03 18:48Z, Codex): updated the plan after the `Loop -> Second Bloom` rename to reflect the new package path `com.scf.secondbloom`, the renamed UI/theme identifiers, the updated project branding, and the post-rename Gradle verification results.

Revision note (2026-04-03 19:01Z, Codex): updated the plan after moving the repository root to `/Users/peng/AndroidStudioProjects/Second Bloom`, aligning Android Studio workspace metadata with the new project name, and rerunning Gradle verification from the renamed directory.

Revision note (2026-04-04 00:00Z, Codex): updated the plan after adding `docs/api/backend-architecture.md`, linking it from the API documentation set, and documenting the repository’s new source of truth for backend runtime shape, storage boundaries, security expectations, and phased service evolution.

Revision note (2026-04-04 00:10Z, Codex): updated the plan after adding `docs/api/backend-delivery-plan.md` and `docs/api/postgres-schema-draft.sql`, linking them into the backend documentation set, and recording the new implementation-ready guidance for stack selection, schema bring-up, and rollout sequencing.

Revision note (2026-04-04 02:10Z, Codex): updated the plan after implementing the first runnable backend in `/Users/peng/AndroidStudioProjects/second-bloom-backend`, reconciling the backend docs with the Android contract, and verifying the sibling backend test suite locally.

Revision note (2026-04-04 05:00Z, Codex): updated the plan after validating the Android app against the live sibling backend on the emulator, fixing the missing `INTERNET` permission, moving `RealRemodelApi` blocking calls onto `Dispatchers.IO`, adding the debug-only cleartext manifest override for local HTTP bring-up, and confirming that live analysis and plan responses still save into local profile history.

Revision note (2026-04-04 08:15Z, Codex): updated the plan after landing bilingual English/Chinese support with English as the default app language, persisting the selected language in lightweight app preferences, wiring `responseLanguage` through analyze/plan requests to the sibling backend, switching Qwen analyze/plan prompts to the selected language, adding the profile language toggle, localizing the main Compose surfaces, and rerunning both Android unit tests and sibling backend tests successfully.

Revision note (2026-04-04 08:35Z, Codex): updated the plan after making wardrobe history entries editable by wiring each saved garment to its latest plan-generation record, restoring saved plan generations back into `RemodelViewModel` as active workbench state, exposing an explicit "Edit saved plans" entry from the wardrobe UI, and rerunning Android unit tests successfully.

Revision note (2026-04-04 08:55Z, Codex): updated the plan after adding a publish button to the final result page, persisting published remodels in the lightweight local history snapshot, prepending published works into the inspiration feed, and rerunning Android unit tests successfully.

Revision note (2026-04-04 10:05Z, Codex): updated the plan after fixing the final-result screen state machine so pending preview jobs no longer render as "ready" with blank image slots, adding automatic poll resumption when reopening a still-running result page, and adding host-side regression coverage for the timeout-then-resume path.

Revision note (2026-04-04 10:40Z, Codex): updated the plan after adding an inspiration-detail route and screen, wiring inspiration cards to open a Xiaohongshu-style detail page, introducing lightweight locally persisted likes/bookmarks/comments for inspiration items, and adding regression coverage for inspiration engagement persistence.

Revision note (2026-04-04 14:35Z, Codex): updated the plan after simplifying the inspiration home screen into a content-first feed with a lighter header, a single retained AI-remodel CTA, and unified published/editorial card styling that removes the oversized hero copy and noisy duplicate card metadata.

Revision note (2026-04-04 08:35Z, Codex): updated the plan after moving the hosted backend onto Neon + Vercel Blob persistence, fixing the Vercel runtime `postgresql://` driver mismatch, validating the full hosted preview flow on `https://second-bloom-backend.vercel.app`, switching the local debug backend property to the hosted alias, and reinstalling the debug APK onto the attached emulator.

Revision note (2026-04-04 08:55Z, Codex): updated the plan after switching the hosted backend's Qwen settings to the Singapore-region endpoints, tracing the remaining hosted recognize failure to duplicate model-supplied `analysisId` values in Neon, fixing `analyze_garment` to generate unique server-side analysis ids, and re-validating hosted recognition successfully.

Revision note (2026-04-04 17:20Z, Codex): updated the plan after landing a compilable backend auth/cloud-sync slice in the sibling repository, including Clerk JWT verification scaffolding, new `/me` and `/me/history*` routes, Neon-backed `app_users` plus `user_history_snapshots` tables, canonical snapshot merge helpers, and passing backend contract/unit coverage for the new endpoints.

Revision note (2026-04-04 17:55Z, Codex): updated the plan after integrating the Android-side Clerk auth surface, wiring guest-safe cloud sync bootstrap and push behavior into `RemodelViewModel`, aligning the in-repo Android API contract with the new `/me/**` endpoints, rerunning Android and backend tests successfully, and redeploying the production backend with the authenticated history routes enabled.

Revision note (2026-04-04 18:35Z, Codex): updated the plan after making the Profile login/account entry explicit, wiring `AccountScreen` into the actual nav graph, clarifying the "empty Clerk JWT template means default session token" rule in Android/backend docs, and tightening sign-out handling so it clears active sync context without deleting local guest history.

Revision note (2026-04-12 07:12Z, Codex): updated the plan after completing a deliberate `ralplan` pass for the demo-critical hosted preview/result failure, grounding the scope in `.omx/specs/deep-interview-core-flow-test-fix.md`, and writing execution-ready plan artifacts to `.omx/plans/prd-core-flow-test-fix.md` and `.omx/plans/test-spec-core-flow-test-fix.md` so the next execution lane can drive Android, sibling backend, and required deployment fixes toward two consecutive successful runs on `https://second-bloom-backend.vercel.app`.

Revision note (2026-04-12 07:22Z, Codex): refined the deliberate hosted-preview recovery plan after architect and critic review by converting it into a backend-first gated production-recovery workflow, adding explicit prereq evidence and Gate B signoff artifacts for env/migration/storage/deployment health, adding a 3-scenario pre-mortem, and tightening the test spec so final release proof is bound to one Vercel deployment and one Android build.

Revision note (2026-04-12 07:29Z, Codex): advanced Gate A with fresh live-hosted evidence against `https://second-bloom-backend.vercel.app`: `GET /health/live` and `GET /health/ready` both returned `200`, a public clothing sample successfully reached `POST /analyze-garment`, `POST /generate-remodel-plans`, and `POST /generate-remodel-preview-jobs`, polling `preview-job-f82c6166aaaa407691123109aa05c8b9` later returned `status=completed`, and the final `afterImage` Blob URL answered `HTTP 200` with `content-type: image/png`. This evidence shifted the active suspicion from hosted backend failure toward Android-side preview polling and result-state handling for the demo path.

Revision note (2026-04-12 07:49Z, Codex): executed the first Gate C Android fix pass by extending the preview polling window, persisting the queued `previewJobId` into `RemodelViewModel` state immediately after `createPreviewJob` succeeds, and treating poll-refresh failures on active jobs as "still processing" instead of terminal loss of recovery context. Re-verified with `sh gradlew testDebugUnitTest --console=plain`, `sh gradlew compileDebugAndroidTestKotlin --console=plain`, and `ANDROID_SERIAL=emulator-5554 sh gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.scf.secondbloom.RemodelScreensTest#previewResultScreen_keepsPollingWhenPlanRenderIsStillRunningAfterTimeout --console=plain`, all of which completed successfully. Additional live-device evidence was gathered on the emulator: the app installed and launched, a real hosted image completed `analyze` and `generate plans` successfully, but final Gate D closeout remains blocked because the physical phone disconnected before the same-build two-run real-phone acceptance could be executed.
