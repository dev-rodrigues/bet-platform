package br.devrodrigues.betsettlementservice.adapter.outbound.persistence

import br.devrodrigues.betsettlementservice.domain.port.out.OutboxEventRepository
import org.springframework.stereotype.Repository

@Repository
class OutboxEventAdapter(
    private val outboxEventRepository: OutboxEventRepository
) : OutboxEventRepository {
}