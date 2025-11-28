package br.devrodrigues.betapiservice.adapter.outbound.persistence

import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.BetJpaRepository
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.betapiservice.domain.model.Bet
import br.devrodrigues.betapiservice.domain.port.out.BetRepository
import org.springframework.stereotype.Repository

@Repository
class BetRepositoryImpl(
    private val betJpaRepository: BetJpaRepository
) : BetRepository {

    override fun save(bet: Bet): Bet {
        val saved = betJpaRepository.save(bet.toEntity())
        return saved.toDomain()
    }
}
