package br.devrodrigues.betapiservice.application.validation

import br.devrodrigues.betapiservice.adapter.inbound.web.dto.BetRequestDto
import br.devrodrigues.betapiservice.application.validation.ValidationError
import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.port.out.GameQueryPort
import java.time.Clock
import java.time.Instant
import org.springframework.stereotype.Component

@Component
class BetValidator(
    private val gameQueryPort: GameQueryPort,
    private val clock: Clock = Clock.systemUTC()
) {

    fun validateAndGetGame(request: BetRequestDto): Game {
        val errors = mutableListOf<ValidationError>()
        val game = gameQueryPort.findByExternalId(request.gameId)

        if (game == null) {
            errors.add(
                ValidationError(
                    code = "game.notFound",
                    message = "Jogo ${request.gameId} não encontrado"
                )
            )
        } else {
            val now = Instant.now(clock)
            if (game.startTime.isBefore(now) || game.startTime == now) {
                errors.add(
                    ValidationError(
                        code = "betting.window.closed",
                        message = "Apostas encerradas para o jogo ${game.externalId}"
                    )
                )
            }
        }

        if (errors.isNotEmpty()) {
            throw BetValidationException(errors)
        }
        return requireNotNull(game)
    }
}
