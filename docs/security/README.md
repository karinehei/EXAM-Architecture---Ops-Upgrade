# Security Documentation

This directory contains security documentation for the EXAM system.

## Overview

The EXAM system implements multiple layers of security controls to protect against common threats and vulnerabilities. This documentation describes the security features, their configuration, and best practices.

## Security Features

### 1. Threat Modeling

- **[Threat Model](threat-model.md)**: Comprehensive threat analysis using STRIDE framework
  - Identifies security threats
  - Documents mitigations
  - Risk assessment

### 2. Rate Limiting

- **[Rate Limiting](rate-limiting.md)**: Protection against DoS attacks and API abuse
  - Token bucket algorithm
  - Per-IP rate limits
  - Configurable thresholds

### 3. Security Headers

- **[Security Headers](headers.md)**: HTTP security headers for browser protection
  - HSTS (HTTP Strict Transport Security)
  - X-Frame-Options
  - Content-Security-Policy
  - And more

### 4. Audit Logging

- **[Audit Logging](audit-logging.md)**: Comprehensive logging of all API access
  - Structured log format
  - User tracking
  - Compliance support

## Quick Start

### Enable All Security Features

All security features are enabled by default. No configuration needed for basic usage.

### Disable Security Features (Not Recommended)

```bash
# Disable rate limiting
export RATE_LIMIT_ENABLED=false

# Disable security headers
export SECURITY_HEADERS_ENABLED=false

# Disable audit logging
export AUDIT_ENABLED=false
```

## Configuration

### Environment Variables

See individual documentation files for detailed configuration options:

- [Rate Limiting Configuration](rate-limiting.md#configuration)
- [Security Headers Configuration](headers.md#configuration)
- [Audit Logging Configuration](audit-logging.md#configuration)

### Configuration File

You can also configure security features in `backend/conf/application.conf`:

```hocon
# Rate Limiting
exam.rate.limit.enabled = true
exam.rate.limit.requests = 100
exam.rate.limit.window = 60

# Security Headers
exam.security.headers.enabled = true
exam.security.headers.hsts.maxAge = 31536000

# Audit Logging
exam.audit.enabled = true
exam.audit.log.api.only = true
```

## Security Best Practices

### 1. Production Deployment

- ✅ Enable all security features
- ✅ Use HTTPS/TLS
- ✅ Set strong `APPLICATION_SECRET`
- ✅ Disable dev auth (`DEV_AUTH_ENABLED=false`)
- ✅ Configure appropriate rate limits
- ✅ Monitor audit logs
- ✅ Regular security updates

### 2. Development

- ✅ Use dev auth for local development only
- ✅ Never commit secrets to version control
- ✅ Use `.env` file (not committed)
- ✅ Test security features locally

### 3. Monitoring

- ✅ Monitor rate limit violations
- ✅ Review audit logs regularly
- ✅ Track security headers in responses
- ✅ Monitor for suspicious activity

## Security Testing

### Manual Testing

1. **Rate Limiting**:
   ```bash
   # Make 101 requests quickly
   for i in {1..101}; do curl http://localhost:9000/api/v1/reports; done
   ```

2. **Security Headers**:
   ```bash
   curl -I http://localhost:9000/api/v1/reports
   ```

3. **Audit Logging**:
   ```bash
   tail -f logs/application.log | grep AUDIT
   ```

### Automated Testing

- Use security scanners (OWASP ZAP, Burp Suite)
- Run dependency vulnerability scans (Snyk)
- Perform penetration testing
- Security code reviews

## Compliance

### GDPR

- Audit logging tracks data access
- Personal data handling documented
- Retention policies defined

### PCI DSS

- Audit logging for requirement 10
- Security controls documented
- Access controls implemented

### HIPAA

- Audit controls (164.312(b))
- Integrity controls (164.312(c))
- Access controls

## Incident Response

### Security Incident Checklist

1. **Identify**: Review audit logs and metrics
2. **Contain**: Block IP addresses, disable affected features
3. **Investigate**: Analyze logs, identify root cause
4. **Remediate**: Fix vulnerabilities, update security controls
5. **Document**: Record incident, update threat model
6. **Notify**: Inform stakeholders if required

### Log Analysis

Use audit logs to investigate security incidents:

```bash
# Find failed authentication attempts
grep "statusCode=401\|statusCode=403" logs/audit.log

# Find requests from suspicious IP
grep "clientIp=192.168.1.100" logs/audit.log

# Find rate limit violations
grep "statusCode=429" logs/audit.log
```

## Security Updates

### Regular Updates

- **Dependencies**: Regularly update dependencies (Snyk scans in CI)
- **Framework**: Keep Play Framework updated
- **Database**: Keep PostgreSQL updated
- **OS**: Keep container base images updated

### Vulnerability Management

1. Monitor security advisories
2. Run dependency scans
3. Apply security patches promptly
4. Test updates in staging

## References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP API Security Top 10](https://owasp.org/www-project-api-security/)
- [Play Framework Security](https://www.playframework.com/documentation/latest/SecurityHeaders)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)

## Support

For security concerns or questions:

1. Review this documentation
2. Check threat model for specific threats
3. Review individual feature documentation
4. Contact security team if needed

## Changelog

- **2026-01-09**: Initial security documentation
  - Threat model
  - Rate limiting
  - Security headers
  - Audit logging
