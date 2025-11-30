package br.devrodrigues.resultingestionservice.application.service

import br.devrodrigues.commonevents.MatchesResultEvent
import br.devrodrigues.resultingestionservice.adapter.inbound.web.dto.ProviderMatchResultRequest
import br.devrodrigues.resultingestionservice.adapter.inbound.web.dto.toInput
import br.devrodrigues.resultingestionservice.application.mapper.MatchResultMapper
import br.devrodrigues.resultingestionservice.application.port.out.MatchesResultPublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class MatchResultIngestionServiceTest {

    private val mapper = MatchResultMapper()
    private val publisher: MatchesResultPublisher = mock()
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

        val eventCaptor = argumentCaptor<MatchesResultEvent>()
        doNothing().whenever(publisher).publish(any())

        val result = service.ingest(request.toInput())

        assertThat(result.matchExternalId).isEqualTo(request.matchExternalId)
        assertThat(result.homeScore).isEqualTo(request.homeScore)
        assertThat(result.awayScore).isEqualTo(request.awayScore)
        assertThat(result.status).isEqualTo(request.status)
        assertThat(result.provider).isEqualTo(request.providerEventId)
        assertThat(result.eventId).isNotBlank()
        assertThat(result.emittedAt).isNotNull()
        assertThat(result.occurredAt).isNotNull()

        verify(publisher).publish(eventCaptor.capture())
        val published = eventCaptor.firstValue
        assertThat(published.matchExternalId).isEqualTo(request.matchExternalId)
        assertThat(published.homeScore).isEqualTo(request.homeScore)
        assertThat(published.awayScore).isEqualTo(request.awayScore)
        assertThat(published.status).isEqualTo(request.status)
        assertThat(published.provider).isEqualTo(request.providerEventId)
    }
}
