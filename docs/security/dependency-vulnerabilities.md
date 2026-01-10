# Dependency Vulnerability Management

**Last Updated**: 2026-01-09  
**Status**: Documented, Risk Accepted (Short Term)

## Overview

This document tracks known vulnerabilities in project dependencies and the risk management strategy.

## Current Status

### Frontend Dependencies

**Total Vulnerabilities**: 21
- **High**: 11 (Angular XSS vulnerabilities)
- **Moderate**: 6 (Development dependencies only)
- **Low**: 4

### Backend Dependencies

**Status**: No known high-severity vulnerabilities  
**Monitoring**: Snyk scans in CI pipeline

## Risk Assessment

### High Severity: Angular XSS Vulnerabilities

**Vulnerabilities**:
1. XSS via Unsanitized SVG Script Attributes (GHSA-jrmj-c5cx-3cw6)
2. XSRF Token Leakage via Protocol-Relative URLs (GHSA-58c5-g7wp-6w37)
3. Stored XSS via SVG Animation/MathML (GHSA-v4hv-rgfq-gp49)

**Risk Level**: **MEDIUM-HIGH**

**Rationale**:
- Vulnerabilities require specific attack scenarios (malicious user content)
- Existing mitigations in place:
  - ✅ Content Security Policy (CSP) headers
  - ✅ Input sanitization in backend controllers
  - ✅ Angular's built-in DomSanitizer
  - ✅ Security headers filter
- Angular 17 is still in LTS support
- Upgrade to Angular 21 requires major migration effort

**Mitigation Strategy**:
1. **Input Validation**: All user input is validated and sanitized
2. **CSP Headers**: Restrict script execution sources
3. **HTTPS Only**: Production uses HTTPS (prevents protocol-relative URL issues)
4. **Content Sanitization**: Angular's DomSanitizer for user-generated content

**Action Plan**:
- **Short Term**: Accept risk, maintain existing mitigations
- **Medium Term**: Plan Angular 21 upgrade (2-3 month migration)

### Moderate Severity: Development Dependencies

**Vulnerabilities**:
- esbuild (dev server)
- http-proxy-middleware (dev proxy)
- tmp (dev tooling)
- webpack-dev-server (dev server)

**Risk Level**: **LOW**

**Rationale**:
- All vulnerabilities are in **development dependencies only**
- Not included in production builds
- Require local development environment access
- Specific attack scenarios (malicious websites, local network)

**Action Plan**:
- **Accept risk** - Development environment only
- **Upgrade when Angular is upgraded** (will resolve as transitive dependencies)

## Security Controls in Place

### Backend Security
- ✅ Rate limiting (prevents DoS)
- ✅ Security headers (CSP, HSTS, X-Frame-Options, etc.)
- ✅ Input validation and sanitization
- ✅ Audit logging
- ✅ CORS configuration

### Frontend Security
- ✅ Angular's built-in XSS protection
- ✅ Content Security Policy (via backend headers)
- ✅ HTTPS-only in production
- ✅ Input validation

## Upgrade Strategy

### Angular 21 Migration (In Progress)

**Timeline**: Started 2026-01-09

**Prerequisites**:
1. Full regression test suite
2. Code review for breaking changes
3. Dependency compatibility check
4. Migration guide review
5. Team training on new features (Signals, zoneless change detection)

**Major Breaking Changes in Angular 21**:

1. **Zoneless Change Detection**
   - Angular 21 moves away from Zone.js
   - Change detection is now zoneless by default
   - **Impact**: May require code changes for components relying on Zone.js behavior
   - **Action**: Review and update change detection strategies

2. **Vitest as Default Testing Framework**
   - Replaces Karma/Jasmine as default test runner
   - **Impact**: Existing tests using Karma/Jasmine will need migration
   - **Action**: Migrate test suite to Vitest or continue using Karma (supported but deprecated)
   - **Note**: Current project uses Karma/Jasmine - migration required

3. **Signal Forms**
   - New reactive forms API built on Signals
   - **Impact**: Optional but recommended for new forms
   - **Action**: Existing forms continue to work, but consider migrating for better performance

4. **Dependency Updates**
   - TypeScript version requirements
   - Node.js version requirements
   - Build tool updates

**Migration Steps**:

1. **Preparation**:
   ```bash
   # Review current Angular version
   ng version
   
   # Review migration guide
   # https://angular.io/guide/update-to-version-21
   ```

2. **Update Dependencies**:
   ```bash
   # Update package.json with Angular 21 versions
   # @angular/core@^21.0.0
   # @angular/common@^21.0.0
   # etc.
   
   npm install
   ```

3. **Run Migration Schematics**:
   ```bash
   ng update @angular/core@21 @angular/cli@21
   ```

4. **Code Updates**:
   - Review and fix breaking changes
   - Update change detection if needed
   - Migrate tests to Vitest (or keep Karma)
   - Update any Zone.js-dependent code

5. **Testing**:
   - Full regression test suite
   - Performance testing
   - Browser compatibility testing

6. **CI/CD Updates**:
   - Update test commands if using Vitest
   - Update build configurations
   - Update deployment pipelines

**Estimated Effort**: 2-3 months
- 2-3 weeks: Planning and preparation
- 4-6 weeks: Code migration and testing
- 2-3 weeks: QA and deployment

**Risk Mitigation**:
- Create feature branch for migration
- Maintain current Angular 17 version in production
- Gradual rollout with feature flags
- Comprehensive testing before production deployment

## Monitoring

### Automated Scanning
- ✅ **CI Pipeline**: Snyk scans on every PR
- ✅ **npm audit**: Run before releases
- ✅ **Dependency updates**: Monthly review

### Manual Review
- **Monthly**: Review security advisories
- **Before releases**: Run `npm audit` and review findings
- **Quarterly**: Review upgrade opportunities

## Risk Acceptance

**Decision**: Accept current vulnerabilities with documented mitigations

**Rationale**:
1. High-severity vulnerabilities require specific attack scenarios
2. Existing security controls provide mitigation
3. Dev dependency vulnerabilities don't affect production
4. Angular 21 upgrade requires significant effort (2-3 months)
5. Angular 17 is still in LTS support

**Review Date**: 2026-04-09 (Quarterly review)

## References

- [Frontend Security Audit](../../frontend/SECURITY-AUDIT.md)
- [Angular Security Advisories](https://github.com/angular/angular/security/advisories)
- [npm Security Advisories](https://github.com/advisories)
- [Snyk Vulnerability Database](https://snyk.io/vuln)
