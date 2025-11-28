package br.devrodrigues.betapiservice.application.service

import br.devrodrigues.betapiservice.adapter.inbound.web.dto.GameRequestDto
import br.devrodrigues.betapiservice.application.service.dto.GamePage
import br.devrodrigues.betapiservice.application.validation.GameValidationException
import br.devrodrigues.betapiservice.application.validation.ValidationError
import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.model.GameStatus
import br.devrodrigues.betapiservice.domain.port.out.GameRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class GameService(
    private val gameRepository: GameRepository
) {

    fun list(page: Int, size: Int): GamePage {
        val games = gameRepository.findPage(page, size)
        return GamePage(
            content = games,
            page = page,
            size = size,
            totalElements = games.size.toLong(),
            totalPages = if (games.isEmpty()) 0 else 1
        )
    }

    fun create(request: GameRequestDto): Game {
        val existing = gameRepository.findByExternalId(request.externalId)
        if (existing != null) {
            throw GameValidationException(
                listOf(
                    ValidationError(
                        code = "game.duplicateExternalId",
                        message = "Jogo com externalId ${request.externalId} já existe"
                    )
                )
            )
        }

        val game = Game(
            externalId = request.externalId,
            homeTeam = request.homeTeam,
            awayTeam = request.awayTeam,
            startTime = request.startTime,
            homeScore = request.homeScore,
            awayScore = request.awayScore,
            status = request.status ?: GameStatus.SCHEDULED,
            matchDate = LocalDate.ofInstant(request.startTime, ZoneOffset.UTC)
        )
        return gameRepository.save(game)
    }
}
