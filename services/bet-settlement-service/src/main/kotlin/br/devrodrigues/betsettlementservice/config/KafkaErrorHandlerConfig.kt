package br.devrodrigues.betsettlementservice.config

import org.apache.kafka.common.TopicPartition
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaErrorHandlerConfig(
    @Value("\${app.topics.matches-result}") private val matchesResultTopic: String,
    @Value("\${app.topics.matches-result-dlq:matches.result.dlq}") private val matchesResultDlqTopic: String
) {

    @Bean
    fun kafkaErrorHandler(kafkaTemplate: KafkaTemplate<Any, Any>): DefaultErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { record, _ ->
            val targetTopic =
                if (record.topic() == matchesResultTopic) matchesResultDlqTopic else "${record.topic()}.dlq"
            TopicPartition(targetTopic, record.partition())
        }

        return DefaultErrorHandler(recoverer, FixedBackOff(0L, 0L))
    }
}
