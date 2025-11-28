package br.devrodrigues.betapiservice.domain.model

import java.math.BigDecimal
import java.time.Instant

data class Bet(
    val id: Long? = null,
    val userId: Long,
    val gameId: Long,
    val selection: String,
    val stake: BigDecimal,
    val odds: BigDecimal,
    val status: BetStatus = BetStatus.PENDING,
    val createdAt: Instant = Instant.now()
)

enum class BetStatus {
    PENDING,
    SETTLED
}
