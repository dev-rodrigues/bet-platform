package br.devrodrigues.betapiservice.domain.port.out

import br.devrodrigues.betapiservice.domain.model.Bet

interface BetRepository {
    fun save(bet: Bet): Bet
}
