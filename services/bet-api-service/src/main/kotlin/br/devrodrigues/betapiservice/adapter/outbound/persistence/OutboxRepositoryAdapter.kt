package br.devrodrigues.betapiservice.adapter.outbound.persistence

import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.OutboxEventJpaRepository
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.betapiservice.domain.model.OutboxEvent
import br.devrodrigues.betapiservice.domain.model.OutboxStatus
import br.devrodrigues.betapiservice.domain.port.out.OutboxRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Repository
class OutboxRepositoryAdapter(
    private val outboxEventJpaRepository: OutboxEventJpaRepository
) : OutboxRepository {

    @Transactional
    override fun save(event: OutboxEvent): OutboxEvent =
        outboxEventJpaRepository.save(event.toEntity()).toDomain()

    override fun findPending(limit: Int): List<OutboxEvent> =
        outboxEventJpaRepository
            .findPendingForUpdate(OutboxStatus.PENDING.name, limit)
            .map { it.toDomain() }

    @Transactional
    override fun markPublished(eventIds: List<UUID>) {
        if (eventIds.isNotEmpty()) {
            val processedAt = Instant.now()
            outboxEventJpaRepository.markPublished(
                ids = eventIds,
                processedAt = processedAt
            )
        }
    }

    @Transactional
    override fun markError(eventId: UUID, error: String) {
        outboxEventJpaRepository.markError(
            id = eventId,
            error = error.take(2048),
            processedAt = Instant.now()
        )
    }
}
