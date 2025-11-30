package br.devrodrigues.betsettlementservice.application.service

import br.devrodrigues.betsettlementservice.application.validation.MissingGameForResultException
import br.devrodrigues.betsettlementservice.domain.model.SettlementJob
import br.devrodrigues.betsettlementservice.domain.port.out.GameRepository
import br.devrodrigues.betsettlementservice.domain.port.out.SettlementJobRepository
import br.devrodrigues.commonevents.MatchesResultEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MatchResultService(
    private val gameRepository: GameRepository,
    private val settlementJobRepository: SettlementJobRepository
) {

    private val logger = LoggerFactory.getLogger(MatchResultService::class.java)

    @Transactional
    fun applyResult(event: MatchesResultEvent) {
        val game = gameRepository.findByExternalId(event.matchExternalId.toLong())
        if (game == null) {
            logger.error(
                "Game not found to apply result (policy=DLQ) eventId={} matchExternalId={}",
                event.eventId,
                event.matchExternalId
            )
            throw MissingGameForResultException(event.eventId, event.matchExternalId)
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

        createSettlementJobIfAbsent(updated.id!!, event)
    }

    private fun createSettlementJobIfAbsent(matchId: Long, event: MatchesResultEvent) {
        val existingJob = settlementJobRepository.findByMatchId(matchId)
        if (existingJob != null) {
            logger.info(
                "Settlement job already exists eventId={} matchExternalId={}",
                event.eventId,
                event.matchExternalId
            )
            return
        }

        val job = SettlementJob(
            matchId = matchId,
            externalMatchId = event.matchExternalId,
            status = STATUS_PENDING,
            batchSize = DEFAULT_BATCH_SIZE,
            createdAt = event.emittedAt,
            updatedAt = event.emittedAt,
            lastError = null
        )
        settlementJobRepository.save(job)

        logger.info(
            "Created settlement job eventId={} matchExternalId={} status={}",
            event.eventId,
            event.matchExternalId,
            STATUS_PENDING
        )
    }

    private companion object {
        private const val STATUS_PENDING = "PENDING"
        private const val DEFAULT_BATCH_SIZE = 1000
    }
}
