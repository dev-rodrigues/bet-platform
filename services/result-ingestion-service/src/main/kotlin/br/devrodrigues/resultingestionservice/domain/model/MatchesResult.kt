package br.devrodrigues.resultingestionservice.domain.model

import java.time.Instant

class MatchesResult(
    val eventId: String,
    val occurredAt: Instant,
    val emittedAt: Instant,
    val matchExternalId: String,
    val homeScore: Int,
    val awayScore: Int,
    val status: String,
    val provider: String? = null
)