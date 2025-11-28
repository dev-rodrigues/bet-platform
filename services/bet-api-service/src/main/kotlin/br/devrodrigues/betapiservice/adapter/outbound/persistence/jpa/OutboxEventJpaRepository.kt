package br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa

import br.devrodrigues.betapiservice.domain.model.OutboxStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface OutboxEventJpaRepository : JpaRepository<OutboxEventEntity, UUID> {
    fun findTop50ByStatusOrderByCreatedAtAsc(status: OutboxStatus): List<OutboxEventEntity>

    @Modifying
    @Query("update OutboxEventEntity e set e.status = 'SENT', e.processedAt = CURRENT_TIMESTAMP where e.id in :ids")
    fun markSent(ids: List<UUID>)

    @Modifying
    @Query("update OutboxEventEntity e set e.status = 'FAILED', e.lastError = :error, e.processedAt = CURRENT_TIMESTAMP where e.id = :id")
    fun markFailed(id: UUID, error: String)
}
