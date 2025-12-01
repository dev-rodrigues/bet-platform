package br.devrodrigues.betapiservice.application.service

import br.devrodrigues.betapiservice.domain.model.Bet
import br.devrodrigues.betapiservice.domain.model.OutboxEvent
import br.devrodrigues.betapiservice.domain.port.out.OutboxRepository
import br.devrodrigues.commonevents.BetPlacedEvent
import br.devrodrigues.commonevents.GameCreatedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class OutboxService(
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper
) {

    fun saveBetPlacedEvent(bet: Bet, gameExternalId: Long) {
        val payload = objectMapper.writeValueAsString(
            BetPlacedEvent(
                id = requireNotNull(bet.id),
                userId = bet.userId,
                gameId = bet.gameId,
                gameExternalId = gameExternalId,
                selection = bet.selection,
                stake = bet.stake,
                odds = bet.odds,
                status = bet.status.name,
                createdAt = bet.createdAt
            )
        )

        val event = OutboxEvent(
            id = UUID.randomUUID(),
            aggregateType = "bet",
            aggregateId = bet.id.toString(),
            type = "BET_PLACED",
            payload = payload
        )
        outboxRepository.save(event)
    }

    fun saveGameCreatedEvent(game: GameCreatedPayload) {
        val payload = objectMapper.writeValueAsString(
            GameCreatedEvent(
                eventId = UUID.randomUUID().toString(),
                occurredAt = Instant.now(),
                emittedAt = Instant.now(),
                gameId = game.id,
                externalId = game.externalId,
                homeTeam = game.homeTeam,
                awayTeam = game.awayTeam,
                startTime = game.startTime,
                status = game.status
            )
        )

        val event = OutboxEvent(
            id = UUID.randomUUID(),
            aggregateType = "game",
            aggregateId = game.id.toString(),
            type = "GAME_CREATED",
            payload = payload
        )
        outboxRepository.save(event)
    }
}

data class GameCreatedPayload(
    val id: Long,
    val externalId: Long,
    val homeTeam: String,
    val awayTeam: String,
    val startTime: Instant,
    val status: String
)
