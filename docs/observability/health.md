# Health Endpoints Semantics

## Overview

The EXAM system provides three health endpoints with different semantics for container orchestration and monitoring.

## Endpoints

### 1. Liveness Probe

**Path**: `/health/liveness`

**HTTP Method**: GET

**Purpose**: Indicates if the application process is alive and running.

**Semantics**:
- Returns **200 OK** if the process is running (regardless of dependencies)
- Should **never** check external dependencies (database, external services)
- Used by orchestrators to decide if the container should be **restarted**

**When to use**:
- Kubernetes `livenessProbe`
- Docker health checks for container restart
- Process monitoring

**Example Response:**
```json
{
  "status": "alive",
  "timestamp": 1705312200123
}
```

**Implementation Notes**:
- Minimal processing (no database queries)
- Fast response time (< 10ms)
- Should not fail unless the process is truly dead

### 2. Readiness Probe

**Path**: `/health/readiness`

**HTTP Method**: GET

**Purpose**: Indicates if the application is ready to accept traffic.

**Semantics**:
- Returns **200 OK** only if all critical dependencies are available
- Returns **503 Service Unavailable** if dependencies are unavailable
- Used by orchestrators to decide if traffic should be **routed** to the container
- Checks database connectivity

**When to use**:
- Kubernetes `readinessProbe`
- Load balancer health checks
- Service mesh health checks

**Example Response (Ready):**
```json
{
  "status": "ready",
  "database": "connected",
  "timestamp": 1705312200123
}
```

**Example Response (Not Ready):**
```json
{
  "status": "not_ready",
  "database": "error",
  "error": "Connection timeout",
  "timestamp": 1705312200123
}
```

**Implementation Notes**:
- Checks database connectivity with a simple query
- May take longer than liveness (50-100ms)
- Should fail if critical dependencies are unavailable

### 3. General Health Check

**Path**: `/health`

**HTTP Method**: GET

**Purpose**: Backwards-compatible general health check.

**Semantics**:
- Same as readiness probe
- Maintained for compatibility with existing monitoring tools

## Kubernetes Configuration

### Example Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: exam-backend
spec:
  template:
    spec:
      containers:
      - name: backend
        image: exam-backend:latest
        livenessProbe:
          httpGet:
            path: /health/liveness
            port: 9000
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /health/readiness
            port: 9000
          initialDelaySeconds: 10
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
```

### Probe Configuration Guidelines

**Liveness Probe**:
- `initialDelaySeconds`: 30-60 (allow app to start)
- `periodSeconds`: 10-30 (check frequency)
- `timeoutSeconds`: 5 (response timeout)
- `failureThreshold`: 3 (restart after 3 failures)

**Readiness Probe**:
- `initialDelaySeconds`: 10-20 (faster than liveness)
- `periodSeconds`: 5-10 (more frequent checks)
- `timeoutSeconds`: 3 (faster timeout)
- `failureThreshold`: 3 (remove from service after 3 failures)

## Docker Compose Configuration

```yaml
services:
  backend:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/health/readiness"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 30s
```

## Best Practices

1. **Never check external services in liveness**: Liveness should only check if the process is alive
2. **Check all critical dependencies in readiness**: Database, message queues, etc.
3. **Keep probes fast**: Response time should be < 100ms
4. **Use appropriate timeouts**: Don't set timeouts too low (may cause false positives)
5. **Monitor probe failures**: Alert on repeated probe failures
6. **Graceful degradation**: If non-critical dependencies fail, readiness can still return 200

## Troubleshooting

### Liveness Failing
- **Symptom**: Container keeps restarting
- **Cause**: Application process is crashing
- **Action**: Check application logs, memory limits, startup errors

### Readiness Failing
- **Symptom**: Container running but not receiving traffic
- **Cause**: Database or other dependency unavailable
- **Action**: Check database connectivity, network issues, dependency health

### Both Failing
- **Symptom**: Container not starting or immediately restarting
- **Cause**: Application startup failure
- **Action**: Check startup logs, configuration errors, missing dependencies

## Monitoring

### Metrics to Track
- Probe success/failure rates
- Probe response times
- Time between failures
- Dependency availability

### Alerts
- Alert if liveness fails > 3 times in 5 minutes
- Alert if readiness fails > 5 times in 10 minutes
- Alert if probe response time > 500ms
