package br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa

import br.devrodrigues.betsettlementservice.domain.model.OutboxEvent
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "outbox_event",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_outbox_event_reference_id", columnNames = ["reference_id"])
    ]
)
data class OutboxEventEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID? = null,
    @Column(name = "aggregate_type", nullable = false, length = 64)
    val aggregateType: String,
    @Column(name = "aggregate_id", nullable = false, length = 64)
    val aggregateId: String,
    @Column(name = "event_type", nullable = false, length = 64)
    val eventType: String,
    @Column(name = "payload", nullable = false, columnDefinition = "text")
    val payload: String,
    @Column(name = "status", nullable = false, length = 32)
    val status: String,
    @Column(name = "reference_id", nullable = false, length = 128)
    val referenceId: String
)

fun OutboxEventEntity.toDomain(): OutboxEvent =
    OutboxEvent(
        id = requireNotNull(id),
        aggregateType = aggregateType,
        aggregateId = aggregateId,
        eventType = eventType,
        payload = payload,
        status = status,
        referenceId = referenceId
    )

fun OutboxEvent.toEntity(): OutboxEventEntity =
    OutboxEventEntity(
        id = id,
        aggregateType = aggregateType,
        aggregateId = aggregateId,
        eventType = eventType,
        payload = payload,
        status = status,
        referenceId = referenceId
    )