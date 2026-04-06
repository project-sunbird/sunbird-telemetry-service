You are creating a git commit for changes in the Sunbird Telemetry Service repository. Follow these steps:

## Steps

1. Run `git status` to see what's changed
2. Run `git diff --staged` to see staged changes (if nothing staged, run `git diff HEAD` to see all changes)
3. Run `git log --oneline -10` to understand the recent commit message style
4. Analyze the changes and determine the commit type and scope
5. Stage appropriate files if nothing is staged yet (ask the user before staging if unsure)
6. Present the proposed commit message to the user for confirmation before committing
7. Create the commit

## Commit Message Format

```
{type}: {short description}
```

### Types
- `feat` — new feature or capability
- `fix` — bug fix
- `refactor` — code restructure without behavior change
- `test` — adding or fixing tests
- `chore` — build, config, dependency changes
- `docs` — documentation only

### Scope Areas
- routes — Express route handlers
- service — TelemetryService enrichment/dispatch logic
- dispatcher — Winston dispatcher abstraction
- kafka — Kafka transport
- cassandra — Cassandra transport
- config — envVariables.js or other configuration
- docker — Dockerfile, build.sh
- ci — GitHub Actions, Jenkinsfile

### Rules
- Subject line: max 72 characters, imperative mood ("add", not "added")
- No period at the end of the subject line
- Body (if needed): explain *why*, not *what*
- Reference issue numbers if relevant

## Examples

```
feat: add snappy compression support for Kafka dispatcher
```

```
fix: ensure callback is always invoked in kafka-dispatcher log method

Prevents Winston from stalling when Kafka produce fails.
```

```
refactor: extract message enrichment into separate function
```

```
chore: upgrade kafkajs to 2.2.4
```

```
test: add coverage for proxy-only dispatch mode
```

---

After analyzing the changes, present the proposed commit message to the user for confirmation before committing.
