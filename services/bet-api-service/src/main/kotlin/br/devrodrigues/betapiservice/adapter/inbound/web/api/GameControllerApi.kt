package br.devrodrigues.betapiservice.adapter.inbound.web.api

import br.devrodrigues.betapiservice.adapter.inbound.web.dto.GameRequestDto
import br.devrodrigues.betapiservice.adapter.inbound.web.dto.GameResponseDto
import br.devrodrigues.betapiservice.adapter.inbound.web.error.ValidationErrorResponse
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

@Tag(name = "Games")
@RequestMapping("/games")
interface GameControllerApi {

    @Operation(
        summary = "Cria um jogo",
        description = "Registrar um jogo via POST /games (ex.: http://localhost:8080/games) e receber o registro persistido."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Jogo criado",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = GameResponseDto::class),
                        examples = [
                            ExampleObject(
                                name = "Criado",
                                value = """
                                {
                                  "id": 1,
                                  "externalId": 987,
                                  "homeTeam": "Team A",
                                  "awayTeam": "Team B",
                                  "startTime": "2024-12-01T15:00:00Z",
                                  "homeScore": null,
                                  "awayScore": null,
                                  "status": "SCHEDULED",
                                  "matchDate": "2024-12-01"
                                }
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "422",
                description = "Validação de negócio falhou",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ValidationErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Duplicado",
                                value = """
                                {
                                  "status": 422,
                                  "message": "Validação do jogo falhou",
                                  "path": "/games",
                                  "errors": [
                                    {
                                      "code": "game.duplicateExternalId",
                                      "message": "Jogo com externalId 987 já existe"
                                    }
                                  ],
                                  "timestamp": "2024-01-01T12:00:00Z"
                                }
                                """
                            )
                        ]
                    )
                ]
            )
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
                    schema = Schema(implementation = GameRequestDto::class),
                    examples = [
                        ExampleObject(
                            name = "Novo jogo",
                            value = """
                            {
                              "externalId": 987,
                              "homeTeam": "Team A",
                              "awayTeam": "Team B",
                              "startTime": "2024-12-01T15:00:00Z",
                              "status": "SCHEDULED"
                            }
                            """
                        )
                    ]
                )
            ]
        )
        request: GameRequestDto
    ): ResponseEntity<GameResponseDto>
}
