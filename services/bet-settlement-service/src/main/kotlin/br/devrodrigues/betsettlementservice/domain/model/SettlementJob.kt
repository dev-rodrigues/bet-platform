package br.devrodrigues.betsettlementservice.domain.model

import java.time.Instant

data class SettlementJob(
    val id: Long? = null,
    val matchId: Long,
    val game: Game? = null,
    val externalMatchId: String,
    val status: String,
    val batchSize: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastError: String? = null
)
