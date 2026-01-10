.PHONY: help dev up down build clean test logs

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'

dev: ## Start all services in development mode
	@echo "Starting EXAM system in development mode..."
	@if [ ! -f .env ]; then \
		echo "Creating .env from .env.example..."; \
		cp .env.example .env; \
	fi
	docker compose up --build

up: ## Start all services (detached)
	docker compose up -d

down: ## Stop all services
	docker compose down

build: ## Build all Docker images
	docker compose build

clean: ## Stop services and remove volumes
	docker compose down -v
	docker system prune -f

test: ## Run tests (backend and frontend)
	@echo "Running backend tests..."
	cd backend && sbt test
	@echo "Running frontend tests..."
	cd frontend && npm test -- --watch=false

logs: ## Show logs from all services
	docker compose logs -f

logs-backend: ## Show backend logs
	docker compose logs -f backend

logs-frontend: ## Show frontend logs
	docker compose logs -f frontend

logs-db: ## Show database logs
	docker compose logs -f postgres

shell-backend: ## Open shell in backend container
	docker compose exec backend /bin/bash

shell-frontend: ## Open shell in frontend container
	docker compose exec frontend /bin/sh

shell-db: ## Open PostgreSQL shell
	docker compose exec postgres psql -U exam_user -d exam

restart: ## Restart all services
	docker compose restart

status: ## Show status of all services
	docker compose ps

clean-pid: ## Remove stale RUNNING_PID file from backend container
	@echo "Removing stale RUNNING_PID file..."
	docker compose exec backend rm -f /app/exam-backend/RUNNING_PID || echo "Container not running or file doesn't exist"
