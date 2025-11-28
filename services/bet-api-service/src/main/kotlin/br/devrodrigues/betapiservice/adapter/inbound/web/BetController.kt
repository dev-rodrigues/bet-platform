package br.devrodrigues.betapiservice.adapter.inbound.web

import br.devrodrigues.betapiservice.adapter.inbound.web.dto.BetRequestDto
import br.devrodrigues.betapiservice.adapter.inbound.web.dto.BetResponseDto
import br.devrodrigues.betapiservice.adapter.inbound.web.dto.toResponseDto
import br.devrodrigues.betapiservice.application.service.BetService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bets")
class BetController(private val betService: BetService) {

    @PostMapping
    fun create(@Valid @RequestBody request: BetRequestDto): ResponseEntity<BetResponseDto> {
        val bet = betService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(bet.toResponseDto())
    }
}
