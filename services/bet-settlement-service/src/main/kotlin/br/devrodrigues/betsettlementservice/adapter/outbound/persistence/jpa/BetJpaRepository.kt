package br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BetJpaRepository : JpaRepository<BetEntity, Long> {
    fun findByGameIdAndStatus(gameId: Long, status: String): List<BetEntity>
}
