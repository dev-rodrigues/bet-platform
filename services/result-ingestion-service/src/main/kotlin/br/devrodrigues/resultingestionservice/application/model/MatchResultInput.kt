package br.devrodrigues.resultingestionservice.application.model

data class MatchResultInput(
    val matchExternalId: String,
    val homeScore: Int,
    val awayScore: Int,
    val status: String,
    val providerEventId: String? = null
)
