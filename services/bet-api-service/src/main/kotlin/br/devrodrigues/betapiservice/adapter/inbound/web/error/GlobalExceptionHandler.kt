package br.devrodrigues.betapiservice.adapter.inbound.web.error

import br.devrodrigues.betapiservice.application.validation.BetValidationException
import br.devrodrigues.betapiservice.application.validation.GameValidationException
import br.devrodrigues.betapiservice.application.validation.ValidationError
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBeanValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult
            .allErrors
            .map {
                val field = (it as? FieldError)?.field ?: "object"
                ValidationError(
                    code = "validation.$field",
                    message = it.defaultMessage ?: "Valor inválido para $field"
                )
            }
        val status = HttpStatus.BAD_REQUEST
        val body = ValidationErrorResponse(
            status = status.value(),
            message = "Erro de validação de campos",
            path = request.requestURI,
            errors = errors
        )
        return ResponseEntity.status(status).body(body)
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
