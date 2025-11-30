package br.devrodrigues.betsettlementservice.adapter.inbound.messaging

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant

@Component
class BetPlacedListener {

    private val logger = LoggerFactory.getLogger(BetPlacedListener::class.java)

    @KafkaListener(topics = ["\${app.topics.bet-placed}"], groupId = "bet-settlement-service-bets")
    fun onBetPlaced(event: BetPlacedMessage) {
        logger.info(
            "Received BetPlaced betId={} gameExternalId={} status={}",
            event.id,
            event.gameExternalId,
            event.status
        )
    }
}

data class BetPlacedMessage(
    val id: Long,
    val userId: Long,
    val gameId: Long,
    val gameExternalId: Long,
    val selection: String,
    val stake: BigDecimal,
    val odds: BigDecimal,
    val status: String,
    val createdAt: Instant
)
