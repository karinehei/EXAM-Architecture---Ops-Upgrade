# CI Pipeline Documentation

## Overview

The CI pipeline runs automatically on every push to `main`/`master` and on every pull request. It ensures code quality, security, and buildability before merging.

## Pipeline Stages

### 1. Backend Build & Test

- **Runs on**: Ubuntu Latest
- **Steps**:
  1. Checkout code
  2. Set up JDK 17 (Temurin)
  3. Cache SBT dependencies (for faster builds)
  4. Compile backend code (`sbt compile`)
  5. Run backend tests (`sbt test`)

**Caching**: SBT dependencies and build artifacts are cached between runs to speed up builds.

### 2. Frontend Build & Test

- **Runs on**: Ubuntu Latest
- **Steps**:
  1. Checkout code
  2. Set up Node.js 20
  3. Cache npm dependencies
  4. Install dependencies (`npm ci` or `npm install`)
  5. Run linter (`npm run lint`) - optional, won't fail if not configured
  6. Build frontend (`npm run build`)
  7. Run tests (`npm test`) - optional, won't fail if not configured

**Caching**: npm dependencies are cached between runs.

### 3. Security Scanning

- **Runs on**: Ubuntu Latest
- **Steps**:
  1. Set up Snyk (if token configured)
  2. Scan backend dependencies for vulnerabilities
  3. Scan frontend dependencies for vulnerabilities
  4. Run `npm audit` on frontend

**Note**: Security scans are configured to `continue-on-error: true` so they don't block the pipeline, but vulnerabilities will be reported.

**Setup**: To enable Snyk scanning, add `SNYK_TOKEN` to your GitHub repository secrets.

### 4. Docker Build Validation

- **Runs on**: Ubuntu Latest
- **Depends on**: Backend and Frontend jobs must pass
- **Steps**:
  1. Set up Docker Buildx
  2. Build backend Docker image (validation only, not pushed)
  3. Build frontend Docker image (validation only, not pushed)

**Caching**: Docker layer caching is enabled using GitHub Actions cache.

### 5. CI Success Check

- **Runs on**: Ubuntu Latest
- **Depends on**: All previous jobs
- **Purpose**: Validates that all required jobs passed

## Configuration

### Required Secrets

- `SNYK_TOKEN` (optional): Snyk API token for dependency vulnerability scanning

### Environment Variables

- `SCALA_VERSION`: 2.13.12
- `NODE_VERSION`: 20

## Workflow Triggers

The CI pipeline runs on:
- **Push** to `main` or `master` branches
- **Pull requests** targeting `main` or `master` branches

## Viewing Results

1. Go to the **Actions** tab in your GitHub repository
2. Click on a workflow run to see detailed logs
3. Each job shows:
   - Build logs
   - Test results
   - Security scan results
   - Docker build status

## Troubleshooting

### Backend Tests Failing

- Check SBT compilation errors in the logs
- Verify test dependencies are correct
- Ensure database configuration is not required for unit tests

### Frontend Tests Failing

- Check if tests are properly configured in `package.json`
- Verify Angular test setup
- Check for missing dependencies

### Docker Build Failing

- Verify Dockerfiles are correct
- Check for missing build context files
- Ensure all dependencies are available

### Security Scan Issues

- If Snyk is not configured, scans will be skipped (this is expected)
- To enable Snyk, add `SNYK_TOKEN` to repository secrets
- `npm audit` will run regardless of Snyk configuration

## Customization

### Adding New Jobs

Edit `.github/workflows/ci.yml` and add a new job:

```yaml
new-job:
  name: New Job Name
  runs-on: ubuntu-latest
  steps:
    - name: Checkout code
      uses: actions/checkout@v4
    # Add your steps here
```

### Modifying Cache Keys

Update the `key` field in cache actions to include additional files that affect dependencies:

```yaml
key: ${{ runner.os }}-sbt-${{ hashFiles('backend/build.sbt', 'backend/project/**/*.sbt') }}
```

### Skipping Jobs on Certain Conditions

Use `if` conditions:

```yaml
if: github.event_name == 'pull_request'
```

## Best Practices

1. **Keep builds fast**: Use caching effectively
2. **Fail fast**: Run quick checks first (lint, compile)
3. **Parallel execution**: Jobs run in parallel when possible
4. **Security first**: Don't skip security scans
5. **Clear errors**: Ensure error messages are helpful

## Next Steps

- Set up branch protection rules requiring CI to pass
- Configure status checks for required branches
- Add deployment jobs (after CI passes)
- Set up notifications for failed builds
