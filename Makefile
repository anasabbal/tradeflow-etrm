.DEFAULT_GOAL := help
.PHONY: help build test verify up infra down logs ps seed config clean

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-10s %s\n", $$1, $$2}'

build: ## Compile and package all modules (skip tests)
	mvn -B clean package -DskipTests

test: ## Run unit tests
	mvn -B test

verify: ## Full build with tests, coverage and static analysis
	mvn -B clean verify -Pquality

up: ## Build images and start the full stack
	docker compose up --build -d

infra: ## Start infrastructure only (Postgres, Redpanda, Redis, Prometheus, Grafana)
	docker compose up -d postgres redpanda redis prometheus grafana

down: ## Stop the stack (keep volumes)
	docker compose down

logs: ## Tail logs of all containers
	docker compose logs -f --tail=100

ps: ## Show container status
	docker compose ps

seed: ## Load reference data (placeholders for now)
	docker compose exec -T postgres psql -U tradeflow -d tradeflow < seed/counterparties.sql
	docker compose exec -T postgres psql -U tradeflow -d tradeflow < seed/portfolios.sql
	python3 seed/generate_trades.py

config: ## Validate docker-compose.yml
	docker compose config -q && echo "docker-compose.yml is valid"

clean: ## Remove build output and stack volumes
	mvn -B clean
	docker compose down -v
