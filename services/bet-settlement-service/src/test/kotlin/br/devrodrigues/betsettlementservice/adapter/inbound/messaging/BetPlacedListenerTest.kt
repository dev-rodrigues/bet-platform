package br.devrodrigues.betsettlementservice.adapter.inbound.messaging

import br.devrodrigues.betsettlementservice.BetSettlementServiceApplication
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.BetJpaRepository
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.GameEntity
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.GameJpaRepository
import br.devrodrigues.betsettlementservice.application.event.BetPlacedEvent
import org.assertj.core.api.Assertions.assertThat
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

    @BeforeEach
    fun clearDatabase() {
        betJpaRepository.deleteAll()
        gameJpaRepository.deleteAll()
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
        val game = gameJpaRepository.save(
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

        val event = BetPlacedEvent(
            id = 10L,
            userId = 5L,
            gameId = 9999L, // intentionally mismatched to ensure we align to persisted game id
            gameExternalId = game.externalId,
            selection = "HOME",
            stake = BigDecimal("10.00"),
            odds = BigDecimal("2.50"),
            status = "CREATED",
            createdAt = Instant.parse("2024-03-01T00:00:00Z")
        )

        listener.onBetPlaced(event)

        val persisted = requireNotNull(betJpaRepository.findById(event.id).orElse(null))
        assertThat(persisted.gameId).isEqualTo(game.id)
        assertThat(persisted.gameExternalId).isEqualTo(event.gameExternalId.toString())
        assertThat(persisted.status).isEqualTo("PENDING")
        assertThat(persisted.selection).isEqualTo(event.selection)
        assertThat(persisted.userId).isEqualTo(event.userId)
        assertThat(gameJpaRepository.findById(persisted.gameId)).isPresent
    }
}
