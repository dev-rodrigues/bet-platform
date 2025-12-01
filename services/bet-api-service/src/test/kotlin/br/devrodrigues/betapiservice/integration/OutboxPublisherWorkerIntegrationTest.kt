package br.devrodrigues.betapiservice.integration

import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.OutboxEventJpaRepository
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.betapiservice.adapter.outbound.worker.OutboxPublisherWorker
import br.devrodrigues.betapiservice.domain.model.OutboxEvent
import br.devrodrigues.betapiservice.domain.model.OutboxStatus
import br.devrodrigues.commonevents.BetPlacedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.Instant
import java.util.*

@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = [
        "spring.task.scheduling.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration"
    ]
)
@ActiveProfiles("worker")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OutboxPublisherWorkerIntegrationTest(
    @Autowired private val outboxPublisherWorker: OutboxPublisherWorker,
    @Autowired private val outboxEventJpaRepository: OutboxEventJpaRepository,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private lateinit var betPlacedEvent: BetPlacedEvent
    private lateinit var betConsumer: Consumer<String, String>
    private lateinit var gameConsumer: Consumer<String, String>

    private data class SavedOutbox(val event: OutboxEvent, val payload: String)

    private companion object {
        private const val TOPIC = "test.bets.placed.v1"
        private const val GAME_TOPIC = "test.games.created.v1"
        private const val BET_ID = 123L
        private const val USER_ID = 99L
        private const val GAME_ID = 555L
        private const val GAME_EXTERNAL_ID = 987L

        private val kafkaImage: DockerImageName = DockerImageName
            .parse("confluentinc/cp-kafka:7.5.1")
            .asCompatibleSubstituteFor("confluentinc/cp-kafka")

        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine")).apply {
            withDatabaseName("betting")
            withUsername("betting")
            withPassword("betting")
        }

        @Container
        @JvmStatic
        val kafka: KafkaContainer = KafkaContainer(kafkaImage).apply {
            withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "false")
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            postgres.start()
            kafka.start()
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
            registry.add("app.topics.bet-placed") { TOPIC }
            registry.add("app.topics.game-created") { GAME_TOPIC }
        }
    }

    @BeforeEach
    fun setUp() {
        outboxEventJpaRepository.deleteAll()
        betConsumer = buildConsumer("bets")
        gameConsumer = buildConsumer("games")
        betConsumer.subscribe(listOf(TOPIC))
        gameConsumer.subscribe(listOf(GAME_TOPIC))
        betConsumer.poll(Duration.ofMillis(200))
        gameConsumer.poll(Duration.ofMillis(200))
        betPlacedEvent = defaultBetPlacedEvent()
    }

    @AfterEach
    fun tearDown() {
        betConsumer.close()
        gameConsumer.close()
    }

    @Test
    fun `should publish pending outbox event to kafka and mark as published`() {
        createTopicIfNeeded()
        val savedOutbox = savePendingOutbox(betPlacedEvent)

        outboxPublisherWorker.publishPending()

        val record = consumeSingleRecord()
        assertThat(record.topic()).isEqualTo(TOPIC)
        assertThat(record.key()).isEqualTo(savedOutbox.event.aggregateId)
        assertThat(record.value()).isEqualTo(savedOutbox.payload)

        val persisted = outboxEventJpaRepository.findAll().single()
        assertThat(persisted.status).isEqualTo(OutboxStatus.PUBLISHED)
        assertThat(persisted.processedAt).isNotNull()
        assertThat(persisted.lastError).isNull()
    }

    @Test
    fun `should mark event as error when publishing fails`() {
        deleteTopicIfExists()
        val savedOutbox = savePendingOutbox(betPlacedEvent)

        outboxPublisherWorker.publishPending()

        val records = KafkaTestUtils.getRecords(betConsumer, Duration.ofSeconds(2))
        assertThat(records.count()).isZero()

        val persisted = outboxEventJpaRepository.findAll().single()
        assertThat(persisted.status).isEqualTo(OutboxStatus.ERROR)
        assertThat(persisted.processedAt).isNotNull()
        assertThat(persisted.lastError).isNotNull()
        assertThat(persisted.aggregateId).isEqualTo(savedOutbox.event.aggregateId)
    }

    @Test
    fun `should publish game created event`() {
        createTopicIfNeeded(GAME_TOPIC)
        val payload =
            """{"eventId":"evt-1","occurredAt":"2024-01-01T10:00:00Z","emittedAt":"2024-01-01T10:00:01Z","gameId":$GAME_ID,"externalId":$GAME_EXTERNAL_ID,"homeTeam":"Home","awayTeam":"Away","startTime":"2024-01-02T10:00:00Z","status":"SCHEDULED"}"""
        val outbox = OutboxEvent(
            id = UUID.randomUUID(),
            aggregateType = "game",
            aggregateId = GAME_ID.toString(),
            type = "GAME_CREATED",
            payload = payload,
            status = OutboxStatus.PENDING,
            createdAt = Instant.now()
        )
        outboxEventJpaRepository.save(outbox.toEntity())

        outboxPublisherWorker.publishPending()

        val record = KafkaTestUtils.getRecords(gameConsumer, Duration.ofSeconds(5)).firstOrNull()
        assertThat(record).isNotNull
        assertThat(record!!.topic()).isEqualTo(GAME_TOPIC)
        assertThat(record.key()).isEqualTo(outbox.aggregateId)
        assertThat(record.value()).isEqualTo(payload)
    }

    private fun buildConsumer(group: String): Consumer<String, String> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "outbox-test-group-$group",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
        return DefaultKafkaConsumerFactory<String, String>(props).createConsumer()
    }

    private fun createTopicIfNeeded(topic: String = TOPIC) {
        val adminConfig = mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers
        )
        AdminClient.create(adminConfig).use { client ->
            val topics = client.listTopics().names().get()
            if (!topics.contains(topic)) {
                client.createTopics(listOf(NewTopic(topic, 1, 1))).all().get()
            }
        }
    }

    private fun deleteTopicIfExists(topic: String = TOPIC) {
        val adminConfig = mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers)
        AdminClient.create(adminConfig).use { client ->
            val topics = client.listTopics().names().get()
            if (topics.contains(topic)) {
                client.deleteTopics(listOf(topic)).all().get()
            }
        }
    }

    private fun savePendingOutbox(event: BetPlacedEvent): SavedOutbox {
        val payload = objectMapper.writeValueAsString(event)
        val outbox = OutboxEvent(
            id = UUID.randomUUID(),
            aggregateType = "bet",
            aggregateId = event.id.toString(),
            type = "BET_PLACED",
            payload = payload,
            status = OutboxStatus.PENDING,
            createdAt = Instant.now()
        )
        outboxEventJpaRepository.save(outbox.toEntity())
        return SavedOutbox(outbox, payload)
    }

    private fun consumeSingleRecord(): ConsumerRecord<String, String> {
        val records = KafkaTestUtils.getRecords(betConsumer, Duration.ofSeconds(5))
        assertThat(records.count()).isEqualTo(1)
        return records.iterator().next()
    }

    private fun defaultBetPlacedEvent() = BetPlacedEvent(
        id = BET_ID,
        userId = USER_ID,
        gameId = GAME_ID,
        gameExternalId = GAME_EXTERNAL_ID,
        selection = "Team A",
        stake = java.math.BigDecimal("25.00"),
        odds = java.math.BigDecimal("2.10"),
        status = "PENDING",
        createdAt = Instant.parse("2024-01-01T00:00:00Z")
    )
}
