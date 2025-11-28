package br.devrodrigues.betapiservice.application.validation

import br.devrodrigues.betapiservice.application.service.dto.CreateBetCommand
import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.port.out.GameRepository
import java.time.Clock
import java.time.Instant
import org.springframework.stereotype.Component

@Component
class BetValidator(
    private val gameRepository: GameRepository,
    private val clock: Clock = Clock.systemUTC()
) {

    fun validateAndGetGame(command: CreateBetCommand): Game {
        val errors = mutableListOf<ValidationError>()
        val game = gameRepository.findByExternalId(command.gameId)

        if (game == null) {
            errors.add(
                ValidationError(
                    code = "game.notFound",
                    message = "Jogo ${command.gameId} não encontrado"
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
