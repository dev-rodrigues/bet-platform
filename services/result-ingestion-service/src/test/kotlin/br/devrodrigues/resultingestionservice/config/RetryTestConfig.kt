package br.devrodrigues.resultingestionservice.config

import br.devrodrigues.resultingestionservice.adapter.inbound.messaging.KafkaMatchesResultPublisherAdapter
import br.devrodrigues.resultingestionservice.domain.model.MatchesResult
import br.devrodrigues.resultingestionservice.domain.port.out.MatchesResultPublisher
import org.apache.kafka.common.errors.TimeoutException
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

@TestConfiguration
class RetryTestConfig {
    @Bean
    @Primary
    fun flakyPublisher(delegate: KafkaMatchesResultPublisherAdapter) = FlakyPublisher(delegate)
}

class FlakyPublisher(
    private val delegate: KafkaMatchesResultPublisherAdapter
) : MatchesResultPublisher {
    var failuresBeforeSuccess: Int = 0
    val attempts: AtomicInteger = AtomicInteger(0)
    val published: ConcurrentLinkedQueue<MatchesResult> = ConcurrentLinkedQueue()

    override fun publish(event: MatchesResult) {
        val attempt = attempts.incrementAndGet()
        if (attempt <= failuresBeforeSuccess) {
            throw TimeoutException("simulated kafka timeout on attempt $attempt")
        }
        delegate.publish(event)
        published.add(event)
    }

    fun reset() {
        attempts.set(0)
        published.clear()
        failuresBeforeSuccess = 0
    }
}
