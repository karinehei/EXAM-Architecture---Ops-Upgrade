# EXAM System - Architecture & Ops Upgrade

This is a fork of CSC's EXAM system with comprehensive architecture and operations improvements.

**License**: [MIT License](LICENSE) - Copyright (c) 2026 Karine Heinonen

[![CI](https://github.com/karinehei/EXAM-Architecture---Ops-Upgrade/actions/workflows/ci.yml/badge.svg)](https://github.com/karinehei/EXAM-Architecture---Ops-Upgrade/actions/workflows/ci.yml)

## Technology Stack

- **Backend**: Play Framework (Scala 2.13)
- **Frontend**: Angular 21
- **Database**: PostgreSQL 16
- **Reverse Proxy**: Apache (production), nginx (optional local)
- **Authentication**: Shibboleth/HAKA (production), dev mode for local development

## Quick Start

Get the system running locally in ~15 minutes:

```bash
# Clone and start
git clone <repository-url>
cd "EXAM Architecture & Ops Upgrade"
make dev
```

Then access:
- Frontend: http://localhost:4200
- Backend API: http://localhost:9000
- Health Check: http://localhost:9000/health

**📖 Full setup instructions**: [docs/QUICKSTART.md](docs/QUICKSTART.md)

## Project Structure

```
.
├── backend/              # Play Framework application
│   ├── app/              # Application code
│   ├── conf/             # Configuration files
│   └── Dockerfile        # Backend container
├── frontend/             # Angular application
│   ├── src/              # Source code
│   └── Dockerfile        # Frontend container
├── database/             # Database schema
├── docs/                  # Documentation
│   ├── api/              # API documentation
│   ├── architecture/     # Architecture diagrams
│   ├── adr/              # Architecture Decision Records
│   ├── security/         # Security documentation
│   └── accessibility/    # Accessibility notes
├── perf/                 # Performance tests
├── docker-compose.yml    # Local development environment
├── Makefile              # Convenience commands
└── .env.example          # Environment variables template
```

## Development

### Running Tests

```bash
# All tests
make test

# Backend only
cd backend && sbt test

# Frontend only
cd frontend && npm test
```

### Running Performance Tests

See [perf/README.md](perf/README.md) for load testing instructions.

### Development Guidelines

See [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) for:
- Development workflow
- Code style guidelines
- Testing practices
- Contribution guidelines

## API Documentation

- **OpenAPI Spec**: [docs/api/openapi.yaml](docs/api/openapi.yaml)
- **API Guide**: [docs/api/README.md](docs/api/README.md)

## Security Notes

### Security Features

The EXAM system includes multiple security controls:

- **Rate Limiting**: Protects API endpoints from abuse and DoS attacks
  - Default: 100 requests per 60 seconds per IP
  - Configurable via `RATE_LIMIT_*` environment variables
  - See [Rate Limiting Documentation](docs/security/rate-limiting.md)

- **Security Headers**: HTTP security headers for browser protection
  - HSTS, X-Frame-Options, CSP, and more
  - Enabled by default
  - See [Security Headers Documentation](docs/security/headers.md)

- **Audit Logging**: Comprehensive logging of all API access
  - Tracks user, IP, endpoint, timestamp, and more
  - Structured format for easy parsing
  - See [Audit Logging Documentation](docs/security/audit-logging.md)

### Security Configuration

**⚠️ Important Security Settings**:

- **Dev Authentication**: Never enable `DEV_AUTH_ENABLED=true` in production
- **Application Secret**: Always set a strong `APPLICATION_SECRET` in production
- **HTTPS**: Use HTTPS in production (required for HSTS)
- **Rate Limits**: Adjust rate limits based on your traffic patterns

### Security Documentation

- **Threat Model**: [docs/security/threat-model.md](docs/security/threat-model.md) - Comprehensive threat analysis
- **Security Controls**: [docs/security/README.md](docs/security/README.md) - Complete security documentation
- **Rate Limiting**: [docs/security/rate-limiting.md](docs/security/rate-limiting.md)
- **Security Headers**: [docs/security/headers.md](docs/security/headers.md)
- **Audit Logging**: [docs/security/audit-logging.md](docs/security/audit-logging.md)

## Accessibility

The EXAM system is designed to be accessible to all users, following **WCAG 2.1 Level AA** standards.

### Accessibility Features

- **Semantic HTML**: Proper use of HTML5 semantic elements
- **ARIA Labels**: Comprehensive ARIA labels and roles for screen readers
- **Keyboard Navigation**: All interactive elements are keyboard accessible
- **Focus Indicators**: Visible focus styles for keyboard users
- **Color Contrast**: Text meets WCAG AA contrast requirements (4.5:1)
- **Status Announcements**: Dynamic content updates are announced to screen readers

### Accessibility Documentation

- **A11y Notes**: [docs/accessibility/a11y-notes.md](docs/accessibility/a11y-notes.md) - Findings and fixes
- **A11y Guide**: [docs/accessibility/README.md](docs/accessibility/README.md) - Complete accessibility documentation

### Testing Accessibility

```bash
# Run automated accessibility scan
cd frontend
npm install
npm start  # In one terminal
npm run a11y  # In another terminal
```

Or use browser DevTools:
- Chrome: Lighthouse → Accessibility audit
- Firefox: Accessibility panel

## Architecture

- **C4 Diagrams**: [docs/architecture/README.md](docs/architecture/README.md)
- **ADRs**: [docs/adr/README.md](docs/adr/README.md)

## Available Commands

```bash
make help          # Show all available commands
make dev           # Start all services (development)
make up            # Start services (detached)
make down          # Stop services
make build         # Build Docker images
make test          # Run tests
make logs          # View logs
make clean         # Stop and remove volumes
```

See [docs/QUICKSTART.md](docs/QUICKSTART.md) for detailed command reference.

## Contributing

1. Read [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)
2. Create a feature branch
3. Make your changes with tests
4. Ensure CI passes
5. Submit a pull request

## License

 [MIT License](LICENSE) - Copyright (c) 2026 Karine Heinonen
