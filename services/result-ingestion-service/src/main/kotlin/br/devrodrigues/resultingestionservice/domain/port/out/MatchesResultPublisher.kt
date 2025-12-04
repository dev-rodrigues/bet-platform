package br.devrodrigues.resultingestionservice.domain.port.out

import br.devrodrigues.resultingestionservice.domain.model.MatchesResult

interface MatchesResultPublisher {
    fun publish(event: MatchesResult)
}
