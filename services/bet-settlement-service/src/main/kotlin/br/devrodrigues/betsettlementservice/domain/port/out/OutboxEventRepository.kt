package br.devrodrigues.betsettlementservice.domain.port.out

import br.devrodrigues.betsettlementservice.domain.model.OutboxEvent
import java.util.*

interface OutboxEventRepository {

    fun saveAll(events: List<OutboxEvent>): List<OutboxEvent>

    fun findPendingWalletPayments(limit: Int): List<OutboxEvent>

    fun markPublished(eventIds: List<UUID>)

    fun markError(eventId: UUID, error: String)
}
