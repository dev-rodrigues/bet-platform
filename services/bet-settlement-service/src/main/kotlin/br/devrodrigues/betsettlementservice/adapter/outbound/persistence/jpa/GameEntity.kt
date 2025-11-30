package br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa

import br.devrodrigues.betsettlementservice.domain.model.Game
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "game",
    indexes = [
        Index(name = "ux_game_external_id", columnList = "external_id", unique = true)
    ]
)
data class GameEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "external_id", nullable = false, unique = true)
    val externalId: Long,
    @Column(name = "start_time", nullable = false)
    val startTime: Instant,
    @Column(name = "bets_close_at", nullable = false)
    val betsCloseAt: Instant,
    @Column(name = "status", nullable = false, length = 50)
    val status: String,
    @Column(name = "home_score")
    val homeScore: Int? = null,
    @Column(name = "away_score")
    val awayScore: Int? = null,
    @Column(name = "home_team", nullable = false, length = 100)
    val homeTeam: String,
    @Column(name = "away_team", nullable = false, length = 100)
    val awayTeam: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
)

fun GameEntity.toDomain(): Game =
    Game(
        id = requireNotNull(id),
        externalId = externalId,
        startTime = startTime,
        betsCloseAt = betsCloseAt,
        status = status,
        homeScore = homeScore,
        awayScore = awayScore,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun Game.toEntity(): GameEntity =
    GameEntity(
        id = id,
        externalId = externalId,
        startTime = startTime,
        betsCloseAt = betsCloseAt,
        status = status,
        homeScore = homeScore,
        awayScore = awayScore,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
