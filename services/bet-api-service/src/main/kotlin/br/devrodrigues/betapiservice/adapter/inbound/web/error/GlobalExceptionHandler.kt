package br.devrodrigues.betapiservice.adapter.inbound.web.error

import br.devrodrigues.betapiservice.application.validation.BetValidationException
import br.devrodrigues.betapiservice.application.validation.GameValidationException
import br.devrodrigues.betapiservice.application.validation.ValidationError
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BetValidationException::class)
    fun handleBetValidation(
        ex: BetValidationException,
        request: HttpServletRequest
    ): ResponseEntity<ValidationErrorResponse> {
        return buildValidationResponse("Validação da aposta falhou", ex.errors, request)
    }

    @ExceptionHandler(GameValidationException::class)
    fun handleGameValidation(
        ex: GameValidationException,
        request: HttpServletRequest
    ): ResponseEntity<ValidationErrorResponse> {
        return buildValidationResponse("Validação do jogo falhou", ex.errors, request)
    }

    private fun buildValidationResponse(
        message: String,
        errors: List<ValidationError>,
        request: HttpServletRequest
    ): ResponseEntity<ValidationErrorResponse> {
        val status = HttpStatus.UNPROCESSABLE_ENTITY
        val body = ValidationErrorResponse(
            status = status.value(),
            message = message,
            path = request.requestURI,
            errors = errors
        )
        return ResponseEntity.status(status).body(body)
    }
}
