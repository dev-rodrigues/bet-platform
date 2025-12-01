package br.devrodrigues.betsettlementservice.adapter.outbound.worker

import br.devrodrigues.betsettlementservice.application.service.SettlementWorkerService
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Profile("worker")
class SettlementWorker(
    private val settlementWorkerService: SettlementWorkerService
) {

    @Scheduled(fixedDelayString = "\${app.settlement.worker-delay-ms:2000}")
    fun runScheduled() {
        settlementWorkerService.runOnce()
    }
}
