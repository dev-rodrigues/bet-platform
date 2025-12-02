package br.devrodrigues.betapiservice.adapter.inbound.web

import br.devrodrigues.betapiservice.application.service.BetService
import br.devrodrigues.betapiservice.application.service.dto.CreateBetCommand
import br.devrodrigues.betapiservice.application.validation.BetValidationException
import br.devrodrigues.betapiservice.application.validation.ValidationError
import br.devrodrigues.betapiservice.support.TestFixtures.bet
import br.devrodrigues.betapiservice.support.TestFixtures.betPayload
import br.devrodrigues.betapiservice.support.TestFixtures.betPayloadTeamB
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

@WebMvcTest(BetController::class)
class BetControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var betService: BetService

    @Test
    fun `should create bet and return hateoas link`() {
        val payload = betPayload()
        val returnedBet = bet()
        val captor = argumentCaptor<CreateBetCommand>()
        whenever(betService.create(captor.capture())).thenReturn(returnedBet)

        mockMvc.post("/bets") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.id") { value(99) }
                jsonPath("$.status") { value("PENDING") }
                jsonPath("$.links[0].rel") { value("self") }
                jsonPath("$.links[0].href") { value(org.hamcrest.Matchers.endsWith("/bets/99")) }
            }
            .andReturn()

        val captured = captor.firstValue
        assertEquals(1L, captured.userId)
        assertEquals(10L, captured.gameId)
        assertEquals("HOME_WIN", captured.selection)
        assertEquals(0, captured.stake.compareTo(BigDecimal("50.00")))
        assertEquals(0, captured.odds.compareTo(BigDecimal("2.10")))
    }

    @Test
    fun `should return 422 when validation fails`() {
        val payload = betPayloadTeamB()
        whenever(betService.create(any())).thenThrow(
            BetValidationException(
                listOf(
                    ValidationError(
                        code = "betting.window.closed",
                        message = "Apostas encerradas"
                    )
                )
            )
        )

        mockMvc.post("/bets") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isUnprocessableEntity() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(422) }
                jsonPath("$.path") { value("/bets") }
                jsonPath("$.errors[0].code") { value("betting.window.closed") }
            }
    }
}
