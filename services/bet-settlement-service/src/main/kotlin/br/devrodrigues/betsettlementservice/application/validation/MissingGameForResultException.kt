package br.devrodrigues.betsettlementservice.application.validation

class MissingGameForResultException(
    val eventId: String,
    val matchExternalId: String
) : RuntimeException("Game not found for matchExternalId=$matchExternalId (eventId=$eventId)")
