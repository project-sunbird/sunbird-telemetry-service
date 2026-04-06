---
description: Run ESLint on the codebase and fix violations
---

Run ESLint from the `src/` directory. If `--fix` or `fix` is passed as an argument, auto-fix violations. Otherwise, report them.

```bash
cd src
```

If "$ARGUMENTS" contains "fix":
```bash
npm run lint:fix
```
Otherwise:
```bash
npm run lint
```

Report any remaining violations with file paths and line numbers.
