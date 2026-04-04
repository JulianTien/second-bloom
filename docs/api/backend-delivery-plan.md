# Second Bloom Backend Delivery Plan

This document translates the architecture in `docs/api/backend-architecture.md` into an implementation-ready plan for the first live backend.

Status on 2026-04-04:

- The Android app can already call a real HTTP backend if `secondBloomRemodelApiBaseUrl` is configured.
- The backend itself is still not implemented in this repository.
- A first runnable implementation now exists in the sibling repository path `/Users/peng/AndroidStudioProjects/second-bloom-backend`.
- Visual preview is now documented as a vNext queue-based capability using API + Worker + Redis + Postgres + Object Storage.
- This document recommends one concrete reference stack and a step-by-step delivery sequence.

## 1. Recommended Reference Stack

This is a recommendation, not a hard requirement. It is optimized for fast MVP delivery and AI-provider integration.

### Application layer

- Language: Python 3.12
- Web framework: FastAPI
- Validation: Pydantic v2
- ASGI server: Uvicorn

### Persistence

- Database: Postgres 16
- ORM / SQL toolkit: SQLAlchemy 2.x
- Migrations: Alembic
- JSON storage: `jsonb` columns for warnings, defects, materials, and steps

### Object storage

- S3-compatible object storage
- Examples: AWS S3, Cloudflare R2, MinIO, Supabase Storage

### Queue / coordination

- Redis for preview job enqueueing and short-lived lease keys

### Observability

- Structured logs in JSON
- OpenTelemetry for traces and request correlation
- Metrics export compatible with Prometheus or hosted platform metrics

### Why this stack

- FastAPI is quick to stand up for file upload plus JSON endpoints.
- Python has low-friction integrations for image handling and model providers.
- Pydantic makes it easier to validate and normalize model output before returning it to Android.
- Postgres plus object storage matches the retention and operational boundaries defined in the architecture document.
- For the first real visual preview rollout, use DashScope Qwen image generation on the Beijing endpoint with `qwen-image-2.0-pro` as the default preview model.

## 2. Suggested Repository Layout For The Backend

The backend is not in this repository yet, but this layout is recommended for the future backend repo:

```text
second-bloom-backend/
  app/
    api/
      routes/
        analyze.py
        plans.py
        health.py
        preview.py
      dependencies.py
      error_handlers.py
      schemas/
        analyze.py
        plans.py
        common.py
        preview.py
    core/
      config.py
      logging.py
      security.py
      observability.py
    domain/
      models.py
      enums.py
      validation.py
    services/
      analyze_service.py
      plan_service.py
      storage_service.py
      preview_service.py
    providers/
      vision_provider.py
      plan_provider.py
      openai_provider.py
      glm_provider.py
    queue/
      redis_queue.py
    workers/
      preview_worker.py
    db/
      models/
        analysis.py
        plan.py
        preview.py
      session.py
      migrations/
    tests/
      contract/
      integration/
      unit/
  Dockerfile
  pyproject.toml
  alembic.ini
  README.md
```

## 3. Concrete Module Responsibilities

### `api/routes`

- Parse HTTP requests.
- Return HTTP responses matching `docs/api/mvp-contract.md`.
- Keep route handlers thin.

### `api/schemas`

- Define request and response schemas.
- Validate and serialize public API payloads.

### `services`

- Own use-case orchestration.
- Coordinate storage, provider calls, normalization, and persistence.

### `providers`

- Encapsulate vendor-specific prompt building and SDK usage.
- Never expose provider response shape to `api/routes`.

### `db/models`

- Represent operational persistence records only.
- Avoid putting prompt-building or business logic here.

## 4. Environment Variables

The first backend should support at least these environment variables:

