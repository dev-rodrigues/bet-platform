package br.devrodrigues.betapiservice.adapter.inbound.web.api

import br.devrodrigues.betapiservice.adapter.inbound.web.dto.BetRequestDto
import br.devrodrigues.betapiservice.adapter.inbound.web.dto.BetResponseDto
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
import org.springframework.web.bind.annotation.RequestBody as SpringRequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "Bets")
@RequestMapping("/bets")
interface BetControllerApi {

    @Operation(
        summary = "Cria uma nova aposta",
        description = "Registrar uma aposta na plataforma. Enviar um POST para /bets (ex.: http://localhost:8080/bets) e receber o objeto persistido com link HATEOAS para consulta."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Aposta criada com sucesso",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BetResponseDto::class),
                        examples = [
                            ExampleObject(
                                name = "Criada",
                                value = """
                                {
                                  "id": 123,
                                  "userId": 42,
                                  "gameId": 987,
                                  "selection": "Team A",
                                  "stake": 100.00,
                                  "odds": 2.25,
                                  "status": "PENDING",
                                  "createdAt": "2024-01-01T12:00:00Z",
                                  "links": [
                                    {
                                      "rel": "self",
                                      "href": "http://localhost:8080/bets/123"
                                    }
                                  ]
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
    fun create(
        @Valid
        @SpringRequestBody
        @RequestBody(
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BetRequestDto::class),
                    examples = [
                        ExampleObject(
                            name = "Aposta simples",
                            value = """
                            {
                              "userId": 42,
                              "gameId": 987,
                              "selection": "Team A",
                              "stake": 100.00,
                              "odds": 2.25
                            }
                            """
                        )
                    ]
                )
            ]
        )
        request: BetRequestDto
    ): ResponseEntity<BetResponseDto>
}
