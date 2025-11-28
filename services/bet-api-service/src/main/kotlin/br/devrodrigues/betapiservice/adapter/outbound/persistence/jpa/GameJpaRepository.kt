package br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface GameJpaRepository : PagingAndSortingRepository<GameEntity, Long>, JpaRepository<GameEntity, Long> {
    fun findByExternalId(externalId: Long): GameEntity?
}
