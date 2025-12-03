# bet-settlement-service

O bet-settlement-service mantém uma cópia dos jogos e apostas para poder liquidá-las assim que chega o resultado de
cada partida. Ele consome os eventos `bet-placed` e `game-created` para garantir que aposta e jogo estejam alinhados
no próprio banco, e quando recebe `matches-result` aplica o placar, cria um `settlement_job` e dispara a liquidação.
No perfil `worker`, ele varre jobs pendentes, busca apostas ainda não liquidadas, calcula o outcome/payout, e grava
pedidos de pagamento em `outbox_event` para serem publicados como `payments.requested.v1`. Se um resultado chega sem
jogo correspondente, o listener envia para a DLQ (`matches-result-dlq`) via `KafkaErrorHandlerConfig`.

## Visão rápida (fluxo)

```mermaid
flowchart LR
    subgraph Kafka["Kafka"]
        betPlaced["bet-placed (in)"]
        gameCreated["game-created (in)"]
        matchesResult["matches-result (in)"]
        paymentsRequested["payments-requested (out)"]
    end

    betPlaced --> settlement[bet-settlement-service<br/>listeners]
    gameCreated --> settlement
    matchesResult --> settlement
    settlement --> db[(Postgres<br/>games, bets, settlement_job, outbox_event)]
    db -. jobs pendentes .-> worker[Settlement worker<br/>profile: worker]
    worker --> db
    worker --> paymentsRequested
```

## Tópicos Kafka

### Consumidos

- `app.topics.bet-placed`— evento `BetPlacedEvent`.
- `app.topics.game-created` — evento `GameCreatedEvent`.
- `app.topics.matches-result` — evento `MatchesResultEvent` (usa DLQ se não houver jogo).

### Publicados

- `app.topics.payments-requested` (ex.: `payments.requested.v1`) — payload montado pelo worker com `paymentRequestId`,
  `userId`, `totalAmount`, `matchExternalId`.

## Diagrama UML (Entidades JPA)

```mermaid
classDiagram
    direction LR

    class GameEntity {
        +Long id
        +Long externalId
        +Instant startTime
        +Instant betsCloseAt
        +String status
        +Int? homeScore
        +Int? awayScore
        +String homeTeam
        +String awayTeam
        +Instant createdAt
        +Instant updatedAt
    }

    class BetEntity {
        +Long id
        +Long userId
        +Long gameId
        +String gameExternalId
        +String selection
        +BigDecimal stake
        +BigDecimal odds
        +String status
        +BigDecimal? payout
        +Instant createdAt
        +Instant updatedAt
    }

    class SettlementJobEntity {
        +Long id
        +Long matchId
        +String externalMatchId
        +String status
        +Int batchSize
        +Instant createdAt
        +Instant updatedAt
        +String? lastError
    }

    class OutboxEventEntity {
        +UUID id
        +String aggregateType
        +String aggregateId
        +String eventType
        +String payload
        +String status
        +String referenceId
    }

    GameEntity "1" <--> "0..1" SettlementJobEntity : match_id
    GameEntity "1" <--> "0..*" BetEntity : game_id
```
