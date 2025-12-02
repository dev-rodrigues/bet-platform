package br.devrodrigues.betsettlementservice.domain.model

import java.util.*

data class OutboxEvent(
    val id: UUID,
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    val payload: String,
    val status: String,
    val referenceId: String
)