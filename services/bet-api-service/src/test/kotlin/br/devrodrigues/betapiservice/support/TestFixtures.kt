package br.devrodrigues.betapiservice.support

import br.devrodrigues.betapiservice.application.service.dto.CreateBetCommand
import br.devrodrigues.betapiservice.application.service.dto.CreateGameCommand
import br.devrodrigues.betapiservice.domain.model.Bet
import br.devrodrigues.betapiservice.domain.model.BetStatus
import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.model.GameStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

object TestFixtures {
    fun betCommand(
        userId: Long = 1L,
        gameId: Long = 10L,
        selection: String = "Team A",
        stake: BigDecimal = BigDecimal("50.00"),
        odds: BigDecimal = BigDecimal("2.10")
    ) = CreateBetCommand(
        userId = userId,
        gameId = gameId,
        selection = selection,
        stake = stake,
        odds = odds
    )

    fun gameCommand(
        externalId: Long = 123L,
        homeTeam: String = "Home",
        awayTeam: String = "Away",
        startTime: Instant = Instant.now().plusSeconds(3_600),
        homeScore: Int? = null,
        awayScore: Int? = null,
        status: GameStatus? = GameStatus.SCHEDULED
    ) = CreateGameCommand(
        externalId = externalId,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        startTime = startTime,
        homeScore = homeScore,
        awayScore = awayScore,
        status = status
    )

    fun game(
        id: Long? = null,
        externalId: Long = 10L,
        startTime: Instant = Instant.now().plusSeconds(3_600),
        homeTeam: String = "Home",
        awayTeam: String = "Away",
        status: GameStatus = GameStatus.SCHEDULED
    ): Game = Game(
        id = id,
        externalId = externalId,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        startTime = startTime,
        homeScore = null,
        awayScore = null,
        status = status,
        matchDate = LocalDate.ofInstant(startTime, ZoneOffset.UTC)
    )

    fun bet(
        id: Long? = 99L,
        userId: Long = 1L,
        gameId: Long = 10L,
        selection: String = "Team A",
        stake: BigDecimal = BigDecimal("50.00"),
        odds: BigDecimal = BigDecimal("2.10"),
        status: BetStatus = BetStatus.PENDING,
        createdAt: Instant = Instant.parse("2024-01-01T12:00:00Z")
    ): Bet = Bet(
        id = id,
        userId = userId,
        gameId = gameId,
        selection = selection,
        stake = stake,
        odds = odds,
        status = status,
        createdAt = createdAt
    )

    fun betPayload(
        userId: Long = 1,
        gameId: Long = 10,
        selection: String = "Team A",
        stake: Number = 50.00,
        odds: Number = 2.10
    ): Map<String, Any> = mapOf(
        "userId" to userId,
        "gameId" to gameId,
        "selection" to selection,
        "stake" to stake,
        "odds" to odds
    )

    fun betPayloadTeamB(): Map<String, Any> = betPayload(
        userId = 2,
        gameId = 20,
        selection = "Team B",
        stake = 10.00,
        odds = 1.50
    )
}
