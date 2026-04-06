You are creating an implementation plan for a change in the Sunbird Telemetry Service — a Node.js/Express server with Winston-based pluggable dispatchers for Kafka, Cassandra, and file.

## Steps

1. **Understand the request** — If the task is vague, ask one focused clarifying question.

2. **Explore the codebase** — Before planning, read relevant existing files:
   - `src/routes/index.js` for route patterns
   - `src/service/telemetry-service.js` for enrichment and dispatch logic
   - `src/dispatcher/dispatcher.js` for transport selection
   - `src/dispatcher/kafka-dispatcher.js` or `cassandra-dispatcher.js` for transport patterns
   - `src/envVariables.js` for configuration
   - `src/app.js` for Express setup

3. **Produce the plan** using the structure below.

4. **Confirm before implementing** — Present the plan and ask the user to approve.

---

## Plan Structure

### Summary
One sentence: what will be built, where it lives, and why.

### Files to Change
```
CREATE  src/dispatcher/{name}-dispatcher.js
CREATE  src/test/dispatcher/{name}-dispatcher.spec.js
MODIFY  src/dispatcher/dispatcher.js
MODIFY  src/envVariables.js
MODIFY  src/routes/index.js        (if new endpoint)
MODIFY  src/service/telemetry-service.js  (if enrichment changes)
```

### Layer Breakdown

**1. Route Layer** (`src/routes/index.js`)
- New endpoints (if any)
- Header extraction changes
- Request validation

**2. Service Layer** (`src/service/telemetry-service.js`)
- Message enrichment changes
- Dispatch mode changes

**3. Dispatcher Layer** (`src/dispatcher/dispatcher.js`)
- New transport registration
- Transport selection logic

**4. Transport Layer** (`src/dispatcher/{name}-dispatcher.js`)
- Winston Transport subclass
- Connection management
- `log()` and `health()` implementation

**5. Configuration** (`src/envVariables.js`)
- New env vars with defaults

### Test Plan
```
test/routes/index.spec.js
  - [ ] New endpoint happy path
  - [ ] Error cases

test/service/telemetry-service.spec.js
  - [ ] Enrichment logic
  - [ ] Dispatch mode

test/dispatcher/{name}-dispatcher.spec.js
  - [ ] log() success and failure
  - [ ] health() check
```

### Build & Verify
```bash
cd src
npm run lint
npm test
```

### Open Questions
List any ambiguities (empty if none).

---

Produce this plan now based on the user's request. If anything is unclear, ask first.
