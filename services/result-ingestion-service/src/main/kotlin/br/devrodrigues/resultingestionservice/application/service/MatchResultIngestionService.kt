package br.devrodrigues.resultingestionservice.application.service

import br.devrodrigues.commonevents.MatchesResultEvent
import br.devrodrigues.resultingestionservice.application.mapper.MatchResultMapper
import br.devrodrigues.resultingestionservice.application.model.MatchResultInput
import br.devrodrigues.resultingestionservice.domain.model.MatchesResult
import br.devrodrigues.resultingestionservice.domain.port.out.MatchesResultPublisher
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.stereotype.Service

@Service
class MatchResultIngestionService(
    private val mapper: MatchResultMapper,
    private val publisher: MatchesResultPublisher
) {
    @Retry(name = "matchesResultPublisher")
    fun ingest(input: MatchResultInput): MatchesResultEvent {
        val event = mapper.toEvent(input)
        publisher.publish(
            MatchesResult(
                eventId = event.eventId,
                occurredAt = event.occurredAt,
                emittedAt = event.emittedAt,
                matchExternalId = event.matchExternalId,
                homeScore = event.homeScore,
                awayScore = event.awayScore,
                status = event.status,
                provider = event.provider,
            )
        )
        return event
    }
}
