# Second Bloom MVP API Contract

This document is the source of truth for the first Android MVP backend.

## 1. Analyze Garment

- Method: `POST`
- Path: `/analyze-garment`
- Content-Type: `multipart/form-data`

### Multipart fields

- `image`: uploaded image binary
- `fileName`: original file name
- `mimeType`: image mime type
- `fileSizeBytes`: optional original size in bytes

### Success response

```json
{
  "analysis": {
    "analysisId": "analysis-123",
    "garmentType": "白色衬衫",
    "color": "白色",
    "material": "棉质",
    "style": "简约基础",
    "defects": [
      { "name": "袖口轻微磨损", "severity": "medium" }
    ],
    "backgroundComplexity": "low",
    "confidence": 0.92,
    "warnings": [
      { "code": "low_confidence", "message": "识别置信度偏低，建议确认后继续。" }
    ]
  }
}
```

### Error model

- `400` / `415`: invalid or unsupported image
- `422`: model could not produce a valid analysis payload
- `5xx`: transient server error

## 2. Generate Remodel Plans

- Method: `POST`
- Path: `/generate-remodel-plans`
- Content-Type: `application/json`

### Request body

```json
{
  "intent": "daily",
  "confirmedAnalysis": {
    "analysisId": "analysis-123",
    "garmentType": "白色衬衫",
    "color": "白色",
    "material": "棉质",
    "style": "简约基础",
    "defects": [],
    "backgroundComplexity": "low",
    "confidence": 0.92,
    "warnings": []
  },
  "userPreferences": "保留正式感"
}
```

### Success response

```json
{
  "plans": [
    {
      "planId": "plan-123",
      "title": "日常焕新 方案一",
      "summary": "保留原有轮廓并优化细节。",
      "difficulty": "easy",
      "materials": ["布用剪刀", "定位针", "同色线"],
      "estimatedTime": "1-2 小时",
      "steps": [
        { "title": "整理衣片", "detail": "先熨平并标记需要保留与调整的区域。" }
      ]
    }
  ],
  "reasoningNote": "optional backend note"
}
```

### Error model

- `400`: malformed request
- `422`: no valid plans generated
- `5xx`: transient server error

## 3. Visual Preview Jobs

Visual preview is now part of the active Android flow. After `POST /generate-remodel-plans` returns stable `planId` values, the client first lets the user confirm one plan, then creates an async final-edit job for that single plan and polls until the job reaches a terminal state.

- Method: `POST`
- Path: `/generate-remodel-preview-jobs`
- Content-Type: `application/json`

### Request body

```json
{
  "analysisId": "analysis-123",
  "renderMode": "simulation",
  "planId": "plan-123"
}
```

- `analysisId` must match the source image that was previously analyzed.
- `renderMode` currently accepts only `simulation`.
- `planId` must be a single confirmed plan ID returned by `POST /generate-remodel-plans`.

### Success response

`202 Accepted`

```json
{
  "previewJobId": "preview-job-123",
  "status": "queued",
  "requestedPlanCount": 1,
  "pollPath": "/remodel-preview-jobs/preview-job-123"
}
```

### Lookup

- Method: `GET`
- Path: `/remodel-preview-jobs/{previewJobId}`

### Success response

`200 OK`

```json
{
  "previewJobId": "preview-job-123",
  "analysisId": "analysis-123",
  "renderMode": "simulation",
  "status": "completed",
  "requestedPlanCount": 1,
  "completedPlanCount": 1,
  "failedPlanCount": 0,
  "pollPath": "/remodel-preview-jobs/preview-job-123",
  "results": [
    {
      "planId": "plan-123",
      "renderStatus": "completed",
      "beforeImage": {
        "assetId": "asset-before-1",
        "url": "https://storage.example/before.png",
        "expiresAt": "2026-04-05T12:00:00Z"
      },
      "afterImage": {
        "assetId": "asset-after-1",
        "url": "https://storage.example/after.png",
        "expiresAt": "2026-04-05T12:00:00Z"
      },
      "comparisonImage": {
        "assetId": "asset-compare-1",
        "url": "https://storage.example/comparison.png",
        "expiresAt": "2026-04-05T12:00:00Z"
      },
      "disclaimer": "AI visual simulation only. Final garment may differ.",
      "errorMessage": null
    }
  ]
}
```

### Preview statuses

- Job status: `queued`, `running`, `completed`, `completed_with_failures`, `failed`, `expired`
- Per-plan render status: `queued`, `running`, `completed`, `failed`, `filtered`

### Preview error model

- `400`: malformed request body
- `404`: preview job not found, `analysisId` not found, or `planId` not found
- `409`: preview job or asset has expired, or source image can no longer be loaded
- `422`: invalid `renderMode` or invalid `planId`
- `5xx`: transient worker, storage, or database failure

