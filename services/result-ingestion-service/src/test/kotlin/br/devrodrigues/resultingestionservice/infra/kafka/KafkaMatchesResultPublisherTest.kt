package br.devrodrigues.resultingestionservice.infra.kafka

import br.devrodrigues.commonevents.MatchesResultEvent
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

class KafkaMatchesResultPublisherTest {

    private val kafkaTemplate: KafkaTemplate<String, MatchesResultEvent> = mock()
    private val topic = "matches.result.v1"
    private val publisher = KafkaMatchesResultPublisher(kafkaTemplate, topic)

    @Test
    fun `should send event with matchExternalId as key`() {
        val event = MatchesResultEvent(
            eventId = UUID.randomUUID().toString(),
            occurredAt = Instant.now(),
            emittedAt = Instant.now(),
            matchExternalId = "match-1",
            homeScore = 1,
            awayScore = 0,
            status = "FINISHED",
            provider = null
        )

        whenever(kafkaTemplate.send(any(), any<String>(), any()))
            .thenReturn(CompletableFuture.completedFuture(null))

        publisher.publish(event)

        verify(kafkaTemplate).send(topic, event.matchExternalId, event)
    }
}
