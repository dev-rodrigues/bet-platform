package br.devrodrigues.resultingestionservice.application.mapper

import br.devrodrigues.resultingestionservice.application.model.MatchResultInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MatchResultMapperTest {

    private val mapper = MatchResultMapper()

    @Test
    fun `should map provider request to event with generated metadata`() {
        val input = MatchResultInput(
            matchExternalId = "match-123",
            homeScore = 2,
            awayScore = 1,
            status = "FINISHED",
            providerEventId = "prov-1"
        )

        val event = mapper.toEvent(input)

        assertThat(event.eventId).isNotBlank()
        assertThat(event.matchExternalId).isEqualTo(input.matchExternalId)
        assertThat(event.homeScore).isEqualTo(input.homeScore)
        assertThat(event.awayScore).isEqualTo(input.awayScore)
        assertThat(event.status).isEqualTo(input.status)
        assertThat(event.provider).isEqualTo(input.providerEventId)
        assertThat(event.occurredAt).isNotNull()
        assertThat(event.emittedAt).isNotNull()
    }
}
