package br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BetJpaRepository : JpaRepository<BetEntity, Long> {

    @Query(
        value = """
            SELECT * FROM bet
            WHERE game_id = :gameId
              AND status = 'PENDING'
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true
    )
    fun findPendingByGameIdForUpdate(gameId: Long, limit: Int): List<BetEntity>
}
