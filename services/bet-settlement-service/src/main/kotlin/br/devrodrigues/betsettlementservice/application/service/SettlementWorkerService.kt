package br.devrodrigues.betsettlementservice.application.service

import br.devrodrigues.betsettlementservice.domain.model.Bet
import br.devrodrigues.betsettlementservice.domain.model.Game
import br.devrodrigues.betsettlementservice.domain.model.OutboxEvent
import br.devrodrigues.betsettlementservice.domain.port.out.BetRepository
import br.devrodrigues.betsettlementservice.domain.port.out.OutboxEventRepository
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
    private val betRepository: BetRepository,
    private val outboxEventRepository: OutboxEventRepository,
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

        var runningJob = job.copy(
            status = "RUNNING",
            updatedAt = Instant.now()
        )

        runningJob = settlementJobRepository.save(runningJob)

        logger.info("Marked settlement job id={} as RUNNING (gameId={})", job.id, game.id)

        processPendingBets(
            matchId = job.matchId,
            batchSize = job.batchSize,
            jobId = job.id!!,
            game = game
        )

        settlementJobRepository.save(
            runningJob.copy(
                status = "FINISHED",
                updatedAt = Instant.now()
            )
        )

        logger.info("Marked settlement job id={} as FINISHED", job.id)
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

        if (payoutsByUser.isNotEmpty()) {
            val outboxEvents = resolvePaymentsByUser(
                payoutsByUser = payoutsByUser,
                game = game,
                matchId = matchId,
                jobId = jobId
            )

            outboxEventRepository.saveAll(outboxEvents)
        }


    }

    private fun resolvePaymentsByUser(
        matchId: Long,
        payoutsByUser: Map<Long, BigDecimal>,
        game: Game,
        jobId: Long
    ): List<OutboxEvent> {
        if (payoutsByUser.isEmpty()) {
            return emptyList()
        }

        val createdAt = Instant.now()

        return payoutsByUser.map { (userId, totalAmount) ->
            val paymentRequestId = "MATCH${matchId}_USER${userId}_BATCH-${jobId}"

            val payload = buildPaymentRequestedPayload(
                paymentRequestId = paymentRequestId,
                createdAt = createdAt,
                userId = userId,
                totalAmount = totalAmount,
                matchExternalId = game.externalId.toString()
            )

            OutboxEvent(
                aggregateType = "WALLET_PAYMENT_REQUEST",
                aggregateId = userId.toString(),
                eventType = "payments.requested.v1",
                payload = payload,
                status = "PENDING",
                referenceId = paymentRequestId
            )
        }
    }

    private fun buildPaymentRequestedPayload(
        paymentRequestId: String,
        createdAt: Instant,
        userId: Long,
        totalAmount: BigDecimal,
        matchExternalId: String
    ): String {
        return """
            {
              "paymentRequestId": "$paymentRequestId",
              "createdAt": "$createdAt",
              "userId": $userId,
              "totalAmount": $totalAmount,
              "currency": "BRL",
              "reason": "BET_PAYOUT",
              "matchExternalId": "$matchExternalId"
            }
        """.trimIndent()
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