## 4. Authenticated User Profile

Authenticated user and cloud-history APIs live under `/me/**`. They are optional for the anonymous remodel flow, but required for account restore and cross-device sync.

- Auth: `Authorization: Bearer <Clerk session token>`
- Token source: Clerk Android SDK using the configured JWT template
- Anonymous access: not allowed

### Get Current User

- Method: `GET`
- Path: `/me`

### Success response

```json
{
  "clerkUserId": "user_2abc123",
  "primaryEmail": "demo@example.com",
  "displayName": "Second Bloom Demo",
  "avatarUrl": "https://images.clerk.dev/avatar.png"
}
```

### Error model

- `401`: missing or invalid bearer token
- `5xx`: transient auth or database failure

## 5. Authenticated Cloud History

Cloud history mirrors the Android local snapshot structure so the client can merge, restore, and re-upload data without maintaining a second long-lived domain model.

### Snapshot envelope

```json
{
  "schemaVersion": 1,
  "revision": 3,
  "snapshot": {
    "analyses": [],
    "planGenerations": [],
    "publishedRemodels": [],
    "inspirationEngagements": []
  }
}
```

### Get Cloud History

- Method: `GET`
- Path: `/me/history`

### Success response

`200 OK`

Returns the snapshot envelope above.

### Bootstrap Cloud History

- Method: `POST`
- Path: `/me/history/bootstrap`
- Content-Type: `application/json`

### Request body

```json
{
  "schemaVersion": 1,
  "snapshot": {
    "analyses": [],
    "planGenerations": [],
    "publishedRemodels": [],
    "inspirationEngagements": []
  }
}
```

### Success response

`200 OK`

```json
{
  "schemaVersion": 1,
  "revision": 1,
  "snapshot": {
    "analyses": [],
    "planGenerations": [],
    "publishedRemodels": [],
    "inspirationEngagements": []
  },
  "mergeApplied": false
}
```

- `mergeApplied=false` means the server initialized the user's cloud snapshot directly from the local payload.
- `mergeApplied=true` means the server already had cloud history and returned a merged canonical snapshot instead.

### Update Cloud History

- Method: `PUT`
- Path: `/me/history`
- Content-Type: `application/json`

### Request body

```json
{
  "baseRevision": 1,
  "schemaVersion": 1,
  "snapshot": {
    "analyses": [],
    "planGenerations": [],
    "publishedRemodels": [],
    "inspirationEngagements": []
  }
}
```

### Success response

`200 OK`

Returns the snapshot envelope with the next `revision`.

### Conflict response

`409 Conflict`

```json
{
  "message": "history revision conflict",
  "schemaVersion": 1,
  "revision": 4,
  "snapshot": {
    "analyses": [],
    "planGenerations": [],
    "publishedRemodels": [],
    "inspirationEngagements": []
  }
}
```

- Android should merge its pending local snapshot against the returned remote `snapshot`, then retry `PUT /me/history` using the returned `revision` as the new `baseRevision`.

### Cloud-history error model

- `401`: missing or invalid bearer token
- `409`: revision conflict; the response includes the latest canonical snapshot
- `5xx`: transient auth, merge, or database failure

## Notes

- System-level backend design lives in `docs/api/backend-architecture.md`.
- Implementation-ready backend rollout guidance lives in `docs/api/backend-delivery-plan.md`.
- The first relational schema draft lives in `docs/api/postgres-schema-draft.sql`.
- Public error responses should include at least a top-level `message` field.
- Visual preview uses an async job model and currently returns signed-style asset URLs from `GET /remodel-preview-jobs/{previewJobId}`.
- Android fallback remains `MockRemodelApi` until `REMODEL_USE_REAL_API=true` and `REMODEL_API_BASE_URL` is configured.
- Debug builds can opt into the real backend by setting `secondBloomRemodelApiBaseUrl` in Gradle properties or `local.properties`; when the value is blank, the app stays in mock mode.
- Debug builds can opt into Clerk-backed account features by setting `secondBloomClerkPublishableKey` and `secondBloomClerkJwtTemplate` in Gradle properties or `local.properties`.
- For the current Android v1 rollout, `secondBloomClerkJwtTemplate` may stay blank. In that case, the app sends Clerk's default session token to `/me/**`.
- For the current Android app, the visible login entry lives in the `我的主页 / Profile` tab. Unauthenticated users should see a primary `登录 / 注册` action there, and signed-in users should see an explicit `账号中心` entry there.
- Current Android app now waits for the user to confirm one plan before starting preview polling, preserves anonymous use when Clerk is not configured, and only enables account restore plus cloud history sync after a successful Clerk login.
