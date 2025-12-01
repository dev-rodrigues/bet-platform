package br.devrodrigues.betsettlementservice.application.service

import br.devrodrigues.betsettlementservice.domain.port.out.BetRepository
import br.devrodrigues.betsettlementservice.domain.port.out.SettlementJobRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

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
            logger.info("No pending settlement jobs to process")
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

        processPendingBets(job.matchId, job.batchSize, job.id!!)
    }

    private fun processPendingBets(matchId: Long, batchSize: Int, jobId: Long) {
        while (true) {
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

            logger.info(
                "Fetched {} pending bets for settlement matchId={} (batchSize={})",
                pendingBets.size,
                matchId,
                batchSize
            )

            return
        }
    }
}
