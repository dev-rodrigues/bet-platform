package br.devrodrigues.resultingestionservice.application.port.out

import br.devrodrigues.resultingestionservice.domain.model.WebhookFallbackEvent

interface WebhookFallbackRepository {
    fun save(event: WebhookFallbackEvent): WebhookFallbackEvent
    fun findPending(limit: Int): List<WebhookFallbackEvent>
}
