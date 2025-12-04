package br.devrodrigues.resultingestionservice.application.service

import br.devrodrigues.commonevents.MatchesResultEvent
import br.devrodrigues.resultingestionservice.application.mapper.MatchResultMapper
import br.devrodrigues.resultingestionservice.application.model.MatchResultInput
import br.devrodrigues.resultingestionservice.domain.model.MatchesResult
import br.devrodrigues.resultingestionservice.domain.model.WebhookFallbackEvent
import br.devrodrigues.resultingestionservice.domain.port.out.MatchesResultPublisher
import br.devrodrigues.resultingestionservice.domain.port.out.WebhookFallbackRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service

@Service
class MatchResultIngestionService(
    private val mapper: MatchResultMapper,
    private val publisher: MatchesResultPublisher,
    private val webhookFallbackRepository: WebhookFallbackRepository,
    private val objectMapper: ObjectMapper,
    private val selfProvider: ObjectProvider<MatchResultIngestionService>
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun ingest(input: MatchResultInput): MatchesResultEvent {
        val event = mapper.toEvent(input)
        self().publishWithCircuitBreaker(event)
        return event
    }

    @CircuitBreaker(name = "matchesResultPublisher", fallbackMethod = "storeWebhookFallback")
    fun publishWithCircuitBreaker(event: MatchesResultEvent) {
        self().publishWithRetry(event)
    }

    @Retry(name = "matchesResultPublisher")
    fun publishWithRetry(event: MatchesResultEvent) {
        publisher.publish(
            MatchesResult(
                eventId = event.eventId,
                occurredAt = event.occurredAt,
                emittedAt = event.emittedAt,
                matchExternalId = event.matchExternalId,
                homeScore = event.homeScore,
                awayScore = event.awayScore,
                status = event.status,
                provider = event.provider,
            )
        )
    }

    private fun storeWebhookFallback(event: MatchesResultEvent, exception: Throwable) {
        runCatching {
            val payload = objectMapper.writeValueAsString(event)
            webhookFallbackRepository.save(WebhookFallbackEvent(payload = payload))
        }.onFailure { ex ->
            logger.error("Failed to persist webhook fallback for match ${event.matchExternalId}", ex)
        }
        logger.warn(
            "Circuit breaker opened after retries, stored webhook fallback for match {}",
            event.matchExternalId,
            exception
        )
    }

    private fun self(): MatchResultIngestionService = selfProvider.getObject()
}
