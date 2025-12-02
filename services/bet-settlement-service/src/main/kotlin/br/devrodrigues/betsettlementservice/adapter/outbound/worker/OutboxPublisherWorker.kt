package br.devrodrigues.betsettlementservice.adapter.outbound.worker

import br.devrodrigues.betsettlementservice.domain.model.OutboxEvent
import br.devrodrigues.betsettlementservice.domain.port.out.OutboxEventRepository
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
    private val outboxEventRepository: OutboxEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${app.topics.payments-requested:payments.requested.v1}")
    private val paymentsRequestedTopic: String,
    @Value("\${app.outbox.batch-size:100}")
    private val outboxBatchSize: Int
) {

    @Scheduled(fixedDelayString = "\${app.outbox.publisher-delay-ms:2000}")
    @Transactional
    fun publishPendingWalletPayments() {
        val publishedIds = outboxEventRepository
            .findPendingWalletPayments(outboxBatchSize)
            .takeIf { it.isNotEmpty() }
            ?.mapNotNull(::publishSafely)
            .orEmpty()

        if (publishedIds.isNotEmpty()) {
            outboxEventRepository.markPublished(publishedIds)
        }
    }

    private fun publishSafely(event: OutboxEvent): UUID? =
        runCatching {
            publishEvent(event)
            event.id
        }.onFailure { ex ->
            logger.error("Erro ao publicar evento de pagamento ${event.id}", ex)
            outboxEventRepository.markError(event.id!!, ex.message ?: DEFAULT_ERROR_MESSAGE)
        }.getOrNull()

    private fun publishEvent(event: OutboxEvent) {
        if (event.aggregateType != "WALLET_PAYMENT_REQUEST") {
            logger.debug(
                "Ignorando evento de outbox id={} com aggregateType={}",
                event.id,
                event.aggregateType
            )
            return
        }

        val record = recordWithType(
            topic = paymentsRequestedTopic,
            key = event.aggregateId,
            payload = event.payload,
            targetType = String::class.java
        )

        kafkaTemplate.send(record).get()
    }

    private fun recordWithType(
        topic: String,
        key: String,
        payload: String,
        targetType: Class<*>
    ): ProducerRecord<String, String> {
        val record = ProducerRecord(topic, null, key, payload)
        record.headers().add(
            RecordHeader(
                AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME,
                targetType.name.toByteArray()
            )
        )
        return record
    }

    private companion object {
        private const val DEFAULT_ERROR_MESSAGE = "Erro ao publicar evento de pagamento"
        private val logger = LoggerFactory.getLogger(OutboxPublisherWorker::class.java)
    }
}

