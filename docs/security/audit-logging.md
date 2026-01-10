# Audit Logging

## Overview

Audit logging records all API access for security auditing, compliance, and forensic analysis. Every API request is logged with details about who accessed what, when, and from where.

## Implementation

The audit logging filter (`AuditLogFilter`) logs all API requests in a structured format for easy parsing and analysis.

## Logged Information

Each audit log entry includes:

| Field | Description | Example |
|-------|-------------|---------|
| `timestamp` | ISO 8601 timestamp | `2026-01-09T20:00:00.000Z` |
| `method` | HTTP method | `GET`, `POST`, `PUT`, `DELETE` |
| `path` | Request path | `/api/v1/reports` |
| `queryString` | Query parameters | `page=1&pageSize=20` |
| `statusCode` | HTTP status code | `200`, `404`, `500` |
| `userId` | User identifier | `user123` or `anonymous` |
| `userEmail` | User email | `user@example.com` |
| `clientIp` | Client IP address | `192.168.1.100` |
| `userAgent` | Browser/user agent | `Mozilla/5.0...` |
| `correlationId` | Request correlation ID | `abc-123-def-456` |
| `durationMs` | Request duration in milliseconds | `45` |

## Log Format

Audit logs are written in a structured, space-separated format:

```
AUDIT: timestamp=2026-01-09T20:00:00.000Z method=GET path=/api/v1/reports queryString=page=1 statusCode=200 userId=user123 userEmail=user@example.com clientIp=192.168.1.100 userAgent=Mozilla/5.0 correlationId=abc-123 durationMs=45
```

### Log Levels

- **INFO**: Successful requests (status code < 400)
- **WARN**: Failed requests (status code >= 400)

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `AUDIT_ENABLED` | `true` | Enable/disable audit logging |
| `AUDIT_LOG_API_ONLY` | `true` | Only log API endpoints (exclude static assets) |
| `AUDIT_LOG_SUCCESS` | `true` | Log successful requests |
| `AUDIT_LOG_FAILURES` | `true` | Log failed requests |

### Configuration File

You can also configure in `backend/conf/application.conf`:

```hocon
exam.audit.enabled = true
exam.audit.log.api.only = true
exam.audit.log.success = true
exam.audit.log.failures = true
```

## Scope

### Logged Endpoints

By default, audit logging applies to:
- `/api/*` - All API endpoints
- `/health` - Health check endpoints
- `/metrics` - Metrics endpoint

### Excluded Endpoints

When `AUDIT_LOG_API_ONLY=true`, the following are NOT logged:
- Static assets (CSS, JS, images)
- Favicon requests
- Other non-API endpoints

Set `AUDIT_LOG_API_ONLY=false` to log all requests.

## Log Storage

### Current Implementation

- **Location**: Application logs (same as application logs)
- **Format**: Structured text format
- **Rotation**: Managed by logback configuration

### Production Recommendations

For production deployments, consider:

1. **Centralized Logging**: Send audit logs to a centralized logging system (ELK, Splunk, CloudWatch)
2. **Separate Log File**: Configure a dedicated audit log file
3. **Immutable Storage**: Store audit logs in immutable storage (WORM)
4. **Retention Policy**: Define retention policies based on compliance requirements
5. **Encryption**: Encrypt audit logs at rest

### Example: Separate Audit Log File

Add to `backend/conf/logback.xml`:

```xml
<appender name="AUDIT_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
  <file>${application.home:-.}/logs/audit.log</file>
  <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    <fileNamePattern>${application.home:-.}/logs/audit.%d{yyyy-MM-dd}.log</fileNamePattern>
    <maxHistory>90</maxHistory>
  </rollingPolicy>
  <encoder>
    <pattern>%message%n</pattern>
  </encoder>
</appender>

<logger name="audit" level="INFO" additivity="false">
  <appender-ref ref="AUDIT_FILE"/>
</logger>
```

## Log Parsing

### Using grep

Find all failed requests:
```bash
grep "statusCode=[45]" logs/audit.log
```

Find requests from specific IP:
```bash
grep "clientIp=192.168.1.100" logs/audit.log
```

Find requests by user:
```bash
grep "userId=user123" logs/audit.log
```

### Using jq (if converted to JSON)

Convert to JSON format for easier parsing:
```bash
# Parse audit log entry
echo "AUDIT: timestamp=2026-01-09T20:00:00.000Z method=GET path=/api/v1/reports statusCode=200 userId=user123" | \
  sed 's/AUDIT: //' | \
  sed 's/\([^= ]*\)=\([^ ]*\)/"\1":"\2",/g' | \
  sed 's/,$//' | \
  sed 's/^/{/' | \
  sed 's/$/}/' | \
  jq .
```

### Using Log Aggregation Tools

For production, use log aggregation tools:
- **ELK Stack**: Elasticsearch, Logstash, Kibana
- **Splunk**: Enterprise log analysis
- **CloudWatch**: AWS log aggregation
- **Datadog**: Log management and analysis

## Compliance

### GDPR Considerations

- **Personal Data**: Audit logs may contain personal data (IP addresses, user identifiers)
- **Retention**: Define retention policies (typically 1-7 years for compliance)
- **Access Control**: Restrict access to audit logs
- **Right to Erasure**: Consider how to handle deletion requests

### PCI DSS

- **Requirement 10**: Track and monitor all access to network resources
- **Requirement 10.2**: Implement automated audit trails
- **Requirement 10.3**: Record audit trail entries

### HIPAA

- **§164.312(b)**: Audit controls to record and examine activity
- **§164.312(c)**: Integrity controls to ensure audit logs are not altered

## Security Considerations

1. **Log Tampering**: Protect audit logs from modification
2. **Log Access**: Restrict access to audit logs (separate permissions)
3. **Log Integrity**: Use cryptographic hashing to detect tampering
4. **Sensitive Data**: Avoid logging sensitive data (passwords, tokens, PII)
5. **Log Volume**: Monitor log volume to prevent disk space issues

## Performance Impact

- **Minimal**: Audit logging adds ~1-2ms per request
- **Asynchronous**: Consider making audit logging asynchronous for high-traffic scenarios
- **Sampling**: For very high traffic, consider sampling (log 1 in N requests)

## Disabling Audit Logging

To disable audit logging (not recommended for production):

```bash
export AUDIT_ENABLED=false
```

## Testing

### Verify Audit Logging

1. Make an API request:
```bash
curl http://localhost:9000/api/v1/reports
```

2. Check logs:
```bash
tail -f logs/application.log | grep AUDIT
```

Expected output:
```
AUDIT: timestamp=2026-01-09T20:00:00.000Z method=GET path=/api/v1/reports queryString= statusCode=200 userId=anonymous userEmail= clientIp=127.0.0.1 userAgent=curl/7.68.0 correlationId=abc-123 durationMs=45
```

## References

- [OWASP: Logging Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html)
- [NIST: Security and Privacy Controls](https://csrc.nist.gov/publications/detail/sp/800-53/rev-5/final)
- [GDPR: Article 30 - Records of Processing Activities](https://gdpr-info.eu/art-30-gdpr/)
