package br.devrodrigues.betsettlementservice.adapter.inbound.messaging

import br.devrodrigues.betsettlementservice.BetSettlementServiceApplication
import br.devrodrigues.betsettlementservice.adapter.outbound.persistence.jpa.GameJpaRepository
import br.devrodrigues.commonevents.GameCreatedEvent
import org.assertj.core.api.Assertions.assertThat
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
class GameCreatedListenerTest(
    @Autowired private val listener: GameCreatedListener,
    @Autowired private val gameJpaRepository: GameJpaRepository
) {
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
    fun clearDatabase() {
        gameJpaRepository.deleteAll()
    }

    @Test
    fun `should upsert game on received event`() {
        val event = GameCreatedEvent(
            eventId = "event-1",
            occurredAt = Instant.parse("2024-01-01T00:00:00Z"),
            emittedAt = Instant.parse("2024-01-01T00:00:10Z"),
            gameId = 101L,
            externalId = 2024L,
            homeTeam = "Home FC",
            awayTeam = "Away FC",
            startTime = Instant.parse("2024-02-01T12:00:00Z"),
            status = "SCHEDULED"
        )

        listener.onGameCreated(event)

        val persisted = requireNotNull(gameJpaRepository.findByExternalId(event.externalId))

        assertThat(gameJpaRepository.count()).isEqualTo(1)
        assertThat(persisted.id).isNotNull()
        assertThat(persisted.externalId).isEqualTo(event.externalId)
        assertThat(persisted.status).isEqualTo(event.status)
        assertThat(persisted.homeTeam).isEqualTo(event.homeTeam)
        assertThat(persisted.awayTeam).isEqualTo(event.awayTeam)

        val updatedEvent = event.copy(
            status = "IN_PLAY",
            homeTeam = "Home Renamed",
            emittedAt = Instant.parse("2024-01-01T00:01:00Z")
        )

        listener.onGameCreated(updatedEvent)

        val updated = requireNotNull(gameJpaRepository.findByExternalId(event.externalId))
        assertThat(gameJpaRepository.count()).isEqualTo(1)
        assertThat(updated.id).isEqualTo(persisted.id)
        assertThat(updated.status).isEqualTo("IN_PLAY")
        assertThat(updated.homeTeam).isEqualTo("Home Renamed")
        assertThat(updated.awayTeam).isEqualTo(event.awayTeam)
    }
}
