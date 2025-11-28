package br.devrodrigues.betapiservice.web.dto

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
