package br.devrodrigues.betapiservice.application.service.dto

import java.math.BigDecimal

data class CreateBetCommand(
    val userId: Long,
    val gameId: Long,
    val selection: String,
    val stake: BigDecimal,
    val odds: BigDecimal
)
