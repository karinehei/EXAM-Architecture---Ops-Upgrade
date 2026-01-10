# Architecture Documentation

This directory contains C4 model diagrams and architecture documentation for the EXAM system.

## C4 Model Diagrams

The C4 model provides a hierarchical view of the system architecture:

### Context Diagram
**Status**: Planned  
**File**: `context.md`

Shows the system in context with external actors (users, administrators) and external systems (Shibboleth/HAKA authentication).

### Container Diagram
**Status**: Planned  
**File**: `container.md`

Shows the high-level containers:
- **Frontend**: Angular 21 web application
- **Backend**: Play Framework (Scala) REST API
- **Database**: PostgreSQL 16
- **Reverse Proxy**: Apache/nginx (production)

### Component Diagram
**Status**: Planned  
**File**: `component.md`

Shows key components within the backend:
- Controllers (HomeController, HealthController, ReportController, MetricsController)
- Repositories (ReportRepository)
- Filters (CorrelationIdFilter, RateLimitFilter, SecurityHeadersFilter, AuditLogFilter, MetricsFilter, DevAuthFilter)
- Services and DTOs

## Architecture Overview

### Technology Stack

- **Frontend**: Angular 21 (zoneless change detection)
- **Backend**: Play Framework 2.8.19 (Scala 2.13.12)
- **Database**: PostgreSQL 16
- **Build**: Docker Compose for local development
- **CI/CD**: GitHub Actions

### Key Architectural Decisions

See [Architecture Decision Records](../adr/README.md) for detailed decisions on:
- Dev authentication strategy
- OpenAPI approach
- Observability implementation
- Performance optimizations
- Security controls

## System Design Principles

1. **Separation of Concerns**: Clear separation between frontend, backend, and database
2. **RESTful API**: RESTful design with OpenAPI specification
3. **Security by Default**: Rate limiting, security headers, audit logging enabled by default
4. **Observability**: Structured logging, health checks, and metrics
5. **Developer Experience**: One-command startup, Docker Compose, comprehensive documentation

## Related Documentation

- [API Documentation](../api/README.md)
- [Security Documentation](../security/README.md)
- [Observability Documentation](../observability/README.md)
- [Quick Start Guide](../QUICKSTART.md)
