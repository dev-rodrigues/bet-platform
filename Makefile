ifeq ($(shell docker compose version >/dev/null 2>&1 && echo ok),ok)
COMPOSE_BIN ?= docker compose
else ifneq ($(shell command -v docker-compose >/dev/null 2>&1 && echo ok),)
COMPOSE_BIN ?= docker-compose
else
COMPOSE_BIN ?= docker compose
endif

COMPOSE := $(COMPOSE_BIN) -f $(CURDIR)/infra/docker-compose.yaml
DB_USER ?= betting
DB_NAME ?= betting
DOCKER ?= docker
BET_API_IMAGE ?= bet-api-service:local
BET_SETTLEMENT_IMAGE ?= bet-settlement-service:local
RESULT_INGESTION_IMAGE ?= result-ingestion-service:local

.PHONY: up up-all down down-all restart logs ps psql infra build-images build-bet-api build-bet-settlement build-result-ingestion

# Subir apenas a infra (default)
up: infra

infra:
	$(COMPOSE) up -d

# Subir infra + apps (perfis apps)
up-all:
	$(COMPOSE) --profile apps up -d

logs:
	$(COMPOSE) logs -f

ps:
	$(COMPOSE) ps

psql:
	$(COMPOSE) exec postgres psql -U $(DB_USER) -d $(DB_NAME)

down:
	$(COMPOSE) down -v

down-all:
	$(COMPOSE) --profile apps down -v

restart: down up

# Build docker images
build-images: build-bet-api build-bet-settlement build-result-ingestion

build-bet-api:
	$(DOCKER) build -f services/bet-api-service/Dockerfile -t $(BET_API_IMAGE) .

build-bet-settlement:
	$(DOCKER) build -f services/bet-settlement-service/Dockerfile -t $(BET_SETTLEMENT_IMAGE) .

build-result-ingestion:
	$(DOCKER) build -f services/result-ingestion-service/Dockerfile -t $(RESULT_INGESTION_IMAGE) .
