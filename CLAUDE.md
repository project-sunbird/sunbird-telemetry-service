# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sunbird Telemetry Service — a Node.js/Express server that ingests telemetry events via HTTP and dispatches them to configurable backends (Kafka, Cassandra, file, or console). Supports local storage, remote proxy forwarding, or both simultaneously.

## Common Commands

All commands run from the `src/` directory:

```bash
cd src
npm install           # Install dependencies
npm run start         # Start server (default port 9001)
npm test              # Run tests with coverage (Mocha + nyc)
npm run lint          # ESLint check
npm run lint:fix      # ESLint auto-fix
```

Run a single test file:
```bash
cd src && npx mocha 'test/dispatcher/kafka-dispatcher.spec.js' --exit
```

Set `node_env=test` to disable clustering when running tests.

Docker build uses the two-stage process in `build.sh` (Dockerfile.Build → Dockerfile).

## Architecture

```
POST /v1/telemetry → routes/index.js → TelemetryService → Dispatcher → Transport
GET  /health       → routes/index.js → Dispatcher.health()
```

**app.js** — Express server entry point with `express-cluster` for multi-worker mode. Body parser limit is 5MB.

**routes/index.js** — Two endpoints only. Extracts `x-device-id`, `x-channel-id`, `x-app-id` headers.

**service/telemetry-service.js** — Singleton. Enriches each event with `mid` (uuid), `did`, `channel`, `pid`, `syncts`. Three dispatch modes controlled by env vars: local-only, proxy-only, or hybrid (both).

**dispatcher/dispatcher.js** — Uses Winston 2.x logger as a pluggable transport abstraction. Transport type selected by `telemetry_local_storage_type` env var (`kafka`/`file`/`cassandra`/undefined→console).

**dispatcher/kafka-dispatcher.js** — kafkajs-based Winston transport. Uses message `mid` as Kafka key. Injects `dataset` field from config if not already present. Supports gzip/snappy compression.

**dispatcher/cassandra-dispatcher.js** — Winston-cassandra transport with TTL and configurable partitioning (hour/day/month).

## Key Environment Variables

Configured in `envVariables.js`. Key ones:

| Variable | Purpose | Default |
|---|---|---|
| `telemetry_local_storage_type` | Dispatcher type: `kafka`, `file`, `cassandra` | console |
| `telemetry_local_storage_enabled` | Enable local dispatch | `true` |
| `telemetry_proxy_enabled` | Enable forwarding to remote URL | - |
| `telemetry_proxy_url` | Remote proxy endpoint | - |
| `telemetry_kafka_broker_list` | Kafka broker addresses | - |
| `telemetry_kafka_topic` | Kafka topic name | - |
| `telemetry_kafka_compression` | `none`/`gzip`/`snappy` | `none` |
| `telemetry_dataset` | Dataset tag injected into messages | `sb-telemetry` |
| `telemetry_service_port` | Server port | `9001` |
| `telemetry_service_threads` | Cluster worker count | CPU count |

## Code Style

- ESLint enforced: single quotes, semicolons required, 2-space indent
- Node.js CommonJS modules (`require`/`module.exports`)
- Winston 2.x API (not 3.x) — `log(level, msg, meta, callback)` pattern

## Testing

- Mocha + Chai + chai-http for route tests, Sinon for mocking
- Kafka integration tests need a running Kafka broker
- Coverage via nyc, reports in `coverage/` and `mochawesome-report/`
