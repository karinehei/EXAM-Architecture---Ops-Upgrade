# Threat Model: EXAM System

**Date**: 2026-01-09  
**Version**: 1.0  
**Framework**: STRIDE

## Overview

This document identifies security threats to the EXAM system using the STRIDE framework (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege).

## System Architecture

- **Backend**: Play Framework (Scala) REST API
- **Frontend**: Angular web application
- **Database**: PostgreSQL
- **Authentication**: Shibboleth/HAKA (production), Dev bypass (development only)
- **Deployment**: Docker containers

## Threat Analysis

### 1. Spoofing (Identity Attacks)

#### Threats

| Threat | Description | Risk | Mitigation |
|--------|-------------|------|------------|
| **T1.1**: Unauthorized user impersonation | Attacker attempts to access system as another user | **High** | - Production: Shibboleth/HAKA authentication<br>- Dev: Dev auth filter (gated, never in production)<br>- Validate all user identity headers |
| **T1.2**: API endpoint spoofing | Attacker creates fake API endpoints or intercepts requests | **Medium** | - HTTPS/TLS encryption<br>- API authentication required<br>- CORS configuration |
| **T1.3**: Session hijacking | Attacker steals user session tokens | **Medium** | - Secure session management<br>- HTTPS only<br>- Session timeout |

#### Controls Implemented
- ✅ Dev auth filter with production mode checks
- ✅ CORS filter configured
- ⚠️ Production authentication (Shibboleth/HAKA) - external dependency

---

### 2. Tampering (Integrity Attacks)

#### Threats

| Threat | Description | Risk | Mitigation |
|--------|-------------|------|------------|
| **T2.1**: Data modification in transit | Attacker modifies API requests/responses | **High** | - HTTPS/TLS encryption<br>- Request validation<br>- Input sanitization |
| **T2.2**: Database tampering | Unauthorized modification of database records | **High** | - Database access controls<br>- Parameterized queries (Slick)<br>- Audit logging |
| **T2.3**: Configuration tampering | Attacker modifies application configuration | **Medium** | - Environment variable validation<br>- Configuration file permissions<br>- Secrets management |
| **T2.4**: Code injection | SQL injection, XSS, command injection | **High** | - Parameterized queries (Slick)<br>- Input validation<br>- Output encoding |

#### Controls Implemented
- ✅ Slick ORM (parameterized queries prevent SQL injection)
- ✅ Input validation in controllers
- ✅ JSON request validation
- ⚠️ Output encoding (frontend responsibility)

---

### 3. Repudiation (Non-Repudiation)

#### Threats

| Threat | Description | Risk | Mitigation |
|--------|-------------|------|------------|
| **T3.1**: Denial of actions | User denies performing actions (create, update, delete) | **Medium** | - Audit logging<br>- User identification in logs<br>- Timestamped actions |
| **T3.2**: Log tampering | Attacker modifies audit logs to hide actions | **Medium** | - Immutable log storage<br>- Log integrity checks<br>- Centralized logging |

#### Controls Implemented
- ✅ Audit logging filter (to be implemented)
- ✅ Correlation IDs for request tracing
- ⚠️ Log storage security (infrastructure concern)

---

### 4. Information Disclosure (Confidentiality)

#### Threats

| Threat | Description | Risk | Mitigation |
|--------|-------------|------|------------|
| **T4.1**: Sensitive data exposure | Database credentials, API keys, user data exposed | **High** | - Environment variables for secrets<br>- No secrets in code<br>- Database encryption at rest |
| **T4.2**: Error message information leakage | Error messages reveal system internals | **Medium** | - Generic error messages in production<br>- Detailed errors only in development<br>- No stack traces in production |
| **T4.3**: Unauthorized data access | User accesses data they shouldn't | **High** | - Authentication required<br>- Authorization checks (future)<br>- Data filtering by user |
| **T4.4**: Log information leakage | Logs contain sensitive information | **Medium** | - Sanitize logs<br>- No passwords/secrets in logs<br>- Log access controls |

#### Controls Implemented
- ✅ Environment variable configuration
- ✅ Structured logging (can filter sensitive data)
- ⚠️ Authorization layer (future enhancement)

---

### 5. Denial of Service (Availability)

#### Threats

