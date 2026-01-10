# Rate Limiting

## Overview

Rate limiting protects the EXAM API from abuse and denial-of-service (DoS) attacks by limiting the number of requests a client can make within a specified time window.

## Implementation

The rate limiting filter (`RateLimitFilter`) uses a **token bucket algorithm** with a sliding window approach:

- **Per-IP limiting**: Each client IP address has its own rate limit bucket
- **Sliding window**: Requests are counted within a time window (default: 60 seconds)
- **In-memory storage**: Rate limit state is stored in memory (suitable for single-instance deployments)

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `RATE_LIMIT_ENABLED` | `true` | Enable/disable rate limiting |
| `RATE_LIMIT_REQUESTS` | `100` | Maximum requests allowed per window |
| `RATE_LIMIT_WINDOW` | `60` | Time window in seconds |
| `RATE_LIMIT_API_PATHS` | `/api/v1/` | Comma-separated paths to rate limit |

### Configuration File

You can also configure rate limiting in `backend/conf/application.conf`:

```hocon
exam.rate.limit.enabled = true
exam.rate.limit.requests = 100
exam.rate.limit.window = 60
exam.rate.limit.api.paths = "/api/v1/"
```

## Behavior

### Rate Limit Headers

All API responses include rate limit headers:

- `X-RateLimit-Limit`: Maximum requests allowed per window
- `X-RateLimit-Remaining`: Remaining requests in current window
- `X-RateLimit-Reset`: Unix timestamp when the rate limit resets

### Rate Limit Exceeded

When a client exceeds the rate limit:

- **Status Code**: `429 Too Many Requests`
- **Response Body**: JSON error with details
- **Headers**: 
  - `Retry-After`: Seconds until rate limit resets
  - `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`

Example response:

```json
{
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "Rate limit exceeded. Maximum 100 requests per 60 seconds.",
  "retryAfter": 45
}
```

## Client IP Detection

The filter detects client IP addresses in the following order:

1. `X-Forwarded-For` header (for reverse proxies/load balancers)
2. `X-Real-IP` header (for nginx)
3. Direct connection IP address

**Important**: When behind a reverse proxy, ensure the proxy sets the `X-Forwarded-For` or `X-Real-IP` header correctly.

## Scope

### Protected Endpoints

By default, rate limiting applies to:
- `/api/v1/*` - All API endpoints

### Excluded Endpoints

Rate limiting does NOT apply to:
- `/health` - Health check endpoints
- `/metrics` - Metrics endpoint
- `/` - Home endpoint

You can customize which paths are rate-limited via the `RATE_LIMIT_API_PATHS` configuration.

## Performance Considerations

- **Memory Usage**: Rate limit state is stored in memory. For high-traffic deployments, consider using Redis or a distributed cache.
- **Cleanup**: Old rate limit entries are cleaned up every 5 minutes to prevent memory leaks.
- **Thread Safety**: The implementation uses `ConcurrentHashMap` for thread-safe access.

## Production Recommendations

### For Single-Instance Deployments

The current in-memory implementation is suitable for single-instance deployments.

### For Multi-Instance Deployments

For distributed deployments (multiple backend instances), consider:

1. **Redis-based rate limiting**: Use Redis to share rate limit state across instances
2. **Load balancer rate limiting**: Configure rate limiting at the load balancer level (nginx, HAProxy)
3. **API Gateway**: Use an API gateway (Kong, AWS API Gateway) for centralized rate limiting

### Tuning Guidelines

- **High-traffic APIs**: Increase `RATE_LIMIT_REQUESTS` (e.g., 200-500)
- **Sensitive endpoints**: Decrease `RATE_LIMIT_REQUESTS` (e.g., 10-50)
- **Short windows**: Use smaller `RATE_LIMIT_WINDOW` (e.g., 30 seconds) for burst protection
- **Long windows**: Use larger `RATE_LIMIT_WINDOW` (e.g., 300 seconds) for sustained rate limiting

## Monitoring

Monitor rate limiting effectiveness:

1. **Logs**: Check for "Rate limit exceeded" warnings
2. **Metrics**: Track 429 status codes in metrics endpoint
3. **Headers**: Monitor `X-RateLimit-Remaining` values in responses

## Disabling Rate Limiting

To disable rate limiting (not recommended for production):

```bash
export RATE_LIMIT_ENABLED=false
```

Or in `application.conf`:

```hocon
exam.rate.limit.enabled = false
```

## Testing

Test rate limiting locally:

```bash
# Make 101 requests quickly
for i in {1..101}; do
  curl -s http://localhost:9000/api/v1/reports | jq -r '.error // "OK"'
done
```

You should see `RATE_LIMIT_EXCEEDED` errors after 100 requests.

## References

- [OWASP: Rate Limiting](https://owasp.org/www-community/controls/Blocking_Brute_Force_Attacks)
- [RFC 6585: 429 Too Many Requests](https://tools.ietf.org/html/rfc6585)
