package br.devrodrigues.betapiservice.web

import br.devrodrigues.betapiservice.domain.Bet
import br.devrodrigues.betapiservice.domain.BetStatus
import java.math.BigDecimal
import java.time.Instant

data class BetResponse(
    val id: Long,
    val userId: Long,
    val gameId: Long,
    val selection: String,
    val stake: BigDecimal,
    val odds: BigDecimal,
    val status: BetStatus,
    val createdAt: Instant
)

fun Bet.toResponse(): BetResponse =
    BetResponse(
        id = requireNotNull(id),
        userId = userId,
        gameId = gameId,
        selection = selection,
        stake = stake,
        odds = odds,
        status = status,
        createdAt = createdAt
    )
