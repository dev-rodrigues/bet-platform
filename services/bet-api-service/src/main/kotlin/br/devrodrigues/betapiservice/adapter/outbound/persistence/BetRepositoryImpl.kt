package br.devrodrigues.betapiservice.adapter.outbound.persistence

import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.BetJpaRepository
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.GameJpaRepository
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.betapiservice.domain.model.Bet
import br.devrodrigues.betapiservice.domain.port.out.BetRepository
import org.springframework.stereotype.Repository

@Repository
class BetRepositoryImpl(
    private val betJpaRepository: BetJpaRepository,
    private val gameJpaRepository: GameJpaRepository
) : BetRepository {

    override fun save(bet: Bet): Bet {
        val gameRef = gameJpaRepository.getReferenceById(bet.gameId)
        val saved = betJpaRepository.save(bet.toEntity(gameRef))
        return saved.toDomain()
    }
}
