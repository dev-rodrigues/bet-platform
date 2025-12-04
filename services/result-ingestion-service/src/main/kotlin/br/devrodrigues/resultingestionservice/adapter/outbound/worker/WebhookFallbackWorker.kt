package br.devrodrigues.resultingestionservice.adapter.outbound.worker

import br.devrodrigues.resultingestionservice.application.service.MatchResultIngestionService
import br.devrodrigues.resultingestionservice.domain.model.WebhookFallbackEvent
import br.devrodrigues.resultingestionservice.domain.port.out.WebhookFallbackRepository
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
    @Value("\${app.outbox.batch-size:100}")
    private val batchSize: Int
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Scheduled(fixedDelayString = "\${app.outbox.publisher-delay-ms:1000}")
    @Transactional
    fun retryFailedWebhooks() {
        val events = webhookFallbackRepository
            .findPending(batchSize)
            .takeIf { it.isNotEmpty() }
            ?.mapNotNull(::retrySafely)
    }

    private fun retrySafely(events: WebhookFallbackEvent): UUID? =
        runCatching {
//            matchResultIngestionService.ingest(input = )
            events.id
        }.onFailure { ex ->
            logger.error("Erro ao reenviar webhook ${events.id}", ex)

        }.getOrNull()


}