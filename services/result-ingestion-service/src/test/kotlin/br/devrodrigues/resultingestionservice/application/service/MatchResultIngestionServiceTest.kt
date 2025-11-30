package br.devrodrigues.resultingestionservice.application.service

import br.devrodrigues.resultingestionservice.application.mapper.MatchResultMapper
import br.devrodrigues.resultingestionservice.application.model.MatchResultInput
import br.devrodrigues.resultingestionservice.application.port.out.MatchesResultPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MatchResultIngestionServiceTest {

    private val mapper = MatchResultMapper()
    private val publisher: MatchesResultPublisher = mockk(relaxed = true)
    private val service = MatchResultIngestionService(mapper, publisher)

    @Test
    fun `should map input and publish event`() {
        val input = MatchResultInput(
            matchExternalId = "match-123",
            homeScore = 1,
            awayScore = 1,
            status = "FINISHED",
            providerEventId = "prov-1"
        )

        every { publisher.publish(any()) } returns Unit

        val result = service.ingest(input)

        assertThat(result.matchExternalId).isEqualTo(input.matchExternalId)
        assertThat(result.homeScore).isEqualTo(input.homeScore)
        assertThat(result.awayScore).isEqualTo(input.awayScore)
        assertThat(result.status).isEqualTo(input.status)
        assertThat(result.provider).isEqualTo(input.providerEventId)
        assertThat(result.eventId).isNotBlank()
        assertThat(result.emittedAt).isNotNull()
        assertThat(result.occurredAt).isNotNull()

        verify {
            publisher.publish(withArg { published ->
                assertThat(published.matchExternalId).isEqualTo(input.matchExternalId)
            })
        }
    }
}
