package br.devrodrigues.betsettlementservice.domain.model

import java.time.Instant

data class Game(
    val id: Long? = null,
    val externalId: Long,
    val startTime: Instant,
    val betsCloseAt: Instant,
    val status: String,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val homeTeam: String,
    val awayTeam: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
