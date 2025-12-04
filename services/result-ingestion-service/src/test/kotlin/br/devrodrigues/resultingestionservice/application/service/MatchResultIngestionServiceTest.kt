package br.devrodrigues.resultingestionservice.application.service

import br.devrodrigues.resultingestionservice.application.model.MatchResultInput
import br.devrodrigues.resultingestionservice.config.FlakyPublisher
import br.devrodrigues.resultingestionservice.config.RetryTestConfig
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.TimeoutException
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutionException

@SpringBootTest
@Import(RetryTestConfig::class)
@Testcontainers
class MatchResultIngestionServiceTest {

    @Autowired
    lateinit var service: MatchResultIngestionService

    @Autowired
    lateinit var flakyPublisher: FlakyPublisher

    private val topic = "matches.result.v1"

    companion object {
        @Container
        @JvmField
        val kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"))

        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("betting")
            withUsername("betting")
            withPassword("betting")
        }

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
            registry.add("resilience4j.retry.instances.matchesResultPublisher.max-attempts") { 3 }
            registry.add("resilience4j.retry.instances.matchesResultPublisher.wait-duration") { "10ms" }
            registry.add("resilience4j.retry.instances.matchesResultPublisher.enable-exponential-backoff") { true }
            registry.add("resilience4j.retry.instances.matchesResultPublisher.exponential-backoff-multiplier") { 2.0 }
            registry.add("resilience4j.retry.instances.matchesResultPublisher.exponential-max-wait-duration") { "100ms" }
            registry.add("resilience4j.retry.instances.matchesResultPublisher.enable-randomized-wait") { false }

            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }

    @BeforeEach
    fun setup() {
        ensureTopicExists()
        flakyPublisher.reset()
    }

    @Test
    fun `should retry transient failures and publish once`() {
        flakyPublisher.failuresBeforeSuccess = 2

        val input = MatchResultInput(
            matchExternalId = "match-${UUID.randomUUID()}",
            homeScore = 2,
            awayScore = 1,
            status = "FINISHED",
            providerEventId = "prov-${UUID.randomUUID()}"
        )

        val event = service.ingest(input)

        assertEquals(3, flakyPublisher.attempts.get(), "should attempt initial try + 2 retries")
        assertEquals(1, flakyPublisher.published.size, "event should be published once after retries")
        assertEquals(input.matchExternalId, event.matchExternalId)

        val consumer = kafkaConsumer()
        consumer.use {
            it.subscribe(listOf(topic))
            val records = it.poll(Duration.ofSeconds(5))
            assertTrue(records.count() > 0, "record should be available after successful publish")
            assertTrue(records.any { record -> record.value().contains(input.matchExternalId) })
        }
    }

    @Test
    fun `should stop after max attempts on persistent failure`() {
        flakyPublisher.failuresBeforeSuccess = 5

        val input = MatchResultInput(
            matchExternalId = "match-${UUID.randomUUID()}",
            homeScore = 0,
            awayScore = 0,
            status = "PENDING",
            providerEventId = "prov-${UUID.randomUUID()}"
        )

        assertThrows<TimeoutException> { service.ingest(input) }
        assertEquals(3, flakyPublisher.attempts.get(), "should stop at configured max attempts")
        assertTrue(flakyPublisher.published.isEmpty(), "no event should be published when all attempts fail")
    }

    private fun kafkaConsumer(): KafkaConsumer<String, String> {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "test-${UUID.randomUUID()}")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }
        return KafkaConsumer(props)
    }

    private fun ensureTopicExists() {
        val props = mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers)
        AdminClient.create(props).use { admin ->
            val topicDefinition = NewTopic(topic, 1, 1.toShort())
            try {
                admin.createTopics(listOf(topicDefinition)).all().get()
            } catch (ex: ExecutionException) {
                if (ex.cause !is TopicExistsException) {
                    throw ex
                } else {
                    // topic already exists; ignore
                }
            }
        }
    }
}
