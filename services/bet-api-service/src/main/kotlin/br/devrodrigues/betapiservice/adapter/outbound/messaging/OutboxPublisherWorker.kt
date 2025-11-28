package br.devrodrigues.betapiservice.adapter.outbound.messaging

import br.devrodrigues.betapiservice.domain.model.OutboxEvent
import br.devrodrigues.betapiservice.config.AppProperties
import br.devrodrigues.betapiservice.domain.port.out.OutboxRepository
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

    private val logger = LoggerFactory.getLogger(OutboxPublisherWorker::class.java)

    @Scheduled(fixedDelayString = "\${app.outbox.publisher-delay-ms:2000}")
    @Transactional
    fun publishPending() {
        val pending = outboxRepository.findPending(appProperties.outbox.batchSize)
        if (pending.isEmpty()) {
            return
        }

        val publishedIds = mutableListOf<java.util.UUID>()
        pending.forEach { event ->
            try {
                publishEvent(event)
                publishedIds.add(event.id)
            } catch (ex: Exception) {
                logger.error("Erro ao publicar evento ${event.id}", ex)
                outboxRepository.markError(event.id, ex.message ?: "Erro ao publicar evento")
            }
        }

        if (publishedIds.isNotEmpty()) {
            outboxRepository.markPublished(publishedIds)
        }
    }

    private fun publishEvent(event: OutboxEvent) {
        when (event.type) {
            "BET_PLACED" -> kafkaTemplate.send(appProperties.topics.betPlaced, event.aggregateId, event.payload).get()
            else -> logger.warn("Tipo de evento desconhecido ignorado: {}", event.type)
        }
    }
}
