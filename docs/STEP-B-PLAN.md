# Step B: Implementation Plan & Checklist

## Overview

This document outlines the concrete implementation plan for the Architecture & Ops Upgrade, mapped to each deliverable. All changes will be implemented in small, reviewable commits.

## Deliverable 1: Developer Experience (DX) & Local Environment

### Checklist
- [ ] Create `docker-compose.yml` with services:
  - [ ] PostgreSQL database
  - [ ] Backend service (Play Framework)
  - [ ] Frontend service (Angular dev server or nginx with built assets)
  - [ ] Optional reverse proxy (nginx)
- [ ] Create `Dockerfile` for backend
- [ ] Create `Dockerfile` for frontend
- [ ] Create `.env.example` with all required variables
- [ ] Create `Makefile` with `make dev` command (or document `docker compose up`)
- [ ] Implement dev auth bypass/mock in backend:
  - [ ] Create `DevAuthAction` that checks `DEV_AUTH_ENABLED` env var
  - [ ] Gate it clearly (never enabled by default in production)
  - [ ] Document in README
- [ ] Create `docs/QUICKSTART.md` with setup instructions
- [ ] Test: Verify `docker compose up` starts all services successfully

### Files to Create/Modify
- `docker-compose.yml`
- `backend/Dockerfile`
- `frontend/Dockerfile`
- `.env.example`
- `Makefile`
- `backend/app/filters/DevAuthFilter.scala` (or similar)
- `docs/QUICKSTART.md`
- Update `README.md` with quick start link

---

## Deliverable 2: CI Pipeline (GitHub Actions)

### Checklist
- [ ] Create `.github/workflows/ci.yml` with stages:
  - [ ] Backend: compile, test
  - [ ] Frontend: install, build, lint, test
  - [ ] Security: dependency vulnerability scan (e.g., Snyk, Dependabot)
  - [ ] Container build (optional, for validation)
  - [ ] Artifact caching (sbt cache, npm cache)
- [ ] Configure to run on:
  - [ ] Pull requests
  - [ ] Push to main branch
- [ ] Add CI status badge to README.md
- [ ] Test: Create a test PR to verify CI runs

### Files to Create/Modify
- `.github/workflows/ci.yml`
- Update `README.md` with CI badge

---

## Deliverable 3: OpenAPI + REST Improvements

### Checklist
- [ ] Create OpenAPI 3.0 spec for `/api/v1/reports` endpoints:
  - [ ] Define all endpoints (GET list, GET by id, POST, PUT, DELETE)
  - [ ] Add consistent pagination schema
  - [ ] Add filtering parameters
  - [ ] Add error response schemas (400, 404, 500)
  - [ ] Add request/response examples
  - [ ] Document idempotency (PUT/DELETE)
- [ ] Place spec at `docs/api/openapi.yaml`
- [ ] Implement actual database operations in ReportController:
  - [ ] Complete list with pagination
  - [ ] Complete get by id
  - [ ] Complete create
  - [ ] Complete update
  - [ ] Complete delete
- [ ] Add contract/integration tests:
  - [ ] Test that endpoints match OpenAPI spec
  - [ ] Test pagination behavior
  - [ ] Test error responses
- [ ] Add Swagger UI endpoint (optional but helpful)
- [ ] Document how to use the API in `docs/api/README.md`

### Files to Create/Modify
- `docs/api/openapi.yaml`
- `backend/app/controllers/ReportController.scala` (complete TODOs)
- `backend/app/models/Report.scala` (if needed)
- `backend/app/repositories/ReportRepository.scala` (if needed)
- `backend/test/controllers/ReportControllerSpec.scala`
- `docs/api/README.md`

---

## Deliverable 4: Observability

### Checklist
- [ ] Structured logging (JSON):
  - [ ] Configure Play logger to output JSON format
  - [ ] Add correlation ID middleware (generate/read X-Request-ID header)
  - [ ] Include correlation ID in all log entries
