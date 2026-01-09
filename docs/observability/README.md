# Observability Documentation

## Overview

The EXAM system includes comprehensive observability features:
- **Structured logging** with correlation IDs
- **Health endpoints** (liveness and readiness)
- **Metrics endpoint** (Prometheus-style)
- **Request timing** for key endpoints

## Logging

### Structured Logging

Logs are structured to include:
- **Timestamp**: ISO 8601 format in UTC
- **Log level**: INFO, WARN, ERROR, etc.
- **Logger name**: Class/package that generated the log
- **Correlation ID**: Request tracing ID (see below)
- **Message**: Log message
- **MDC (Mapped Diagnostic Context)**: Additional context

### Correlation IDs

Every request is assigned a unique correlation ID:
- **Header**: `X-Request-ID`
- **Generation**: Auto-generated if not provided by client
- **Usage**: Included in all log entries for request tracing
- **Response**: Returned in response headers

**Example:**
```bash
curl -H "X-Request-ID: my-custom-id" http://localhost:9000/api/v1/reports
```

### Log Format

**Development (Console):**
```
2024-01-15T10:30:00.123Z [INFO] [controllers.ReportController] [abc123] Processing request
```

**Production (JSON - if logstash encoder available):**
```json
{
  "timestamp": "2024-01-15T10:30:00.123Z",
  "level": "INFO",
  "logger": "controllers.ReportController",
  "correlationId": "abc123",
  "message": "Processing request",
  "thread": "application-akka.actor.default-dispatcher-3"
}
```

**Production (Pattern - fallback):**
```
2024-01-15T10:30:00.123Z [INFO] [controllers.ReportController] [abc123] Processing request
```

### Log Files

- **Location**: `logs/application.log`
- **Rotation**: Daily rotation, 30 days retention
- **Format**: Same as console output

### Configuration

Logging is configured in `backend/conf/logback.xml`:
- Console appender for development
- File appender for production
- Optional JSON encoder (requires logstash-logback-encoder dependency)

## Health Endpoints

### Liveness Probe

**Endpoint**: `GET /health/liveness`

**Purpose**: Indicates if the application process is alive.

**Response**: Always returns 200 if the process is running.

**Use Case**: Kubernetes/container orchestrators use this to determine if the container should be restarted.

**Example:**
```bash
curl http://localhost:9000/health/liveness
```

**Response:**
```json
{
  "status": "alive",
  "timestamp": 1705312200123
}
```

### Readiness Probe

**Endpoint**: `GET /health/readiness`

**Purpose**: Indicates if the application is ready to serve traffic (dependencies available).

**Response**: 
- **200 OK**: Application is ready (database connected)
- **503 Service Unavailable**: Application is not ready (database unavailable)

**Use Case**: Kubernetes/container orchestrators use this to determine if traffic should be routed to the container.

**Example:**
```bash
curl http://localhost:9000/health/readiness
```

**Response (Ready):**
```json
{
  "status": "ready",
  "database": "connected",
  "timestamp": 1705312200123
}
```

**Response (Not Ready):**
```json
{
  "status": "not_ready",
  "database": "error",
  "error": "Connection timeout",
  "timestamp": 1705312200123
}
```

### General Health Check

**Endpoint**: `GET /health`

**Purpose**: Backwards-compatible health check (same as readiness).

**Response**: Same as readiness probe.

## Metrics

### Metrics Endpoint

**Endpoint**: `GET /metrics`

**Format**: Prometheus text format (version 0.0.4)

**Purpose**: Provides application metrics for monitoring and alerting.

### Available Metrics

#### Request Count
```
http_requests_total{endpoint="GET /api/v1/reports"} 42
```

#### Error Count
```
http_errors_total{endpoint="GET /api/v1/reports"} 2
```

#### Average Latency
```
http_request_duration_seconds_avg{endpoint="GET /api/v1/reports"} 0.123
```

