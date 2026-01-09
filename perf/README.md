# Performance Testing

This directory contains performance testing scripts and results for the EXAM system.

## Overview

Performance testing is conducted using [k6](https://k6.io/), a modern load testing tool. The tests focus on the `/api/v1/reports` endpoint with pagination, which is a critical read-heavy operation.

## Prerequisites

1. **Install k6**: 
   - macOS: `brew install k6`
   - Linux: See [k6 installation guide](https://k6.io/docs/getting-started/installation/)
   - Windows: Download from [k6 releases](https://github.com/grafana/k6/releases)

2. **Start the application**: 
   ```bash
   make dev
   # or
   docker compose up
   ```

3. **Seed test data** (optional but recommended):
   ```bash
   # Use the seed script to populate the database with test data
   ./perf/seed-data.sh
   ```

## Running Load Tests

### Basic Test

Run the load test against the default localhost endpoint:

```bash
k6 run perf/load-test.js
```

### Custom Base URL

Test against a different endpoint:

```bash
K6_BASE_URL=http://localhost:9000 k6 run perf/load-test.js
```

### Test Scenarios

The load test script (`load-test.js`) includes:

- **Ramp-up phase**: Gradually increase load from 0 to 10 users over 30 seconds
- **Steady state**: Maintain 10 users for 1 minute
- **Stress phase**: Increase to 20 users over 30 seconds, maintain for 1 minute
- **Ramp-down**: Gradually decrease to 0 users

### Test Metrics

The test measures:

- **Latency**: p50, p95, p99 percentiles
- **Throughput**: Requests per second
- **Error rate**: Percentage of failed requests
- **Response validation**: Ensures correct JSON structure and status codes

### Thresholds

The test fails if:

- 95th percentile latency exceeds 500ms
- 99th percentile latency exceeds 1000ms
- Error rate exceeds 1%

## Test Results

Results are saved in two formats:

1. **Console output**: Summary statistics printed to stdout
2. **JSON file**: Detailed results saved to `perf/results.json`

### Baseline Results

See `baseline-results.md` for initial performance measurements before optimizations.

### Improvement Results

See `improvement-results.md` for performance measurements after optimizations, including before/after comparison.

## Performance Optimizations

### Implemented Optimizations

1. **Database Index Optimization** (see `improvement-results.md`):
   - Added GIN (full-text search) index on `title` and `content` for filter queries (PostgreSQL only)
   - Optimized `created_at` index for DESC sorting
   - Optimized count query execution (parallel queries)

**Note**: The GIN index is PostgreSQL-specific and should be applied manually to production databases using `database/migrations/002_add_gin_index.sql`. H2 (used in tests) does not support GIN indexes.

### Future Optimization Opportunities

- Connection pool tuning
- Query result caching (Redis)
- Database read replicas for read-heavy workloads
- Full-text search index for better filter performance

## Continuous Performance Testing

Performance tests can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions step
- name: Run performance tests
  run: |
    docker compose up -d
    sleep 10  # Wait for services to be ready
    k6 run perf/load-test.js
```

## Troubleshooting

### High Latency

- Check database connection pool settings
- Monitor database query performance
- Check application logs for errors
- Verify database indexes are being used

### High Error Rate

- Check application logs
- Verify database connectivity
- Check resource limits (CPU, memory)
- Review error response details in test output

### Test Timeout

- Increase test duration in `load-test.js`
- Reduce target user count
- Check if application is running and accessible

## References

- [k6 Documentation](https://k6.io/docs/)
- [k6 JavaScript API](https://k6.io/docs/javascript-api/)
- [Performance Testing Best Practices](https://k6.io/docs/test-types/load-testing/)
