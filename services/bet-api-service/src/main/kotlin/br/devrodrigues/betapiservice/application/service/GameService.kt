package br.devrodrigues.betapiservice.application.service

import br.devrodrigues.betapiservice.application.service.dto.CreateGameCommand
import br.devrodrigues.betapiservice.application.service.dto.GamePage
import br.devrodrigues.betapiservice.application.validation.GameValidationException
import br.devrodrigues.betapiservice.application.validation.ValidationError
import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.model.GameStatus
import br.devrodrigues.betapiservice.domain.port.out.GameRepository
import br.devrodrigues.commonevents.MatchesResultEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val outboxService: OutboxService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

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

    fun create(command: CreateGameCommand): Game {
        val existing = gameRepository.findByExternalId(command.externalId)
        if (existing != null) {
            throw GameValidationException(
                listOf(
                    ValidationError(
                        code = "game.duplicateExternalId",
                        message = "Jogo com externalId ${command.externalId} já existe"
                    )
                )
            )
        }

        val game = Game(
            externalId = command.externalId,
            homeTeam = command.homeTeam,
            awayTeam = command.awayTeam,
            startTime = command.startTime,
            homeScore = command.homeScore,
            awayScore = command.awayScore,
            status = command.status ?: GameStatus.SCHEDULED,
            matchDate = LocalDate.ofInstant(command.startTime, ZoneOffset.UTC)
        )
        val savedGame = gameRepository.save(game)
        publishGameCreated(savedGame)
        return savedGame
    }

    fun applyResult(
        event: MatchesResultEvent
    ) {
        val game = gameRepository.findByExternalId(event.matchExternalId.toLong())

        if (game == null) {
            logger.warn("Game ${event.matchExternalId} not found")
            return
        }

        val updated = game.copy(
            homeScore = event.homeScore,
            awayScore = event.awayScore,
            status = GameStatus.from(event.status)
        )

        gameRepository.save(updated)
    }

    private fun publishGameCreated(game: Game) {
        val id = requireNotNull(game.id) { "Game id must be present to publish creation event" }
        outboxService.saveGameCreatedEvent(
            GameCreatedPayload(
                id = id,
                externalId = game.externalId,
                homeTeam = game.homeTeam,
                awayTeam = game.awayTeam,
                startTime = game.startTime,
                status = game.status.name
            )
        )
    }
}
