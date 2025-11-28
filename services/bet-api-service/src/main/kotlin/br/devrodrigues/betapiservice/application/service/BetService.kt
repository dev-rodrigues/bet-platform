package br.devrodrigues.betapiservice.application.service

import br.devrodrigues.betapiservice.adapter.inbound.web.dto.BetRequestDto
import br.devrodrigues.betapiservice.application.service.OutboxService
import br.devrodrigues.betapiservice.application.validation.BetValidator
import br.devrodrigues.betapiservice.domain.model.Bet
import br.devrodrigues.betapiservice.domain.model.BetStatus
import br.devrodrigues.betapiservice.domain.port.out.BetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BetService(
    private val betRepository: BetRepository,
    private val betValidator: BetValidator,
    private val outboxService: OutboxService
) {

    @Transactional
    fun create(request: BetRequestDto): Bet {
        val game = betValidator.validateAndGetGame(request)
        val bet = Bet(
            userId = request.userId,
            gameId = requireNotNull(game.id) { "Game id is required" },
            selection = request.selection,
            stake = request.stake,
            odds = request.odds,
            status = BetStatus.PENDING
        )
        val saved = betRepository.save(bet)
        outboxService.saveBetPlacedEvent(saved, game.externalId)
        return saved
    }
}
