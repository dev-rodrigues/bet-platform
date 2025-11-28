package br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface BetJpaRepository : JpaRepository<BetEntity, Long>
