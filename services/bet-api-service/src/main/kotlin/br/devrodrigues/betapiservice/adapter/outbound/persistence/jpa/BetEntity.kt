package br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa

import br.devrodrigues.betapiservice.domain.model.Bet
import br.devrodrigues.betapiservice.domain.model.BetStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "bets")
data class BetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val userId: Long,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    val game: GameEntity,
    @Column(nullable = false, length = 32)
    val selection: String,
    @Column(nullable = false, precision = 14, scale = 2)
    val stake: BigDecimal,
    @Column(nullable = false, precision = 10, scale = 4)
    val odds: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    val status: BetStatus,
    @Column(nullable = false)
    val createdAt: Instant
)

fun Bet.toEntity(gameEntity: GameEntity): BetEntity =
    BetEntity(
        id = id,
        userId = userId,
        game = gameEntity,
        selection = selection,
        stake = stake,
        odds = odds,
        status = status,
        createdAt = createdAt
    )

fun BetEntity.toDomain(): Bet =
    Bet(
        id = id,
        userId = userId,
        gameId = game.id ?: error("Game id is required"),
        selection = selection,
        stake = stake,
        odds = odds,
        status = status,
        createdAt = createdAt
    )
