package br.devrodrigues.resultingestionservice.adapter.outbound.worker

import br.devrodrigues.commonevents.MatchesResultEvent
import br.devrodrigues.resultingestionservice.application.model.MatchResultInput
import br.devrodrigues.resultingestionservice.application.service.MatchResultIngestionService
import br.devrodrigues.resultingestionservice.domain.model.WebhookFallbackEvent
import br.devrodrigues.resultingestionservice.domain.model.WebhookFallbackStatus
import br.devrodrigues.resultingestionservice.domain.port.out.WebhookFallbackRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("worker")
class WebhookFallbackWorker(
    private val matchResultIngestionService: MatchResultIngestionService,
    private val webhookFallbackRepository: WebhookFallbackRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${app.outbox.batch-size:100}")
    private val batchSize: Int
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Scheduled(fixedDelayString = "\${app.outbox.publisher-delay-ms:1000}")
    @Transactional
    fun retryFailedWebhooks() {
        webhookFallbackRepository
            .findPending(batchSize)
            .forEach { retrySafely(it) }
    }

    private fun retrySafely(event: WebhookFallbackEvent): UUID? =
        runCatching {
            val deserialized = objectMapper.readValue(event.payload, MatchesResultEvent::class.java)
            val input = MatchResultInput(
                matchExternalId = deserialized.matchExternalId,
                homeScore = deserialized.homeScore,
                awayScore = deserialized.awayScore,
                status = deserialized.status,
                providerEventId = deserialized.provider
            )

            matchResultIngestionService.ingest(input)
            webhookFallbackRepository.save(event.copy(status = WebhookFallbackStatus.RESENT)).id
        }.onFailure { ex ->
            logger.error("Erro ao reenviar webhook ${event.id}", ex)
            webhookFallbackRepository.save(event.copy(status = WebhookFallbackStatus.PENDING)).id
        }.getOrNull()
}
