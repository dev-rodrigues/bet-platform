package br.devrodrigues.resultingestionservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface WebhookFallbackEventJpaRepository : JpaRepository<WebhookFallbackEventEntity, UUID> {

    @Query(
        value = """
            select *
            from webhook_events_fallback
            where status = 'PENDING'
            order by created_at asc
            limit :limit
            for update skip locked
        """,
        nativeQuery = true
    )
    fun findPendingForUpdate(limit: Int): List<WebhookFallbackEventEntity>
}
