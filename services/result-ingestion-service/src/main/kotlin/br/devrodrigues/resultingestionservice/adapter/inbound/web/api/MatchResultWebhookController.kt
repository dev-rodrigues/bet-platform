package br.devrodrigues.resultingestionservice.adapter.inbound.web.api

import br.devrodrigues.resultingestionservice.adapter.inbound.web.dto.ProviderMatchResultRequest
import br.devrodrigues.resultingestionservice.adapter.inbound.web.dto.toInput
import br.devrodrigues.resultingestionservice.application.service.MatchResultIngestionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class MatchResultIngestionResponse(
    val status: String,
    val matchExternalId: String,
    val eventId: String
)

@RestController
@RequestMapping("/webhook/matches/result")
class MatchResultWebhookController(
    private val service: MatchResultIngestionService
) {

    @PostMapping
    fun ingest(@Valid @RequestBody request: ProviderMatchResultRequest): ResponseEntity<MatchResultIngestionResponse> {
        val event = service.ingest(request.toInput())

        val response = MatchResultIngestionResponse(
            status = "ACCEPTED",
            matchExternalId = event.matchExternalId,
            eventId = event.eventId
        )

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response)
    }
}
