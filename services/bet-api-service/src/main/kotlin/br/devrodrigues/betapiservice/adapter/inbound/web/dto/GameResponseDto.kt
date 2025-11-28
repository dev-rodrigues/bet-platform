package br.devrodrigues.betapiservice.adapter.inbound.web.dto

import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.model.GameStatus
import java.time.Instant
import java.time.LocalDate

data class GameResponseDto(
    val id: Long,
    val externalId: Long,
    val homeTeam: String,
    val awayTeam: String,
    val startTime: Instant,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: GameStatus,
    val matchDate: LocalDate
)

fun Game.toResponseDto(): GameResponseDto =
    GameResponseDto(
        id = requireNotNull(id),
        externalId = externalId,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        startTime = startTime,
        homeScore = homeScore,
        awayScore = awayScore,
        status = status,
        matchDate = matchDate
    )
