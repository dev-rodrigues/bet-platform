package br.devrodrigues.betapiservice.application.service.dto

import br.devrodrigues.betapiservice.domain.model.GameStatus
import java.time.Instant

data class CreateGameCommand(
    val externalId: Long,
    val homeTeam: String,
    val awayTeam: String,
    val startTime: Instant,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val status: GameStatus? = null
)
