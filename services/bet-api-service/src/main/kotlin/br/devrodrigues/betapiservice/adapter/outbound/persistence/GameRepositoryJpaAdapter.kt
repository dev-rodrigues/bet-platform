package br.devrodrigues.betapiservice.adapter.outbound.persistence

import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.GameJpaRepository
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.port.out.GameRepository
import org.springframework.stereotype.Repository

@Repository
class GameRepositoryJpaAdapter(
    private val gameJpaRepository: GameJpaRepository
) : GameRepository {

    override fun findByExternalId(externalId: Long): Game? =
        gameJpaRepository.findByExternalId(externalId)?.toDomain()

    override fun save(game: Game): Game =
        gameJpaRepository.save(game.toEntity()).toDomain()
}
