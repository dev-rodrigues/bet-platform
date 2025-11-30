package br.devrodrigues.resultingestionservice.application.service

import br.devrodrigues.commonevents.MatchesResultEvent
import br.devrodrigues.resultingestionservice.adapter.inbound.web.dto.ProviderMatchResultRequest
import br.devrodrigues.resultingestionservice.adapter.inbound.web.dto.toInput
import br.devrodrigues.resultingestionservice.application.mapper.MatchResultMapper
import br.devrodrigues.resultingestionservice.application.port.out.MatchesResultPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MatchResultIngestionServiceTest {

    private val mapper = MatchResultMapper()
    private val publisher: MatchesResultPublisher = mockk(relaxed = true)
    private val service = MatchResultIngestionService(mapper, publisher)

    @Test
    fun `should publish mapped event when receiving provider request`() {
        val request = ProviderMatchResultRequest(
            matchExternalId = "match-123",
            homeScore = 1,
            awayScore = 1,
            status = "FINISHED",
            providerEventId = "prov-1"
        )

        val capturedEvent = slot<MatchesResultEvent>()
        every { publisher.publish(capture(capturedEvent)) } returns Unit

        val result = service.ingest(request.toInput())

        assertThat(result.matchExternalId).isEqualTo(request.matchExternalId)
        assertThat(result.homeScore).isEqualTo(request.homeScore)
        assertThat(result.awayScore).isEqualTo(request.awayScore)
        assertThat(result.status).isEqualTo(request.status)
        assertThat(result.provider).isEqualTo(request.providerEventId)
        assertThat(result.eventId).isNotBlank()
        assertThat(result.emittedAt).isNotNull()
        assertThat(result.occurredAt).isNotNull()

        verify { publisher.publish(any()) }
        assertThat(capturedEvent.isCaptured).isTrue()
        assertThat(capturedEvent.captured.matchExternalId).isEqualTo(request.matchExternalId)
        assertThat(capturedEvent.captured.homeScore).isEqualTo(request.homeScore)
        assertThat(capturedEvent.captured.awayScore).isEqualTo(request.awayScore)
        assertThat(capturedEvent.captured.status).isEqualTo(request.status)
        assertThat(capturedEvent.captured.provider).isEqualTo(request.providerEventId)
    }
}
