package br.devrodrigues.resultingestionservice.integration

import br.devrodrigues.commonevents.MatchesResultEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MatchResultWebhookIntegrationTest {

    companion object {
        @Container
        private val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"))

        @JvmStatic
        @DynamicPropertySource
        fun registerKafkaProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
            registry.add("bet.kafka.topics.matches-result") { "matches.result.v1" }
        }
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val restTemplate: RestTemplate by lazy {
        RestTemplateBuilder()
            .rootUri("http://localhost:$port")
            .build()
    }

    @Test
    fun `should publish event to kafka after receiving webhook`() {
        val payload = mapOf(
            "matchExternalId" to "match-123",
            "homeScore" to 2,
            "awayScore" to 1,
            "status" to "FINISHED",
            "providerEventId" to "prov-1"
        )

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val requestEntity = HttpEntity(objectMapper.writeValueAsString(payload), headers)

        val response = restTemplate.postForEntity("/webhook/matches/result", requestEntity, Map::class.java)

        assertThat(response.statusCode.value()).isEqualTo(202)
        assertThat(response.body?.get("status")).isEqualTo("ACCEPTED")
        assertThat(response.body?.get("matchExternalId")).isEqualTo("match-123")

        val consumerProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "result-ingestion-integration-${UUID.randomUUID()}",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java
        )

        KafkaConsumer<String, ByteArray>(consumerProps).use { consumer ->
            consumer.subscribe(listOf("matches.result.v1"))
            val records = consumer.poll(Duration.ofSeconds(10))
            val record = records.firstOrNull()

            assertThat(record).isNotNull
            val event = objectMapper.readValue(record!!.value(), MatchesResultEvent::class.java)
            assertThat(event.matchExternalId).isEqualTo("match-123")
            assertThat(event.homeScore).isEqualTo(2)
            assertThat(event.awayScore).isEqualTo(1)
            assertThat(event.status).isEqualTo("FINISHED")
            assertThat(event.provider).isEqualTo("prov-1")
        }
    }
}
