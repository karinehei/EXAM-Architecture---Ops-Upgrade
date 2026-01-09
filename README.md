# EXAM System - Architecture & Ops Upgrade

This is a fork of CSC's EXAM system with comprehensive architecture and operations improvements.

[![CI](https://github.com/karinehei/EXAM-Architecture---Ops-Upgrade/actions/workflows/ci.yml/badge.svg)](https://github.com/karinehei/EXAM-Architecture---Ops-Upgrade/actions/workflows/ci.yml)

## Technology Stack

- **Backend**: Play Framework (Scala 2.13)
- **Frontend**: Angular 17
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

- **Dev Authentication**: Never enable `DEV_AUTH_ENABLED=true` in production
- **Threat Model**: [docs/security/threat-model.md](docs/security/threat-model.md)
- **Security Controls**: [docs/security/README.md](docs/security/README.md)

## Accessibility

- **A11y Notes**: [docs/accessibility/a11y-notes.md](docs/accessibility/a11y-notes.md)
- **A11y Testing**: [docs/accessibility/README.md](docs/accessibility/README.md)

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

[Add your license information here]
