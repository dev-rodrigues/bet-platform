package br.devrodrigues.resultingestionservice.adapter.inbound.messaging

import br.devrodrigues.resultingestionservice.domain.model.MatchesResult
import br.devrodrigues.resultingestionservice.domain.port.out.MatchesResultPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaMatchesResultPublisherAdapter(
    private val kafkaTemplate: KafkaTemplate<String, MatchesResult>,
    @Value("\${bet.kafka.topics.matches-result}") private val topic: String
) : MatchesResultPublisher {

    override fun publish(event: MatchesResult) {
        kafkaTemplate.send(topic, event.matchExternalId, event).get()
    }
}