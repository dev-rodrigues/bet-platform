package br.devrodrigues.betapiservice.application.service

import br.devrodrigues.betapiservice.application.validation.GameValidationException
import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.model.GameStatus
import br.devrodrigues.betapiservice.domain.port.out.GameRepository
import br.devrodrigues.betapiservice.support.TestFixtures.game
import br.devrodrigues.betapiservice.support.TestFixtures.gameCommand
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset

class GameServiceTest {

    private val gameRepository = mockk<GameRepository>()
    private val gameService = GameService(gameRepository)

    @BeforeEach
    fun setup() {
        clearMocks(gameRepository)
    }

    @Test
    fun `should create game with default status when not provided`() {
        val command = gameCommand(status = null)
        val savedSlot = slot<Game>()
        every { gameRepository.findByExternalId(command.externalId) } returns null
        every { gameRepository.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 5L) }

        val result = gameService.create(command)

        assertEquals(5L, result.id)
        assertEquals(GameStatus.SCHEDULED, result.status)
        assertEquals(command.externalId, result.externalId)
        assertEquals(command.homeTeam, result.homeTeam)
        assertEquals(command.awayTeam, result.awayTeam)
        assertEquals(command.startTime, result.startTime)
        assertEquals(LocalDate.ofInstant(command.startTime, ZoneOffset.UTC), result.matchDate)
        verify(exactly = 1) { gameRepository.findByExternalId(command.externalId) }
        verify(exactly = 1) { gameRepository.save(any()) }
    }

    @Test
    fun `should reject duplicated external id`() {
        val command = gameCommand()
        every { gameRepository.findByExternalId(command.externalId) } returns game(
            id = 1L,
            externalId = command.externalId,
            startTime = command.startTime,
            homeTeam = "X",
            awayTeam = "Y"
        )

        assertThrows(GameValidationException::class.java) {
            gameService.create(command)
        }

        verify(exactly = 1) { gameRepository.findByExternalId(command.externalId) }
        verify(exactly = 0) { gameRepository.save(any()) }
    }

    @Test
    fun `list should return page with correct totals`() {
        val games = listOf(
            game(id = 1L, externalId = 11L),
            game(id = 2L, externalId = 12L)
        )
        every { gameRepository.findPage(0, 10) } returns games

        val page = gameService.list(page = 0, size = 10)

        assertEquals(games, page.content)
        assertEquals(0, page.page)
        assertEquals(10, page.size)
        assertEquals(2, page.totalElements)
        assertEquals(1, page.totalPages)
        verify(exactly = 1) { gameRepository.findPage(0, 10) }
    }
}
