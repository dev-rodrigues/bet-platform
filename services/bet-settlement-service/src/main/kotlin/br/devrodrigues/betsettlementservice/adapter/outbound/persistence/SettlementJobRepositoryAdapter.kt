package br.devrodrigues.betsettlementservice.adapter.outbound.persistence

import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.SettlementJobJpaRepository
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.betsettlementservice.domain.model.SettlementJob
import br.devrodrigues.betsettlementservice.domain.port.out.SettlementJobRepository
import org.springframework.stereotype.Repository

@Repository
class SettlementJobRepositoryAdapter(
    private val settlementJobJpaRepository: SettlementJobJpaRepository
) : SettlementJobRepository {

    override fun findByMatchId(matchId: Long): SettlementJob? =
        settlementJobJpaRepository.findByMatchId(matchId)?.toDomain()

    override fun save(job: SettlementJob): SettlementJob =
        settlementJobJpaRepository.save(job.toEntity()).toDomain()
}
