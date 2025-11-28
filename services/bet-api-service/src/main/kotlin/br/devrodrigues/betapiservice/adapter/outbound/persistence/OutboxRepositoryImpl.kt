package br.devrodrigues.betapiservice.adapter.outbound.persistence

import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.OutboxEventJpaRepository
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.betapiservice.domain.model.OutboxEvent
import br.devrodrigues.betapiservice.domain.model.OutboxStatus
import br.devrodrigues.betapiservice.domain.port.out.OutboxRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class OutboxRepositoryImpl(
    private val outboxEventJpaRepository: OutboxEventJpaRepository
) : OutboxRepository {

    @Transactional
    override fun save(event: OutboxEvent): OutboxEvent =
        outboxEventJpaRepository.save(event.toEntity()).toDomain()

    override fun findPending(limit: Int): List<OutboxEvent> =
        outboxEventJpaRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)
            .take(limit)
            .map { it.toDomain() }

    @Transactional
    override fun markSent(eventIds: List<UUID>) {
        if (eventIds.isNotEmpty()) {
            outboxEventJpaRepository.markSent(eventIds)
        }
    }

    @Transactional
    override fun markFailed(eventId: UUID, error: String) {
        outboxEventJpaRepository.markFailed(eventId, error.take(2048))
    }
}
