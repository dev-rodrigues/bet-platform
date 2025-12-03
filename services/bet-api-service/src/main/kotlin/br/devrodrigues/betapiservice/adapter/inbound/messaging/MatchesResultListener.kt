package br.devrodrigues.betapiservice.adapter.inbound.messaging

import br.devrodrigues.betapiservice.application.service.GameService
import br.devrodrigues.commonevents.MatchesResultEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class MatchesResultListener(
    private val gameService: GameService
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @KafkaListener(
        topics = ["\${app.topics.matches-result}"],
        groupId = "\${app.kafka.consumer-groups.matches-result}"
    )
    fun onMatchResult(event: MatchesResultEvent) {
        logger.info(
            "Received MatchesResultEvent eventId={} matchExternalId={} status={}",
            event.eventId,
            event.matchExternalId,
            event.status
        )

        gameService.run { applyResult(event) }
    }
}
