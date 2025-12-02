package br.devrodrigues.betsettlementservice.domain.port.out

import br.devrodrigues.betsettlementservice.domain.model.OutboxEvent

interface OutboxEventRepository {

    fun saveAll(events: List<OutboxEvent>): List<OutboxEvent>
}
