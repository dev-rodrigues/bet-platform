package br.devrodrigues.betsettlementservice.adapter.inbound.messaging

import br.devrodrigues.betsettlementservice.application.event.BetPlacedEvent
import br.devrodrigues.betsettlementservice.application.service.BetPlacementService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class BetPlacedListener(
    private val betPlacementService: BetPlacementService
) {

    private val logger = LoggerFactory.getLogger(BetPlacedListener::class.java)

    @KafkaListener(topics = ["\${app.topics.bet-placed}"], groupId = "bet-settlement-service-bets")
    fun onBetPlaced(event: BetPlacedEvent) {
        logger.info(
            "Received BetPlaced betId={} gameExternalId={} status={}",
            event.id,
            event.gameExternalId,
            event.status
        )

        betPlacementService.upsert(
            event
        )
    }
}
