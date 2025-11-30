package br.devrodrigues.commonevents

import java.time.Instant

data class MatchesResultEvent(
    val eventId: String,
    val occurredAt: Instant,
    val emittedAt: Instant,
    val matchExternalId: String,
    val homeScore: Int,
    val awayScore: Int,
    val status: String,
    val provider: String? = null
)