- `APP_ENV`
- `APP_PORT`
- `LOG_LEVEL`
- `DATABASE_URL`
- `OBJECT_STORAGE_ENDPOINT`
- `OBJECT_STORAGE_BUCKET`
- `OBJECT_STORAGE_ACCESS_KEY`
- `OBJECT_STORAGE_SECRET_KEY`
- `OBJECT_STORAGE_REGION`
- `OBJECT_STORAGE_FORCE_PATH_STYLE`
- `REDIS_URL`
- `SIGNED_URL_TTL_SECONDS`
- `PREVIEW_JOB_TTL_SECONDS`
- `PREVIEW_LEASE_TTL_SECONDS`
- `VISION_PROVIDER`
- `VISION_MODEL_NAME`
- `PLAN_PROVIDER`
- `PLAN_MODEL_NAME`
- `PREVIEW_PROVIDER`
- `PREVIEW_MODEL_NAME`
- `SEGMENTATION_PROVIDER`
- `SEGMENTATION_MODEL_NAME`
- `VISUAL_QA_PROVIDER`
- `VISUAL_QA_MODEL_NAME`
- `DASHSCOPE_API_KEY`
- `QWEN_VISION_BASE_URL`
- `QWEN_VISION_MODEL_NAME`
- `QWEN_PLAN_BASE_URL`
- `QWEN_PLAN_MODEL_NAME`
- `QWEN_CHAT_TIMEOUT_SECONDS`
- `QWEN_IMAGE_API_KEY`
- `QWEN_IMAGE_EDIT_ENDPOINT`
- `QWEN_IMAGE_EDIT_MODEL_NAME`
- `QWEN_IMAGE_EDIT_FALLBACK_MODEL_NAME`
- `QWEN_IMAGE_EDIT_PROMPT_EXTEND`
- `QWEN_IMAGE_EDIT_WATERMARK`
- `QWEN_IMAGE_EDIT_TIMEOUT_SECONDS`
- `QWEN_IMAGE_EDIT_OUTPUT_COUNT`
- `QWEN_IMAGE_EDIT_TARGET_LONG_EDGE`
- `QWEN_IMAGE_EDIT_NEGATIVE_PROMPT`
- `QWEN_VISUAL_QA_BASE_URL`
- `QWEN_VISUAL_QA_MODEL_NAME`
- `QWEN_VISUAL_QA_TIMEOUT_SECONDS`
- `QWEN_VISUAL_QA_MINIMUM_SCORE`
- `OPENAI_API_KEY`
- `GLM_API_KEY`
- `MAX_UPLOAD_BYTES`
- `RAW_PROVIDER_PAYLOAD_LOGGING_ENABLED`
- `IMAGE_RETENTION_DAYS`
- `RESULT_RETENTION_DAYS`

## 5. Request Validation Rules

These should be implemented explicitly rather than left to provider behavior.

### `POST /analyze-garment`

- Accept only image mime types the Android app can plausibly produce, such as `image/jpeg`, `image/png`, and optionally `image/webp`.
- Reject files larger than the configured `MAX_UPLOAD_BYTES`.
- Require `fileName` and `mimeType`.
- Allow `fileSizeBytes` to be absent or blank.
- Normalize image orientation before provider submission.

### `POST /generate-remodel-plans`

- Require a valid `intent`.
- Require `confirmedAnalysis.analysisId`.
- Require non-empty `garmentType`, `color`, `material`, and `style`.
- Allow `warnings` and `defects` to be empty arrays.
- Allow `userPreferences` to be omitted or `null`, and normalize blank strings internally if needed.
- Assign and persist a stable public `planId` for every returned plan before exposing it to preview jobs.

## 6. Provider Output Normalization Rules

The backend should convert provider output into the app contract with deterministic rules.

### Analysis normalization

- If garment type is missing, treat the result as invalid.
- Map provider-specific confidence scales into `0.0` to `1.0`.
- Normalize background complexity into one of `low` or `high`.
- Normalize warning codes into a finite server-approved set.
- Cap the number of defects returned to a practical UI-safe maximum.

### Plan normalization

- Require at least one plan after filtering invalid candidates.
- Normalize `difficulty` into `easy`, `medium`, or `hard`.
- Ensure each plan has at least one step.
- Drop empty material names and empty step titles.
- If all candidate plans become invalid after normalization, return `422`.

## 7. Postgres Delivery Sequence

Use `docs/api/postgres-schema-draft.sql` as the starting point.

Suggested migration order:

1. Create enums or validated text constraints.
2. Create `analysis_requests`.
3. Create `analysis_results`.
4. Create `plan_requests`.
5. Create `plan_results` with a stable public `planId`.
6. Add indexes for `request_received_at`, `status`, and `analysis_id`.
7. Add retention or cleanup jobs after the happy path is stable.

## 8. Endpoint Implementation Order

Build the backend in this order:

1. `GET /health/live`
2. `GET /health/ready`
3. `POST /analyze-garment` with a fake provider implementation
4. `POST /generate-remodel-plans` with a fake provider implementation
5. Replace fake analysis provider with the real provider adapter
6. Replace fake plan provider with the real provider adapter
7. Wire object storage persistence
8. Wire Postgres persistence
9. Add structured logging and traces
10. Run Android end-to-end validation against staging

This order reduces integration risk because it proves the public contract before provider behavior is introduced.

