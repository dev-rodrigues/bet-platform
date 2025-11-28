package br.devrodrigues.betapiservice.adapter.inbound.web

import br.devrodrigues.betapiservice.adapter.inbound.web.api.BetControllerApi
import br.devrodrigues.betapiservice.adapter.inbound.web.dto.BetRequestDto
import br.devrodrigues.betapiservice.adapter.inbound.web.dto.BetResponseDto
import br.devrodrigues.betapiservice.adapter.inbound.web.dto.toResponseDto
import br.devrodrigues.betapiservice.application.service.BetService
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bets")
class BetController(private val betService: BetService) : BetControllerApi {

    override fun create(request: BetRequestDto): ResponseEntity<BetResponseDto> {
        val bet = betService.create(request)
        val dto = bet.toResponseDto().withSelfLink()
        return ResponseEntity.status(HttpStatus.CREATED).body(dto)
    }

    private fun BetResponseDto.withSelfLink(): BetResponseDto {
        val selfLink = linkTo(BetController::class.java).slash(id).withSelfRel()
        return copy(links = listOf(selfLink))
    }
}
