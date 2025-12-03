package br.devrodrigues.betsettlementservice.adapter.inbound.messaging

import br.devrodrigues.betsettlementservice.application.service.MatchResultService
import br.devrodrigues.commonevents.MatchesResultEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class MatchesResultListener(
    private val matchResultService: MatchResultService
) {

    private val logger = LoggerFactory.getLogger(MatchesResultListener::class.java)

    @KafkaListener(
        topics = ["\${app.topics.matches-result}"],
        groupId = "\${app.kafka.consumer-groups.matches-result}"
    )
    fun onMatchesResult(event: MatchesResultEvent) {
        logger.info(
            "Processing MatchesResultEvent eventId={} matchExternalId={} status={}",
            event.eventId,
            event.matchExternalId,
            event.status
        )
        matchResultService.applyResult(event)
    }
}
