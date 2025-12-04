package br.devrodrigues.resultingestionservice.adapter.outbound.worker

import jakarta.transaction.Transactional
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("worker")
class WebhookFallbackWorker(
) {

    @Scheduled(fixedDelayString = "\${app.outbox.publisher-delay-ms:1000}")
    @Transactional
    fun retryFailedWebhooks() {
    }
}