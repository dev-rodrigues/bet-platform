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

.PHONY: up down restart logs ps psql

up:
	$(COMPOSE) up -d

logs:
	$(COMPOSE) logs -f

ps:
	$(COMPOSE) ps

psql:
	$(COMPOSE) exec postgres psql -U $(DB_USER) -d $(DB_NAME)

down:
	$(COMPOSE) down -v

restart: down up
