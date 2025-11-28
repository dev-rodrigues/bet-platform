# Bet Platform

## Ambiente de desenvolvimento
- Pré-requisitos: Docker Desktop (ou Docker Engine) com Compose (plugin `docker compose` ou binário `docker-compose`) e `make` instalado.
- Subir stack local (Postgres + Redpanda + Console):
  ```sh
  make up
  ```
- Parar e remover (inclui volumes):
  ```sh
  make down
  ```
- Logs em tempo real:
  ```sh
  make logs
  ```
- Listar estado dos serviços:
  ```sh
  make ps
  ```
- Se o plugin `docker compose` não estiver disponível, o Makefile tenta usar `docker-compose`. Você também pode forçar: `COMPOSE_BIN=docker-compose make up`.

## Acesso às ferramentas
- **Postgres**: `localhost:5432`, db/user/password `betting`. Exemplo de conexão:
  ```sh
  psql postgresql://betting:betting@localhost:5432/betting
  # ou dentro do container
  make psql
  ```
- **Redpanda (Kafka)**:
  - Broker para clientes na máquina host: `localhost:19092` (ex.: `KAFKA_BOOTSTRAP_SERVERS=localhost:19092`).
  - Listener interno (containers): `redpanda:9092`.
  - Admin API (health/metrics, sem UI): `localhost:9644`.
- **Redpanda Console (UI)**: painel web em `http://localhost:8080` para tópicos, mensagens e consumer groups (sobe via `make up`).
- **Swagger do bet-api-service**: com o serviço rodando (`./gradlew :services:bet-api-service:bootRun`), acesse `http://localhost:8080/swagger-ui/index.html` para a documentação e testes.

## Estrutura
- `infra/docker-compose.yaml` define o stack local.
- `Makefile` na raiz facilita comandos `up/down/logs/ps/psql`.
