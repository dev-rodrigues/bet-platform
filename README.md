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

## Arquitetura geral (alto nível)

```mermaid
flowchart TD
    subgraph Kafka["Kafka tópicos"]
        direction TB
        subgraph KafkaIn["IN"]
            tMatches["matches.result.v1"]
        end
        subgraph KafkaOut["OUT"]
            tBetPlaced["bets.placed.v1"]
            tGameCreated["games.created.v1"]
            tPayments["payments.requested.v1"]
            tMatchesDlq["matches.result.dlq"]
        end
    end

    subgraph Sources["Fontes"]
        resultFeed["Fornecedores de resultado"]
    end

    subgraph Services["Serviços"]
        direction LR
        subgraph Ingestion["result-ingestion-service"]
            ingestion["webhook resultados"]
        end
        subgraph API["bet-api-service"]
            apiWeb["web (HTTP bets/games\nconsome matches.result)"]
            apiWorker["worker (outbox publisher)"]
            subgraph ApiDB["bd bet-api"]
                dbApi["betting"]
            end
        end
        subgraph Settlement["bet-settlement-service"]
            direction TB
            settlementListeners["listeners\n(bets.placed/games.created/matches.result)"]
            settlementWorker["worker\n(liquidação + outbox)"]
            subgraph SettlementDB["bd bet-settlement"]
                dbSettlement["bet_settlement"]
            end
        end
    end

    resultFeed --> ingestion["result-ingestion-service\n(webhook resultados)"]
    ingestion --> tMatches
    apiWeb --> dbApi
    apiWorker --> tBetPlaced
    apiWorker --> tGameCreated
    tMatches --> apiWeb
    tBetPlaced --> settlementListeners
    tGameCreated --> settlementListeners
    tMatches --> settlementListeners
    settlementListeners --> dbSettlement
    settlementListeners -.-> tMatchesDlq
    settlementWorker --> tPayments
%% Cores por tipo de fluxo
    linkStyle 1 stroke: #1f77b4, stroke-width: 2
    linkStyle 5 stroke: #1f77b4, stroke-width: 2
    linkStyle 8 stroke: #1f77b4, stroke-width: 2
    linkStyle 3 stroke: #ff7f0e, stroke-width: 2
    linkStyle 6 stroke: #ff7f0e, stroke-width: 2
    linkStyle 4 stroke: #2ca02c, stroke-width: 2
    linkStyle 7 stroke: #2ca02c, stroke-width: 2
    linkStyle 10 stroke: #999, stroke-width: 2, stroke-dasharray: 4 2
    linkStyle 2 stroke: #888, stroke-width: 1.5
    linkStyle 9 stroke: #888, stroke-width: 1.5
    linkStyle 11 stroke: #888, stroke-width: 1.5
    linkStyle 0 stroke: #666, stroke-width: 1.5
```

Fluxo geral:

- `bet-api-service` recebe bets/games via HTTP, persiste em `betting`, publica `bets.placed.v1` e `games.created.v1`, e
  consome `matches.result.v1` para manter jogos atualizados.
- `result-ingestion-service` recebe webhooks de resultados e publica `matches.result.v1`.
- `bet-settlement-service` consome `bets.placed.v1`, `games.created.v1` e `matches.result.v1`, liquida apostas em
  `bet_settlement`, e publica `payments.requested.v1`; se um resultado chegar sem jogo, envia para `matches.result.dlq`.
- Consumidores externos (ex.: carteira) leem `payments.requested.v1`.
