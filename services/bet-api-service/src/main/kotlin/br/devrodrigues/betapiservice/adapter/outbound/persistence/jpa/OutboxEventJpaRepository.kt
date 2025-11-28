package br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa

import br.devrodrigues.betapiservice.domain.model.OutboxStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface OutboxEventJpaRepository : JpaRepository<OutboxEventEntity, UUID> {
    fun findByStatusOrderByCreatedAtAsc(status: OutboxStatus, pageable: Pageable): List<OutboxEventEntity>

    @Modifying
    @Query("update OutboxEventEntity e set e.status = 'PUBLISHED', e.processedAt = CURRENT_TIMESTAMP where e.id in :ids")
    fun markPublished(ids: List<UUID>)

    @Modifying
    @Query("update OutboxEventEntity e set e.status = 'ERROR', e.lastError = :error, e.processedAt = CURRENT_TIMESTAMP where e.id = :id")
    fun markError(id: UUID, error: String)
}
