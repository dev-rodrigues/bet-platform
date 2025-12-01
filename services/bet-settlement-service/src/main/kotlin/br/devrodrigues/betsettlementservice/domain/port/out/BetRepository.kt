package br.devrodrigues.betsettlementservice.domain.port.out

import br.devrodrigues.betsettlementservice.domain.model.Bet

interface BetRepository {
    fun findById(id: Long): Bet?
    fun save(bet: Bet): Bet
}
