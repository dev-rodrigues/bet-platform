package br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa

import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.model.GameStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(
    name = "game",
    indexes = [
        Index(name = "idx_game_external_id", columnList = "external_id", unique = true),
        Index(name = "idx_game_match_date", columnList = "match_date")
    ]
)
data class GameEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "external_id", nullable = false, unique = true, length = 100)
    val externalId: Long,
    @Column(name = "home_team", nullable = false, length = 100)
    val homeTeam: String,
    @Column(name = "away_team", nullable = false, length = 100)
    val awayTeam: String,
    @Column(name = "start_time", nullable = false)
    val startTime: Instant,
    @Column(name = "home_score")
    val homeScore: Int? = null,
    @Column(name = "away_score")
    val awayScore: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    val status: GameStatus,
    @Column(name = "match_date", nullable = false)
    val matchDate: LocalDate
)

fun GameEntity.toDomain(): Game =
    Game(
        id = requireNotNull(id),
        externalId = externalId,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        startTime = startTime,
        homeScore = homeScore,
        awayScore = awayScore,
        status = status,
        matchDate = matchDate
    )

fun Game.toEntity(): GameEntity =
    GameEntity(
        id = id,
        externalId = externalId,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        startTime = startTime,
        homeScore = homeScore,
        awayScore = awayScore,
        status = status,
        matchDate = matchDate
    )
