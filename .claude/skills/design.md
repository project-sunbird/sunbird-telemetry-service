You are helping design a new feature for the Sunbird Telemetry Service — a Node.js/Express server that dispatches telemetry events to Kafka, Cassandra, or file via Winston transports.

The user has described a feature or requirement. Produce a concrete design that fits the project's established patterns.

## Design Output Structure

### 1. Feature Summary
One paragraph: what the feature does, which layer(s) it affects (route, service, dispatcher, transport), and why it's needed.

### 2. API Design (if route changes needed)
```
METHOD /v1/{path}
Headers: x-device-id, x-channel-id, x-app-id (if needed)

Request body:
{
  field: type,
  ...
}

Response (200):
{
  id: "api.telemetry",
  ver: "1.0",
  ts: "...",
  params: { status: "successful" }
}

Error responses:
- 400: {validation failure reason}
- 500: {internal/dispatch failure}
```

### 3. Layer-by-Layer Design

#### Route Layer (`src/routes/index.js`)
- HTTP method + path
- Header extraction
- Request validation

#### Service Layer (`src/service/telemetry-service.js`)
- Message enrichment logic
- Dispatch mode (local / proxy / hybrid)
- Any new fields added to events

#### Dispatcher Layer (`src/dispatcher/dispatcher.js`)
- Transport selection changes (if any)
- New transport type (if any)

#### Transport Layer (`src/dispatcher/{name}-dispatcher.js`)
If adding a new transport:
- Winston Transport subclass with `log(level, msg, meta, callback)`
- `health(callback)` method for health checks
- Connection management and error handling

### 4. Configuration
| Env Variable | Default | Purpose |
|---|---|---|
| `telemetry_*` | ... | ... |

Must be added to `src/envVariables.js`.

### 5. Test Plan
```
Route tests (test/routes/):
- [ ] New endpoint accepts valid payload
- [ ] Returns error for invalid input
- [ ] Health check reflects new transport status

Service tests (test/service/):
- [ ] Message enrichment correct
- [ ] Dispatch mode honored

Transport tests (test/dispatcher/):
- [ ] log() sends to backend correctly
- [ ] log() calls callback on success and failure
- [ ] health() returns status
```

### 6. Open Questions
List ambiguities that need clarification.

---

Now produce this design for the feature the user described. Ask one clarifying question first if the requirement is ambiguous.
