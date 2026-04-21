# Contributing

## Workflow

1. Create a focused branch for your change.
2. Keep product-facing behavior aligned with `PRD.md` and `docs/api/`.
3. Run the smallest relevant Gradle task before opening a pull request.
4. Update docs when build steps, architecture, or API assumptions change.

## Pull Requests

- Use a concise summary and explain user impact.
- Include validation steps such as `./gradlew testDebugUnitTest`.
- Attach screenshots for visible UI changes.

## Local Configuration

- Do not commit `local.properties`.
- Keep API URLs and Clerk keys in local machine config only.
