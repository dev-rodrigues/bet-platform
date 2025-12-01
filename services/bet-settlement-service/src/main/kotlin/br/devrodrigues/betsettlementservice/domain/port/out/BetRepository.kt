package br.devrodrigues.betsettlementservice.domain.port.out

import br.devrodrigues.betsettlementservice.domain.model.Bet

interface BetRepository {
    fun findById(id: Long): Bet?
    fun findPendingByGameId(gameId: Long, limit: Int): List<Bet>
    fun save(bet: Bet): Bet
}
