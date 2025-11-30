# bet-api-service

API de apostas: expõe criação/listagem de jogos e criação de bets, persistindo em Postgres e publicando eventos
`BET_PLACED` via outbox para Kafka.

## Visão rápida (fluxo)

```mermaid
flowchart LR
    client[Clientes HTTP] --> api[bet-api-service<br/>Perfil padrão]
    api --> db[(Postgres<br/>bets/outbox)]
    db --> worker[Worker<br/>Perfil: worker]
    worker --> kafka[Kafka<br/>app.topics.bet-placed]
    worker --> db
```

## Fluxo de processamento /bets

```mermaid
flowchart TD
    start([POST /bets]) --> validate{Janela aberta?}
    validate -->|ok| persistBet[Salvar bet]
    validate -->|falha| fail422[Retornar 422]
    persistBet --> saveOutbox[Salvar outbox BET_PLACED]
    saveOutbox --> created[Responder 201 com bet]
    saveOutbox --> pendingOutbox[Pendente na outbox]
    pendingOutbox --> workerLoop[Worker lê PENDING]
    workerLoop --> publishKafka[Publicar em Kafka tópico bet_placed]
    publishKafka --> markPublished[markPublished<br/>status PUBLISHED]
    publishKafka -->|erro| markError[markError<br/>status ERROR]
```

## Endpoints principais

- `POST /games` – cria jogo.
- `GET  /games` – lista paginada.
- `POST /bets` – cria aposta para jogo futuro; valida janela e persiste outbox `BET_PLACED`.
