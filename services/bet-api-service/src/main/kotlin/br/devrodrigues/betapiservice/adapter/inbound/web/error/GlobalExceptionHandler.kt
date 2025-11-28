package br.devrodrigues.betapiservice.adapter.inbound.web.error

import br.devrodrigues.betapiservice.application.validation.BetValidationException
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
        val status = HttpStatus.UNPROCESSABLE_ENTITY
        val body = ValidationErrorResponse(
            status = status.value(),
            message = "Validação da aposta falhou",
            path = request.requestURI,
            errors = ex.errors
        )
        return ResponseEntity.status(status).body(body)
    }
}
