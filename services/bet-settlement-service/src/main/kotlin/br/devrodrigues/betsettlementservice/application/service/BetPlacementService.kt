package br.devrodrigues.betsettlementservice.application.service

import br.devrodrigues.betsettlementservice.application.validation.MissingGameForBetPlacementException
import br.devrodrigues.betsettlementservice.domain.model.Bet
import br.devrodrigues.betsettlementservice.domain.port.out.BetRepository
import br.devrodrigues.betsettlementservice.domain.port.out.GameRepository
import br.devrodrigues.commonevents.BetPlacedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BetPlacementService(
    private val betRepository: BetRepository,
    private val gameRepository: GameRepository
) {

    private val logger = LoggerFactory.getLogger(BetPlacementService::class.java)

    @Transactional
    fun upsert(event: BetPlacedEvent) {
        val consistentGameId = resolveGameId(event)
        val existing = betRepository.findById(event.id)

        if (existing == null) {
            val bet = Bet(
                id = event.id,
                userId = event.userId,
                gameId = consistentGameId,
                gameExternalId = event.gameExternalId.toString(),
                selection = event.selection,
                stake = event.stake,
                odds = event.odds,
                status = STATUS_PENDING,
                payout = null,
                createdAt = event.createdAt,
                updatedAt = event.createdAt
            )
            betRepository.save(bet)

            logger.info(
                "Created bet from BetPlaced id={} gameId={} externalId={}",
                event.id,
                consistentGameId,
                event.gameExternalId
            )
            return
        }

        val updated = existing.copy(
            userId = event.userId,
            gameId = consistentGameId,
            gameExternalId = event.gameExternalId.toString(),
            selection = event.selection,
            stake = event.stake,
            odds = event.odds,
            status = event.status,
            updatedAt = event.createdAt
        )
        betRepository.save(updated)

        logger.info(
            "Updated bet from BetPlaced id={} gameId={} externalId={} status={}",
            event.id,
            consistentGameId,
            event.gameExternalId,
            event.status
        )
    }

    private fun resolveGameId(event: BetPlacedEvent): Long {
        val game = gameRepository.findByExternalId(event.gameExternalId)
            ?: throw MissingGameForBetPlacementException(event.id, event.gameExternalId)

        val persistedGameId = requireNotNull(game.id) {
            "Game id must not be null for externalId=${event.gameExternalId}"
        }

        if (persistedGameId != event.gameId) {
            logger.info(
                "Adjusting bet gameId to match persisted game id persistedGameId={} providedGameId={} externalId={}",
                persistedGameId,
                event.gameId,
                event.gameExternalId
            )
        }

        return persistedGameId
    }

    private companion object {
        private const val STATUS_PENDING = "PENDING"
    }
}