- [ ] Health endpoint improvements:
  - [ ] Separate `/health/liveness` and `/health/readiness`
  - [ ] Document semantics in `docs/observability/health.md`
- [ ] Metrics endpoint:
  - [ ] Add Prometheus-style metrics endpoint at `/metrics`
  - [ ] Track: request count, latency (p50, p95, p99), error rate
  - [ ] Use lightweight library (e.g., Micrometer or simple implementation)
- [ ] Add timing middleware for key endpoints
- [ ] Document observability setup in `docs/observability/README.md`

### Files to Create/Modify
- `backend/app/filters/CorrelationIdFilter.scala`
- `backend/app/controllers/HealthController.scala` (add liveness/readiness)
- `backend/app/controllers/MetricsController.scala` (new)
- `backend/app/filters/MetricsFilter.scala` (new)
- `backend/conf/logback.xml` (configure JSON logging)
- `docs/observability/README.md`
- `docs/observability/health.md`

---

## Deliverable 5: Performance Testing + Improvements

### Checklist
- [ ] Create load test setup:
  - [ ] Choose tool: k6 (recommended) or Gatling
  - [ ] Create test script under `perf/load-test.js` (or `.scala` for Gatling)
  - [ ] Test scenario: Report list endpoint with pagination
  - [ ] Define load profile (ramp-up, steady state, ramp-down)
- [ ] Baseline measurements:
  - [ ] Run load test against current implementation
  - [ ] Document: p50, p95, p99 latency, throughput (req/s)
  - [ ] Save results in `perf/baseline-results.md`
- [ ] Identify optimization opportunity:
  - [ ] Example: Add database index, optimize query, add caching
  - [ ] Implement the optimization
- [ ] After measurements:
  - [ ] Run load test again
  - [ ] Document improvement in `perf/improvement-results.md`
  - [ ] Show before/after comparison
- [ ] Document how to run load tests in `perf/README.md`

### Files to Create/Modify
- `perf/load-test.js` (or Gatling equivalent)
- `perf/README.md`
- `perf/baseline-results.md`
- `perf/improvement-results.md`
- Database schema/index changes
- Backend optimization code

---

## Deliverable 6: Security Hardening

### Checklist
- [ ] Threat model document:
  - [ ] Create `docs/security/threat-model.md`
  - [ ] Use STRIDE framework
  - [ ] Document threats and mitigations
- [ ] Implement security controls:
  - [ ] Rate limiting for `/api/v1/*` endpoints:
    - [ ] Use Play filter or library (e.g., bucket4j, guava rate limiter)
    - [ ] Configurable via environment variables
    - [ ] Document in `docs/security/rate-limiting.md`
  - [ ] Security headers:
    - [ ] Add HSTS, X-Frame-Options, X-Content-Type-Options, CSP
    - [ ] Configure in Play filters
    - [ ] Document in `docs/security/headers.md`
  - [ ] Audit logging:
    - [ ] Log all API access (endpoint, user, timestamp, IP)
    - [ ] Structured format for easy parsing
    - [ ] Document in `docs/security/audit-logging.md`
- [ ] Document security configuration in `docs/security/README.md`
- [ ] Add security notes to main README

### Files to Create/Modify
- `docs/security/threat-model.md`
- `docs/security/rate-limiting.md`
- `docs/security/headers.md`
- `docs/security/audit-logging.md`
- `docs/security/README.md`
- `backend/app/filters/RateLimitFilter.scala`
- `backend/app/filters/SecurityHeadersFilter.scala`
- `backend/app/filters/AuditLogFilter.scala`

---

## Deliverable 7: Accessibility (A11y)

### Checklist
- [ ] Add automated a11y testing:
  - [ ] Choose tool: axe-core (via @axe-core/cli or ng-axe)
  - [ ] Add to frontend test suite or CI
  - [ ] Test critical views: report list, report detail (if exists)
