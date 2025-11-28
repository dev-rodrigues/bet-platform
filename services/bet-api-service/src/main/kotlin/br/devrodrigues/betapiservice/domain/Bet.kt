package br.devrodrigues.betapiservice.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "bets")
data class Bet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false)
    val gameId: Long,
    @Column(nullable = false, length = 32)
    val selection: String,
    @Column(nullable = false, precision = 14, scale = 2)
    val stake: BigDecimal,
    @Column(nullable = false, precision = 10, scale = 4)
    val odds: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    val status: BetStatus = BetStatus.PENDING,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)

enum class BetStatus {
    PENDING,
    SETTLED
}
