package br.devrodrigues.betsettlementservice.adapter.inbound.messaging

import br.devrodrigues.betsettlementservice.application.service.GameCreationService
import br.devrodrigues.commonevents.GameCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class GameCreatedListener(
    private val gameCreationService: GameCreationService
) {

    private val logger = LoggerFactory.getLogger(GameCreatedListener::class.java)

    @KafkaListener(topics = ["\${app.topics.game-created}"], groupId = "bet-settlement-service")
    fun onGameCreated(event: GameCreatedEvent) {
        logger.info("Processing GameCreatedEvent eventId={} externalId={}", event.eventId, event.externalId)
        gameCreationService.upsert(event)
    }
}
