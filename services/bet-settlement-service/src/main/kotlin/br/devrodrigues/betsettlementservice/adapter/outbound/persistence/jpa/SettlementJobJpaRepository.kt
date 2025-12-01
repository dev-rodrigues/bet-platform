package br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SettlementJobJpaRepository : JpaRepository<SettlementJobEntity, Long> {
    fun findByMatchId(matchId: Long): SettlementJobEntity?

    @Query(
        value = "SELECT * FROM settlement_job WHERE status = 'PENDING' LIMIT 1 FOR UPDATE SKIP LOCKED",
        nativeQuery = true
    )
    fun findNextPendingForUpdate(): SettlementJobEntity?
}
