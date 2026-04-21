# API Summary

Second Bloom consumes a backend contract maintained in this repository's `docs/api/` directory.

## Source of truth

- `docs/api/mvp-contract.md`
- `docs/api/backend-architecture.md`
- `docs/api/backend-delivery-plan.md`
- `docs/api/postgres-schema-draft.sql`

## Client integration rules

- The Android app should not hardcode production API URLs in tracked files.
- Debug API configuration comes from `secondBloomRemodelApiBaseUrl`.
- Contract updates should be reflected in both the Android data layer and the docs listed above.
