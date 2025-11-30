package br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa

import br.devrodrigues.betsettlementservice.domain.model.SettlementJob
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "settlement_job",
    indexes = [
        Index(name = "ux_settlement_job_match", columnList = "match_id", unique = true)
    ]
)
data class SettlementJobEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "match_id", nullable = false)
    val matchId: Long,
    @Column(name = "external_match_id", nullable = false, length = 100)
    val externalMatchId: String,
    @Column(name = "status", nullable = false, length = 20)
    val status: String,
    @Column(name = "batch_size", nullable = false)
    val batchSize: Int,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
    @Column(name = "last_error")
    val lastError: String? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "match_id",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_settlement_job_game")
    )
    val game: GameEntity? = null
)

fun SettlementJobEntity.toDomain(): SettlementJob =
    SettlementJob(
        id = requireNotNull(id),
        matchId = matchId,
        externalMatchId = externalMatchId,
        status = status,
        batchSize = batchSize,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastError = lastError
    )

fun SettlementJob.toEntity(): SettlementJobEntity =
    SettlementJobEntity(
        id = id,
        matchId = matchId,
        externalMatchId = externalMatchId,
        status = status,
        batchSize = batchSize,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastError = lastError
    )
