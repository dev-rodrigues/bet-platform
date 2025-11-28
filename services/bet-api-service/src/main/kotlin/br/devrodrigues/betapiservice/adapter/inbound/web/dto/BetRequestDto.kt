package br.devrodrigues.betapiservice.adapter.inbound.web.dto

import br.devrodrigues.betapiservice.application.service.dto.CreateBetCommand
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class BetRequestDto(
    @field:NotNull @field:Positive
    val userId: Long,
    @field:NotNull @field:Positive
    val gameId: Long,
    @field:NotBlank
    val selection: String,
    @field:NotNull @field:DecimalMin("0.01")
    val stake: BigDecimal,
    @field:NotNull @field:DecimalMin("1.01")
    val odds: BigDecimal
)

fun BetRequestDto.toCommand() = CreateBetCommand(
    userId = userId,
    gameId = gameId,
    selection = selection,
    stake = stake,
    odds = odds
)
