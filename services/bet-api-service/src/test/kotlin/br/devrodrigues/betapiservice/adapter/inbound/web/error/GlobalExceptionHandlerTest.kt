package br.devrodrigues.betapiservice.adapter.inbound.web.error

import br.devrodrigues.betapiservice.adapter.inbound.web.BetController
import br.devrodrigues.betapiservice.adapter.inbound.web.GameController
import br.devrodrigues.betapiservice.application.service.BetService
import br.devrodrigues.betapiservice.application.service.GameService
import br.devrodrigues.betapiservice.application.validation.BetValidationException
import br.devrodrigues.betapiservice.application.validation.GameValidationException
import br.devrodrigues.betapiservice.application.validation.ValidationError
import br.devrodrigues.betapiservice.support.TestFixtures.betPayload
import br.devrodrigues.betapiservice.support.TestFixtures.gamePayload
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(controllers = [BetController::class, GameController::class])
class GlobalExceptionHandlerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var betService: BetService

    @MockBean
    private lateinit var gameService: GameService

    @Test
    fun `should return 400 on bean validation errors`() {
        val invalidPayload = betPayload(
            stake = 0,   // DecimalMin 0.01
            odds = 0.5   // DecimalMin 1.01
        )

        mockMvc.post("/bets") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidPayload)
        }
            .andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(400) }
                jsonPath("$.path") { value("/bets") }
                jsonPath("$.errors[*].code") { value(hasItems("validation.stake", "validation.odds")) }
            }
    }

    @Test
    fun `should return 422 on bet validation exception`() {
        whenever(betService.create(any())).thenThrow(
            BetValidationException(
                listOf(ValidationError(code = "betting.window.closed", message = "Apostas encerradas"))
            )
        )

        mockMvc.post("/bets") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(betPayload())
        }
            .andExpect {
                status { isUnprocessableEntity() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(422) }
                jsonPath("$.path") { value("/bets") }
                jsonPath("$.errors[0].code") { value("betting.window.closed") }
            }
    }

    @Test
    fun `should return 422 on game validation exception`() {
        whenever(gameService.create(any())).thenThrow(
            GameValidationException(
                listOf(ValidationError(code = "game.duplicateExternalId", message = "duplicated"))
            )
        )

        mockMvc.post("/games") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(gamePayload())
        }
            .andExpect {
                status { isUnprocessableEntity() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(422) }
                jsonPath("$.path") { value("/games") }
                jsonPath("$.errors[0].code") { value("game.duplicateExternalId") }
            }
    }
}
