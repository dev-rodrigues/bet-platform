package br.devrodrigues.betsettlementservice.adapter.outbound.persistence

import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.GameJpaRepository
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.betsettlementservice.domain.model.Game
import br.devrodrigues.betsettlementservice.domain.port.out.GameRepository
import org.springframework.stereotype.Repository

@Repository
class GameRepositoryAdapter(
    private val gameJpaRepository: GameJpaRepository
) : GameRepository {

    override fun findByExternalId(externalId: Long): Game? =
        gameJpaRepository.findByExternalId(externalId)?.toDomain()

    override fun save(game: Game): Game =
        gameJpaRepository.save(game.toEntity()).toDomain()
}
