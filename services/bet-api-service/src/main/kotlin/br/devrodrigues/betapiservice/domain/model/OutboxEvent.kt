package br.devrodrigues.betapiservice.domain.model

import java.time.Instant
import java.util.UUID

data class OutboxEvent(
    val id: UUID = UUID.randomUUID(),
    val aggregateType: String,
    val aggregateId: String,
    val type: String,
    val payload: String,
    val status: OutboxStatus = OutboxStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val processedAt: Instant? = null,
    val lastError: String? = null
)

enum class OutboxStatus {
    PENDING,
    SENT,
    FAILED
}
