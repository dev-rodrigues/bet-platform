package br.devrodrigues.betapiservice.application.validation

data class ValidationError(
    val code: String,
    val message: String
)

class BetValidationException(val errors: List<ValidationError>) : RuntimeException("Bet validation failed")
