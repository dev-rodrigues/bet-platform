package br.devrodrigues.resultingestionservice.application.service

import br.devrodrigues.commonevents.MatchesResultEvent
import br.devrodrigues.resultingestionservice.application.mapper.MatchResultMapper
import br.devrodrigues.resultingestionservice.application.model.MatchResultInput
import br.devrodrigues.resultingestionservice.application.port.out.MatchesResultPublisher
import org.springframework.stereotype.Service

@Service
class MatchResultIngestionService(
    private val mapper: MatchResultMapper,
    private val publisher: MatchesResultPublisher
) {
    fun ingest(input: MatchResultInput): MatchesResultEvent {
        val event = mapper.toEvent(input)
        publisher.publish(event)
        return event
    }
}
