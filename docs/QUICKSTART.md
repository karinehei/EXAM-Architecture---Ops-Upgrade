# Quick Start Guide

This guide will help you get the EXAM system running locally in about 15 minutes.

## Prerequisites

- **Docker** and **Docker Compose** installed and running
  - Docker Desktop (Windows/Mac) or Docker Engine (Linux)
  - Minimum version: Docker 20.10+, Docker Compose 2.0+
- **Make** (optional, for convenience commands)
  - Windows: Install via Chocolatey or use Git Bash
  - Mac/Linux: Usually pre-installed

## Quick Start (One Command)

1. **Clone the repository** (if you haven't already):
   ```bash
   git clone <repository-url>
   cd "EXAM Architecture & Ops Upgrade"
   ```

2. **Start the system**:
   ```bash
   make dev
   ```
   
   Or without Make:
   ```bash
   docker compose up --build
   ```

3. **Wait for services to start** (about 2-3 minutes on first run):
   - PostgreSQL database will initialize
   - Backend will build and start
   - Frontend will install dependencies and start dev server

4. **Access the application**:
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:9000
   - Health check: http://localhost:9000/health

## Environment Configuration

### First Time Setup

1. **Copy the example environment file**:
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` if needed** (defaults work for local dev):
   ```bash
   # Key settings:
   DEV_AUTH_ENABLED=true    # Enable dev auth bypass (local only!)
   DEV_AUTH_USER=your-name  # Your dev user name
   ```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_NAME` | `exam` | PostgreSQL database name |
| `DB_USERNAME` | `exam_user` | PostgreSQL username |
| `DB_PASSWORD` | `exam_password` | PostgreSQL password |
| `DB_PORT` | `5432` | PostgreSQL port (host mapping) |
| `BACKEND_PORT` | `9000` | Backend API port |
| `FRONTEND_PORT` | `4200` | Frontend dev server port |
| `APPLICATION_SECRET` | `changeme...` | Play Framework secret key |
| `DEV_AUTH_ENABLED` | `false` | Enable dev auth bypass |
| `DEV_AUTH_USER` | `dev-user` | Dev user identity |

**⚠️ Security Warning**: Never set `DEV_AUTH_ENABLED=true` in production!

## Development Workflow

### Starting Services

```bash
# Start all services (development mode)
make dev

# Start in background (detached)
make up

# Or use docker compose directly
docker compose up -d
```

### Stopping Services

```bash
# Stop services
make down

# Stop and remove volumes (clean slate)
make clean
```

### Viewing Logs

```bash
# All services
make logs

# Specific service
make logs-backend
make logs-frontend
make logs-db
```

### Running Tests

```bash
# Run all tests
make test

# Backend only
cd backend && sbt test

# Frontend only
cd frontend && npm test
```

### Database Access

```bash
# Open PostgreSQL shell
make shell-db

# Or connect directly
docker compose exec postgres psql -U exam_user -d exam
```

### Backend Shell

```bash
# Open shell in backend container
make shell-backend
```

### Frontend Shell

```bash
# Open shell in frontend container
make shell-frontend
```

## Service URLs

Once running, access:

- **Frontend (Angular Dev Server)**: http://localhost:4200
- **Backend API**: http://localhost:9000
- **Health Check**: http://localhost:9000/health
- **API Endpoints**: http://localhost:9000/api/v1/reports

## Troubleshooting

### Port Already in Use

If you get port conflicts:

1. Edit `.env` and change the port numbers
2. Or stop the conflicting service:
   ```bash
   # Find what's using the port (example for 9000)
   # Windows:
   netstat -ano | findstr :9000
   # Linux/Mac:
   lsof -i :9000
   ```

### Database Connection Issues

1. Check PostgreSQL is healthy:
   ```bash
   docker compose ps
   ```

2. Check database logs:
   ```bash
   make logs-db
   ```

3. Verify environment variables in `.env`

### Backend Won't Start

1. Check backend logs:
   ```bash
   make logs-backend
   ```

2. Verify database is ready (backend waits for DB health check)

3. Check application configuration:
   ```bash
   docker compose exec backend cat conf/application.conf
   ```

### Frontend Build Issues

1. Clear node_modules and reinstall:
   ```bash
   docker compose exec frontend rm -rf node_modules
   docker compose restart frontend
   ```

2. Check frontend logs:
   ```bash
   make logs-frontend
   ```

### Clean Start

If things get into a bad state:

```bash
# Stop everything and remove volumes
make clean

# Start fresh
make dev
```

## Development Authentication

For local development, you can bypass Shibboleth/HAKA authentication:

1. Set in `.env`:
   ```
   DEV_AUTH_ENABLED=true
   DEV_AUTH_USER=your-name
   ```

2. Restart backend:
   ```bash
   docker compose restart backend
   ```

3. The filter will inject mock user headers:
   - `X-User-Id`: Your dev user name
   - `X-User-Email`: `{user}@dev.local`
   - `X-User-Name`: `Development User ({user})`
   - `X-Auth-Source`: `dev-bypass`

**⚠️ Important**: This is ONLY for local development. The filter will log warnings if enabled in production mode.

## Next Steps

- Read [Development Guide](DEVELOPMENT.md) for coding guidelines
- Check [API Documentation](../docs/api/README.md) for API usage
- Review [Architecture Documentation](../docs/architecture/README.md) for system design

## Getting Help

- Check logs: `make logs`
- Review documentation in `docs/` directory
- Check service status: `make status`
