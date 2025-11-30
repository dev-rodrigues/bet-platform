package br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GameJpaRepository : JpaRepository<GameEntity, Long> {
    fun findByExternalId(externalId: Long): GameEntity?
}
