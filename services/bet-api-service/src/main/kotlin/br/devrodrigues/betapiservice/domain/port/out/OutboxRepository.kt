package br.devrodrigues.betapiservice.domain.port.out

import br.devrodrigues.betapiservice.domain.model.OutboxEvent
import br.devrodrigues.betapiservice.domain.model.OutboxStatus

interface OutboxRepository {
    fun save(event: OutboxEvent): OutboxEvent
    fun findPending(limit: Int): List<OutboxEvent>
    fun markSent(eventIds: List<java.util.UUID>)
    fun markFailed(eventId: java.util.UUID, error: String)
}
