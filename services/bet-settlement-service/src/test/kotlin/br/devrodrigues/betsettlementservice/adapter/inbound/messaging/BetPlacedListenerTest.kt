package br.devrodrigues.betsettlementservice.adapter.inbound.messaging

import br.devrodrigues.betsettlementservice.BetSettlementServiceApplication
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.BetEntity
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.BetJpaRepository
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.GameEntity
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.GameJpaRepository
import br.devrodrigues.betsettlementservice.support.BetPlacedEventFixtures
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
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
class BetPlacedListenerTest(
    @Autowired private val listener: BetPlacedListener,
    @Autowired private val betJpaRepository: BetJpaRepository,
    @Autowired private val gameJpaRepository: GameJpaRepository
) {

    private lateinit var defaultGame: GameEntity

    @BeforeEach
    fun clearDatabase() {
        betJpaRepository.deleteAll()
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

    @Test
    @Transactional
    fun `should persist bet linked to existing game`() {
        val event = BetPlacedEventFixtures.default(
            gameId = 9999L,
            gameExternalId = defaultGame.externalId
        )

        listener.onBetPlaced(event)

        val persisted = requireNotNull(betJpaRepository.findById(event.id).orElse(null))
        assertThat(persisted.gameId).isEqualTo(defaultGame.id)
        assertThat(persisted.gameExternalId).isEqualTo(event.gameExternalId.toString())
        assertThat(persisted.status).isEqualTo("PENDING")
        assertThat(persisted.selection).isEqualTo(event.selection)
        assertThat(persisted.userId).isEqualTo(event.userId)
        assertThat(gameJpaRepository.findById(persisted.gameId)).isPresent
    }

    @Test
    @Transactional
    fun `should update existing bet details`() {
        val existing = betJpaRepository.save(
            BetEntity(
                id = 20L,
                userId = 1L,
                gameId = defaultGame.id!!,
                gameExternalId = defaultGame.externalId.toString(),
                selection = "HOME",
                stake = BigDecimal("50.00"),
                odds = BigDecimal("1.50"),
                status = "PENDING",
                payout = null,
                createdAt = Instant.parse("2024-01-05T00:00:00Z"),
                updatedAt = Instant.parse("2024-01-05T00:00:00Z")
            )
        )

        val event = BetPlacedEventFixtures.default(
            id = existing.id,
            userId = 2L,
            gameId = 9999L,
            gameExternalId = defaultGame.externalId,
            selection = "AWAY",
            stake = BigDecimal("75.00"),
            odds = BigDecimal("2.00"),
            status = "CONFIRMED",
            createdAt = Instant.parse("2024-03-02T00:00:00Z")
        )

        listener.onBetPlaced(event)

        val updated = requireNotNull(betJpaRepository.findById(existing.id).orElse(null))
        assertThat(updated.id).isEqualTo(existing.id)
        assertThat(updated.gameId).isEqualTo(defaultGame.id)
        assertThat(updated.createdAt).isEqualTo(existing.createdAt)
        assertThat(updated.updatedAt).isEqualTo(event.createdAt)
        assertThat(updated.userId).isEqualTo(event.userId)
        assertThat(updated.selection).isEqualTo(event.selection)
        assertThat(updated.stake).isEqualByComparingTo(event.stake)
        assertThat(updated.odds).isEqualByComparingTo(event.odds)
        assertThat(updated.status).isEqualTo(event.status)
    }

    @Test
    fun `should fail when game does not exist`() {
        betJpaRepository.deleteAll()
        gameJpaRepository.deleteAll()

        val event = BetPlacedEventFixtures.default(
            id = 30L,
            userId = 9L,
            gameId = 123L,
            gameExternalId = 9999L,
            selection = "DRAW",
            stake = BigDecimal("12.00"),
            odds = BigDecimal("3.00"),
            status = "CREATED",
            createdAt = Instant.parse("2024-03-15T00:00:00Z")
        )

        assertThatThrownBy { listener.onBetPlaced(event) }
            .hasMessageContaining("Game not found")

        assertThat(betJpaRepository.findById(event.id)).isNotPresent
    }
}
