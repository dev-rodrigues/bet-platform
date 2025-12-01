package br.devrodrigues.betsettlementservice.adapter.outbound.persistence

import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.BetJpaRepository
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.betsettlementservice.domain.model.Bet
import br.devrodrigues.betsettlementservice.domain.port.out.BetRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class BetRepositoryAdapter(
    private val betJpaRepository: BetJpaRepository
) : BetRepository {

    override fun findById(id: Long): Bet? =
        betJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(bet: Bet): Bet =
        betJpaRepository.save(bet.toEntity()).toDomain()
}
