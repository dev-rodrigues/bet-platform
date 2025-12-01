package br.devrodrigues.betsettlementservice.application.validation

class MissingGameForBetPlacementException(
    val betId: Long,
    val gameExternalId: Long
) : RuntimeException("Game not found for betId=$betId and externalId=$gameExternalId")
