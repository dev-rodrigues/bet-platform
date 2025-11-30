package br.devrodrigues.betsettlementservice.application.service

import br.devrodrigues.betsettlementservice.domain.model.Game
import br.devrodrigues.betsettlementservice.domain.port.out.GameRepository
import br.devrodrigues.commonevents.GameCreatedEvent
import org.springframework.stereotype.Service

@Service
class GameCreationService(
    private val gameRepository: GameRepository
) {

    fun upsert(event: GameCreatedEvent) {
        val existing = gameRepository.findByExternalId(event.externalId)

        val game = existing?.copy(
            startTime = event.startTime,
            status = event.status,
            homeTeam = event.homeTeam,
            awayTeam = event.awayTeam,
            updatedAt = event.emittedAt
        ) ?: Game(
            externalId = event.externalId,
            startTime = event.startTime,
            betsCloseAt = event.startTime,
            status = event.status,
            homeTeam = event.homeTeam,
            awayTeam = event.awayTeam,
            createdAt = event.emittedAt,
            updatedAt = event.emittedAt
        )

        gameRepository.save(game)
    }
}
