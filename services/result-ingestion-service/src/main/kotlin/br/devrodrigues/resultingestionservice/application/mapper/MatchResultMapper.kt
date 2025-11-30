package br.devrodrigues.resultingestionservice.application.mapper

import br.devrodrigues.commonevents.MatchesResultEvent
import br.devrodrigues.resultingestionservice.application.model.MatchResultInput
import java.time.Instant
import java.util.*

class MatchResultMapper {

    fun toEvent(input: MatchResultInput): MatchesResultEvent {
        val occurredAt = Instant.now()
        val emittedAt = Instant.now()

        return MatchesResultEvent(
            eventId = UUID.randomUUID().toString(),
            occurredAt = occurredAt,
            emittedAt = emittedAt,
            matchExternalId = input.matchExternalId,
            homeScore = input.homeScore,
            awayScore = input.awayScore,
            status = input.status,
            provider = input.providerEventId
        )
    }
}
