package br.devrodrigues.betapiservice.service

import br.devrodrigues.betapiservice.domain.Bet
import br.devrodrigues.betapiservice.domain.BetStatus
import br.devrodrigues.betapiservice.repository.BetRepository
import br.devrodrigues.betapiservice.web.BetRequest
import org.springframework.stereotype.Service

@Service
class BetService(private val betRepository: BetRepository) {

    fun create(request: BetRequest): Bet {
        val bet = Bet(
            userId = request.userId,
            gameId = request.gameId,
            selection = request.selection,
            stake = request.stake,
            odds = request.odds,
            status = BetStatus.PENDING
        )
        return betRepository.save(bet)
    }
}
