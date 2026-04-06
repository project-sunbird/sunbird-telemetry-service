---
description: Run a full health check — lint, tests, and verify the build
---

Run all quality checks for the telemetry service in sequence:

1. **Lint**: `cd src && npm run lint`
2. **Tests**: `cd src && npm test`

Report the status of each step. If any step fails, diagnose the issue using the build-validator agent before continuing to the next step.
