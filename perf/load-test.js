import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const listLatency = new Trend('list_latency');
const listThroughput = new Trend('list_throughput');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 10 },  // Ramp up to 10 users over 30 seconds
    { duration: '1m', target: 10 },   // Stay at 10 users for 1 minute
    { duration: '30s', target: 20 },  // Ramp up to 20 users over 30 seconds
    { duration: '1m', target: 20 },   // Stay at 20 users for 1 minute
    { duration: '30s', target: 0 },   // Ramp down to 0 users over 30 seconds
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // 95% of requests should be below 500ms, 99% below 1s
    http_req_failed: ['rate<0.01'], // Error rate should be less than 1%
    errors: ['rate<0.01'],
  },
};

// Base URL - can be overridden with K6_BASE_URL environment variable
const BASE_URL = __ENV.K6_BASE_URL || 'http://localhost:9000';

export default function () {
  // Test scenario: Report list endpoint with pagination
  const page = Math.floor(Math.random() * 10) + 1; // Random page between 1-10
  const pageSize = [10, 20, 50][Math.floor(Math.random() * 3)]; // Random page size
  const filters = Math.random() > 0.7 ? `?filters=test&page=${page}&pageSize=${pageSize}` : `?page=${page}&pageSize=${pageSize}`;
  
  const url = `${BASE_URL}/api/v1/reports${filters}`;
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-Request-ID': `test-${Math.random().toString(36).substring(7)}`,
    },
    tags: { name: 'ReportList' },
  };

  const startTime = Date.now();
  const response = http.get(url, params);
  const duration = Date.now() - startTime;

  // Record metrics
  listLatency.add(duration);
  listThroughput.add(1);

  // Validate response
  const success = check(response, {
    'status is 200': (r) => r.status === 200,
    'response has data array': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.data !== undefined && Array.isArray(body.data);
      } catch (e) {
        return false;
      }
    },
    'response has pagination': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.pagination !== undefined && 
               body.pagination.page !== undefined && 
               body.pagination.pageSize !== undefined;
      } catch (e) {
        return false;
      }
    },
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  if (!success) {
    errorRate.add(1);
  } else {
    errorRate.add(0);
  }

  // Think time - simulate user reading the results
  sleep(Math.random() * 2 + 1); // Random sleep between 1-3 seconds
}

export function handleSummary(data) {
  return {
    'perf/results.json': JSON.stringify(data, null, 2),
  };
}
