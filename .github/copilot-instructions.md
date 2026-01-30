# GitHub Copilot Instructions for Sunbird Telemetry Service

## Project Overview
This is a Node.js microservice that provides APIs for Sunbird telemetry. The service is built with Express.js and uses Kafka for message dispatching.

## Technology Stack
- **Node.js**: v22.15
- **Framework**: Express.js v4.16
- **Message Queue**: Kafka (using kafka-node v5.0.0)
- **Clustering**: express-cluster for multi-process support
- **Logging**: Winston with Cassandra and daily rotate file transports

## Project Structure
```
src/
├── app.js              # Main Express application setup
├── routes/             # API route definitions
├── service/            # Business logic layer
├── dispatcher/         # Kafka message dispatchers
├── test/               # Test files (mirrors src structure)
├── envVariables.js     # Environment configuration
└── package.json        # Dependencies and scripts
```

## Development Setup

### Prerequisites
- Node.js v22.15
- Kafka and Zookeeper running locally

### Installation
```bash
cd src
npm ci
```

### Running the Service
```bash
npm run start
```

### Working Directory
Always run npm commands from the `src/` directory, as that's where `package.json` is located.

## Code Style and Quality

### ESLint Configuration
- **Environment**: Node.js, ES2021
- **Indentation**: 2 spaces
- **Quotes**: Single quotes
- **Semicolons**: Required
- **Rules**:
  - `no-console`: warn
  - `no-debugger`: error
  - `no-unused-vars`: warn

### Linting
```bash
npm run lint          # Check for issues
npm run lint:fix      # Auto-fix issues
```

**Important**: Always run linting before committing code. The CI pipeline will fail PRs with linting errors.

## Testing

### Test Framework
- **Test Runner**: Mocha
- **Assertions**: Chai (with chai-http for HTTP testing)
- **Mocking/Stubbing**: Sinon
- **Coverage**: NYC (Istanbul)
- **Reporting**: Mochawesome

### Test Structure
- Tests are located in `src/test/` directory
- Test files follow the pattern `*.spec.js`
- Tests mirror the source structure (e.g., `service/telemetry-service.js` → `test/service/telemetry-service.spec.js`)

### Test Patterns
```javascript
// Standard test structure
const chai = require('chai');
const sinon = require('sinon');
const expect = chai.expect;

describe('Feature Name', () => {
  beforeEach(() => {
    // Setup stubs and mocks
    // Clear require cache if testing different configurations
  });

  afterEach(() => {
    // Restore stubs
  });

  it('should describe expected behavior', () => {
    // Test implementation
  });
});
```

### Running Tests
```bash
npm test                    # Run all tests with coverage
npm run test-with-coverage  # Run tests and upload to Codacy
```

## API Development Guidelines

### Express Application Structure
- The application uses clustering with configurable thread count
- CORS is enabled for all origins
- Body size limit is 5MB
- Keep-alive timeout is set to 5 minutes (300,000ms)

### Creating New Routes
1. Define routes in `src/routes/` directory
2. Implement business logic in `src/service/` directory
3. Keep routes thin - delegate to service layer
4. Follow existing patterns for consistency

### Environment Variables
- Configuration is centralized in `src/envVariables.js`
- Use environment variables for all configurable values
- Never hardcode credentials or sensitive data

## Kafka Integration

### Dispatcher Pattern
- Kafka dispatchers are in `src/dispatcher/` directory
- Use kafka-node library for Kafka interactions
- Follow the existing dispatcher patterns for consistency

## CI/CD

### Pull Request Checks
- **Linting**: Must pass ESLint checks
- **Dependencies**: Installed via `npm ci` for deterministic builds
- **Node Modules Caching**: GitHub Actions caches dependencies for faster builds

### Workflow Location
- `.github/workflows/pull_request.yml`: Runs on all PRs

### Before Submitting PRs
1. Run `npm run lint:fix` to fix auto-fixable issues
2. Run `npm test` to ensure tests pass
3. Ensure working directory is `src/` when running npm commands

## Docker

### Image Building
- Uses multi-stage builds (see `Dockerfile` and `Dockerfile.Build`)
- Images are published to GitHub Container Registry (GHCR)
- Automated builds trigger on tag creation

### Image Naming
- Repository: `ghcr.io/project-sunbird/sunbird-telemetry-service`
- Tag format: `{TAG_NAME}_{COMMIT_HASH}_{RUN_NUMBER}`

## Best Practices

### Code Organization
- Keep functions small and focused
- Use clear, descriptive variable names
- Follow the single responsibility principle
- Maintain consistent error handling patterns

### Error Handling
- Use Express error handling middleware
- Log errors appropriately with Winston
- Return meaningful HTTP status codes

### Performance
- Leverage clustering for multi-core utilization
- Use appropriate caching strategies
- Monitor Kafka consumer lag

### Security
- Never commit credentials or secrets
- Validate and sanitize all inputs
- Use latest security patches for dependencies
- Follow OWASP best practices for web services

## Common Tasks

### Adding a New Dependency
```bash
cd src
npm install <package-name>
```

### Updating Dependencies
```bash
cd src
npm update
npm audit fix  # Fix security vulnerabilities
```

### Debugging
- Set `node_env=test` to run in single-process mode for easier debugging
- Use debug logs with the Winston logger
- Check Kafka consumer status if messages aren't processing

## Additional Resources
- [Sunbird Documentation](https://github.com/project-sunbird)
- [Express.js Documentation](https://expressjs.com/)
- [Kafka Node Documentation](https://www.npmjs.com/package/kafka-node)

## Questions or Issues?
For questions or issues, please open an issue in the GitHub repository or refer to the project documentation.
