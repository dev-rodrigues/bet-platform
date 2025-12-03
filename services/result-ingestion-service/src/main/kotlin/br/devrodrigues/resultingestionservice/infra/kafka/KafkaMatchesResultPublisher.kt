package br.devrodrigues.resultingestionservice.infra.kafka

import br.devrodrigues.commonevents.MatchesResultEvent
import br.devrodrigues.resultingestionservice.application.port.out.MatchesResultPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaMatchesResultPublisher(
    private val kafkaTemplate: KafkaTemplate<String, MatchesResultEvent>,
    @Value("\${bet.kafka.topics.matches-result}") private val topic: String
) : MatchesResultPublisher {

    override fun publish(event: MatchesResultEvent) {
        kafkaTemplate.send(topic, event.matchExternalId, event).get()
    }
}
