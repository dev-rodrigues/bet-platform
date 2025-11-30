package br.devrodrigues.commonevents

import java.time.Instant

data class GameCreatedEvent(
    val eventId: String,
    val occurredAt: Instant,
    val emittedAt: Instant,
    val gameId: Long,
    val externalId: Long,
    val homeTeam: String,
    val awayTeam: String,
    val startTime: Instant,
    val status: String
)