#### Latency Percentiles
```
http_request_duration_seconds{endpoint="GET /api/v1/reports",quantile="0.5"} 0.100
http_request_duration_seconds{endpoint="GET /api/v1/reports",quantile="0.95"} 0.250
http_request_duration_seconds{endpoint="GET /api/v1/reports",quantile="0.99"} 0.500
```

### Example Usage

**Query metrics:**
```bash
curl http://localhost:9000/metrics
```

**Response:**
```
http_requests_total{endpoint="GET /api/v1/reports"} 42 1705312200
http_errors_total{endpoint="GET /api/v1/reports"} 2 1705312200
http_request_duration_seconds_avg{endpoint="GET /api/v1/reports"} 0.123 1705312200
http_request_duration_seconds{endpoint="GET /api/v1/reports",quantile="0.5"} 0.100 1705312200
http_request_duration_seconds{endpoint="GET /api/v1/reports",quantile="0.95"} 0.250 1705312200
http_request_duration_seconds{endpoint="GET /api/v1/reports",quantile="0.99"} 0.500 1705312200
```

### Prometheus Integration

To scrape metrics with Prometheus, add to `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'exam-backend'
    scrape_interval: 15s
    static_configs:
      - targets: ['localhost:9000']
    metrics_path: '/metrics'
```

### Metrics Storage

**Current Implementation**: In-memory metrics store
- **Limitation**: Metrics reset on application restart
- **Histogram Size**: Last 1000 measurements per endpoint (to prevent memory issues)

**Production Recommendation**: 
- Use a proper metrics library (e.g., Micrometer, Dropwizard Metrics)
- Export to Prometheus, StatsD, or similar
- Consider using a time-series database for historical data

## Request Timing

All requests are automatically timed by the `MetricsFilter`:
- **Start time**: Captured at request start
- **Duration**: Calculated at response completion
- **Recording**: Metrics recorded asynchronously (non-blocking)

Timing includes:
- Network latency
- Application processing time
- Database query time
- Response serialization

## Monitoring Best Practices

### 1. Health Checks
- Use `/health/liveness` for container restart decisions
- Use `/health/readiness` for traffic routing decisions
- Configure appropriate intervals (e.g., 10s for liveness, 5s for readiness)

### 2. Metrics Collection
- Scrape `/metrics` endpoint regularly (e.g., every 15-30 seconds)
- Set up alerts on error rates and latency percentiles
- Monitor p95 and p99 latencies for SLA compliance

### 3. Log Aggregation
- Collect logs from `logs/application.log`
- Parse correlation IDs for request tracing
- Set up log aggregation (ELK, Splunk, etc.) for production

### 4. Correlation ID Usage
- Always include `X-Request-ID` in client requests
- Use correlation ID to trace requests across services
- Include correlation ID in error reports

## Troubleshooting

### Logs Not Appearing
- Check `logs/application.log` file permissions
- Verify logback.xml configuration
- Check application logs for configuration errors

### Metrics Not Updating
- Verify MetricsFilter is registered in filters
- Check that requests are being processed
- Verify metrics endpoint is accessible

### Health Checks Failing
- Check database connectivity
- Verify database credentials
- Check application logs for errors

## Configuration

### Environment Variables

- `LOG_LEVEL`: Set log level (INFO, DEBUG, WARN, ERROR)
- `LOG_FORMAT`: Set log format (json, pattern)

### Logback Configuration

Edit `backend/conf/logback.xml` to customize:
- Log levels per package
- Appender configuration
- Log format (JSON vs pattern)

## Next Steps

1. **Set up Prometheus**: Configure Prometheus to scrape metrics
2. **Set up Grafana**: Create dashboards for visualization
3. **Log Aggregation**: Set up ELK stack or similar
4. **Alerting**: Configure alerts based on metrics
5. **Distributed Tracing**: Consider adding OpenTelemetry/Jaeger for distributed systems
