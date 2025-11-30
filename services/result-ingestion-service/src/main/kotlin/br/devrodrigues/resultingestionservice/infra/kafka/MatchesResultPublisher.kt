package br.devrodrigues.resultingestionservice.infra.kafka

import br.devrodrigues.commonevents.MatchesResultEvent

interface MatchesResultPublisher {
    fun publish(event: MatchesResultEvent)
}