- [ ] Run initial scan and document findings:
  - [ ] Create `docs/accessibility/a11y-notes.md`
  - [ ] List issues found
  - [ ] Prioritize fixes
- [ ] Fix identified issues:
  - [ ] Ensure proper ARIA labels
  - [ ] Ensure keyboard navigation
  - [ ] Ensure color contrast
  - [ ] Ensure semantic HTML
- [ ] Verify fixes don't break existing UI
- [ ] Document a11y testing process in `docs/accessibility/README.md`

### Files to Create/Modify
- `frontend/package.json` (add a11y testing dependencies)
- `frontend/e2e/a11y.spec.ts` (or similar)
- `docs/accessibility/a11y-notes.md`
- `docs/accessibility/README.md`
- Frontend component fixes

---

## Architecture Documentation

### Checklist
- [ ] C4 Model diagrams (Mermaid format):
  - [ ] Context diagram: System and external actors
  - [ ] Container diagram: Backend, Frontend, Database, Reverse Proxy
  - [ ] Component diagram: Key components of backend (controllers, services, repositories)
  - [ ] Place in `docs/architecture/`
- [ ] ADRs (Architecture Decision Records):
  - [ ] ADR-001: Dev Auth Strategy
  - [ ] ADR-002: OpenAPI Approach
  - [ ] ADR-003: Observability Approach
  - [ ] ADR-004: Performance Optimization Choice
  - [ ] ADR-005: Security Control Choice
  - [ ] Place in `docs/adr/`
- [ ] Create architecture index: `docs/architecture/README.md`

### Files to Create/Modify
- `docs/architecture/context.md` (Mermaid diagram)
- `docs/architecture/container.md` (Mermaid diagram)
- `docs/architecture/component.md` (Mermaid diagram)
- `docs/architecture/README.md`
- `docs/adr/0001-dev-auth-strategy.md`
- `docs/adr/0002-openapi-approach.md`
- `docs/adr/0003-observability-approach.md`
- `docs/adr/0004-performance-optimization.md`
- `docs/adr/0005-security-controls.md`
- `docs/adr/README.md`

---

## Final Documentation Updates

### Checklist
- [ ] Update main `README.md` with:
  - [ ] Quickstart section (link to detailed docs)
  - [ ] Run tests section
  - [ ] Run perf tests section
  - [ ] Security notes section
  - [ ] A11y notes section
  - [ ] Links to all documentation
- [ ] Create `docs/DEVELOPMENT.md` with:
  - [ ] Development workflow
  - [ ] Testing guidelines
  - [ ] Code style
  - [ ] Contribution guidelines

### Files to Create/Modify
- `README.md` (comprehensive update)
- `docs/DEVELOPMENT.md`

---

## Implementation Order (Recommended)

1. **DX & Local Environment** (Deliverable 1) - Foundation for everything else
2. **CI Pipeline** (Deliverable 2) - Ensures quality gates
3. **OpenAPI + REST** (Deliverable 3) - Complete the API implementation
4. **Observability** (Deliverable 4) - Needed for performance testing
5. **Performance** (Deliverable 5) - Can measure improvements
6. **Security** (Deliverable 6) - Important but can be done in parallel
7. **Accessibility** (Deliverable 7) - Can be done in parallel
8. **Architecture Docs** - Throughout, but finalize at the end

---

## Success Criteria

- ✅ New contributor can run system locally in ~15 minutes
- ✅ CI runs green on PR with backend+frontend tests
- ✅ OpenAPI spec validates and matches implementation
- ✅ Load tests show measurable improvement (documented)
- ✅ Security controls enabled by config and documented
- ✅ A11y checks run in CI or documented as local step
- ✅ Documentation is complete and navigable

---

## Next Steps

Proceed with implementation starting with Deliverable 1 (DX & Local Environment), making small, reviewable commits after each logical unit of work.
