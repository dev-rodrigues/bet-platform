package br.devrodrigues.betsettlementservice.application.service

import br.devrodrigues.betsettlementservice.domain.port.out.GameRepository
import br.devrodrigues.commonevents.MatchesResultEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MatchResultService(
    private val gameRepository: GameRepository
) {

    private val logger = LoggerFactory.getLogger(MatchResultService::class.java)

    @Transactional
    fun applyResult(event: MatchesResultEvent) {
        val game = gameRepository.findByExternalId(event.matchExternalId.toLong())
        if (game == null) {
            logger.warn(
                "Game not found to apply result eventId={} matchExternalId={}",
                event.eventId,
                event.matchExternalId
            )
            return
        }

        val updated = game.copy(
            homeScore = event.homeScore,
            awayScore = event.awayScore,
            status = event.status,
            updatedAt = event.emittedAt
        )
        gameRepository.save(updated)

        logger.info(
            "Applied match result eventId={} matchExternalId={} status={}",
            event.eventId,
            event.matchExternalId,
            event.status
        )
    }
}
