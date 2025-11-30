package br.devrodrigues.betapiservice.integration

import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.BetJpaRepository
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.GameJpaRepository
import br.devrodrigues.betapiservice.adapter.outbound.persistence.jpa.OutboxEventJpaRepository
import br.devrodrigues.betapiservice.application.event.BetPlacedEvent
import br.devrodrigues.betapiservice.domain.model.BetStatus
import br.devrodrigues.betapiservice.domain.model.GameStatus
import br.devrodrigues.betapiservice.domain.model.OutboxStatus
import br.devrodrigues.betapiservice.domain.port.out.GameRepository
import br.devrodrigues.betapiservice.support.TestFixtures.betPayload
import br.devrodrigues.betapiservice.support.TestFixtures.game
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.Instant

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BetCreationFlowIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val gameRepository: GameRepository,
    @Autowired private val betJpaRepository: BetJpaRepository,
    @Autowired private val outboxEventJpaRepository: OutboxEventJpaRepository,
    @Autowired private val gameJpaRepository: GameJpaRepository
) {

    private lateinit var basePayload: Map<String, Any>
    private val stake = BigDecimal("75.50")
    private val odds = BigDecimal("1.85")
    private val selection = "Team A wins"
    private val userId = 42L

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
        }
    }

    @BeforeEach
    fun cleanDatabase() {
        outboxEventJpaRepository.deleteAll()
        betJpaRepository.deleteAll()
        gameJpaRepository.deleteAll()
        basePayload = betPayload(
            userId = userId,
            gameId = 0,
            selection = selection,
            stake = stake,
            odds = odds
        )
    }

    @Test
    fun `should create bet, persist entities and outbox event`() {
        val gameExternalId = Instant.now().toEpochMilli()
        val savedGame = gameRepository.save(
            game(
                externalId = gameExternalId,
                startTime = Instant.now().plusSeconds(3_600),
                status = GameStatus.SCHEDULED
            )
        )

        val responseJson = postBetExpectingCreated(payloadForGame(gameExternalId))
        val betId = responseJson["id"].asLong()

        assertThat(responseJson["gameId"].asLong()).isEqualTo(savedGame.id)
        assertThat(responseJson["status"].asText()).isEqualTo(BetStatus.PENDING.name)
        assertThat(responseJson["links"][0]["href"].asText()).endsWith("/bets/$betId")

        val persistedBet = betJpaRepository.findAll().single()
        assertThat(persistedBet.userId).isEqualTo(userId)
        assertThat(persistedBet.game.id).isEqualTo(savedGame.id)
        assertThat(persistedBet.selection).isEqualTo(selection)
        assertThat(persistedBet.stake).isEqualByComparingTo(stake)
        assertThat(persistedBet.odds).isEqualByComparingTo(odds)
        assertThat(persistedBet.status).isEqualTo(BetStatus.PENDING)

        val outboxEvent = outboxEventJpaRepository.findAll().single()
        val payloadEvent = objectMapper.readValue(outboxEvent.payload, BetPlacedEvent::class.java)

        assertThat(outboxEvent.aggregateType).isEqualTo("bet")
        assertThat(outboxEvent.aggregateId).isEqualTo(betId.toString())
        assertThat(outboxEvent.type).isEqualTo("BET_PLACED")
        assertThat(outboxEvent.status).isEqualTo(OutboxStatus.PENDING)

        assertThat(payloadEvent.id).isEqualTo(betId)
        assertThat(payloadEvent.userId).isEqualTo(userId)
        assertThat(payloadEvent.gameId).isEqualTo(savedGame.id)
        assertThat(payloadEvent.gameExternalId).isEqualTo(gameExternalId)
        assertThat(payloadEvent.selection).isEqualTo(selection)
        assertThat(payloadEvent.stake).isEqualByComparingTo(stake)
        assertThat(payloadEvent.odds).isEqualByComparingTo(odds)
        assertThat(payloadEvent.status).isEqualTo(BetStatus.PENDING)
    }

    @Test
    fun `should return 422 when game is not found`() {
        val missingGameId = Instant.now().toEpochMilli()

        assertUnprocessable(payloadForGame(missingGameId)) { gameId ->
            jsonPath("$.errors[0].code") { value("game.notFound") }
            jsonPath("$.errors[0].message") { value("Jogo $gameId não encontrado") }
        }

        assertThat(betJpaRepository.count()).isZero()
        assertThat(outboxEventJpaRepository.count()).isZero()
    }

    @Test
    fun `should return 422 when betting window is closed`() {
        val closedGame = gameRepository.save(
            game(
                externalId = Instant.now().toEpochMilli(),
                startTime = Instant.now().minusSeconds(60),
                status = GameStatus.SCHEDULED
            )
        )

        assertUnprocessable(payloadForGame(closedGame.externalId)) { gameId ->
            jsonPath("$.errors[0].code") { value("betting.window.closed") }
            jsonPath("$.errors[0].message") { value("Apostas encerradas para o jogo $gameId") }
        }

        assertThat(betJpaRepository.count()).isZero()
        assertThat(outboxEventJpaRepository.count()).isZero()
    }

    private fun postBetExpectingCreated(payload: Map<String, Any>) =
        mockMvc.post("/bets") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()
            .response
            .contentAsString
            .let(objectMapper::readTree)

    private fun payloadForGame(gameId: Long): Map<String, Any> = basePayload + ("gameId" to gameId)

    private fun assertUnprocessable(
        payload: Map<String, Any>,
        errorAssertions: MockMvcResultMatchersDsl.(gameId: Long) -> Unit
    ) {
        val gameId = payload["gameId"] as Long
        mockMvc.post("/bets") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isUnprocessableEntity() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(422) }
                jsonPath("$.path") { value("/bets") }
                errorAssertions(gameId)
            }
    }
}
