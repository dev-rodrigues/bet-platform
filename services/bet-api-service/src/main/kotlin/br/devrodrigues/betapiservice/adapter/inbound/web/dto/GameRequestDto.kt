package br.devrodrigues.betapiservice.adapter.inbound.web.dto

import br.devrodrigues.betapiservice.domain.model.GameStatus
import br.devrodrigues.betapiservice.application.service.dto.CreateGameCommand
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class GameRequestDto(
    @field:NotNull
    val externalId: Long,
    @field:NotBlank
    val homeTeam: String,
    @field:NotBlank
    val awayTeam: String,
    @field:NotNull @field:Future
    val startTime: Instant,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val status: GameStatus? = null
)

fun GameRequestDto.toCommand() = CreateGameCommand(
    externalId = externalId,
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    startTime = startTime,
    homeScore = homeScore,
    awayScore = awayScore,
    status = status
)
