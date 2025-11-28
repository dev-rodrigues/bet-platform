package br.devrodrigues.betapiservice.application.service.dto

import br.devrodrigues.betapiservice.domain.model.Game

data class GamePage(
    val content: List<Game>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
