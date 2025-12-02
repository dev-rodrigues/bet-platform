package br.devrodrigues.betsettlementservice.adapter.outbound.persistence

import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.betsettlementservice.domain.model.OutboxEvent
import br.devrodrigues.betsettlementservice.domain.port.out.OutboxEventRepository
import org.springframework.stereotype.Repository
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.OutboxEventRepository as OutboxEventJpaRepository

@Repository
class OutboxEventAdapter(
    private val outboxEventJpaRepository: OutboxEventJpaRepository
) : OutboxEventRepository {

    override fun saveAll(events: List<OutboxEvent>): List<OutboxEvent> =
        outboxEventJpaRepository
            .saveAll(events.map { it.toEntity() })
            .map { it.toDomain() }
}
