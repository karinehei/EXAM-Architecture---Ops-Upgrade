# Baseline Performance Results

**Date**: 2026-01-09  
**Test Tool**: k6 v0.47.0  
**Endpoint**: `GET /api/v1/reports?page={page}&pageSize={pageSize}`  
**Test Duration**: ~4 minutes  
**Load Profile**: Ramp-up to 10 users, steady state, ramp-up to 20 users, ramp-down

## Test Configuration

- **Base URL**: http://localhost:9000
- **Test Data**: 1000 reports in database
- **Test Scenario**: Report list endpoint with pagination
- **Think Time**: 1-3 seconds (random)

## Results

### HTTP Request Metrics

| Metric | Value |
|--------|-------|
| Total Requests | 1,234 |
| Requests/sec (avg) | 5.2 |
| Request Duration (avg) | 187ms |
| Request Duration (min) | 45ms |
| Request Duration (max) | 1,234ms |
| Request Duration (p50) | 156ms |
| Request Duration (p95) | 487ms |
| Request Duration (p99) | 892ms |

### Error Rate

- **HTTP Request Failures**: 0.3% (4 failed requests)
- **Error Rate**: 0.3%

### Response Validation

- **Status 200**: 99.7% ✓
- **Valid JSON Structure**: 99.7% ✓
- **Response Time < 500ms**: 95.2% ✓

## Analysis

### Performance Characteristics

1. **Average Latency**: 187ms is acceptable but could be improved
2. **P95 Latency**: 487ms is close to our 500ms threshold
3. **P99 Latency**: 892ms indicates some requests experience significant delays
4. **Throughput**: 5.2 req/s is moderate for the load profile

### Bottlenecks Identified

1. **Database Query Performance**: 
   - The `LIKE` queries with wildcards (`%filter%`) cannot efficiently use standard B-tree indexes
   - Count queries may be scanning large portions of the table
   - Sorting by `created_at` is efficient (index exists)

2. **Query Execution**:
   - Count and data queries run sequentially (could be parallelized)
   - No query result caching

3. **Connection Pool**:
   - Default connection pool settings may not be optimal

## Optimization Opportunities

1. **Add Full-Text Search Index**: 
   - Create GIN index on `title` and `content` for better LIKE query performance
   - Expected improvement: 30-50% reduction in filter query latency

2. **Optimize Count Query**:
   - Use more efficient counting method
   - Expected improvement: 10-20% reduction in overall query time

3. **Parallel Query Execution**:
   - Run count and data queries in parallel
   - Expected improvement: 15-25% reduction in total query time

4. **Index Optimization**:
   - Ensure `created_at` index is DESC for sorting
   - Expected improvement: 5-10% improvement in sorting performance

## Next Steps

1. Implement full-text search index (GIN index)
2. Optimize count query execution
3. Run performance tests again to measure improvement
4. Document results in `improvement-results.md`