## 9. Testing Strategy

### Contract tests

- Verify response fields exactly match `docs/api/mvp-contract.md`.
- Verify invalid uploads return `400` or `415`.
- Verify malformed JSON returns `400`.
- Verify invalid model output becomes `422`.

### Unit tests

- Provider output normalization
- Difficulty normalization
- Warning-code normalization
- Error mapping
- Retention helper behavior

### Integration tests

- Analyze endpoint with real multipart parsing
- Plan endpoint with real JSON parsing
- Postgres write and read behavior
- Object storage upload behavior using a local fake such as MinIO

### Android validation

- Point `secondBloomRemodelApiBaseUrl` at staging
- Run `sh gradlew testDebugUnitTest`
- Run `sh gradlew assembleDebugAndroidTest`
- Run `sh gradlew connectedDebugAndroidTest` on a device or emulator
- Manually verify one successful image upload plus one expected failure path

## 10. Visual Preview VNext

Visual preview is a separate asynchronous capability for `simulation` only. It uses the same stable `planId` values returned by `POST /generate-remodel-plans`, but it does not block the main plan-generation response. Android now asks the user to confirm one plan first, then creates a final-image-edit job for that single `planId`.

### Public contract summary

- `POST /generate-remodel-preview-jobs`
  - Accepts `renderMode=simulation` only.
  - Accepts exactly 1 confirmed `planId` returned by `POST /generate-remodel-plans`.
  - Returns a `previewJobId`, the requested single plan count, and the initial job state.
- `GET /remodel-preview-jobs/{previewJobId}`
  - Returns the stored job state, per-plan render state, and `beforeImage`, `afterImage`, and `comparisonImage` object references.
- Public error model
  - `400` malformed body
  - `404` unknown `previewJobId`
  - `409` conflicting active preview job for the same plan set
  - `422` invalid `renderMode` or invalid `planId`
  - `5xx` queue, worker, storage, or database failure

### Reference implementation shape

- API layer validates the request and writes the preview job shell to Postgres.
- Redis carries the enqueue signal and any short-lived lease keys for worker ownership.
- Worker loads the stable `planId` records, renders simulation assets, writes them to object storage, and updates Postgres.
- Postgres is the source of truth for job lookup and status transitions.
- Object storage keeps the generated preview assets only.

### Recommended delivery order

1. Add or persist stable public `planId` values for every generated plan.
2. Add `preview_jobs` and `preview_job_renders`.
3. Add preview request and response schemas plus route handlers.
4. Add Redis-backed queueing and a preview worker process.
5. Add contract tests for success, `404`, `409`, and `422` paths.
6. Add staging smoke tests for the async preview lifecycle.

## 11. Minimal Deployment Plan

### Local development

- Run backend with Docker Compose or local services.
- Use local Postgres.
- Use local Redis for preview jobs.
- Use MinIO for object storage.
- Use fake model providers by default.

### Staging

- Deploy one containerized backend service.
- Use a managed Postgres instance.
- Use a managed Redis service or Redis-compatible cache.
- Use one private object storage bucket.
- Enable real provider credentials.
- Use this environment for Android integration testing.

### Production

- Reuse the staging topology with stronger secrets management, alerts, backup settings, and Redis durability.
- Keep one backend service unless traffic or operational complexity proves a split is needed.

## 12. Rollout Checklist

- `docs/api/mvp-contract.md` and backend response schemas match.
- `docs/api/backend-architecture.md` and runtime design still match reality.
- Database migrations apply cleanly.
- Bucket lifecycle rules are configured.
- Redis health checks and queue processing pass.
- Provider secrets are loaded from the deployment environment.
- Structured logs include request IDs.
- Health endpoints pass.
- Android debug build can hit staging through `secondBloomRemodelApiBaseUrl`.
- One real image can complete the full flow and still appear in local app history.

## 13. Deferred Work

Do not build these into the first live backend unless requirements change:

- User accounts
- Authenticated cloud history sync
- Community post creation or feed APIs
- Tailor booking workflows
- Payment or subscription support

## 14. Exit Criteria For “Backend MVP Complete”

The first live backend is complete when all of the following are true:

- Staging and production each expose the two public endpoints plus health checks.
- The backend returns contract-valid analysis and plan responses for at least one normal clothing image.
- Invalid uploads and malformed provider responses map into stable app-visible error categories.
- Uploaded images are stored privately with lifecycle cleanup.
- Postgres stores operational records for successful and failed requests.
- The Android app can complete one real backend flow without code changes other than local base URL configuration.
