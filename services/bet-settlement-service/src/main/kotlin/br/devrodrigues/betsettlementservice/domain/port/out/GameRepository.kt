package br.devrodrigues.betsettlementservice.domain.port.out

import br.devrodrigues.betsettlementservice.domain.model.Game

interface GameRepository {
    fun findByExternalId(externalId: Long): Game?
    fun save(game: Game): Game
}
