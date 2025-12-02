package br.devrodrigues.betapiservice.adapter.inbound.web.dto

import br.devrodrigues.betapiservice.application.service.dto.CreateBetCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class BetRequestDto(
    @field:NotNull @field:Positive
    val userId: Long,
    @field:NotNull @field:Positive
    val gameId: Long,
    @field:NotNull
    @field:Schema(
        description = "Seleção da aposta",
        allowableValues = ["HOME_WIN", "AWAY_WIN", "DRAW"]
    )
    val selection: BetSelection,
    @field:NotNull @field:DecimalMin("0.01")
    val stake: BigDecimal,
    @field:NotNull @field:DecimalMin("1.01")
    val odds: BigDecimal
)

enum class BetSelection {
    HOME_WIN,
    AWAY_WIN,
    DRAW
}

fun BetRequestDto.toCommand() = CreateBetCommand(
    userId = userId,
    gameId = gameId,
    selection = selection.name,
    stake = stake,
    odds = odds
)
