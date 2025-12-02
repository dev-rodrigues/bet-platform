package br.devrodrigues.betsettlementservice.application.service

import br.devrodrigues.betsettlementservice.domain.model.Bet
import br.devrodrigues.betsettlementservice.domain.model.Game
import br.devrodrigues.betsettlementservice.domain.port.out.BetRepository
import br.devrodrigues.betsettlementservice.domain.port.out.SettlementJobRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

enum class BetOutcome { WON, LOST, VOID }

@Service
class SettlementWorkerService(
    private val settlementJobRepository: SettlementJobRepository,
    private val betRepository: BetRepository
) {
    private val logger = LoggerFactory.getLogger(SettlementWorkerService::class.java)

    @Transactional
    fun runOnce() {
        val job = settlementJobRepository.findNextPendingForUpdate()

        if (job == null) {
            logger.debug("No pending settlement jobs to process")
            return
        }

        val game = job.game
        if (game == null) {
            logger.warn("Pending settlement job id={} without loaded game reference", job.id)
            return
        }

        val runningJob = job.copy(
            status = "RUNNING",
            updatedAt = Instant.now()
        )
        settlementJobRepository.save(runningJob)

        logger.info("Marked settlement job id={} as RUNNING (gameId={})", job.id, game.id)

        processPendingBets(
            matchId = job.matchId,
            batchSize = job.batchSize,
            jobId = job.id!!,
            game = game
        )
    }

    private fun processPendingBets(matchId: Long, batchSize: Int, jobId: Long, game: Game) {
        val pendingBets = betRepository.findPendingByGameId(matchId, batchSize)
        if (pendingBets.isEmpty()) {
            logger.info("No pending bets to process for matchId={}", matchId)
            settlementJobRepository.save(
                settlementJobRepository.findByMatchId(matchId)!!.copy(
                    status = "FINISHED",
                    updatedAt = Instant.now()
                )
            )
            logger.info("Marked settlement job id={} as FINISHED", jobId)
            return
        }

        val payoutsByUser = mutableMapOf<Long, BigDecimal>()

        logger.info(
            "Fetched {} pending bets for settlement matchId={} (batchSize={})",
            pendingBets.size,
            matchId,
            batchSize
        )

        pendingBets.forEach { pending ->
            val outcome = resolveBetOutcome(game, pending)
            val payout = calculatePayout(pending, outcome)

            val updated = pending.copy(
                status = outcome.name,
                payout = payout,
                updatedAt = Instant.now()
            )

            betRepository.save(updated)

            if (outcome == BetOutcome.WON || outcome == BetOutcome.VOID) {
                payoutsByUser[pending.userId] =
                    payoutsByUser.getOrDefault(pending.userId, BigDecimal.ZERO) + payout
            }

        }
    }

    private fun resolveBetOutcome(game: Game, bet: Bet): BetOutcome {
        if (game.status == "CANCELED") {
            return BetOutcome.VOID
        }

        val home = game.homeScore!!
        val away = game.awayScore!!

        return when (bet.selection) {
            "HOME_WIN" -> if (home > away) BetOutcome.WON else BetOutcome.LOST
            "AWAY_WIN" -> if (away > home) BetOutcome.WON else BetOutcome.LOST
            "DRAW" -> if (home == away) BetOutcome.WON else BetOutcome.LOST
            else -> throw IllegalArgumentException("Unknown selection: ${bet.selection}")
        }
    }

    fun calculatePayout(bet: Bet, outcome: BetOutcome): BigDecimal {
        return when (outcome) {
            BetOutcome.WON -> bet.stake.multiply(bet.odds)
            BetOutcome.LOST -> BigDecimal.ZERO
            BetOutcome.VOID -> bet.stake
        }
    }
}
