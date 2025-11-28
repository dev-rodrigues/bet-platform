package br.devrodrigues.betapiservice.application.validation

class GameValidationException(val errors: List<ValidationError>) : RuntimeException("Game validation failed")
