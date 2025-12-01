package br.devrodrigues.betsettlementservice.adapter.inbound.messaging

import br.devrodrigues.betsettlementservice.BetSettlementServiceApplication
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.GameEntity
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.GameJpaRepository
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.SettlementJobEntity
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.SettlementJobJpaRepository
import br.devrodrigues.betsettlementservice.application.validation.MissingGameForResultException
import br.devrodrigues.commonevents.MatchesResultEvent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant

@Testcontainers
@SpringBootTest(
    classes = [BetSettlementServiceApplication::class],
    properties = [
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false",
        "spring.kafka.admin.fail-fast=false",
        "spring.kafka.bootstrap-servers=localhost:29092"
    ]
)
class MatchesResultListenerTest(
    @Autowired private val listener: MatchesResultListener,
    @Autowired private val gameJpaRepository: GameJpaRepository,
    @Autowired private val settlementJobJpaRepository: SettlementJobJpaRepository
) {

    private lateinit var defaultGame: GameEntity

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("betting")
            withUsername("betting")
            withPassword("betting")
        }

        @JvmStatic
        @DynamicPropertySource
        fun databaseProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }

    @BeforeEach
    fun setUp() {
        settlementJobJpaRepository.deleteAll()
        gameJpaRepository.deleteAll()
        defaultGame = gameJpaRepository.save(
            GameEntity(
                externalId = 2024L,
                startTime = Instant.parse("2024-02-01T12:00:00Z"),
                betsCloseAt = Instant.parse("2024-02-01T12:00:00Z"),
                status = "SCHEDULED",
                homeTeam = "Home FC",
                awayTeam = "Away FC",
                createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2024-01-01T00:00:00Z")
            )
        )
    }

    @Test
    fun `should apply match result and create settlement job`() {
        val event = resultEvent(matchExternalId = defaultGame.externalId.toString(), status = "FINISHED")

        listener.onMatchesResult(event)

        val updatedGame = requireNotNull(gameJpaRepository.findById(defaultGame.id!!).orElse(null))
        assertThat(updatedGame.status).isEqualTo("FINISHED")
        assertThat(updatedGame.homeScore).isEqualTo(event.homeScore)
        assertThat(updatedGame.awayScore).isEqualTo(event.awayScore)
        assertThat(updatedGame.updatedAt).isEqualTo(event.emittedAt)

        val job = requireNotNull(settlementJobJpaRepository.findByMatchId(defaultGame.id!!))
        assertThat(job.matchId).isEqualTo(defaultGame.id)
        assertThat(job.externalMatchId).isEqualTo(event.matchExternalId)
        assertThat(job.status).isEqualTo("PENDING")
        assertThat(job.batchSize).isEqualTo(1000)
        assertThat(job.createdAt).isEqualTo(event.emittedAt)
        assertThat(job.updatedAt).isEqualTo(event.emittedAt)
    }

    @Test
    fun `should not create duplicate settlement job when one exists`() {
        val existingJob = settlementJobJpaRepository.save(
            SettlementJobEntity(
                matchId = defaultGame.id!!,
                externalMatchId = defaultGame.externalId.toString(),
                status = "PENDING",
                batchSize = 500,
                createdAt = Instant.parse("2024-01-10T00:00:00Z"),
                updatedAt = Instant.parse("2024-01-10T00:00:00Z"),
                lastError = null
            )
        )

        val event = resultEvent(matchExternalId = defaultGame.externalId.toString(), status = "FINISHED")

        listener.onMatchesResult(event)

        val jobs = settlementJobJpaRepository.findAll()
        assertThat(jobs).hasSize(1)
        assertThat(jobs.first().id).isEqualTo(existingJob.id)
        assertThat(jobs.first().updatedAt).isEqualTo(existingJob.updatedAt)
        assertThat(jobs.first().createdAt).isEqualTo(existingJob.createdAt)
    }

    @Test
    fun `should throw when match not found`() {
        gameJpaRepository.deleteAll()

        val event = resultEvent(matchExternalId = "9999", status = "FINISHED")

        assertThatThrownBy { listener.onMatchesResult(event) }
            .isInstanceOf(MissingGameForResultException::class.java)
        assertThat(settlementJobJpaRepository.findAll()).isEmpty()
    }

    private fun resultEvent(
        matchExternalId: String,
        status: String
    ): MatchesResultEvent = MatchesResultEvent(
        eventId = "event-1",
        occurredAt = Instant.parse("2024-03-01T00:00:00Z"),
        emittedAt = Instant.parse("2024-03-01T00:00:10Z"),
        matchExternalId = matchExternalId,
        homeScore = 2,
        awayScore = 1,
        status = status,
        provider = "provider"
    )
}
