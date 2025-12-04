package br.devrodrigues.resultingestionservice.adapter.outbound.worker

import br.devrodrigues.commonevents.MatchesResultEvent
import br.devrodrigues.resultingestionservice.adapter.outbound.persistence.jpa.WebhookFallbackEventJpaRepository
import br.devrodrigues.resultingestionservice.domain.model.WebhookFallbackEvent
import br.devrodrigues.resultingestionservice.domain.model.WebhookFallbackStatus
import br.devrodrigues.resultingestionservice.domain.port.out.WebhookFallbackRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
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
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("worker")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WebhookFallbackWorkerTest {

    @Autowired
    lateinit var worker: WebhookFallbackWorker

    @Autowired
    lateinit var webhookFallbackRepository: WebhookFallbackRepository

    @Autowired
    lateinit var webhookFallbackEventJpaRepository: WebhookFallbackEventJpaRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val topic = "matches.result.v1"
    private lateinit var matchExternalId: String

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
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }

    @BeforeEach
    fun setup() {
        matchExternalId = "match-${UUID.randomUUID()}"
        webhookFallbackEventJpaRepository.deleteAll()
        ensureTopicExists()

        val payload = buildPayload(matchExternalId)
        webhookFallbackRepository.save(WebhookFallbackEvent(payload = payload))
    }

    @Test
    fun `should resend pending fallback and mark as resent`() {
        worker.retryFailedWebhooks()

        val pending = webhookFallbackRepository.findPending(limit = 10)
        assertThat(pending).isEmpty()

        val persisted = webhookFallbackEventJpaRepository.findAll()
        assertThat(persisted).anyMatch { it.status == WebhookFallbackStatus.RESENT }

        KafkaConsumer<String, String>(kafkaConsumerProps()).use { consumer ->
            consumer.subscribe(listOf(topic))
            val records = consumer.poll(Duration.ofSeconds(8))
            assertThat(records).anyMatch { it.value().contains(matchExternalId) }
        }
    }

    @Test
    fun `should process pending events concurrently without duplication`() {
        webhookFallbackEventJpaRepository.deleteAll()
        ensureTopicExists()

        val payloads = (1..200).map {
            val id = "match-${UUID.randomUUID()}"
            WebhookFallbackEvent(payload = buildPayload(id))
        }
        payloads.forEach { webhookFallbackRepository.save(it) }

        val executor = Executors.newFixedThreadPool(2)
        val futures = listOf(
            executor.submit { worker.retryFailedWebhooks() },
            executor.submit { worker.retryFailedWebhooks() }
        )
        futures.forEach { it.get(60, TimeUnit.SECONDS) }
        executor.shutdown()

        val pending = webhookFallbackRepository.findPending(limit = 200)
        assertThat(pending).isEmpty()

        val persisted = webhookFallbackEventJpaRepository.findAll()
        assertThat(persisted).hasSize(200)
        assertThat(persisted.count { it.status == WebhookFallbackStatus.RESENT }).isEqualTo(200)

        KafkaConsumer<String, String>(kafkaConsumerProps()).use { consumer ->
            consumer.subscribe(listOf(topic))
            val received = mutableListOf<String>()
            val deadline = Instant.now().plusSeconds(20)
            while (Instant.now().isBefore(deadline) && received.size < 200) {
                val records = consumer.poll(Duration.ofSeconds(2))
                records.forEach { received.add(it.value()) }
            }
            val distinctMatches = received
                .map { objectMapper.readValue(it, MatchesResultEvent::class.java).matchExternalId }
                .toSet()
            assertThat(distinctMatches).hasSize(200)
        }
    }

    private fun buildPayload(matchExternalId: String) = objectMapper.writeValueAsString(
        MatchesResultEvent(
            eventId = UUID.randomUUID().toString(),
            occurredAt = Instant.now(),
            emittedAt = Instant.now(),
            matchExternalId = matchExternalId,
            homeScore = 1,
            awayScore = 0,
            status = "FINISHED",
            provider = "prov-${UUID.randomUUID()}"
        )
    )

    private fun kafkaConsumerProps(): Properties = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
        put(ConsumerConfig.GROUP_ID_CONFIG, "worker-test-${UUID.randomUUID()}")
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }

    private fun ensureTopicExists() {
        val props = mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers)
        AdminClient.create(props).use { admin ->
            val topicDefinition = NewTopic(topic, 1, 1.toShort())
            try {
                admin.createTopics(listOf(topicDefinition)).all().get()
            } catch (ex: ExecutionException) {
                // topic already exists; ignore
            }
        }
    }
}
