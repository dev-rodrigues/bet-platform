package br.devrodrigues.resultingestionservice.adapter.inbound.web.api

import br.devrodrigues.commonevents.MatchesResultEvent
import br.devrodrigues.resultingestionservice.application.service.MatchResultIngestionService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(MatchResultWebhookController::class)
class MatchResultWebhookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var service: MatchResultIngestionService

    @Test
    fun `should accept valid payload and call service`() {
        val requestPayload = mapOf(
            "matchExternalId" to "match-123",
            "homeScore" to 2,
            "awayScore" to 1,
            "status" to "FINISHED",
            "providerEventId" to "prov-1"
        )

        val event = MatchesResultEvent(
            eventId = "event-1",
            occurredAt = Instant.now(),
            emittedAt = Instant.now(),
            matchExternalId = "match-123",
            homeScore = 2,
            awayScore = 1,
            status = "FINISHED",
            provider = "prov-1"
        )

        whenever(service.ingest(any())).thenReturn(event)

        mockMvc.perform(
            post("/webhook/matches/result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload))
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.status").value("ACCEPTED"))
            .andExpect(jsonPath("$.matchExternalId").value("match-123"))
            .andExpect(jsonPath("$.eventId").value("event-1"))

        verify(service, times(1)).ingest(any())
    }

    @Test
    fun `should return bad request when payload is invalid`() {
        val invalidPayload = mapOf(
            "homeScore" to 2,
            "awayScore" to 1,
            "status" to "FINISHED"
            // missing matchExternalId
        )

        mockMvc.perform(
            post("/webhook/matches/result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPayload))
        )
            .andExpect(status().isBadRequest)

        verify(service, never()).ingest(any())
    }
}
