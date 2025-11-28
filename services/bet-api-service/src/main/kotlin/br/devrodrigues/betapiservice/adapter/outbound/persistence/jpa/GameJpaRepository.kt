package br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface GameJpaRepository : JpaRepository<GameEntity, Long> {
    fun findByExternalId(externalId: Long): GameEntity?
}
