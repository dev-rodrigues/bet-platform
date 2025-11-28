package br.devrodrigues.betapiservice.adapter.inbound.web

import br.devrodrigues.betapiservice.adapter.inbound.web.api.GameControllerApi
import br.devrodrigues.betapiservice.adapter.inbound.web.dto.GameRequestDto
import br.devrodrigues.betapiservice.adapter.inbound.web.dto.GameResponseDto
import br.devrodrigues.betapiservice.adapter.inbound.web.dto.toResponseDto
import br.devrodrigues.betapiservice.application.service.GameService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/games")
class GameController(
    private val gameService: GameService
) : GameControllerApi {

    override fun create(request: GameRequestDto): ResponseEntity<GameResponseDto> {
        val game = gameService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(game.toResponseDto())
    }
}
