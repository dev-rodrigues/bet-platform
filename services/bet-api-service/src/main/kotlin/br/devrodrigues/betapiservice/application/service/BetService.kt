package br.devrodrigues.betapiservice.application.service

import br.devrodrigues.betapiservice.application.service.dto.CreateBetCommand
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
    fun create(command: CreateBetCommand): Bet {
        val game = betValidator.validateAndGetGame(command)
        val bet = Bet(
            userId = command.userId,
            gameId = requireNotNull(game.id) { "Game id is required" },
            selection = command.selection,
            stake = command.stake,
            odds = command.odds,
            status = BetStatus.PENDING
        )
        val saved = betRepository.save(bet)
        outboxService.saveBetPlacedEvent(saved, game.externalId)
        return saved
    }
}
