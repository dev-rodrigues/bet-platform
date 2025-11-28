package br.devrodrigues.betapiservice.adapter.outbound.messaging

import br.devrodrigues.betapiservice.domain.model.OutboxEvent
import br.devrodrigues.betapiservice.config.AppProperties
import br.devrodrigues.betapiservice.domain.port.out.OutboxRepository
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("worker")
class OutboxPublisherWorker(
    private val outboxRepository: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val appProperties: AppProperties
) {

    @Scheduled(fixedDelayString = "\${app.outbox.publisher-delay-ms:2000}")
    @Transactional
    fun publishPending() {
        val publishedIds = outboxRepository
            .findPending(appProperties.outbox.batchSize)
            .takeIf { it.isNotEmpty() }
            ?.mapNotNull(::publishSafely)
            .orEmpty()

        if (publishedIds.isNotEmpty()) outboxRepository.markPublished(publishedIds)
    }

    private fun publishSafely(event: OutboxEvent): UUID? =
        runCatching {
            publishEvent(event)
            event.id
        }.onFailure { ex ->
            logger.error("Erro ao publicar evento ${event.id}", ex)
            outboxRepository.markError(event.id, ex.message ?: DEFAULT_ERROR_MESSAGE)
        }.getOrNull()

    private fun publishEvent(event: OutboxEvent) =
        when (event.type) {
            "BET_PLACED" -> kafkaTemplate.send(
                appProperties.topics.betPlaced,
                event.aggregateId,
                event.payload
            ).get()

            else -> logger.warn("Tipo de evento desconhecido ignorado: {}", event.type)
        }

    private companion object {
        private const val DEFAULT_ERROR_MESSAGE = "Erro ao publicar evento"
        private val logger = LoggerFactory.getLogger(OutboxPublisherWorker::class.java)
    }
}
