package br.devrodrigues.betapiservice.adapter.inbound.web

import br.devrodrigues.betapiservice.application.service.GameService
import br.devrodrigues.betapiservice.application.service.dto.GamePage
import br.devrodrigues.betapiservice.application.validation.GameValidationException
import br.devrodrigues.betapiservice.application.validation.ValidationError
import br.devrodrigues.betapiservice.domain.model.GameStatus
import br.devrodrigues.betapiservice.support.TestFixtures.game
import br.devrodrigues.betapiservice.support.TestFixtures.gamePayload
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(GameController::class)
class GameControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var gameService: GameService

    @Test
    fun `should create game and return 201`() {
        val payload = gamePayload()
        val saved = game(id = 10L, externalId = payload["externalId"] as Long)
        whenever(gameService.create(any())).thenReturn(saved)

        mockMvc.post("/games") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.id") { value(10) }
                jsonPath("$.externalId") { value(payload["externalId"]) }
                jsonPath("$.status") { value(GameStatus.SCHEDULED.name) }
            }
    }

    @Test
    fun `should return 422 when external id duplicated`() {
        val payload = gamePayload()
        whenever(gameService.create(any())).thenThrow(
            GameValidationException(
                listOf(
                    ValidationError(
                        code = "game.duplicateExternalId",
                        message = "duplicated"
                    )
                )
            )
        )

        mockMvc.post("/games") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isUnprocessableEntity() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(422) }
                jsonPath("$.path") { value("/games") }
                jsonPath("$.errors[0].code") { value("game.duplicateExternalId") }
            }
    }

    @Test
    fun `should list games with pagination info`() {
        val games = listOf(
            game(id = 1L, externalId = 11L),
            game(id = 2L, externalId = 12L)
        )
        val page = GamePage(
            content = games,
            page = 0,
            size = 2,
            totalElements = 2,
            totalPages = 1
        )
        whenever(gameService.list(page = 0, size = 2)).thenReturn(page)

        mockMvc.get("/games") {
            param("page", "0")
            param("size", "2")
        }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.page") { value(0) }
                jsonPath("$.size") { value(2) }
                jsonPath("$.totalElements") { value(2) }
                jsonPath("$.totalPages") { value(1) }
                jsonPath("$.content[0].id") { value(1) }
                jsonPath("$.content[0].externalId") { value(11) }
                jsonPath("$.content[1].externalId") { value(12) }
            }
    }
}
