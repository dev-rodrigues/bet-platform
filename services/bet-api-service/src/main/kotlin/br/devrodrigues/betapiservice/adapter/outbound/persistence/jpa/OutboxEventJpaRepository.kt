package br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.*

interface OutboxEventJpaRepository : JpaRepository<OutboxEventEntity, UUID> {
    @Query(
        value = """
            select *
            from outbox_event
            where status = :status
            order by created_at asc
            limit :limit
            for update skip locked
        """,
        nativeQuery = true
    )
    fun findPendingForUpdate(status: String, limit: Int): List<OutboxEventEntity>

    @Modifying
    @Query("update OutboxEventEntity e set e.status = 'PUBLISHED', e.processedAt = :processedAt where e.id in :ids")
    fun markPublished(ids: List<UUID>, processedAt: Instant)

    @Modifying
    @Query("update OutboxEventEntity e set e.status = 'ERROR', e.lastError = :error, e.processedAt = :processedAt where e.id = :id")
    fun markError(id: UUID, error: String, processedAt: Instant)
}
