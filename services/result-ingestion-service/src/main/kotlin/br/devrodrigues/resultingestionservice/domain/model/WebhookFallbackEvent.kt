package br.devrodrigues.resultingestionservice.domain.model

import java.time.Instant
import java.util.*

data class WebhookFallbackEvent(
    val id: UUID = UUID.randomUUID(),
    val payload: String,
    val status: WebhookFallbackStatus = WebhookFallbackStatus.PENDING,
    val createdAt: Instant = Instant.now()
)

enum class WebhookFallbackStatus {
    PENDING,
    RESENT,
    FAILED
}
