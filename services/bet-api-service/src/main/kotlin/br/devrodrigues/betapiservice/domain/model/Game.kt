package br.devrodrigues.betapiservice.domain.model

import java.time.Instant
import java.time.LocalDate

data class Game(
    val id: Long? = null,
    val externalId: Long,
    val homeTeam: String,
    val awayTeam: String,
    val startTime: Instant,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: GameStatus,
    val matchDate: LocalDate
)

enum class GameStatus {
    SCHEDULED,
    LIVE,
    FINISHED,
    CANCELED
}