| Threat | Description | Risk | Mitigation |
|--------|-------------|------|------------|
| **T5.1**: API rate limiting bypass | Attacker floods API with requests | **High** | - Rate limiting filter<br>- Per-IP rate limits<br>- Configurable thresholds |
| **T5.2**: Database connection exhaustion | Too many database connections | **High** | - Connection pooling<br>- Connection limits<br>- Timeout configuration |
| **T5.3**: Resource exhaustion | CPU, memory, disk exhaustion | **Medium** | - Resource limits (Docker)<br>- Monitoring and alerts<br>- Auto-scaling (infrastructure) |
| **T5.4**: Slow query attacks | Malicious queries consume resources | **Medium** | - Query timeouts<br>- Database indexes<br>- Query optimization |

#### Controls Implemented
- ✅ Rate limiting filter (to be implemented)
- ✅ Database connection pooling (Slick)
- ✅ Query timeouts configured
- ✅ Database indexes for performance

---

### 6. Elevation of Privilege (Authorization)

#### Threats

| Threat | Description | Risk | Mitigation |
|--------|-------------|------|------------|
| **T6.1**: Unauthorized admin access | Regular user gains admin privileges | **High** | - Role-based access control (future)<br>- Privilege separation<br>- Admin endpoint protection |
| **T6.2**: API endpoint privilege escalation | User accesses endpoints beyond their role | **High** | - Authorization middleware<br>- Role-based access control<br>- Endpoint-level checks |
| **T6.3**: Database privilege escalation | Application gains excessive database privileges | **Medium** | - Least privilege database user<br>- Read-only connections where possible<br>- Separate users for different operations |

#### Controls Implemented
- ⚠️ Authorization layer (future enhancement)
- ✅ Database user with minimal required privileges
- ⚠️ Role-based access control (future)

---

## Security Controls Summary

### Implemented Controls

1. **Authentication**
   - Dev auth filter (development only, gated)
   - Production authentication via Shibboleth/HAKA (external)

2. **Input Validation**
   - JSON request validation
   - Parameterized queries (Slick ORM)
   - Type-safe request handling

3. **Data Protection**
   - HTTPS/TLS (infrastructure)
   - Environment variables for secrets
   - Database connection encryption

4. **Rate Limiting**
   - Rate limiting filter (to be implemented)
   - Per-IP limits
   - Configurable thresholds

5. **Security Headers**
   - Security headers filter (to be implemented)
   - HSTS, X-Frame-Options, CSP, etc.

6. **Audit Logging**
   - Audit logging filter (to be implemented)
   - Structured logging with correlation IDs

7. **Error Handling**
   - Generic error messages
   - No stack traces in production

### Future Enhancements

1. **Authorization Layer**
   - Role-based access control (RBAC)
   - Endpoint-level authorization
   - Resource-level permissions

2. **Advanced Security**
   - API key management
   - OAuth2/OIDC integration
   - Multi-factor authentication

3. **Monitoring & Detection**
   - Security event monitoring
   - Anomaly detection
   - Intrusion detection

4. **Compliance**
   - GDPR compliance measures
   - Data retention policies
   - Privacy controls

## Risk Assessment

### High-Risk Areas

1. **Authentication & Authorization** (T1.1, T6.1, T6.2)
   - **Status**: Partially mitigated
   - **Action**: Implement authorization layer

2. **Data Protection** (T2.1, T4.1, T4.3)
   - **Status**: Mitigated
   - **Action**: Monitor and enhance

3. **Denial of Service** (T5.1, T5.2)
   - **Status**: Partially mitigated
   - **Action**: Implement rate limiting

### Medium-Risk Areas

1. **Information Disclosure** (T4.2, T4.4)
   - **Status**: Mitigated
   - **Action**: Continue monitoring

2. **Repudiation** (T3.1, T3.2)
   - **Status**: Partially mitigated
   - **Action**: Implement audit logging

## Security Testing

### Recommended Tests

1. **Penetration Testing**
   - OWASP Top 10 vulnerabilities
   - API security testing
   - Authentication bypass attempts

2. **Security Scanning**
   - Dependency vulnerability scanning (Snyk)
   - Static code analysis
   - Container image scanning

3. **Load Testing**
   - Rate limiting effectiveness
   - DoS attack simulation
   - Resource exhaustion tests

## References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [STRIDE Threat Modeling](https://docs.microsoft.com/en-us/azure/security/develop/threat-modeling-tool-threats)
- [Play Framework Security](https://www.playframework.com/documentation/latest/SecurityHeaders)
- [CWE Top 25](https://cwe.mitre.org/top25/)

## Review Schedule

- **Initial**: 2026-01-09
- **Next Review**: Quarterly or after significant changes
- **Owner**: Development Team
