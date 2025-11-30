package br.devrodrigues.betsettlementservice.domain.model

import java.math.BigDecimal
import java.time.Instant

data class Bet(
    val id: Long,
    val userId: Long,
    val gameId: Long,
    val gameExternalId: String,
    val selection: String,
    val stake: BigDecimal,
    val odds: BigDecimal,
    val status: String,
    val payout: BigDecimal?,
    val createdAt: Instant,
    val updatedAt: Instant
)
