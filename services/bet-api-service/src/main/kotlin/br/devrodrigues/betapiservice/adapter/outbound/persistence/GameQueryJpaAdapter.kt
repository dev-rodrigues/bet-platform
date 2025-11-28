package br.devrodrigues.betapiservice.adapter.outbound.persistence

import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.GameJpaRepository
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.port.out.GameQueryPort
import org.springframework.stereotype.Component

@Component
class GameQueryJpaAdapter(
    private val gameJpaRepository: GameJpaRepository
) : GameQueryPort {

    override fun findByExternalId(externalId: Long): Game? =
        gameJpaRepository.findByExternalId(externalId)?.toDomain()
}
