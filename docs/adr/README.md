# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records documenting key technical decisions made during the EXAM system upgrade.

## What are ADRs?

Architecture Decision Records are documents that capture important architectural decisions made in a project, along with their context and consequences. They help future developers understand why certain choices were made.

## ADR Format

Each ADR follows this structure:
- **Status**: Proposed | Accepted | Deprecated | Superseded
- **Context**: The issue motivating this decision
- **Decision**: The change we're proposing or have made
- **Consequences**: What becomes easier or more difficult

## Current ADRs

### ADR-001: Dev Authentication Strategy
**Status**: Accepted  
**File**: `0001-dev-auth-strategy.md` (Planned)

**Decision**: Implement a development-only authentication bypass filter that injects mock user headers, gated by environment configuration.

**Rationale**: 
- Enables local development without Shibboleth/HAKA infrastructure
- Clearly marked as development-only with production mode checks
- Simplifies developer onboarding

### ADR-002: OpenAPI Specification Approach
**Status**: Accepted  
**File**: `0002-openapi-approach.md` (Planned)

**Decision**: Use OpenAPI 3.0 YAML specification for API documentation, with consistent pagination, filtering, and error response schemas.

**Rationale**:
- Industry standard for API documentation
- Enables code generation and contract testing
- Improves API discoverability and integration

### ADR-003: Observability Approach
**Status**: Accepted  
**File**: `0003-observability-approach.md` (Planned)

**Decision**: Implement structured logging with correlation IDs, separate liveness/readiness health checks, and Prometheus-style metrics endpoint.

**Rationale**:
- Structured logging enables better log analysis
- Separate health checks support Kubernetes deployments
- Prometheus metrics enable monitoring and alerting

### ADR-004: Performance Optimization Strategy
**Status**: Accepted  
**File**: `0004-performance-optimization.md` (Planned)

**Decision**: Use PostgreSQL GIN indexes for full-text search and parallel query execution for pagination queries.

**Rationale**:
- GIN indexes significantly improve LIKE query performance
- Parallel queries reduce latency for paginated endpoints
- Measurable performance improvements demonstrated

### ADR-005: Security Controls Implementation
**Status**: Accepted  
**File**: `0005-security-controls.md` (Planned)

**Decision**: Implement rate limiting (token bucket), security headers (CSP, HSTS, etc.), and structured audit logging.

**Rationale**:
- Rate limiting protects against DoS attacks
- Security headers mitigate common web vulnerabilities
- Audit logging supports compliance and security monitoring

### ADR-006: Angular 21 Upgrade
**Status**: Accepted  
**File**: `0006-angular-21-upgrade.md` (Planned)

**Decision**: Upgrade from Angular 17 to Angular 21 to resolve security vulnerabilities, accepting breaking changes (zoneless change detection, Vitest default).

**Rationale**:
- Resolves 11 high-severity XSS vulnerabilities
- Angular 21 is the latest LTS version
- Breaking changes are manageable with existing codebase

## ADR Index

| Number | Title | Status | Date |
|--------|-------|--------|------|
| 0001 | Dev Authentication Strategy | Accepted | 2026-01-09 |
| 0002 | OpenAPI Specification Approach | Accepted | 2026-01-09 |
| 0003 | Observability Approach | Accepted | 2026-01-09 |
| 0004 | Performance Optimization Strategy | Accepted | 2026-01-09 |
| 0005 | Security Controls Implementation | Accepted | 2026-01-09 |
| 0006 | Angular 21 Upgrade | Accepted | 2026-01-10 |

## References

- [ADR Template](https://adr.github.io/)
- [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
