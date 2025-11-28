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
- **Redpanda (Kafka)**: broker em `localhost:9092`, Admin API em `localhost:9644` (saúde/metrics).
- **Redpanda Console (UI)**: `http://localhost:8080` para visualizar tópicos, mensagens e consumer groups.

## Estrutura
- `infra/docker-compose.yaml` define o stack local.
- `Makefile` na raiz facilita comandos `up/down/logs/ps/psql`.

