package br.devrodrigues.betapiservice.adapter.inbound.web.dto

import br.devrodrigues.betapiservice.domain.model.Bet
import br.devrodrigues.betapiservice.domain.model.BetStatus
import org.springframework.hateoas.Link
import java.math.BigDecimal
import java.time.Instant

data class BetResponseDto(
    val id: Long,
    val userId: Long,
    val gameId: Long,
    val selection: String,
    val stake: BigDecimal,
    val odds: BigDecimal,
    val status: BetStatus,
    val createdAt: Instant,
    val links: List<Link> = emptyList()
)

fun Bet.toResponseDto(): BetResponseDto =
    BetResponseDto(
        id = requireNotNull(id),
        userId = userId,
        gameId = gameId,
        selection = selection,
        stake = stake,
        odds = odds,
        status = status,
        createdAt = createdAt
    )
