package br.devrodrigues.resultingestionservice.application.port.out

import br.devrodrigues.commonevents.MatchesResultEvent

interface MatchesResultPublisher {
    fun publish(event: MatchesResultEvent)
}
