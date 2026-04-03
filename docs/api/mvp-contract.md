# Loop MVP API Contract

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

## Notes

- Android fallback remains `MockRemodelApi` until `REMODEL_USE_REAL_API=true` and `REMODEL_API_BASE_URL` is configured.
- MVP intentionally excludes visual preview generation, sharing, account binding, and cloud history sync.
