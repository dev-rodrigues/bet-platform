package br.devrodrigues.betsettlementservice.application.event

import java.math.BigDecimal
import java.time.Instant

data class BetPlacedEvent(
    val id: Long,
    val userId: Long,
    val gameId: Long,
    val gameExternalId: Long,
    val selection: String,
    val stake: BigDecimal,
    val odds: BigDecimal,
    val status: String,
    val createdAt: Instant
)
