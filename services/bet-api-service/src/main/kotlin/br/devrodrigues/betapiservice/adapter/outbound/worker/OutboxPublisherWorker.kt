package br.devrodrigues.betapiservice.adapter.outbound.worker

import br.devrodrigues.betapiservice.config.AppProperties
import br.devrodrigues.betapiservice.domain.model.OutboxEvent
import br.devrodrigues.betapiservice.domain.port.out.OutboxRepository
import br.devrodrigues.commonevents.BetPlacedEvent
import br.devrodrigues.commonevents.GameCreatedEvent
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.mapping.AbstractJavaTypeMapper
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

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
                recordWithType(
                    topic = appProperties.topics.betPlaced,
                    key = event.aggregateId,
                    payload = event.payload,
                    targetType = BetPlacedEvent::class.java
                )
            ).get()

            "GAME_CREATED" -> kafkaTemplate.send(
                recordWithType(
                    topic = appProperties.topics.gameCreated,
                    key = event.aggregateId,
                    payload = event.payload,
                    targetType = GameCreatedEvent::class.java
                )
            ).get()

            else -> logger.warn("Tipo de evento desconhecido ignorado: {}", event.type)
        }

    private fun recordWithType(
        topic: String,
        key: String,
        payload: String,
        targetType: Class<*>
    ): ProducerRecord<String, String> {
        val record = ProducerRecord(topic, null, key, payload)
        record.headers().add(
            RecordHeader(AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME, targetType.name.toByteArray())
        )
        return record
    }

    private companion object {
        private const val DEFAULT_ERROR_MESSAGE = "Erro ao publicar evento"
        private val logger = LoggerFactory.getLogger(OutboxPublisherWorker::class.java)
    }
}
