package br.devrodrigues.resultingestionservice.application.mapper

import br.devrodrigues.resultingestionservice.application.model.MatchResultInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

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

        val before = Instant.now()
        val event = mapper.toEvent(input)
        val after = Instant.now()

        assertThat(event.eventId).isNotBlank()
        assertThat(event.matchExternalId).isEqualTo(input.matchExternalId)
        assertThat(event.homeScore).isEqualTo(input.homeScore)
        assertThat(event.awayScore).isEqualTo(input.awayScore)
        assertThat(event.status).isEqualTo(input.status)
        assertThat(event.provider).isEqualTo(input.providerEventId)
        assertThat(event.occurredAt).isAfterOrEqualTo(before)
        assertThat(event.occurredAt).isBeforeOrEqualTo(after)
        assertThat(event.emittedAt).isAfterOrEqualTo(before)
        assertThat(event.emittedAt).isBeforeOrEqualTo(after)
    }
}
