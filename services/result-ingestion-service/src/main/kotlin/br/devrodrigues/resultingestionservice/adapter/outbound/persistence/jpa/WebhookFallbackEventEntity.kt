package br.devrodrigues.resultingestionservice.adapter.outbound.persistence.jpa

import br.devrodrigues.resultingestionservice.domain.model.WebhookFallbackEvent
import br.devrodrigues.resultingestionservice.domain.model.WebhookFallbackStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.*

@Entity
@Table(name = "webhook_events_fallback")
data class WebhookFallbackEventEntity(
    @Id
    val id: UUID,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    val payload: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    val status: WebhookFallbackStatus,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant
)

fun WebhookFallbackEvent.toEntity(): WebhookFallbackEventEntity =
    WebhookFallbackEventEntity(
        id = id,
        payload = payload,
        status = status,
        createdAt = createdAt
    )

fun WebhookFallbackEventEntity.toDomain(): WebhookFallbackEvent =
    WebhookFallbackEvent(
        id = id,
        payload = payload,
        status = status,
        createdAt = createdAt
    )
