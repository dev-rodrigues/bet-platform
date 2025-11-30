package br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SettlementJobJpaRepository : JpaRepository<SettlementJobEntity, Long> {
    fun findByMatchId(matchId: Long): SettlementJobEntity?
}
