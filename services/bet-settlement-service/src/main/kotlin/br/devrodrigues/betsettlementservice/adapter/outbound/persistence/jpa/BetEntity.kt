package br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa

import br.devrodrigues.betsettlementservice.domain.model.Bet
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "bet",
    indexes = [
        Index(name = "idx_bet_game_status", columnList = "game_id,status"),
        Index(name = "idx_bet_user", columnList = "user_id")
    ]
)
data class BetEntity(
    @Id
    @Column(name = "id")
    val id: Long,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "game_id", nullable = false)
    val gameId: Long,
    @Column(name = "game_external_id", nullable = false, length = 100)
    val gameExternalId: String,
    @Column(name = "selection", nullable = false, length = 100)
    val selection: String,
    @Column(name = "stake", nullable = false, precision = 18, scale = 2)
    val stake: BigDecimal,
    @Column(name = "odds", nullable = false, precision = 10, scale = 2)
    val odds: BigDecimal,
    @Column(name = "status", nullable = false, length = 20)
    val status: String,
    @Column(name = "payout", precision = 18, scale = 2)
    val payout: BigDecimal? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
)

fun BetEntity.toDomain(): Bet =
    Bet(
        id = id,
        userId = userId,
        gameId = gameId,
        gameExternalId = gameExternalId,
        selection = selection,
        stake = stake,
        odds = odds,
        status = status,
        payout = payout,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun Bet.toEntity(): BetEntity =
    BetEntity(
        id = id,
        userId = userId,
        gameId = gameId,
        gameExternalId = gameExternalId,
        selection = selection,
        stake = stake,
        odds = odds,
        status = status,
        payout = payout,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
