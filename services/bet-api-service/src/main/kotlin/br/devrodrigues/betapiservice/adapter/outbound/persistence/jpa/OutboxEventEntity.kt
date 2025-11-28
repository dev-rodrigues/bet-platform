package br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa

import br.devrodrigues.betapiservice.domain.model.OutboxEvent
import br.devrodrigues.betapiservice.domain.model.OutboxStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "outbox_event")
data class OutboxEventEntity(
    @Id
    val id: UUID,
    @Column(nullable = false, length = 64)
    val aggregateType: String,
    @Column(nullable = false, length = 64)
    val aggregateId: String,
    @Column(nullable = false, length = 64)
    val type: String,
    @Column(nullable = false, columnDefinition = "text")
    val payload: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    val status: OutboxStatus,
    @Column(nullable = false)
    val createdAt: Instant,
    val processedAt: Instant? = null,
    @Column(columnDefinition = "text")
    val lastError: String? = null
)

fun OutboxEvent.toEntity(): OutboxEventEntity =
    OutboxEventEntity(
        id = id,
        aggregateType = aggregateType,
        aggregateId = aggregateId,
        type = type,
        payload = payload,
        status = status,
        createdAt = createdAt,
        processedAt = processedAt,
        lastError = lastError
    )

fun OutboxEventEntity.toDomain(): OutboxEvent =
    OutboxEvent(
        id = id,
        aggregateType = aggregateType,
        aggregateId = aggregateId,
        type = type,
        payload = payload,
        status = status,
        createdAt = createdAt,
        processedAt = processedAt,
        lastError = lastError
    )
