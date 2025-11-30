package br.devrodrigues.betsettlementservice.domain.port.out

import br.devrodrigues.betsettlementservice.domain.model.SettlementJob

interface SettlementJobRepository {
    fun findByMatchId(matchId: Long): SettlementJob?
    fun save(job: SettlementJob): SettlementJob
}
