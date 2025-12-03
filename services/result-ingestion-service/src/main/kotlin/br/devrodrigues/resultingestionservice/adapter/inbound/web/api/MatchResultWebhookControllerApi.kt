package br.devrodrigues.resultingestionservice.adapter.inbound.web.api

import br.devrodrigues.resultingestionservice.adapter.inbound.web.dto.ProviderMatchResultRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestBody as SpringRequestBody

@Tag(name = "Match Results")
@RequestMapping("/webhook/matches/result")
interface MatchResultWebhookControllerApi {

    @Operation(
        summary = "Recebe o resultado de uma partida via webhook",
        description = "Endpoint para provedores notificarem o placar final e status de uma partida. Envie um POST para /webhook/matches/result com os dados do jogo."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "Resultado aceito para processamento",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MatchResultIngestionResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Aceito",
                                value = """
                                {
                                  "status": "ACCEPTED",
                                  "matchExternalId": "match-123",
                                  "eventId": "2b1d8cda-4af2-4a75-9b3c-3b5e5f6d4ef8"
                                }
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(responseCode = "400", description = "Payload inválido")
        ]
    )
    @PostMapping
    fun ingest(
        @Valid
        @SpringRequestBody
        @RequestBody(
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProviderMatchResultRequest::class),
                    examples = [
                        ExampleObject(
                            name = "Resultado final",
                            value = """
                            {
                              "matchExternalId": "match-123",
                              "homeScore": 2,
                              "awayScore": 1,
                              "status": "FINISHED",
                              "providerEventId": "provider-event-987"
                            }
                            """
                        )
                    ]
                )
            ]
        )
        request: ProviderMatchResultRequest
    ): ResponseEntity<MatchResultIngestionResponse>
}
