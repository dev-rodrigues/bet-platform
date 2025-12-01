package br.devrodrigues.betsettlementservice.application.service

import br.devrodrigues.betsettlementservice.domain.port.out.SettlementJobRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SettlementWorkerService(
    private val settlementJobRepository: SettlementJobRepository
) {
    private val logger = LoggerFactory.getLogger(SettlementWorkerService::class.java)

    fun runOnce() {
        val job = settlementJobRepository.findNextPendingForUpdate()

        if (job == null) {
            logger.info("No pending settlement jobs to process")
            return
        }

        val runningJob = job.copy(
            status = "RUNNING",
            updatedAt = Instant.now()
        )
        settlementJobRepository.save(runningJob)

        logger.info("Marked settlement job id={} as RUNNING", job.id)

    }
}
