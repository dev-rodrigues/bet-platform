package br.devrodrigues.betapiservice.domain.port.out

import br.devrodrigues.betapiservice.domain.model.Game

interface GameQueryPort {
    fun findByExternalId(externalId: Long): Game?
}
