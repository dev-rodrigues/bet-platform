package br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OutboxEventRepository : JpaRepository<OutboxEventEntity, UUID> {

    @Query(
        value = """
            select *
            from outbox_event
            where aggregate_type = :aggregateType
              and status = :status
            order by id
            limit :limit
            for update skip locked
        """,
        nativeQuery = true
    )
    fun findPendingWalletPaymentsForUpdate(
        aggregateType: String,
        status: String,
        limit: Int
    ): List<OutboxEventEntity>

    @Modifying
    @Query(
        value = """
            update outbox_event
            set status = 'PUBLISHED'
            where id in (:ids)
        """,
        nativeQuery = true
    )
    fun markPublished(ids: List<UUID>)

    @Modifying
    @Query(
        value = """
            update outbox_event
            set status = 'ERROR'
            where id = :id
        """,
        nativeQuery = true
    )
    fun markError(id: UUID)
}
