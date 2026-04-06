You are running tests for the Sunbird Telemetry Service — a Node.js/Express server tested with Mocha, Chai, chai-http, and Sinon.

## Steps

1. **Identify the target** from:
   - The user's message (e.g., "test kafka dispatcher", "test routes")
   - Recently edited files in the conversation
   - The area of the codebase being changed

2. **Determine the test scope** — all tests, single file, or single describe/it block

3. **Run the appropriate command** (see reference below)

4. **Report results** — pass/fail count, any failures with error messages and file locations

5. **Point to coverage report** if the user wants to check coverage

---

## Command Reference

All commands run from `src/`:

```bash
cd src

# Run all tests with coverage
npm test

# Run a specific test file
npx mocha 'test/routes/index.spec.js' --exit

# Run tests matching a grep pattern
npx mocha 'test/**/*.spec.js' --grep "health" --exit

# Run with verbose output
npx mocha 'test/**/*.spec.js' --reporter spec --exit

# Lint check
npm run lint
```

Set `node_env=test` to disable Express clustering in tests.

---

## Test Structure

```
src/test/
├── routes/index.spec.js              # API endpoint tests (chai-http)
├── service/telemetry-service.spec.js  # Service logic tests (sinon mocks)
└── dispatcher/
    ├── dispatcher.spec.js             # Dispatcher abstraction tests
    └── kafka-dispatcher.spec.js       # Kafka transport tests
```

## Test Patterns

### Route Tests (chai-http)
```javascript
const chai = require('chai');
const chaiHttp = require('chai-http');
chai.use(chaiHttp);

describe('POST /v1/telemetry', () => {
  it('should accept valid telemetry', (done) => {
    chai.request(app)
      .post('/v1/telemetry')
      .send({ events: [...] })
      .end((err, res) => {
        expect(res).to.have.status(200);
        done();
      });
  });
});
```

### Service Tests (Sinon)
```javascript
const sinon = require('sinon');

afterEach(() => sinon.restore());

it('enriches message with mid and syncts', () => {
  // stub dispatcher, verify enrichment fields
});
```

### Dispatcher Tests
```javascript
it('selects kafka transport when configured', () => {
  // set telemetry_local_storage_type = 'kafka'
  // verify Kafka transport is added
});
```

---

## Coverage

- Tool: nyc (Istanbul)
- Reports: HTML (`coverage/`), mochawesome (`mochawesome-report/`)
- Run: `cd src && npm test`

---

## Notes

- Kafka dispatcher tests require a running Kafka broker for integration tests
- Use sinon stubs for unit tests to avoid external dependencies
- Always use `--exit` flag with mocha to prevent hanging

---

If the target is ambiguous, ask the user which area they're testing.
