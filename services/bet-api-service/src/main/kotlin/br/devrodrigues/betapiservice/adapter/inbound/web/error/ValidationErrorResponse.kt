package br.devrodrigues.betapiservice.adapter.inbound.web.error

import br.devrodrigues.betapiservice.application.validation.ValidationError
import java.time.Instant

data class ValidationErrorResponse(
    val status: Int,
    val message: String,
    val path: String,
    val errors: List<ValidationError>,
    val timestamp: Instant = Instant.now()
)
