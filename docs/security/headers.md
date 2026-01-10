# Security Headers

## Overview

Security headers protect the EXAM application from common web vulnerabilities by instructing browsers to enforce security policies.

## Implementation

The security headers filter (`SecurityHeadersFilter`) adds the following headers to all HTTP responses:

### Headers Added

| Header | Purpose | Value |
|--------|---------|-------|
| **Strict-Transport-Security (HSTS)** | Forces HTTPS connections | `max-age=31536000; includeSubDomains; preload` |
| **X-Frame-Options** | Prevents clickjacking | `DENY` |
| **X-Content-Type-Options** | Prevents MIME type sniffing | `nosniff` |
| **Content-Security-Policy (CSP)** | Restricts resource loading | See CSP Policy below |
| **X-XSS-Protection** | Enables XSS filter | `1; mode=block` |
| **Referrer-Policy** | Controls referrer information | `strict-origin-when-cross-origin` |
| **Permissions-Policy** | Restricts browser features | `geolocation=(), microphone=(), camera=()` |

## Header Details

### Strict-Transport-Security (HSTS)

**Purpose**: Forces browsers to use HTTPS for all future connections to the domain.

**Behavior**:
- Only added when the request is HTTPS (or `X-Forwarded-Proto: https` is present)
- Not added for HTTP requests (to allow HTTP access during development)
- `max-age`: How long browsers should remember to use HTTPS (default: 1 year)
- `includeSubDomains`: Applies to all subdomains
- `preload`: Eligible for browser HSTS preload lists

**Configuration**:
```bash
export SECURITY_HEADERS_HSTS_MAX_AGE=31536000  # 1 year in seconds
```

### X-Frame-Options

**Purpose**: Prevents the page from being embedded in frames (clickjacking protection).

**Value**: `DENY` - Page cannot be displayed in any frame.

**Alternatives** (if needed):
- `SAMEORIGIN`: Allow framing from same origin
- `ALLOW-FROM uri`: Allow framing from specific URI (deprecated)

### X-Content-Type-Options

**Purpose**: Prevents browsers from MIME-sniffing content types.

**Value**: `nosniff` - Browsers must respect the declared Content-Type.

**Protection**: Prevents attacks where malicious files are served with incorrect MIME types.

### Content-Security-Policy (CSP)

**Purpose**: Restricts which resources (scripts, styles, images, etc.) can be loaded.

**Default Policy**:
```
default-src 'self';
script-src 'self' 'unsafe-inline' 'unsafe-eval' https://unpkg.com;
style-src 'self' 'unsafe-inline' https://unpkg.com;
img-src 'self' data: https:;
font-src 'self' data:;
connect-src 'self' https:;
frame-ancestors 'none';
base-uri 'self';
form-action 'self'
```

**Directives**:
- `default-src 'self'`: Only allow resources from same origin
- `script-src 'self' 'unsafe-inline' 'unsafe-eval' https://unpkg.com`: Allow scripts from same origin, inline scripts, eval (required for Angular), and unpkg.com (for Swagger UI)
- `style-src 'self' 'unsafe-inline' https://unpkg.com`: Allow styles from same origin, inline styles, and unpkg.com (for Swagger UI)
- `img-src 'self' data: https:`: Allow images from same origin, data URIs, and HTTPS
- `font-src 'self' data:`: Allow fonts from same origin and data URIs
- `connect-src 'self' https:`: Allow API calls to same origin and HTTPS
- `frame-ancestors 'none'`: Prevent framing (redundant with X-Frame-Options)
- `base-uri 'self'`: Restrict base tag URLs
- `form-action 'self'`: Restrict form submission URLs

**Custom CSP**:
```bash
export SECURITY_HEADERS_CSP="default-src 'self'; script-src 'self'"
```

### X-XSS-Protection

**Purpose**: Enables browser's built-in XSS filter (legacy, but still useful).

**Value**: `1; mode=block` - Enable filter and block pages if XSS detected.

**Note**: Modern browsers have XSS protection built-in, but this header provides explicit control.

### Referrer-Policy

**Purpose**: Controls how much referrer information is sent with requests.

**Value**: `strict-origin-when-cross-origin`
- Same-origin requests: Send full URL
- Cross-origin HTTPS: Send only origin
- Cross-origin HTTP: Send nothing

**Alternatives**:
- `no-referrer`: Never send referrer
- `same-origin`: Only send for same-origin requests
- `origin`: Always send only origin (not full URL)

### Permissions-Policy

**Purpose**: Restricts browser features (geolocation, camera, microphone, etc.).

**Default**: `geolocation=(), microphone=(), camera=()`
- Disables geolocation, microphone, and camera access

**Customization**: Add more features as needed:
```
Permissions-Policy: geolocation=(), microphone=(), camera=(), payment=()
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SECURITY_HEADERS_ENABLED` | `true` | Enable/disable security headers |
| `SECURITY_HEADERS_HSTS_MAX_AGE` | `31536000` | HSTS max age in seconds (1 year) |
| `SECURITY_HEADERS_CSP` | (default policy) | Custom CSP policy |

### Configuration File

You can also configure in `backend/conf/application.conf`:

```hocon
exam.security.headers.enabled = true
exam.security.headers.hsts.maxAge = 31536000
exam.security.headers.csp = "default-src 'self'; script-src 'self'"
```

## Testing

### Verify Headers

Check that security headers are present:

```bash
curl -I http://localhost:9000/api/v1/reports
```

Expected headers:
```
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Content-Security-Policy: default-src 'self'; ...
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

### HSTS Testing

HSTS is only added for HTTPS requests:

```bash
# HTTP - no HSTS header
curl -I http://localhost:9000/api/v1/reports

# HTTPS - HSTS header present
curl -I https://localhost:9000/api/v1/reports
```

### CSP Testing

Test CSP violations in browser console:
1. Open browser DevTools
2. Check Console for CSP violation reports
3. Adjust CSP policy if needed

## Browser Compatibility

All security headers are supported by modern browsers:

- **HSTS**: Chrome 4+, Firefox 4+, Safari 7+, Edge 12+
- **X-Frame-Options**: All modern browsers
- **X-Content-Type-Options**: All modern browsers
- **CSP**: Chrome 25+, Firefox 23+, Safari 7+, Edge 12+
- **X-XSS-Protection**: IE 8+, Chrome, Safari (legacy)
- **Referrer-Policy**: Chrome 56+, Firefox 50+, Safari 11.1+, Edge 79+
- **Permissions-Policy**: Chrome 88+, Firefox 90+, Safari 15.4+, Edge 88+

## Production Recommendations

1. **HSTS**: Use long max-age (1 year+) in production
2. **CSP**: Tighten CSP policy by removing `unsafe-inline` and `unsafe-eval` if possible
3. **Monitoring**: Monitor CSP violation reports
4. **Testing**: Test headers with security scanners (OWASP ZAP, Burp Suite)

## Disabling Security Headers

To disable security headers (not recommended):

```bash
export SECURITY_HEADERS_ENABLED=false
```

## References

- [OWASP: Secure Headers](https://owasp.org/www-project-secure-headers/)
- [MDN: Content Security Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP)
- [MDN: Strict-Transport-Security](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Strict-Transport-Security)
- [Security Headers Scanner](https://securityheaders.com/)
