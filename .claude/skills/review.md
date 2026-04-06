You are performing a code review of recent changes in the Sunbird Telemetry Service — a Node.js/Express server with Winston dispatchers for Kafka, Cassandra, and file.

## Steps

1. Run `git diff HEAD` to see all unstaged changes, and `git diff --staged` for staged changes. If the user specified a file, review that instead.
2. Read each changed JS file **in full** to understand context.
3. Run `cd src && npm run lint` if lint issues are suspected.
4. For each file, produce a structured review.

## Review Output Format

```
## Code Review

### {relative/path/to/file.js}

**Critical** (must fix before merge)
- Line X: {specific issue and why it matters}

**Warning** (should fix)
- Line X: {issue}

**Suggestion** (optional improvement)
- Line X: {suggestion}

✓ No issues
```

After all files:
```
## Summary
Verdict: Approved | Needs Changes | Blocked
{One sentence explaining the verdict}
```

---

## Review Checklist

**Data Integrity**
- [ ] Message enrichment fields (mid, did, channel, pid, syncts) set correctly
- [ ] Kafka message key set to mid for ordering
- [ ] Dataset field injected when not present
- [ ] Events not silently dropped

**Winston Transport Contract**
- [ ] `log(level, msg, meta, callback)` signature correct
- [ ] Callback always invoked (even on error)
- [ ] Health check handles connection failures

**Async & Error Handling**
- [ ] No unhandled promise rejections
- [ ] Proxy HTTP calls have timeout and error handling
- [ ] Empty catch blocks flagged
- [ ] Callback-based APIs don't swallow errors

**Security**
- [ ] No hardcoded secrets
- [ ] Headers not passed through unsanitized
- [ ] No command injection via user input

**Code Quality**
- [ ] ESLint rules followed (single quotes, semicolons, 2-space indent)
- [ ] No unused variables
- [ ] No duplicated logic

**Configuration**
- [ ] New env vars added to envVariables.js with defaults
- [ ] No hardcoded values that should be configurable

**Testing**
- [ ] Changed code has corresponding test updates
- [ ] Sinon stubs restored after tests
- [ ] Edge cases covered (null headers, empty events)

---

Be direct and line-specific. Skip checklist items that don't apply. If a file is clean, say ✓.
