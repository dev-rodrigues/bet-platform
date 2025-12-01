package br.devrodrigues.betsettlementservice.support

import br.devrodrigues.betsettlementservice.application.event.BetPlacedEvent
import java.math.BigDecimal
import java.time.Instant

object BetPlacedEventFixtures {

    fun default(
        id: Long = 10L,
        userId: Long = 5L,
        gameId: Long = 9999L,
        gameExternalId: Long = 2024L,
        selection: String = "HOME",
        stake: BigDecimal = BigDecimal("10.00"),
        odds: BigDecimal = BigDecimal("2.50"),
        status: String = "CREATED",
        createdAt: Instant = Instant.parse("2024-03-01T00:00:00Z")
    ): BetPlacedEvent =
        BetPlacedEvent(
            id = id,
            userId = userId,
            gameId = gameId,
            gameExternalId = gameExternalId,
            selection = selection,
            stake = stake,
            odds = odds,
            status = status,
            createdAt = createdAt
        )
}
