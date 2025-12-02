package br.devrodrigues.betapiservice.domain.port.out

import br.devrodrigues.betapiservice.domain.model.Game

interface GameRepository {
    fun findById(id: Long): Game?
    fun findByExternalId(externalId: Long): Game?
    fun save(game: Game): Game
    fun findPage(page: Int, size: Int): List<Game>
}
