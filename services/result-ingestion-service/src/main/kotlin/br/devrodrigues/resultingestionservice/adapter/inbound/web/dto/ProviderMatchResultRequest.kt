package br.devrodrigues.resultingestionservice.adapter.inbound.web.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class ProviderMatchResultRequest(
    @field:NotBlank
    val matchExternalId: String,
    @field:Min(0)
    val homeScore: Int,
    @field:Min(0)
    val awayScore: Int,
    @field:NotBlank
    val status: String,
    val providerEventId: String? = null
)
